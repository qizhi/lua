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
package com.cubeia.firebase.game.table.trans;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.GameSeat;
import com.cubeia.firebase.api.game.table.Seat;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.firebase.api.util.UnmodifiableSet;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.game.MapDeltaChange;
import com.cubeia.firebase.game.DeltaChange.Type;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.service.InternalServiceRegistry;

public final class StandardPlayerSet implements TablePlayerSet {

    private static final transient Logger log = Logger.getLogger(StandardPlayerSet.class);

    private boolean isDirty;
    private final StandardTable table;

    private SeatingMap seatingMap;

    StandardPlayerSet(StandardTable table) {
        this.table = table;
    }

    public void addPlayer(GenericPlayer player, int seat) {
        isDirty = true;
        checkSeatClone();
        Seat<GenericPlayer> seatObject = doCloneAdd(player, seat);
        doDeltaAdd(player, seat, seatObject);
    }

    public GenericPlayer getPlayer(int playerId) {
        if(isDirty) return getClone().getPlayers().get(playerId);
        else return getReal().getPlayers().get(playerId);
    }

    public int getPlayerCount() {
        if(isDirty) return getClone().getPlayers().size();
        else return getReal().getPlayers().size();
    }

    public UnmodifiableSet<GenericPlayer> getPlayers() {
        return new UnmodifiableSet<GenericPlayer>() {

            Map<Integer, GenericPlayer> map = (isDirty ? getClone().getPlayers() : getReal().getPlayers());

            public Iterator<GenericPlayer> iterator() {
                return map.values().iterator();
            }

            public boolean contains(GenericPlayer o) {
                if(o == null) return false;
                else return map.containsKey(o.getPlayerId());
            }
        };
    }

    public TableSeatingMap getSeatingMap() {
        isDirty = true;
        checkSeatClone();
        if(seatingMap == null) {
            seatingMap = new SeatingMap(this, getClone());
        }
        return seatingMap;
    }

    public void removePlayer(int playerId) {
        isDirty = true;
        checkSeatClone();
        doCloneRemove(playerId);
    }

    public boolean getIsDirty() {
        return isDirty;
    }

//    This method seems hard to get right with messaging et al.
//    public void seatPlayer(GenericPlayer player, int seat) {
//        JoinRequestAction joinAction = new JoinRequestAction(player.getPlayerId(), table.getId(), seat, player.getName());
//        
//        // Check seating rules
//        boolean allowedBySeatingRules = new DefaultSeatingRules().actionAllowed(joinAction, table);
//        if (!allowedBySeatingRules) {
//            throw new IllegalArgumentException("Seating not allowed");
//        }
//
//        table.getNotifier().notifyAllPlayersExceptOne(joinAction, player.getPlayerId());
//
//        // NOTE: Access to ServiceRegistry here because injection through all layers would be complicated.
//        InternalServiceRegistry registry = InternalComponentAccess.getRegistry();
//
//        if (registry != null) {
//            PublicClientRegistryService clientRegistry = registry.getServiceInstance(PublicClientRegistryService.class);
//            clientRegistry.registerPlayerToTable(table.getId(), player.getPlayerId(), seat, table.getMetaData().getMttId(), false);
//        } else {
//            log.warn("No service registry found. Ignoring setting player-table association for pid["+player.getPlayerId()+"] and table["+table.getId()+"]");
//        }
//
//        addPlayer(player, seat);
//    }

    public void unseatPlayer(int playerId) {
        if (getPlayer(playerId) != null) {
            LeaveAction leaveAction = new LeaveAction(playerId, table.getId());
            table.getNotifier().notifyAllPlayersExceptOne(leaveAction, playerId);

            // NOTE: Access to ServiceRegistry here because injection through all layers would be complicated.
            InternalServiceRegistry registry = InternalComponentAccess.getRegistry();

            if (registry != null) {
                PublicClientRegistryService clientRegistry = registry.getServiceInstance(PublicClientRegistryService.class);
                clientRegistry.registerPlayerToTable(table.getId(), playerId, -1, table.getMetaData().getMttId(), true);
            } else {
                log.warn("No service registry found. Ignoring player-table association removal for pid["+playerId+"] and table["+table.getId()+"]");
            }

            removePlayer(playerId);
        }

    }

    @Override
    public String toString() {
        return "StandardPlayerSet; seats: " + getClone().getSeats();
    }

    // --- PRIVATE METHODS --- //

    private Seat<GenericPlayer> getSeatForPlayerId(int playerId) {
        Seat<GenericPlayer> result = null;
        for (Seat<GenericPlayer> seat : getClone().getSeats()) {
            if (seat.isOccupied() && seat.getPlayerId() == playerId) {
                result = seat;
                break;
            }
        }
        return result;
    }

