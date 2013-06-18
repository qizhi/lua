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
package com.cubeia.firebase.server.game.activation;

import static com.cubeia.firebase.api.lobby.LobbyPathType.TABLES;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.CleanupPlayerAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.activator.CreationParticipant;
import com.cubeia.firebase.api.game.activator.CreationRequestDeniedException;
import com.cubeia.firebase.api.game.activator.DefaultCreationParticipant;
import com.cubeia.firebase.api.game.activator.MttAwareActivator;
import com.cubeia.firebase.api.game.activator.RequestAwareActivator;
import com.cubeia.firebase.api.game.activator.RequestCreationParticipant;
import com.cubeia.firebase.api.game.handler.AbstractTableActionHandler;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributeMapper;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.lobby.LobbyTableFilter;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.game.table.TableFactory;
import com.cubeia.firebase.game.table.comm.CreationRequestData;
import com.cubeia.firebase.game.table.trans.StandardTable;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.ObjectEmerged;
import com.cubeia.firebase.server.commands.TableCommandData;
import com.cubeia.firebase.server.commands.TableCreated;
import com.cubeia.firebase.server.commands.TableRemoved;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.lobby.model.DefaultLobbyTableAccessor;
import com.cubeia.firebase.server.lobby.model.MapLobbyAttributeAccessor;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.util.GameObjectIdSysStateMapper;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.service.space.ExtendedGameObjectSpace;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.LobbyTableImpl;
import com.cubeia.util.Lists;

// FIXME: Bloody well refactor this class. It's horrible. /LJN
public class DefaultTableFactory implements com.cubeia.firebase.api.game.activator.TableFactory {
	
	/**
	 * The path to a particular table for a particular tournament.
	 */
	public static LobbyPath getLobbyPathForMttTable(int gameId, int mttId, int tableId) {
		return new LobbyPath(gameId, "mtt/" + mttId, tableId);
	}
	
	/**
	 * The path to the lobby node under which the tables for a tournament is stored. This
	 * is used when a tournament is destroyed in order to clear the lobby completely.
	 */
	public static LobbyPath getNodeLobbyPathForMttTables(int gameId, int mttId) {
		return new LobbyPath(gameId, "mtt/" + mttId);
	}

    private static final int TABLE_LOCK_TIMEOUT = 1000;
    
    
    // --- INSTANCE MEMBERS --- //
    
	private final ActivationBean activator;
    private final CreationParticipant defPart;
    private final GameObjectSpace<FirebaseTable, GameAction> tables;
    private final Logger log = Logger.getLogger(getClass());
    private final GameObjectIdSysStateMapper sysMapper;
    private final SystemStateServiceContract state;
	private final TableFactory<FirebaseTable> tableFactory;
	private final ClusterConnection conn;
	private final ClientRegistryServiceContract clientReg;
	private final MBusContract mbus;
	private final JndiProvider jndiProvider;

    DefaultTableFactory(ActivationBean activator, GameObjectIdSysStateMapper sysMapper, SystemStateServiceContract state, ClientRegistryServiceContract clientReg, TableFactory<FirebaseTable> tableFactory, ClusterConnection conn) {
        this.sysMapper = sysMapper;
        this.state = state;
		this.clientReg = clientReg;
		this.tableFactory = tableFactory;
		this.conn = conn;
        defPart = new DefaultCreationParticipant();
        ServiceRegistry services = InternalComponentAccess.getRegistry();
        jndiProvider = services.getServiceInstance(JndiProvider.class);
        TableSpaceServiceContract inst = services.getServiceInstance(TableSpaceServiceContract.class);
        tables = inst.getObjectSpace(FirebaseTable.class, GameAction.class);
        mbus = services.getServiceInstance(MBusContract.class);
        this.activator = activator;
    }
    

	public int getGameId() {
		return activator.def.getId();
	}

	public LobbyTable createTable(int seats, CreationParticipant participant) {
		LobbyTable[] tables = createTables(1, seats, participant);
		return (tables.length == 0 ? null : tables[0]);
	}
	
