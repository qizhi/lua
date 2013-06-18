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
package com.cubeia.firebase.server.mtt;

import static com.cubeia.firebase.api.lobby.LobbyPathType.MTT;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.lobby.AttributeMapper;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttFactory;
import com.cubeia.firebase.api.mtt.activator.CreationParticipant;
import com.cubeia.firebase.api.mtt.activator.DefaultCreationParticipant;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributeMapper;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributes;
import com.cubeia.firebase.api.mtt.lobby.MttLobbyObject;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.mttplayerreg.TournamentPlayerRegistry;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.mtt.comm.MttCreated;
import com.cubeia.firebase.mtt.comm.MttFinalized;
import com.cubeia.firebase.mtt.comm.MttRemoved;
import com.cubeia.firebase.mtt.comm.TableRemoval;
import com.cubeia.firebase.mtt.state.MttStateData;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.ObjectEmerged;
import com.cubeia.firebase.server.game.activation.DefaultTableFactory;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyAttributeAccessor;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyTableAccessor;
import com.cubeia.firebase.server.lobby.model.MapLobbyAttributeAccessor;
import com.cubeia.firebase.server.mtt.tables.TableSystemStateMapper;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.util.GameObjectIdSysStateMapper;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.CommandDispatcher;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;
import com.cubeia.firebase.util.LobbyMttImpl;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

public class MttFactoryImpl implements MttFactory {

	/** Timeout for commit lock when creating a new mtt state. */
	private static final int TABLE_CREATION_LOCK_TIMEOUT = 500;
	private static final long FINALIZER_DELAY = 100;
	
	private static transient Logger log = Logger.getLogger(MttFactoryImpl.class);
	
	private final ServiceRegistry services;
	private final TableSpaceServiceContract spaceService;
	private final GameObjectIdSysStateMapper idGenerator;
	private final DefaultCreationParticipant defParticipant;
	private final SystemStateServiceContract state;
	private final MBusContract mbus;
	
	private final JmxScheduler scheduler;
	
	/**
	 * Constructor
	 * @param idGenerator 
	 *  
	 * @param services, the service registry
	 */
	public MttFactoryImpl(ServiceRegistry services, GameObjectIdSysStateMapper idGenerator) {
		this.services = services;
		this.idGenerator = idGenerator;
		defParticipant = new DefaultCreationParticipant();
		spaceService = services.getServiceInstance(TableSpaceServiceContract.class);
		state = services.getServiceInstance(SystemStateServiceContract.class);
		mbus = services.getServiceInstance(MBusContract.class);
		scheduler = new JmxScheduler(1, "MttFactory - Finalizer Thread");
		scheduler.start();
	}

	public void destroy() {
		scheduler.stop();
	}
	
	public MttLobbyObject getTournamentInstance(int mttInstanceId) {
		return findInstance(mttInstanceId);
	}
	
	public void destroyMtt(int gameId, int mttId) {
		removeLobbyData(mttId);
		Set<Integer> tables = TableSystemStateMapper.getAllTables(state, mttId);
		if(tables != null) {
			fireRemoveMttTables(gameId, mttId, tables);
		}
		fireMttRemoved(mttId, true);
		// Update system registry
		TournamentPlayerRegistry reg = services.getServiceInstance(TournamentPlayerRegistry.class);
		reg.unregisterAll(mttId);
		// Remove system registry root node
		state.removeNode(DefaultTableFactory.getNodeLobbyPathForMttTables(gameId, mttId).getNameSpace());
		// Fire remove
		fireMttRemoved(mttId, false);
	}
	
	public String getTest(int id) {
		return getTournamentInstance(id).toString();
	}
	private void fireRemoveMttTables(int gameId, int mttId, Set<Integer> tables) {
		CommandDispatcher con = getCommandDispatcher();
        int[] arr = toIntArray(tables);
		TableRemoval comm = new TableRemoval(mttId, gameId, arr);
        try {
            con.dispatch(comm);
        } catch (ClusterException e) {
            Logger.getLogger(getClass()).error("Failed to send table removal command!", e);
        }
	}

