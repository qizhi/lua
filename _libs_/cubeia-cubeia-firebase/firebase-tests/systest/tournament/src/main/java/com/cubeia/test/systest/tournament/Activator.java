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
package com.cubeia.test.systest.tournament;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttFactory;
import com.cubeia.firebase.api.mtt.activator.ActivatorContext;
import com.cubeia.firebase.api.mtt.activator.CreationParticipant;
import com.cubeia.firebase.api.mtt.activator.MttActivator;
import com.cubeia.firebase.api.mtt.lobby.MttLobbyObject;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;
import com.cubeia.test.systest.io.protocol.ProtocolObjectFactory;
import com.cubeia.test.systest.tournament.tests.StandardProcessor;

public class Activator implements MttActivator, ActivatorMBean, RoutableActivator {

	public static final String ACTIVATOR_OBJECTNAME = "com.cubeia.test.systest:type=MttActivator";
	
	private MttFactory factory;
	private ActivatorContext con;

	private final Logger log = Logger.getLogger(getClass());
	
	public void setMttFactory(MttFactory factory) {
		checkContextClassLoaderAndJNDI();
		this.factory = factory;
	}

	public void destroy() { 
		destroyJmx();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onAction(ActivatorAction e) {
		checkContextClassLoaderAndJNDI();
		
		/*
		 * Activator routing test
		 */
		
		byte[] arr = (byte[])e.getData();
		ActivatorTestRequestPacket req = toTestRequestPacket(arr);
		ActivatorRouter router = con.getActivatorRouter();
		MttDataAction dataAction = new MttDataAction(req.mttId, -1);
		dataAction.getAttributes().addAll(e.getAttributes());
		dataAction.setData(ByteBuffer.wrap(arr));
		router.dispatchToTournament(req.mttId, dataAction);
	}

	public void init(ActivatorContext con) throws SystemException {
		checkContextClassLoaderAndJNDI();
		this.con = con;
		initJmx();
	}

	public void start() { 
		checkContextClassLoaderAndJNDI();
	}

	public void stop() { 
		checkContextClassLoaderAndJNDI();
	}

	
	// --- MBEAN METHODS --- //
	
	public int createTournament(int gameId, int seatsPerTable, String name, int capacity, int minPlayers) {
		return createTournament(gameId, seatsPerTable, name, capacity, minPlayers, StandardProcessor.class.getName());
	}
	
	@Override
	public int createTournament(int gameId, int seatsPerTable, String name, int capacity, int minPlayers, String processorClass) {
		return createTournament(gameId, "", seatsPerTable, name, capacity, minPlayers, processorClass);
	}
	
	public int createTournament(int gameId, String domain, int seatsPerTable, String name, int capacity, int minPlayers, String processorClass) {
		MttLobbyObject o = factory.createMtt(con.getMttId(), name, new Participant(gameId, domain, seatsPerTable, name, capacity, minPlayers, processorClass));
		return o.getTournamentId();
	}
	
	public void destroy(int gameId, int mttInstanceId) {
		factory.destroyMtt(gameId, mttInstanceId);
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private void initJmx() {
		try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName(ACTIVATOR_OBJECTNAME);
            mbs.registerMBean(this, monitorName);
        } catch (Exception e) {
            log.error("Error registering to jmx", e);
        }
	}
	
	private void destroyJmx() {
		try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName monitorName = new ObjectName(ACTIVATOR_OBJECTNAME);
            mbs.unregisterMBean(monitorName);
        } catch (Exception e) {
            log.error("Error unregistering to jmx", e);
        }
	}
	
	private ActivatorTestRequestPacket toTestRequestPacket(byte[] arr) {
		// try {
			return (ActivatorTestRequestPacket)new StyxSerializer(new ProtocolObjectFactory()).unpack(ByteBuffer.wrap(arr));
		/* } catch (IOException ex) {
			Logger.getLogger(getClass()).error("Failed to unmarchal packet", ex);
			return null;
		} */
	}
	
	private void checkContextClassLoaderAndJNDI() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null || !cl.equals(getClass().getClassLoader())) {
			throw new IllegalStateException("Found class loader: " + cl + "; Expected: " + getClass().getClassLoader());
		}
		checkJndiAccess();
	}

	private void checkJndiAccess() {
		try {
			InitialContext con = new InitialContext();
			Object o = con.lookup("java:comp/env/jdbc/test-jta");
			DataSource src = (DataSource) o;
			if(src == null) {
				throw new IllegalStateException("Did not find data source java:comp/env/jdbc/test-jta");
			}
		} catch(NameNotFoundException e) {
			throw new IllegalStateException("Did not find data source java:comp/env/jdbc/test-jta");
		} catch (NamingException e) {
			throw new IllegalStateException("Failed to lookup data source java:comp/env/jdbc/test-jta", e);
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Participant implements CreationParticipant {

		private final int gameId;
		private final int seatsPerTable;
		private final String name;
		private final int capacity;
		private final int minPlayers;
		private final String processor;
		private final String domain;

		public Participant(int gameId, String domain, int seatsPerTable, String name, int capacity, int minPlayers, String processorClass) {
			this.gameId = gameId;
			this.domain = domain;
			this.seatsPerTable = seatsPerTable;
			this.name = name;
			this.capacity = capacity;
			this.minPlayers = minPlayers;
			this.processor = processorClass;
		}

		public LobbyPath getLobbyPathForTournament(MTTState mtt) {
			return new LobbyPath(mtt.getMttLogicId(), domain, mtt.getId());
		}

		public void tournamentCreated(MTTState mtt, LobbyAttributeAccessor acc) { 
			MTTStateSupport state = ((MTTStateSupport) mtt);
			state.setCapacity(capacity);
			state.setGameId(gameId);
			state.setMinPlayers(minPlayers);
			state.setName(name);
			state.setSeats(seatsPerTable);
			checkCreateProcessor(state);
		}		
		
		private void checkCreateProcessor(MTTStateSupport data) {
			if(processor != null) {
				try {
					Class<?> cl = getClass().getClassLoader().loadClass(processor);
					if(!TournamentTestProcessor.class.isAssignableFrom(cl)) {
						throw new IllegalArgumentException("Class '" + cl.getName() + "' is not an instanceof TournamentTestProcessor");
					}
					TournamentTestProcessor proc = (TournamentTestProcessor) cl.newInstance();
					data.setState(proc);
				} catch (Exception e) {
					Logger.getLogger(getClass()).error("Failed to create processor", e);
				}
			}
		}
	}
}