    private TableData getClone() {
        return table.getDataClone();
    }

    public TableData getReal() {
        return table.getRealData();
    }

    private void checkPlayerClone() {
        CloneTableData clone = table.getDataClone();
        if(clone.getPlayers() == null) {
            Map<Integer, GenericPlayer> plyrs = table.getRealData().getPlayers();
            Map<Integer, GenericPlayer> next = new TreeMap<Integer, GenericPlayer>();
            for (GenericPlayer p : plyrs.values()) {
                next.put(p.getPlayerId(), new GenericPlayer(p)); // CLONING
            }
            clone.setPlayers(next);
        }
    }

    private void checkSeatClone() {
        checkPlayerClone();
        CloneTableData clone = table.getDataClone();
        if(clone.getSeats() == null) {
            List<Seat<GenericPlayer>> seats = table.getRealData().getSeats();
            List<Seat<GenericPlayer>> next = new LinkedList<Seat<GenericPlayer>>();
            for (Seat<GenericPlayer> s : seats) {
                Seat<GenericPlayer> seat = new GameSeat<GenericPlayer>(s);
                if(s.isOccupied()) {
                    seat.seat(getPlayer(s.getPlayerId())); // USE ONE CLONE
                }
                next.add(seat);
            }
            clone.setSeats(next);
        }
    }

    private Seat<GenericPlayer> doCloneAdd(GenericPlayer player, int seat) {
        TableData clone = getClone();
        List<Seat<GenericPlayer>> seats = clone.getSeats();
        Seat<GenericPlayer> seatObject = seats.get(seat);
        player.setSeatId(seat);
        seatObject.seat(player);  
        Map<Integer, GenericPlayer> plyrs = clone.getPlayers();
        plyrs.put(player.getPlayerId(), player);
        return seatObject;
    }

    private void doDeltaAdd(GenericPlayer player, int seat, Seat<GenericPlayer> seatObject) {
        List<Seat<GenericPlayer>> realSeats = getReal().getSeats();
        Map<Integer, GenericPlayer> realPlayers = getReal().getPlayers();
        addDelta(new MapDeltaChange<Integer, GenericPlayer>(realPlayers, Type.SET, player.getPlayerId(), player));
        addDelta(new ListDeltaChange<Seat<GenericPlayer>>(realSeats, Type.SET, seat, seatObject));
    }

    private void addDelta(DeltaChange change) {
        table.getDeltaChanges().add(change);
    }

    private void doCloneRemove(int playerId) {
        TableData clone = getClone();
        GenericPlayer plyr = clone.getPlayers().remove(playerId);
        if(plyr != null) {
            Seat<GenericPlayer> seat = getSeatForPlayerId(playerId);
            if(seat != null) seat.clear();
            doDeltaRem(playerId, seat);
        }
    }

    private void doDeltaRem(int playerId, Seat<GenericPlayer> seat) {
        List<Seat<GenericPlayer>> realSeats = getReal().getSeats();
        Map<Integer, GenericPlayer> realPlayers = getReal().getPlayers();
        addDelta(new MapDeltaChange<Integer, GenericPlayer>(realPlayers, Type.REM, playerId, null));
        addDelta(new ListDeltaChange<Seat<GenericPlayer>>(realSeats, Type.SET, seat.getId(), seat));
    }


    // --- PRIVATE CLASSES --- //

    private final static class SeatingMap implements TableSeatingMap {

        private final TableData clone;
        private final TablePlayerSet plyrs;

        private SeatingMap(TablePlayerSet plyrs, TableData clone) {
            this.plyrs = plyrs;
            this.clone = clone;
        }

        public int countSeatedPlayers() {
            int c = 0;
            for (Seat<GenericPlayer> s : clone.getSeats()) {
                if(s.isOccupied()) {
                    c++;
                }
            }
            return c;
        }

        public int getFirstVacantSeat() {
            for (Seat<GenericPlayer> s : clone.getSeats()) {
                if(s.isVacant()) {
                    return s.getId(); // EARLY RETURN
                }
            }
            return -1;
        }

        public int getNumberOfSeats() {
            return clone.getSeats().size();
        }

        public Seat<GenericPlayer> getSeat(int seatId) {
            if(seatId >= clone.getSeats().size()) return null;
            else return clone.getSeats().get(seatId);
        }

        public boolean hasVacantSeats() {
            return plyrs.getPlayerCount() < getNumberOfSeats();
        }

        public boolean isSeatVacant(int seatId) {
            Seat<GenericPlayer> seat = getSeat(seatId);
            return (seat == null ? false : seat.isVacant());
        }
    }
}