    public LobbyTable[] createTables(int count, int seats, CreationParticipant participant) {
    	List<TmpTable> tmpTables = new LinkedList<TmpTable>();
    	List<LobbyTable> lobbyTables = new LinkedList<LobbyTable>();
    	List<FirebaseTable> tmp = new LinkedList<FirebaseTable>();
    	for (int i = 0; i < count; i++) {
	    	TmpTable tmpTable = createSingleTable(seats, participant);
	    	if(tmpTable != null) {
	    		tmp.add(tmpTable.table);
	    		lobbyTables.add(tmpTable.lobbyTable);
	    		tmpTables.add(tmpTable);
	    	}
    	}
    	fireTablesCreated(seats, tmpTables);
    	boolean batch = (tables instanceof ExtendedGameObjectSpace<?, ?>);
    	if(batch) {
    		((ExtendedGameObjectSpace<FirebaseTable, GameAction>)tables).add(Lists.toArray(tmp, FirebaseTable.class));
    		fireEmerged(toIdArray(tmp));
    	}
    	for (TmpTable table : tmpTables) {
    		if(!batch) {
    			tables.add(table.table);
    			fireEmerged(new int[] { table.data.getId() });
    		}
    		table.acc.flush(this.state);
    	}
        return Lists.toArray(lobbyTables, LobbyTable.class);
    }

	private TmpTable createSingleTable(int seats, CreationParticipant participant) {
		/*
    	 * Steps are:
    	 * 
    	 * 1) Create table
    	 * 2) Notify master
    	 * 
    	 *  [On master node]
    	 * 3) Modify mbus
    	 * 4) Modify lobby
    	 * 
    	 *  [On this node]
    	 * 5) Put in space
    	 */
        CreationParticipant part = checkParticipant(participant);
        GameDefinition def = activator.def;
        InternalMetaData meta = createMetaData(def);
        TableData tableData = tableFactory.createTableData(meta, seats);
        FirebaseTable table = tableFactory.createTable(tableData);
        MapLobbyAttributeAccessor acc = null;
        LobbyPath lobbyPath = null;
        int tableId = table.getId();
        String name = null;
        /* Step 1 */
        try {
			table.begin(TABLE_LOCK_TIMEOUT);
	        // if (!checkGameSupport(table)) return null; // EARLY RETURN!!! FAILED !!!
	        name = assignNameAndId(part, def, meta, table);
	        LobbyPath tmp = part.getLobbyPathForTable(table);        
	        lobbyPath = new LobbyPath(TABLES, meta.getGameId(), tmp.getDomain(), tableId);
	        Map<String, AttributeValue> firstLobby = new DefaultTableAttributeMapper().toMap(table);
	        acc = new MapLobbyAttributeAccessor(lobbyPath, firstLobby);
	        meta.setLobbyPath(lobbyPath);
	        part.tableCreated(table, acc);
	        enforceTableAttributes(table, acc);
	        table.commit();
		} catch (InterruptedException e) {
			// Really?!
			log.error("Failed to get lock on newly created table?!", e);
			return null; // STUPID BLOODY RETURN
		} finally {
			table.release();
		}
		/* Step 2 */
        // tables.add(table);
        Map<String, AttributeValue> atts = acc.getAllAttributes();
        /* Step 3 (and 4 on master) */
        // fireTableCreated(tableId, lobbyPath, seats, name, atts);
        /* Step 5 */
        // acc.flush(state);
        LobbyTableImpl lobbyTable = new LobbyTableImpl(tableId, atts, lobbyPath);
		return new TmpTable(acc, name, lobbyTable, table, tableData);
	}
	
    
	/*
     * TODO: Make faster, is currently doing a linear search over the system
     * state in order to find the table
     */
    public boolean destroyTable(int tableid, boolean force) {
    	log.debug("Removing table: " + tableid);
        fireTableRemoved(tableid, -1);
        return removeLobbyData(tableid, force);
    }

	public boolean destroyTable(LobbyTable table, boolean force) {
        Arguments.notNull(table, "table");
        log.debug("Removing table: " + table.getTableId());
        fireTableRemoved(table.getTableId(), -1);
        return removeLobbyData(table, force);
	}
    
    public LobbyTable[] listTables() {
        return listTables(null, null);
    }
    
    public LobbyTable[] listTables(LobbyTableFilter filt) {
        return listTables(null, filt);
    }
    
    public LobbyTable[] listTables(LobbyPath branch) {
		return listTables(branch, null);
	}
	
