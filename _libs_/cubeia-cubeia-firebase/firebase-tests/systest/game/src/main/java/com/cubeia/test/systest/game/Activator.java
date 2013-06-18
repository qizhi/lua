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
package com.cubeia.test.systest.game;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.CreationParticipant;
import com.cubeia.firebase.api.game.activator.CreationRequestDeniedException;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.game.activator.MttAwareActivator;
import com.cubeia.firebase.api.game.activator.RequestAwareActivator;
import com.cubeia.firebase.api.game.activator.RequestCreationParticipant;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;
import com.cubeia.test.systest.io.protocol.ProtocolObjectFactory;
import com.cubeia.test.systest.io.protocol.QuickSeatRequest;

public class Activator implements GameActivator, RequestAwareActivator, MttAwareActivator, ActivatorMBean, RoutableActivator {

	public static final String ACTIVATOR_OBJECTNAME = "com.cubeia.test.systest:type=GameActivator";

	private ActivatorContext con;
	
	private final Logger log = Logger.getLogger(getClass());

	public void destroy() { 
		checkContextClassLoaderAndJNDI();
		destroyJmx();
	}

	public void init(ActivatorContext con) throws SystemException {
		checkContextClassLoaderAndJNDI();
		this.con = con;
		initJmx();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onAction(ActivatorAction e) {
		checkContextClassLoaderAndJNDI();
		
		/*
		 * Activator touring test
		 */
		
		byte[] arr = (byte[])e.getData();
		ProtocolObject obj = toProtocolObject(arr);
		ActivatorRouter router = con.getActivatorRouter();
		if(obj instanceof ActivatorTestRequestPacket) {
			ActivatorTestRequestPacket req = (ActivatorTestRequestPacket) obj;
			GameDataAction dataAction = new GameDataAction(-1, req.tableId);
			dataAction.getAttributes().addAll(e.getAttributes());
			dataAction.setData(ByteBuffer.wrap(arr));
			router.dispatchToGame(req.tableId, dataAction);
		} else if(obj instanceof QuickSeatRequest) {
			QuickSeatRequest req = (QuickSeatRequest) obj;
			// JoinRequestPacket pack = new JoinRequestPacket(req.tableId, (byte) -1, null);
			JoinRequestAction dataAction = new JoinRequestAction(req.pid, req.tableId, (byte) -1, "");
			// arr = fromProtocolObject(pack);
			// dataAction.setData(ByteBuffer.wrap(arr));
			router.dispatchToGame(req.tableId, dataAction);
		}
	}

	public void start() { 
		checkContextClassLoaderAndJNDI();
	}

	public void stop() { 
		checkContextClassLoaderAndJNDI();
	}

	
	// --- REQUEST AWARE --- //
	
	public RequestCreationParticipant getParticipantForRequest(int pid, int seats, Attribute[] attributes) throws CreationRequestDeniedException {
		return new RequestParticipant();
	}
	
	
	// --- MTT AWARE --- //
	
	public void mttTableCreated(Table table, int mttId, Object att, LobbyAttributeAccessor acc) {
		table.getGameState().setState(new Data());
	}
	
	
	
	// --- MBEAN METHODS --- //
	
	public int createTable(int seats, String name, String props) {
		return createTable(seats, name, props, null);
	}
	
	public int createTable(int seats, String name, String props, String processor) {
		return createTable(seats, "", name, props, processor);
	}
	
	public int createTable(int seats, String domain, String name, String props, String processor) {
		TableFactory fact = con.getTableFactory();
		log.debug("Creating table with " + seats + " seats, domain '" + domain + "', name '" + name + "' and properties: " + props);
		LobbyTable table = fact.createTable(seats, new NameParticipant(name, domain, props, processor));
		log.debug("Table created with id " + table.getTableId());
		return table.getTableId();
	}

	public boolean destroyTable(int id) {
		log.debug("Destroying table " + id);
		TableFactory fact = con.getTableFactory();
		return fact.destroyTable(id, true);
	}
	
	public int createTable(int seats, String name) {
		return createTable(seats, name, null);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	/*private byte[] fromProtocolObject(JoinRequestPacket obj) {
		try {
			return new StyxSerializer(new ProtocolObjectFactory()).packArray(obj);
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to marchal packet", e);
			return null;
		}
	}*/
	
	private ProtocolObject toProtocolObject(byte[] arr) {
		// try {
			return new StyxSerializer(new ProtocolObjectFactory()).unpack(ByteBuffer.wrap(arr));
		/*} catch (IOException ex) {
			Logger.getLogger(getClass()).error("Failed to unmarchal packet", ex);
			return null;
		}*/
	}
	
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
	
	private static class BaseParticipant implements CreationParticipant {

		private final String props;

		private BaseParticipant() { 
			props = null;
		}
		
		private BaseParticipant(String props) {
			this.props = props;
		}

		public LobbyPath getLobbyPathForTable(Table table) {
			return new LobbyPath(table.getMetaData().getGameId(), "", table.getId());
		}

		public String getTableName(GameDefinition def, Table table) {
			return null;
		}

		public void tableCreated(Table table, LobbyTableAttributeAccessor acc) {
			table.getGameState().setState(new Data());
			checkSetProperties(acc);
		}

		
		// --- PRIVATE METHODS --- //
		
		private void checkSetProperties(LobbyTableAttributeAccessor acc) {
			if(props != null) {
				String[] split = props.split(",");
				for (String s : split) {
					if(s.length() > 0) {
						int i = s.indexOf('=');
						if(i == -1) {
							acc.setStringAttribute(s, s);
						} else {
							String key = s.substring(0, i);
							String val = s.substring(i + 1);
							acc.setStringAttribute(key, val);
						}
					}
				}
			}
		}
	}
	
	private static class NameParticipant extends BaseParticipant {

		private final String name;
		private final String processor;
		private final String domain;

		private NameParticipant(String domain, String name, String props, String processor) {
			super(props);
			this.domain = (domain == null ? "" : domain);
			this.name = name;
			this.processor = processor;
		}
		
		@Override
		public LobbyPath getLobbyPathForTable(Table table) {
			return new LobbyPath(table.getMetaData().getGameId(), domain, table.getId());
		}

		public String getTableName(GameDefinition def, Table table) {
			return name;
		}
		
		@Override
		public void tableCreated(Table table, LobbyTableAttributeAccessor acc) {
			super.tableCreated(table, acc);
			checkCreateProcessor(table);
		}

		
		// --- PRIVATE METHODS ---- //
		
		private void checkCreateProcessor(Table table) {
			if(processor != null) {
				try {
					Class<?> cl = getClass().getClassLoader().loadClass(processor);
					if(!TestProcessor.class.isAssignableFrom(cl)) {
						throw new IllegalArgumentException("Class '" + cl.getName() + "' is not an instanceof TestProcessor");
					}
					TestProcessor proc = (TestProcessor) cl.newInstance();
					Data data = (Data) table.getGameState().getState();
					data.setProcessor(proc);
				} catch (Exception e) {
					Logger.getLogger(getClass()).error("Failed to create processor", e);
				}
			}
		}
	}
	
	private static class RequestParticipant extends BaseParticipant implements RequestCreationParticipant {

		public int[] modifyInvitees(int[] invitees) {
			return invitees;
		}

		public boolean reserveSeatsForInvitees() {
			return true;
		}
	}
}
