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
package com.cubeia.firebase.server.gateway.event;

import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.io.protocol.ForcedLogoutPacket;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.server.event.processing.ReceiverEventDaemonBase;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.routing.ClientNodeRouter;
import com.cubeia.firebase.service.chat.ChatServiceContract;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

public class ReceivingChatEventDaemon extends ReceiverEventDaemonBase implements Initializable<ClientNodeContext>, ChatEventDaemonLoopback {

	private static final int DEF_SCHEDULER_SIZE = 2;
	private static final String SCHEDULER_NAME = "ReceivingChatEventDaemon";
	private static final long FORCED_CLOSED_WAIT = 2000;
	
	
	// --- INSTANCE MEMBERS --- //
	
	private ClientNodeContext con;
	private ClientRegistry registry;
    private JmxScheduler forceCloseScheduler;

    private final Logger log = Logger.getLogger(getClass());
	private final Logger clientLog = Logger.getLogger("CLIENTS");
	
	public ReceivingChatEventDaemon(String nodeId) {
		super(nodeId);
	}
	
	@Override
	protected void dispatch(RouterEvent ev) {
    	try {
	    	Event<?> event = ev.getRoutedEvent();
	    	event.unwrapForTarget(null);
	    	/*
	    	 * Since the multiple player events extends the 
	    	 * single player events we need to inspect
	    	 * the instance in correct order. I.e. multiple 
	    	 * player events first.
	    	 */
	    	/*if (event instanceof MulticastGameEvent) {
	    		dispatch((MulticastGameEvent) event);
	    		
			} else*/ if (event instanceof MulticastClientEvent) {
	    		dispatch((MulticastClientEvent) event);
	    		
			} else if (event instanceof ClientEvent<?>) {
				dispatch((ClientEvent<?>)event);
				
			} else if (event instanceof GameEvent) {
				dispatch((GameEvent)event);
			}
	    	
    	} catch (Throwable e) {
    		log.error("Exception when dispatching client event: "+e, e);
    		throw new RuntimeException(e);
    	} finally {
    		ev.acknowledge();
    	}
	}

	@Override
	protected String getSchedulerName() {
		return SCHEDULER_NAME;
	}

	public void init(ClientNodeContext con) throws SystemException {
		this.con = con;
		doSuperInit(con);
		initRegistry();
		initScheduler();
		initLoopback();
	}
	
	@Override
	public void destroy() {
		destroyScheduler();
		super.destroy();
	}

	public void dispatch(MulticastClientEvent event) {
    	HitCounter.getInstance().inc("MPCEventToClient");
        for (Object o : event.getPlayers()) {
            // TODO: Add check for client [handle(playerid)]
            executeEvent(((Integer)o).intValue(), (GameAction)event.getAction());
        }
    }
    
    

	// --- PRIVATE METHODS --- //
	
    private void initLoopback() {
    	ChatServiceContract chat = InternalComponentAccess.getRegistry().getServiceInstance(ChatServiceContract.class);
        chat.setLocalLoopback(this);
	}
	
    private void dispatch(ClientEvent<?> event) {
        HitCounter.getInstance().inc("ClientEventToClient");
        executeEvent(event.getPlayerId(), (GameAction)event.getAction());
    }
    
    private void dispatch(GameEvent event) {
    	HitCounter.getInstance().inc("GameEventToClient");
        executeEvent(event.getPlayerId(), event.getAction());
    }
    
    /*public void dispatch(MulticastGameEvent event) {
    	HitCounter.getInstance().inc("MPGEventToClient");
        for (Object o : event.getPlayers()) {
            // TODO: Add check for client [handle(playerid)]
            executeEvent(((Integer)o).intValue(), event.getAction());
        }
    }*/
    
    private void executeEvent(int playerid, GameAction action) {
        try{            
            // Get Client
        	Client client = registry.getClient(playerid);
            if (client != null) {
            	// Does the client have the correct session id? 
            	if ( client.getSessionId().equals(registry.getSessionId(playerid))) {
            		// Handle action
            		client.getActionHandler().handleAction(action);
            	} else {
            		// wrong session id, close this client
            		clientLog.debug("Client with session id "+client.getSessionId()+" will be forcibly closed by request: "+action);
            		
            		// create a forced logout packet
            		ForcedLogoutPacket packet = new ForcedLogoutPacket();
            		// TODO: change to non hardcoded values  
            		packet.code = 1; 
            		packet.message = "You have been logged in from another place";
            		// send packet
            		client.sendClientPacket(packet);
            		
            		// close client connection after two seconds
            		ForceClose forceClose = new ForceClose(client);
            		forceCloseScheduler.schedule(forceClose, FORCED_CLOSED_WAIT, TimeUnit.MILLISECONDS);
            		
            	}
            } else {
                log.warn("Client not found in registry. Playerid: "+playerid+" Action: "+action);
            }
            
        } catch(Exception ex) {
            log.error("Could not dispatch action: "+ex,ex);
        }
    }
	
	private void initScheduler() {
		forceCloseScheduler = new JmxScheduler(1, "ClientChatEventDaemon-Force-Close");
	}
	
	private void destroyScheduler() {
		forceCloseScheduler.stop();
	}

	private void initRegistry() {
		registry = con.getServices().getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();	
	}
	
	private void doSuperInit(ClientNodeContext con) {
		MBeanServer mbs = con.getMBeanServer();
		ClientNodeRouter router = con.getNodeRouter();
		Receiver<RouterEvent> receiver = router.getChatTopicReceiver();
		super.init(getSchedulerSize(), mbs, receiver);
	}
	
	private int getSchedulerSize() {
		return DEF_SCHEDULER_SIZE;
	}
	
	
	// --- INNER CLASSES --- //
	
	private class ForceClose extends SafeRunnable {
		
		private final Client client;
		
		public ForceClose(Client client) { this.client = client; }
		
		public void innerRun() {
			client.close();
		}
	}
}