	public LobbyTable[] listTables(LobbyPath branch, LobbyTableFilter filt) {
		if (branch == null) {
			branch = new LobbyPath(activator.def.getId());
		}
		List<LobbyTable> list = new LinkedList<LobbyTable>();
        DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
        Set<String> endNodes = state.getEndNodes(branch.getNameSpace());
        for (String path : endNodes) {
            if (path.startsWith(branch.getRoot())) {
            	LobbyPath lobbyPath = new LobbyPath().parseFqn(path);
                if (lobbyPath != null && isTable(acc, lobbyPath)) {
                    int id = acc.getIntAttribute(lobbyPath, DefaultTableAttributes._ID.toString());
                    Map<String, AttributeValue> atts = acc.getAllAttributes(lobbyPath);
                    if (filt == null || filt.accept(atts)) {
                    	lobbyPath = new LobbyPath(lobbyPath, id);
                        list.add(new LobbyTableImpl(id, atts, lobbyPath));
                    }
                }
            }
        }
        return list.toArray(new LobbyTable[list.size()]);
	}
	
	
	
	// --- PACKAGE ACCESS --- //
	
	TmpResponse createRequestedTable(CreationRequestData data) throws SeatingFailedException, CreationRequestDeniedException {
		RequestCreationParticipant part = ((RequestAwareActivator)activator.act).getParticipantForRequest(data.getPid(), data.getSeats(), data.getAttributes());
		/*
		 * 1) Create table
		 * 2) Check/modify invitees
		 *
		 *  [On master node]
    	 * 3) Modify mbus
    	 * 4) Modify lobby
    	 * 
    	 *  [On this node]
    	 * 5) Put in space
    	 * 
    	 *  [after return]
    	 * 6) send response
    	 * 7) send invitees
		 */
        GameDefinition def = activator.def;
        InternalMetaData meta = createMetaData(def);
        TableData tableData = tableFactory.createTableData(meta, data.getSeats());
        FirebaseTable table = tableFactory.createTable(tableData);
        MapLobbyAttributeAccessor acc = null;
        LobbyPath lobbyPath = null;
        int tableId = table.getId();
        String name = null;
        Invite[] invites = null;
        int inviterSeat = -1;
        boolean shouldReserve = false;
        /* Step 1 */
        try {
			table.begin(TABLE_LOCK_TIMEOUT);
	        // if (!checkGameSupport(table)) return null; // EARLY RETURN!!! FAILED !!!
	        name = assignNameAndId(part, def, meta, table);
	        lobbyPath = part.getLobbyPathForTable(table);        
	        acc = new MapLobbyAttributeAccessor(lobbyPath);
	        meta.setLobbyPath(lobbyPath);
	        /* Step 2 */
			int[] invitees = part.modifyInvitees(data.getInvitees());
			shouldReserve = part.reserveSeatsForInvitees();
			// seat god
			inviterSeat = seatPlayer(table, data.getPid(), -1);
			invites = (invitees == null ? new Invite[0] : new Invite[invitees.length]);
			if(shouldReserve) {
				// reserve seat for players and create array
				for (int i = 0; i < invites.length; i++) {
					int seat = seatPlayer(table, invitees[i], -1);
					invites[i] = new Invite(invitees[i], seat);
				}
			} else {
				// no seating, just create array
				for (int i = 0; i < invites.length; i++) {
					invites[i] = new Invite(invitees[i], -1);
				}
			}
			// Notify participant
	        part.tableCreated(table, acc);
	        enforceTableAttributes(table, acc);
			// do commit
	        table.commit();
		} catch (InterruptedException e) {
			// Really?!
			log.error("Failed to get lock on newly created table?!", e);
			return null; // STUPID BLOODY RETURN
		} finally {
			table.release();
		}
		Map<String, AttributeValue> atts = acc.getAllAttributes();
        /* Step 3 (and 4 on master) */
        fireTableCreated(tableId, lobbyPath, data.getSeats(), name, atts, tableData);
        /* Step 5 */
        tables.add(table);
        // FIRE EMERGE !!
        fireEmerged(new int[] { table.getId() });
        /* Step 6 */
        acc.flush(state);
        /* And... done */
        log.debug("Ordinary table '" + name + "' (" + tableId + ") created");
        return new TmpResponse(tableId, inviterSeat, invites, shouldReserve);
	}

	boolean destroyMttTable(int tableId, int mttId) {
        fireTableRemoved(tableId, mttId);
        return removeMttLobbyData(tableId, mttId);
    }
	
