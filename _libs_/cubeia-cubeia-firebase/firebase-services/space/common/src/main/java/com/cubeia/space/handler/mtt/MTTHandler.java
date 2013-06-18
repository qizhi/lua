/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.space.handler.mtt;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.mtt.state.MttStateData;
import com.cubeia.firebase.mtt.state.MttStateFactory;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.mtt.processor.MttActionProcessor;
import com.cubeia.firebase.server.processor.GameObjectProcessor;
import com.cubeia.firebase.service.space.SpaceObjectNotFoundException;
import com.cubeia.space.Space;
import com.cubeia.space.handler.AbstractTXHandler;

/**
 * Logical layer on top of a replicated space that handles
 * MTTs and mtt actions.
 * 
 *
 * @author Fredrik
 */
public class MTTHandler extends AbstractTXHandler<TransactionalMttState, MttAction> {

	private final Logger log = Logger.getLogger(MTTHandler.class);
	
	private Space<MttStateData> space;
	private final MttStateFactory factory;
	
	public MTTHandler(Space<MttStateData> space, ServiceRegistry reg, MttStateFactory factory) {
		super(reg);
		this.factory = factory;
		this.space = space;
	}

	public void start() {
		space.start();
		state = State.STARTED;
	}

	public void stop() {
		space.stop();
		state = State.STOPPED;
	}
	
    public void halt() {
    	space.halt();
    }
    
    public boolean isHalted() {
    	return space.isHalted();
    }
    
    public void resume() {
    	space.resume();
    }

    public boolean remove(int id) {
    	return space.remove(id);
	}
   	
	public TransactionalMttState add(TransactionalMttState mtt) {
		Arguments.notNull(mtt, "mtt");
		MttStateData state = factory.extractState(mtt);
		space.add(state);
		return mtt;
	}

	public boolean exists(int objectid) {
		return space.exists(objectid);
	}

	public TransactionalMttState peek(int id) {
		MttStateData data = space.peek(id);
		if (data != null) {
			return factory.createMttState(data);
		} else {
			return null;
		}
	}	
	
	public void registerClassloader(int mttId, ClassLoader loader) {
		factory.setMttClassLoader(mttId, loader);
	}
	
	public void unregisterClassloader(int mttId) {
		factory.setMttClassLoader(mttId, null);
	}
	
	public boolean handle(Event<MttAction> event, GameObjectProcessor<MttAction> gop) throws SpaceObjectNotFoundException {
    	/*
    	 * This is the transaction order:
    	 * 
    	 *  1) Lock space
    	 *  2) Take mtt data
    	 *  3) Create FB mtt
    	 *  4) Begin JBC transaction
    	 *  5) Create notifier
    	 *  
    	 * [local actions]
    	 *  * User transaction
    	 *  * Process action
    	 *  * Commit user transaction
    	 * [/local actions]
    	 *  
    	 * <tx commit />
    	 * 
    	 *  5) Flush notifier
    	 *  4) Commit JBC transaction
    	 *  3) Flush FB mtt
    	 *  2) Put mtt data
    	 *  1) Unlock space
    	 */
		
		if(log.isTraceEnabled()) {
        	log.trace("Handle event: " + event);
        }
		MttActionProcessor proc = checkProcessor(gop);
		MttStateData state = space.take(event.getFirstTargetId());
    	if(state == null) {
        	if(event.isTransient()) {
        		/* 
        		 * Trac issue [ #209 ]
        		 * 
        		 * This is a transient action. It is most probably a
        		 * scheduled event, and between the scheduling and and running
        		 * the mtt has been removed, which we now ignore.
        		 */
        		if(log.isDebugEnabled()) {
        			log.debug("Dropping transient action: " + event);
        		}
        	} else {
        		throw new SpaceObjectNotFoundException(event.getFirstTargetId());
        	}
        } else {
        	TransactionalMttState mtt = factory.createMttState(state);
        	MttAction action = unrapEvent(event, mtt);
            MTTState mttState = mtt.getMttState();
            wrapAndDispatch(proc, mtt, action);
            mtt.setMttState(mttState);
        }
        return true;
	}
	
	@Override
	protected boolean isJtaEnabled() {
		return space.isJtaEnabled();
	}

	
	// --- PRIVATE METHODS --- //
	
	private void wrapAndDispatch(MttActionProcessor proc, TransactionalMttState mtt, MttAction action) {
		attachJbcTransaction();
		UserTransaction trans = getUserTransaction();
		if(trans != null) {
			wrapJtaAndDispatch(proc, mtt, action, trans);
		} else {
			proc.handleAction(mtt, action);
		}
	}

	private void wrapJtaAndDispatch(MttActionProcessor proc, TransactionalMttState mtt, MttAction action, UserTransaction trans) {
		boolean done = false;
		try {
			trans.begin();
			proc.handleAction(mtt, action);
			trans.commit();
			done = true;
		} catch (NotSupportedException e) {
			failTransaction(e);
		} catch (SystemException e) {
			failTransaction(e);
		} catch (SecurityException e) {
			failTransaction(e);
		} catch (IllegalStateException e) {
			failTransaction(e);
		} catch (RollbackException e) {
			failTransaction(e);
		} catch (HeuristicMixedException e) {
			failTransaction(e);
		} catch (HeuristicRollbackException e) {
			failTransaction(e);
		} finally {
			if(!done) {
				try {
					trans.rollback();
				} catch (Exception e) {
					log.fatal("Failed to rollback transaction!", e);
				}
			}
		}
	}

	private void failTransaction(Exception e) {
		throw new IllegalStateException("Failed user transaction", e);
	}

	private MttAction unrapEvent(Event<MttAction> event, TransactionalMttState mtt) {
		int mttId = getMttLogicId(mtt);
		ClassLoader loader = factory.getMttClassLoader(mttId);
		try {
			event.unwrapForTarget(loader);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to deserialize event for target", e);
		}
		return event.getAction();
	}

	private int getMttLogicId(TransactionalMttState mtt) {
		return mtt.getMttState().getMttLogicId();
	}
	
	private MttActionProcessor checkProcessor(GameObjectProcessor<MttAction> gop) {
		MttActionProcessor proc = null;
    	if (gop instanceof MttActionProcessor) {
			proc = (MttActionProcessor) gop;
		} else {
			log.error("Wrong processor type found (expected MttActionProcessor): "+gop.getClass());
		}
		return proc;
	}
}