	private int[] toIntArray(Set<Integer> tables) {
		int[] arr = new int[tables.size()];
        int i = 0;
        for (Integer id : tables) {
            arr[i++] = id;
        }
		return arr;
	}

	private CommandDispatcher getCommandDispatcher() {
		ConnectionServiceContract serv = services.getServiceInstance(ConnectionServiceContract.class);
		return serv.getSharedConnection().getCommandDispatcher();
	}

	private void removeLobbyData(int mttId) {
		MttLobbyObject inst = findInstance(mttId);
		if(inst != null) {
			LobbyMttImpl impl = (LobbyMttImpl)inst;
			LobbyPath path = impl.getLobbyPath();
			DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
			acc.removeNode(path);
		} 
	}

	private MttLobbyObject findInstance(final int mttId) {
		MttLobbyObject[] arr = listTournamentInstances(new TournamentFilter() {
		
			public boolean accept(int id, Map<String, AttributeValue> attribs) {
				return id == mttId;
			}
		
		});
		return (arr.length == 0 ? null : arr[0]);
		/*MttLobbyObject[] insts = listTournamentInstances();
		for (MttLobbyObject o : insts) {
			if(o.getTournamentId() == mttId) {
				return o;
			}
		}
		return null;*/
	}

	/**
	 * Create a new mtt state and add it to the space.
	 *
	 * FIXME: This method currently only creates MTT State Support objects!
	 * We need to allow any implementation of MTT State
	 */
	public MttLobbyObject createMtt(int mttLogicId, String name, CreationParticipant part) {
	    if (log.isDebugEnabled()) {
	        log.debug("Create tournament: logicid["+mttLogicId+"] name["+name+"] part["+part+"]");
	    }
		int id = getNewMttInstanceId();
		CreationParticipant participant = null;
		if(part != null) {
			participant = part;
		} else {
			participant = defParticipant;
		}
		LobbyPath path = null;
		MapLobbyAttributeAccessor acc = null;
		MTTState mtt = new MTTStateSupport(mttLogicId, id);
		mtt.setName(name);
		
		MttStateData data = spaceService.getMttFactory().createMttState(mttLogicId, id);
		TransactionalMttState txState = spaceService.getMttFactory().createMttState(data);
		try {
			// We will not have any contention so this should not occur...
			txState.begin(TABLE_CREATION_LOCK_TIMEOUT);
		} catch (InterruptedException e) {
			log.error("I was interrupted when creating a new mtt state.", e);
		}
		
		// Do lobby stuff...
		LobbyPath tmp = participant.getLobbyPathForTournament(mtt);
		path = new LobbyPath(MTT, mttLogicId, tmp.getDomain(), id);
		acc = new MapLobbyAttributeAccessor(path);
		participant.tournamentCreated(mtt, acc);
		
		/*
		 * Ticket #574: "_READY" will not be set at all, the
		 * first time this attribute is set is when the tournament
		 * is fully created.
		 */
		enforceAttributes(mtt, acc);
		
		mtt.setLobbyPath(path);
		txState.setMttState(mtt);
		// Get the MTT Space and add the mtt state
		GameObjectSpace<TransactionalMttState, MttAction> space = spaceService.getObjectSpace(TransactionalMttState.class, MttAction.class);
	
		/* 
		 * Note, if we don't commit the txState before we add it to the space, 
		 * null values will be read on the receiver side.
		 */ 
		txState.commit();
		txState.release();
		
		/*
		 * Clarification regarding the order in this creation chain:
		 * 
		 * 	1) Mtt added, mbus is changed
		 * 	2) State object added to cache
		 *  3) Lobby attributes flushed (but no visibility flag)
		 *  4) Emerged (scheduled actions picked up, not needed)
		 *  5) Finalized (init and visibility set buy mtt node)
		 */
		
		fireMttAdded(id);
		space.add(txState);
		acc.flush(state);
		fireEmerged(new int[] { id });
		
		/*
		 * Ticket #574: We'll schedule a final event which will trigger
		 * 1) "tournamentCreated" to be called; followed by 2) the "ready"
		 * flag to be set to "true". This method is scheduled in order for
		 * it to be executed after this method has returned. 
		 */
		scheduleFireFinalized(new int[] { id });
		
		Map<String, AttributeValue> atts = acc.getAllAttributes();
        return new LobbyMttImpl(id, atts, path);
	}	