	/**
	 * Please make sure the activator is MTT aware before calling this
	 * method. This will create a table and call the activator. Returns
	 * the new id of the table.
	 */
	Set<Integer> createMttTables(int mttId, TableCommandData[] attachment, Object att) {
		log.debug("Received request for " + attachment.length + " tournament tables");
		Set<Integer> set = new TreeSet<Integer>();
		List<TmpMttTable> list = new LinkedList<TmpMttTable>();
		List<FirebaseTable> tmp = new LinkedList<FirebaseTable>();
		for (TableCommandData data : attachment) {
			TmpMttTable tmpt = createSingleMttTable(mttId, data.getSeats(), data.getName(), data.getAttributes(), att);
			// tmp.acc.flush(this.state);
			tmp.add(tmpt.table);
			set.add(tmpt.id);
			list.add(tmpt);
		}
		log.debug("Created " + set.size() + " tournament tables");
		fireMttTablesCreated(mttId, list);
    	boolean batch = (tables instanceof ExtendedGameObjectSpace<?, ?>);
    	if(batch) {
    		((ExtendedGameObjectSpace<FirebaseTable, GameAction>)tables).add(Lists.toArray(tmp, FirebaseTable.class));
    		fireEmerged(toIdArray(tmp));
    	}	
		for (TmpMttTable table : list) {
			if(!batch) {
    			tables.add(table.table);
    			fireEmerged(new int[] { table.data.getId() });
    		}
			table.acc.flush(state);
		}
		return set;
	}

    
    // --- PRIVATE METHODS --- //
	
