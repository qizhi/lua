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
package com.cubeia.test.loadtest.game;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.Game;
import com.cubeia.firebase.api.game.GameProcessor;
import com.cubeia.firebase.api.game.TableListenerProvider;
import com.cubeia.firebase.api.game.context.GameContext;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.TableListenerAdapter;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.test.loadtest.io.protocol.LoadRequest;
import com.cubeia.test.loadtest.io.protocol.LoadResponse;
import com.cubeia.test.loadtest.io.protocol.ProtocolObjectFactory;

public class GameImpl implements Game, TableListenerProvider, GameImplMBean {

	private Logger log = Logger.getLogger(getClass());
	
	private final AtomicInteger total = new AtomicInteger();
	private final AtomicInteger recon = new AtomicInteger();
	private final AtomicInteger fails = new AtomicInteger();
	
	@Override
	public void init(GameContext con) throws SystemException { 
		MBeanServer serv = ManagementFactory.getPlatformMBeanServer();
		try {
			serv.registerMBean(this, new ObjectName("com.cubeia.test.loadtest.game:type=Stats"));
		} catch (Exception e) {
			log.error("Failed to mount MBean", e);
		} 
	}

	@Override
	public TableListener getTableListener(Table table) {
		return new Listener();
	}
	
	@Override
	public void destroy() { }

	@Override
	public GameProcessor getGameProcessor() {
		return new Processor();
	}
	
	
	// --- MBEAN METHODS --- //
	
	public int getNoOfReconnects() {
		return recon.get();
	};
	
	@Override
	public int getNoOfSequenceErrors() {
		return fails.get();
	}
	
	@Override
	public int getTotalPackets() {
		return total.get();
	}
	

	
	// --- INNER CLASSES --- //
	
	private class Listener extends TableListenerAdapter {

		/*private final ProtocolObjectFactory fact = new ProtocolObjectFactory();
		private final StyxSerializer styx = new StyxSerializer(fact);
		
		@Override
		public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {
			LoadData data = (LoadData) table.getGameState().getState();
			if(status == PlayerStatus.CONNECTED && data.counter.containsKey(playerId)) {
				int count = data.counter.get(playerId);
				log.info("Possible re-connect, resending state. Table["+table.getId()+"] Pid["+playerId+"] previous["+count+"]");
				GameDataAction action = createAction(playerId, table.getId(), count);
				table.getNotifier().notifyPlayer(playerId, action);
			}
		}
		
		
		// --- PRIVATE METHODS --- //
		
		private GameDataAction createAction(int playerId, int tableId, int count) {
			try {
				GameDataAction action = new GameDataAction(playerId, tableId);
				LoadResponse resp = new LoadResponse();
				resp.pid = playerId;
				resp.tableid = tableId;
				resp.seq = count;
				resp.resend = true;
				action.setData(styx.pack(resp));
				return action;
			} catch (Exception e) {
				throw new IllegalStateException("protocol encode error", e);
			}
		}*/
	}
	
	private class Processor implements GameProcessor {
		
		private final ProtocolObjectFactory fact = new ProtocolObjectFactory();
		private final StyxSerializer styx = new StyxSerializer(fact);
		
		@Override
		public void handle(GameDataAction action, Table table) {
			LoadRequest packet = parsePacket(action);
			verifyPacketSequence(packet, table);
			checkForceDelay(packet, table);
			sendToAll(packet, table);
		}

		@Override
		public void handle(GameObjectAction action, Table table) { }
		
		
		// --- PRIVATE METHODS --- //
		
		private void checkForceDelay(LoadRequest packet, Table table) {
			if(packet.delay > 0) {
				try {
					Thread.sleep(packet.delay);
				} catch(InterruptedException  e) { }
			}
		}
		
		private void sendToAll(LoadRequest packet, Table table) {
			GameDataAction action = createAction(packet);
			table.getNotifier().notifyAllPlayers(action);
		}
		
		private GameDataAction createAction(LoadRequest packet) {
			try {
				GameDataAction action = new GameDataAction(packet.pid, packet.tableid);
				LoadResponse resp = new LoadResponse();
				resp.pid = packet.pid;
				resp.tableid = packet.tableid;
				resp.seq = packet.seq;
				resp.resend = false;
				action.setData(styx.pack(resp));
				return action;
			} catch (Exception e) {
				throw new IllegalStateException("protocol encode error", e);
			}
		}

		private void verifyPacketSequence(LoadRequest packet, Table table) {
			LoadData data = (LoadData) table.getGameState().getState();
			/*
			 * Hack: we're treating SEQ 1 as the first from a bot, this is
			 * black magic and should be done with seating etc...
			 */
			if (packet.seq != 1 && data.counter.containsKey(packet.pid)) {
				Integer previous = data.counter.get(packet.pid);
				if (previous == packet.seq && packet.resend) {
					log.info("Reconnect resend detected! Table["+table.getId()+"] Pid["+packet.pid+"] seq["+packet.seq+"]");
					recon.incrementAndGet();
				} else if (previous + 1 != packet.seq) {
					log.error("Bad packet order detected. Table["+table.getId()+"] Pid["+packet.pid+"] seq["+packet.seq+"] previous["+previous+"]");
					fails.incrementAndGet();
				} else {
					if(log.isTraceEnabled()) {
						log.debug("Packet accepted. Table["+table.getId()+"] Pid["+packet.pid+"] seq["+packet.seq+"]");
					}
				}
			} else {
				log.info("First packet accepted. Table["+table.getId()+"] Pid["+packet.pid+"] seq["+packet.seq+"]");
			}
			data.counter.put(packet.pid, packet.seq);
			total.incrementAndGet();
		}

		private LoadRequest parsePacket(GameDataAction action) {
			try {
				return (LoadRequest) styx.unpack(action.getData());
			} catch (Exception e) {
				throw new IllegalStateException("protocol parse error", e);
			}
		}
	}
}
