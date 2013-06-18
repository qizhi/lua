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
package com.cubeia.test.systest.tournament.tests;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;
import com.cubeia.firebase.api.mtt.seating.SeatingContainer;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.firebase.api.mtt.support.MTTSupport;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;
import com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.test.systest.io.protocol.ActivatorTestMttResponsePacket;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;
import com.cubeia.test.systest.io.protocol.ProtocolObjectFactory;
import com.cubeia.test.systest.tournament.BalanceCalculator;

/*
 * This is the standard processor you get if you don't specify anything 
 * else. 
 */
public class StandardProcessor extends ProcessorBase {

	private static final long serialVersionUID = -7900430775695899750L;
	
	private static final long START_DELAY = 500;

	public PlayerInterceptor getPlayerInterceptor(final MTTSupport mtt, MTTStateSupport state) {
		return null;
	}

	public PlayerListener getPlayerListener(final MTTSupport mtt, MTTStateSupport state) {
		return new PlayerListener() {
		
			public void playerUnregistered(MttInstance inst, int pid) { }
		
			public void playerRegistered(MttInstance inst, MttRegistrationRequest request) {
				MTTStateSupport sup = (MTTStateSupport) inst.getState();
				PlayerRegistry reg = sup.getPlayerRegistry();
				if(reg.size() == sup.getCapacity()) {
					mtt.createTables(sup, noTables(sup.getCapacity(), sup.getSeats()), "Systest MTT_");
				}
			}

			private int noTables(int capacity, int seats) {
				if(capacity % seats == 0) {
					return (int)(capacity / seats);
				} else {
					return (int)(capacity / seats) + 1;
				}
			}
		};
	}
	
	@SuppressWarnings("deprecation")
	public void process(MttDataAction action, MttInstance instance) {
		// Testing activator routers here... Systest: TestActivationRouters.java 
		ActivatorTestRequestPacket req = toTestRequestPacket(action.getData().array());
		ActivatorTestMttResponsePacket resp = new ActivatorTestMttResponsePacket(req.mttId);
		MttDataAction dataAction = createDataAction(req.clientId, req.mttId, resp);
		dataAction.getAttributes().addAll(action.getAttributes());
		getMTTSupport().getMttNotifier().notifyPlayer(req.clientId, dataAction);
	}

	public void process(MttSeatingFailedAction action, MttInstance instance) { }

	public void process(MttRoundReportAction action, MttInstance mttInstance) { }

	public void process(MttTablesCreatedAction action, MttInstance instance) {
		MTTStateSupport state = (MTTStateSupport)instance.getState();
		Collection<SeatingContainer> seating = toContainerMap(state, action.getTables());
		getMTTSupport().seatPlayers(state, seating);
        MttObjectAction oa = new MttObjectAction(instance.getId(), new Starter(seating));
        instance.getScheduler().scheduleAction(oa, START_DELAY);
	}

	public void process(MttObjectAction action, MttInstance instance) {
		if(action.getAttachment() instanceof Starter) {
        	startMtt(instance, ((Starter)action.getAttachment()).getTables());
        }
	}

	public void tournamentCreated(MttInstance mttInstance) {
		// TODO Auto-generated method stub

	}

	public void tournamentDestroyed(MttInstance mttInstance) {
		// TODO Auto-generated method stub

	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void startMtt(MttInstance instance, Collection<Integer> tables) {
		// state.setTournamentLobbyStatus(TournamentLobbyStatus.STARTING);
		// jmxHandler.getStatsBean(state.getId()).setState(TournamentLobbyStatus.STARTING);
		getMTTSupport().sendRoundStartActionToTables((MTTStateSupport)instance.getState(), tables);
	}
	
	protected MttDataAction createDataAction(int playerId, int mttId, ProtocolObject packet) {
		MttDataAction act = new MttDataAction(mttId, playerId);
		// try {
			act.setData(new StyxSerializer(new ProtocolObjectFactory()).pack(packet));
		/*} catch (IOException e) {
			Logger.getLogger(getClass()).error("Failed to serialize action", e);
		}*/
		return act;
	}
	
	private Collection<SeatingContainer> toContainerMap(MTTStateSupport state, Set<Integer> tables) {
		List<Integer> tab = new ArrayList<Integer>(tables);
		Collection<MttPlayer> players = state.getPlayerRegistry().getPlayers();
		List<Set<MttPlayer>> balance = new BalanceCalculator<MttPlayer>().balance(players, tables.size());
		List<SeatingContainer> list = new LinkedList<SeatingContainer>();
		int i = 0;
		for (Set<MttPlayer> set : balance) {
			for (MttPlayer p : set) {
				list.add(new SeatingContainer(p.getPlayerId(), tab.get(i)));
			}
			i++;
		}
		return list;
	}
	
	private ActivatorTestRequestPacket toTestRequestPacket(byte[] arr) {
		// try {
			return (ActivatorTestRequestPacket)new StyxSerializer(new ProtocolObjectFactory()).unpack(ByteBuffer.wrap(arr));
		/*} catch (IOException ex) {
			Logger.getLogger(getClass()).error("Failed to unmarchal packet", ex);
			return null;
		}*/
	}

	
	// --- PRIVATE CLASSES --- //
	
    public static class Starter implements Serializable {
    	
    	private static final long serialVersionUID = 7790526034128408302L;
		
    	private Collection<Integer> tables;
    	
    	public Starter(Collection<SeatingContainer> seating) {
    		tables = new HashSet<Integer>();
    		for (SeatingContainer c : seating) {
    			tables.add(c.getTableId());
    		}
    	}

		public Collection<Integer> getTables() {
			return tables;
		} 
	}
}