	private TmpMttTable createSingleMttTable(final int mttId, int seats, String name, Attribute[] attributes, final Object attachment) {
        GameDefinition def = activator.def;
        InternalMetaData meta = createMetaData(def);
        TableData tableData = tableFactory.createTableData(meta, seats);
        final FirebaseTable table = tableFactory.createTable(tableData);
        MapLobbyAttributeAccessor acc = null;
        LobbyPath lobbyPath = null;
        int tableId = table.getId();
        try {
			table.begin(TABLE_LOCK_TIMEOUT);
	        // if (!checkGameSupport(table)) return null; // EARLY RETURN!!! FAILED !!!
	        meta.setName(name);
	        meta.setGameId(def.getId()); 
	        meta.setMttId(mttId);
	        meta.setType(TableType.MULTI_TABLE_TOURNAMENT);
	        lobbyPath = getLobbyPathForMttTable(def.getId(), mttId, tableId);
	        meta.setLobbyPath(lobbyPath);
	        // meta.setName("mtt-"+tableId);
	        acc = new MapLobbyAttributeAccessor(lobbyPath);
	        final MapLobbyAttributeAccessor tmpAcc = acc;
	        jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
	        	@Override
	        	public Object invoke() {
	        		return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
	        			@Override
	        			public Object invoke() {
	        				((MttAwareActivator)activator.act).mttTableCreated(table, mttId, attachment, tmpAcc);
	        		        return null;
	        			}
					}, activator.act.getClass().getClassLoader());
	        	}
			});
	        enforceTableAttributes(table, acc);
	        table.commit();
		} catch (InterruptedException e) {
			// Really?!
			log.error("Failed to get lock on newly created table?!", e);
			return null; // STUPID BLOODY RETURN
		} finally {
			table.release();
		}
        //tables.add(table);
        log.debug("Tournament table '" + name + "' (" + tableId + ") created");
        TmpMttTable tmp = new TmpMttTable(acc, name, tableId, seats, lobbyPath, table, tableData);
        return tmp;
	}
	
	private int seatPlayer(FirebaseTable table, int pid, int mttId) throws SeatingFailedException {
		int seat = getFirstVacantSeat(table, pid);
		GenericPlayer player = new GenericPlayer(pid, "");
		player.setStatus(PlayerStatus.RESERVATION);
		table.getPlayerSet().addPlayer(player, seat);
		scheduleCleanup(table, player);
		ClientRegistry reg = clientReg.getClientRegistry();
		reg.addClientTable(pid, table.getId(), seat, mttId);
		return seat;
	}

	private void scheduleCleanup(FirebaseTable table, GenericPlayer player) {
		TableScheduler scheduler = ((StandardTable)table).getScheduler();
		CleanupPlayerAction act = new CleanupPlayerAction(player.getPlayerId(), table.getId(), PlayerStatus.RESERVATION);
		scheduler.scheduleAction(act, AbstractTableActionHandler.DEFAULT_PLAYER_RECONNECT_TIMEOUT_MS);
	}

	private int getFirstVacantSeat(FirebaseTable table, int pid) throws SeatingFailedException {
		int seat = table.getPlayerSet().getSeatingMap().getFirstVacantSeat();
		if(seat == -1) {
			log.error("Failed to reserve seat for player " + pid + " on request. Could not find any empty seats.");
			throw new SeatingFailedException();
		}
		return seat;
	}

	private String assignNameAndId(CreationParticipant part, GameDefinition def, InternalMetaData meta, FirebaseTable table) {
		String name;
		name = part.getTableName(def, table);
		if(name == null) {
			log.debug("Ticket #533: Table creation participant returned a null name; assigning default name (from DefaultCreationParticipant)");
			name = new DefaultCreationParticipant().getTableName(def, table);
		}
		meta.setName(name);
		meta.setGameId(def.getId());
		return name;
	}

	private InternalMetaData createMetaData(GameDefinition def) {
		InternalMetaData meta = new InternalMetaData();
        meta.setTableId(sysMapper.generateNewObjectId());
        meta.setRevisionId(activator.rev.getVersion());
        meta.setGameClass(def.getClassname());
		return meta;
	}

	private CreationParticipant checkParticipant(CreationParticipant participant) {
		CreationParticipant part = defPart;
        if (participant != null) part = participant;
		return part;
	}
	
	private void fireTableRemoved(int tableid, int mttId) {
		TableCommandData data = new TableCommandData(tableid, mttId, -1, null, null, null, getPartition(tableid), null);
		try {
			conn.getCommandDispatcher().send(Constants.TABLE_CREATION_COMMAND_CHANNEL, new TableRemoved(data, mttId));
			// conn.getCommandDispatcher().dispatch(new TableRemoved(data, mttId));
		} catch (ClusterException e) {
			log.fatal("Failed to dispatch table creation command", e);
		}
	}
	
	private Partition getPartition(int tableid) {
		PartitionMap map = mbus.getCurrentPartitionMap();
		return map.getPartitionForChannel(EventType.GAME, tableid);
	}

	private void fireTableCreated(int tableId, LobbyPath lobbyPath, int seats, String name, Map<String, AttributeValue> atts, TableData table) {
		TableCommandData data = new TableCommandData(tableId, -1, seats, name, lobbyPath, toAttributes(atts), null, table);
		doFireAdded(new TableCommandData[] { data }, -1);
	}
	
	private void fireTablesCreated(int seats, List<TmpTable> tables) {
		TableCommandData[] arr = new TableCommandData[tables.size()];
		for (int i = 0; i < arr.length; i++) {
			TmpTable table = tables.get(i);
			String name = table.name;
			int tableId = table.lobbyTable.getTableId();
			LobbyPath path = table.lobbyTable.getLobbyPath();
			Attribute[] attributes = toAttributes(table.lobbyTable.getAttributes());
			arr[i] = new TableCommandData(tableId, -1, seats, name, path, attributes, null, table.data);
		}
		doFireAdded(arr, -1);
	}
	
	private void fireMttTablesCreated(int mttId, List<TmpMttTable> tables) {
		TableCommandData[] arr = new TableCommandData[tables.size()];
		for (int i = 0; i < arr.length; i++) {
			TmpMttTable table = tables.get(i);
			String name = table.name;
			int tableId = table.id;
			int seats = table.seats;
			LobbyPath path = table.path;
			Attribute[] attributes = toAttributes(table.acc.getAllAttributes());
			arr[i] = new TableCommandData(tableId, mttId, seats, name, path, attributes, null, table.data);
		}
		doFireAdded(arr, mttId);
	}

	private void doFireAdded(TableCommandData[] data, int mttId) {
		try {
			conn.getCommandDispatcher().send(Constants.TABLE_CREATION_COMMAND_CHANNEL, new TableCreated(data, mttId));
		} catch (ClusterException e) {
			log.fatal("Failed to dispatch table creation command", e);
		}
	}
	
	private void fireEmerged(int[] idArray) {
		try {
			conn.getCommandDispatcher().send(Constants.TABLE_CREATION_COMMAND_CHANNEL, new ObjectEmerged(idArray));
		} catch (ClusterException e) {
			log.fatal("Failed to dispatch table emerged command", e);
		}
	}

	private int[] toIdArray(List<FirebaseTable> tmp) {
		int i = 0;
		int[] arr = new int[tmp.size()];
		for (FirebaseTable t : tmp) {
			arr[i++] = t.getId();
		}
		return arr;
	}

    private Attribute[] toAttributes(Map<String, AttributeValue> atts) {
		int i = 0; 
		Attribute[] arr = new Attribute[atts.size()];
		for (Entry<String, AttributeValue> e : atts.entrySet()) {
			arr[i++] = new Attribute(e.getKey(), e.getValue());
		}
		return arr;
	}

	/*private boolean removeTableData(int tableid) {
        return tables.remove(tableid);
    }*/

    private boolean removeLobbyData(LobbyTable tab, boolean force) {
        if (tab instanceof LobbyTableImpl) {
        	LobbyPath path = ((LobbyTableImpl)tab).getLobbyPath();
            DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
            int seated = acc.getIntAttribute(path, DefaultTableAttributes._SEATED.toString());
            if (seated > 0 && !force) return false;
            else {
                acc.removeNode(path);
                return true;
            }
        } else {
            return removeLobbyData(tab.getTableId(), force);
        }
    }
    
	private boolean removeMttLobbyData(int tableId, int mttId) {
		GameDefinition def = activator.def;
		DefaultLobbyTableAccessor acc = new DefaultLobbyTableAccessor(state);
		LobbyPath lobbyPath = getLobbyPathForMttTable(def.getId(), mttId, tableId);
		acc.removeNode(lobbyPath);
        return true;
    }

    private boolean removeLobbyData(int tableid, boolean force) {
        LobbyTable[] arr = listTables(new IDFilter(tableid));
        if (arr.length > 1) {
            log.error("Found multiple table with the same id '" + tableid + "'");
            return false;
        } else if (arr.length == 1) {
        	return removeLobbyData(arr[0], force);
        } else {
            return false;
        }
    }

    // return true if this is a table
    private boolean isTable(DefaultLobbyTableAccessor acc, LobbyPath path) {
        return DefaultTableAttributeMapper.isTable(acc, path);
    }

    /*
     * Checks if the game is an instance of GameSupport. Sets an instance of
     * itself to the table as state.
     */
    /*private boolean checkGameSupport(final FirebaseTable t) {
        try {
            Class cl = activator.rev.loadGameClass();
            if (GameSupport.class.isAssignableFrom(cl)) {
                GameSupport game = (GameSupport)activator.rev.newGameInstance();
                game.setTableId(t.getId());
                //t.begin(TABLE_LOCK_TIMEOUT);
                t.getGameState().setState(game);
                //t.commit();
                //t.release();
            }
            return true;
        } catch (ClassNotFoundException e) {
            log.error("Could not create Game Support class", e);
            return false;
        } catch (InstantiationException e) {
            log.error("Could not create Game Support class", e);
            return false;
        } catch (IllegalAccessException e) {
            log.error("Could not create Game Support class", e);
            return false;
        } catch (InterruptedException e) {
        	log.error("Could not create Game Support class", e);
        	return false;
		}
    }*/

    // set default values
    private void enforceTableAttributes(Table tab, LobbyTableAttributeAccessor acc) {
        DefaultTableAttributeMapper.setRequiredValues(tab, acc);
    }
    
    
    // --- INNER CLASSES --- //
    
	private static class TmpTable {
		
		private final MapLobbyAttributeAccessor acc;
		private final LobbyTableImpl lobbyTable;
		private final String name;
		private final TableData data;
		private final FirebaseTable table;
		 
		 private TmpTable(MapLobbyAttributeAccessor acc, String name, LobbyTableImpl lobbyTable, FirebaseTable table, TableData data) {
			this.name = name;
			this.table = table;
			this.lobbyTable = lobbyTable;
			this.acc = acc;
			this.data = data;
		 }
	}
	
	private static class TmpMttTable {
		
		private final int id;
		private final int seats;
		private final MapLobbyAttributeAccessor acc;
		private final String name;
		private final LobbyPath path;
		private final TableData data;
		private final FirebaseTable table;
		 
		 private TmpMttTable(MapLobbyAttributeAccessor acc, String name, int id, int seats, LobbyPath path, FirebaseTable table, TableData data) {
			this.name = name;
			this.id = id;
			this.acc = acc;
			this.seats = seats;
			this.path = path;
			this.table = table;
			this.data = data;
		 }
	}
}