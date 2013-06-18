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
package com.cubeia.firebase.service.mbus.local;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.mbus.common.SelectingSimpleChannelAccess;
import com.cubeia.firebase.service.mbus.common.SelectorNotifier;
import com.cubeia.firebase.service.mbus.common.SimpleChannel;
import com.cubeia.firebase.service.mbus.common.SimpleChannelPooledHandoff;
import com.cubeia.firebase.service.mbus.common.SimpleChannelReceiver;
import com.cubeia.firebase.service.mbus.common.StrictReceiver;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusDetails;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.MBusListener;
import com.cubeia.firebase.service.messagebus.MBusRedistributor;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RedistributionListener;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.PartitionMapImpl;
import com.cubeia.firebase.service.messagebus.util.PartitionMapInfo;

public class MBusService implements MBusContract, Service, MBusServiceMBean {
	
	private static final AtomicInteger COUNT = new AtomicInteger();
	
	private final InternalMapping mapping = new InternalMapping();
	private final List<RedistributionListener> redistLists = new CopyOnWriteArrayList<RedistributionListener>();
	
	private PartitionedQueues repo;
	private ServiceContext con;
	private SocketAddress localSocket;

	private ChatSenderReceiver chatHub;
	private ClientSenderReceiver clientHub;
	
	public MBusService() {
		repo = new PartitionedQueues(mapping);
	}
	
	
	// --- MBUS LISTENERS --- //

	@Override
	public void addMBusListener(MBusListener list) {
		Arguments.notNull(list, "list");
		mapping.addMBusListener(list);
	}
	
	@Override
	public void removeMBusListener(MBusListener list) {
		Arguments.notNull(list, "list");
		mapping.removeMBusListener(list);
	}

	
	
	// --- REDISTRIBUTION LISTENERS --- //
	
	@Override
	public void addRedistributorListener(RedistributionListener list) { 
		Arguments.notNull(list, "list");
		redistLists.add(list);
	}
	
	@Override
	public void removeRedistributorListener(RedistributionListener list) {
		Arguments.notNull(list, "list");
		redistLists.remove(list);
	}
	
	
	// --- MBUS SERVICES --- //
	
	@Override
	public Receiver<ChannelEvent> createReceiver(EventType type, String partition, String ownerId) throws MBusException {
		if(type == EventType.CHAT) {
			return checkCreateChatHub(ownerId);
		} else if(type == EventType.CLIENT) {
			return createClientReceiver(ownerId);
		} else if(type == EventType.MTT) {
			return new MttReceiver(repo);
		} else {
			return new GameReceiver(repo);
		}
	}

	@Override
	public Sender<Event<?>> createSender(EventType type, String ownerId) throws MBusException {
		if(type == EventType.CHAT) {
			return checkCreateChatHub(ownerId);
		} else if(type == EventType.CLIENT) {
			return checkCreateClientHub(ownerId);
		} else {
			return new InternalSender(ownerId, repo.getSinkForType(type));
		}
	}

	@Override
	public PartitionMap getCurrentPartitionMap() {
		return new PartitionMapImpl(mapping);
	}

	@Override
	public MBusDetails getMBusDetails() {
		// WARNING: This returns the same socket
		// for all types, it feels like an unknown
		MBusDetails det = new MBusDetails();
		det.setSocketIdFor(EventType.GAME, localSocket);
		det.setSocketIdFor(EventType.MTT, localSocket);
		det.setSocketIdFor(EventType.CHAT, localSocket);
		det.setSocketIdFor(EventType.CLIENT, localSocket);
		return det;
	}

	@Override
	public MBusRedistributor getRedistributor() {
		return new Redistributor(mapping, redistLists);
	}
	
	
	// --- MBEAN --- //
	
	@Override
	public String[] getDetails() {
		MBusDetails det = getMBusDetails();
		EventType[] types = EventType.values();
		String[] arr = new String[types.length];
		for (int i = 0; i < arr.length; i++) {
			EventType t = types[i];
			arr[i] = t.toString() + " = " + det.getSocketIdFor(t);
		}
		return arr;
	}
	
	@Override
	public boolean isHalted() {
		return repo.isHalted();
	}
	
	
	// --- HALTABLE --- //
	
	@Override
	public void resume(ClusterLayout lay) { 
		repo.resume();
	}

	@Override
	public void halt() { 
		repo.halt();
	}


	
	// --- SERVICE METHODS --- //

	@Override
	public void destroy() { 
		repo.destroy();
		destroyJmx();
	}

	@Override
	public void init(ServiceContext con) throws SystemException {
		this.con = con; 
		repo.init(con.getMBeanServer());
		setupLocalSocket();
		initJmx();
	}

	@Override
	public void start() { }

	@Override
	public void stop() { }

	
	// --- PRIVATE METHODS --- //
	
	private Receiver<ChannelEvent> createClientReceiver(String ownerId) {
		checkCreateClientHub(ownerId);
		String id = "clientReceiver" + COUNT.incrementAndGet();
		SelectingSimpleChannelAccess fetcher = new SelectingSimpleChannelAccess(id, EventType.CLIENT, con.getMBeanServer(), getSimpleChannel(), (SelectorNotifier)null);
		SimpleChannelPooledHandoff exec = new SimpleChannelPooledHandoff(id, EventType.CLIENT, fetcher);
		return new StrictReceiver(ownerId, exec, con.getMBeanServer());
	}
	
	private SimpleChannel getSimpleChannel() {
		return new SimpleChannel() {
			
			private EventListener<ChannelEvent> listener;
			
			@Override
			public void setChannelreceiver(final SimpleChannelReceiver recevier) {
				if(listener != null) {
					clientHub.removeEventListener(listener);
				}
				if(recevier != null) {
					listener = new EventListener<ChannelEvent>() {
						@Override
						public void eventReceived(ChannelEvent event) {
							recevier.receive(event);
						}
					};
					clientHub.addEventListener(listener);
				}
			}
			
			@Override
			public String getLocalAddress() {
				return "n/a";
			}
		};
	}


	private void setupLocalSocket() throws SystemCoreException {
		ServiceRegistry reg = con.getParentRegistry();
		ConnectionServiceContract serv = reg.getServiceInstance(ConnectionServiceContract.class);
		ClusterConnection c = serv.getSharedConnection();
		localSocket = c.getLocalAddress();
	}
	
	private synchronized ChatSenderReceiver checkCreateChatHub(String ownerId) {
		if(chatHub == null) {
			chatHub = new ChatSenderReceiver(ownerId);
		}
		return chatHub;
	}

	private synchronized ClientSenderReceiver checkCreateClientHub(String ownerId) {
		if(clientHub == null) {
			clientHub = new ClientSenderReceiver(ownerId);
		}
		return clientHub;
	}
	
	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.mbus:type=PartitionMap");
	        mbs.registerMBean(new PartitionMapInfo(mapping), monitorName);
	        monitorName = new ObjectName("com.cubeia.firebase.mbus:type=MBusService");
	        mbs.registerMBean(this, monitorName);
		} catch(Exception e) {
			Logger.getLogger(getClass()).error("failed to start mbean", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.mbus:type=PartitionMap");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
	        monitorName = new ObjectName("com.cubeia.firebase.mbus:type=MBusService");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			Logger.getLogger(getClass()).error("failed to stop mbean", e);
		}
	}
}