	private void scheduleFireFinalized(final int[] ids) {
		scheduler.schedule(new SafeRunnable() {
		
			@Override
			protected void innerRun() {
				fireFinalized(ids);
			}
		}, FINALIZER_DELAY, TimeUnit.MILLISECONDS);
	}

	private void fireMttAdded(int id) {
		doFire(new MttCreated(id));
	}

	private void fireMttRemoved(int id, boolean isPre) {
		doFire(new MttRemoved(id, getPartition(id), isPre));
	}
	
	private Partition getPartition(int mttId) {
		PartitionMap map = mbus.getCurrentPartitionMap();
		return map.getPartitionForChannel(EventType.MTT, mttId);
	}
	
    private void doFire(Command<?> com) {
		ConnectionServiceContract serv = services.getServiceInstance(ConnectionServiceContract.class);
		try {
			ClusterConnection conn = serv.getSharedConnection();
			CommandDispatcher disp = conn.getCommandDispatcher();
			disp.send(Constants.MTT_CREATION_COMMAND_CHANNEL, com);
		} catch(ClusterException e) {
			log.fatal("Failed to send creation event to master.", e);
		}
	}
	
	private void fireEmerged(int[] ids) {
		doFire(new ObjectEmerged(ids));
	}
	
	private void fireFinalized(int[] ids) {
		doFire(new MttFinalized(ids));
	}

	private int getNewMttInstanceId() {
		return idGenerator.generateNewObjectId();
	}
	
    // set default values
    private void enforceAttributes(MTTState mtt, LobbyTableAttributeAccessor acc) {
        DefaultMttAttributeMapper.setRequiredValues(mtt, acc);
        // DefaultMttAttributeMapper.setReady(acc, false);
    }

    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.mtt.MttFactory#listTournamentInstances()
     */
    public MttLobbyObject[] listTournamentInstances() {
    	return listTournamentInstances(SystemStateConstants.TOURNAMENT_ROOT_FQN, null);
    }
    
    private MttLobbyObject[] listTournamentInstances(TournamentFilter filter) {
    	return listTournamentInstances(SystemStateConstants.TOURNAMENT_ROOT_FQN, filter);
    }
    
    
    /* (non-Javadoc)
     * @see com.cubeia.firebase.api.mtt.MttFactory#listTournamentInstances(int)
     */
    public MttLobbyObject[] listTournamentInstances(int tournamentLogicId) {
    	return listTournamentInstances(SystemStateConstants.TOURNAMENT_ROOT_FQN + tournamentLogicId, null);
    }
    
    /**
     * Lists tournaments given an address in the lobby tree.
     * 
     * Note, this is supposedly slow.
     * 
     * @param address
     * @return
     */
    public MttLobbyObject[] listTournamentInstances(String address, TournamentFilter filter) {
	    List<MttLobbyObject> list = new LinkedList<MttLobbyObject>();
	    Set<String> endNodes = state.getEndNodes(address);
	    for (String path : endNodes) {
	    	if(isMtt(state, path)) {
	            LobbyPath lobbyPath = new LobbyPath(LobbyPathType.MTT).parseFqn(path);
	            DefaultLobbyAttributeAccessor acc = new DefaultLobbyAttributeAccessor(state, lobbyPath);
	            Map<String, AttributeValue> attribs = acc.getAllAttributes();
	            int id = Integer.parseInt(attribs.get(DefaultMttAttributes._ID.name()).getData().toString());
	            lobbyPath = new LobbyPath(lobbyPath, id);
	            if(filter == null || filter.accept(id, attribs)) {
	            	list.add(new LobbyMttImpl(id, attribs, lobbyPath));
	            }
	    	}
	    }
	    return list.toArray(new MttLobbyObject[list.size()]);
    }
    
	private boolean isMtt(SystemStateServiceContract state, String path) {
		Object test = state.getAttribute(path, AttributeMapper.NODE_TYPE_ATTRIBUTE_NAME);
		return (test != null ? test.equals(AttributeMapper.MTT_NODE_TYPE_ATTRIBUTE_VALUE) : false);
	}
}
