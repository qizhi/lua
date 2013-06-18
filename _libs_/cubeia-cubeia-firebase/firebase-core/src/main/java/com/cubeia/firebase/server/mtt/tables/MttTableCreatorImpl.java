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
package com.cubeia.firebase.server.mtt.tables;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.mtt.comm.TableCreation;
import com.cubeia.firebase.mtt.comm.TableRemoval;
import com.cubeia.firebase.mtt.comm.MttCommand.Type;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.TableCommandData;
import com.cubeia.firebase.server.commands.TableCreated;
import com.cubeia.firebase.server.commands.TableRemoved;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This object is held by the action processor and shared by all
 * mtt instances. This is because it must have a lifetime, which the MTTLogic
 * currently does not have.
 * 
 * @author Fredrik
 */
public class MttTableCreatorImpl implements MttTableCreator {

	private transient Logger log = Logger.getLogger(getClass());
    
	//private MttCommandContext con;
	private ScheduledExecutorService scheduler;
	private Listener listener = new Listener();

	private final SystemStateServiceContract sysState;
	private ClusterConnection con;
	
	public MttTableCreatorImpl(ServiceRegistry services, ScheduledExecutorService scheduler, SystemStateServiceContract sysState) {
		con = services.getServiceInstance(ConnectionServiceContract.class).getSharedConnection();
		this.scheduler = scheduler;
		this.sysState = sysState;
	}


	public void init() {
		con.getCommandReceiver().addCommandListener(Constants.TABLE_CREATION_COMMAND_CHANNEL, listener);
		// con.getCommandReceiver().addCommandListener(listener);
	}

	public void destroy() {
		con.getCommandReceiver().removeCommandListener(Constants.TABLE_CREATION_COMMAND_CHANNEL, listener);
		// con.getCommandReceiver().removeCommandListener(listener);	
	}
	
	public void createTables(int gameId, int mttId, int tableCount, int seats, String baseName, Object attachment) {
		Arguments.notNull(con, "MttCommandContext");
		TableCommandData[] data = createTableData(tableCount, seats, baseName);
		TableCreation command = new TableCreation(Type.REQUEST, mttId, gameId, -1, data);
		command.setGameAttachment(attachment);
		try {
			log.debug("Sending command requesting " + tableCount + " tournament table to be created for mtt " + mttId);
			con.getCommandDispatcher().dispatch(command);
		} catch (ClusterException e) {
			Logger.getLogger(getClass()).error("Failed to send table creation command!", e);
		}
	}

    public void removeTables(int gameId, int mttId, Collection<Integer> tableIds) {
        Arguments.notNull(con, "MttCommandContext");
        int[] tableIdArray = new int[tableIds.size()];
        int i = 0;
        for (Integer tId : tableIds) {
            tableIdArray[i++] = tId;
        }
        TableRemoval tableRemovalCommand = new TableRemoval(mttId, gameId, tableIdArray);
        try {
            con.getCommandDispatcher().dispatch(tableRemovalCommand);
        } catch (ClusterException e) {
            Logger.getLogger(getClass()).error("Failed to send table removal command!", e);
        }
    }
	
	// --- PRIVATE METHODS --- //
    
	private void handleTableRemoved(TableRemoved command) {
		TableSystemStateMapper.handleTableRemoved(sysState, command.getAttachment().getMttId(), command.getAttachment().getId());
	}

	private synchronized void handleTableAdded(TableCreated command) {
		for (TableCommandData data : command.getAttachment()) {
			TableSystemStateMapper.handleTableAdded(sysState, data.getMttId(), data.getId());
		}
	}
	
	private TableCommandData[] createTableData(int tableCount, int seats, String baseName) {
		TableCommandData[] arr = new TableCommandData[tableCount];
		for(int i = 0; i < arr.length; i++) {
			arr[i] = new TableCommandData(-1, -1, seats, baseName + i, null, null, null, null);
		}
		return arr;
	}

    public void removeTables(final int gameId, final int mttId, final Collection<Integer> tableIds, final long delayMs) {
        Arguments.notNull(scheduler, "scheduler");
        scheduler.schedule(new SafeRunnable() {
            public void innerRun() {
                log.debug("delayed (" + delayMs + " ms) removal of tables: " + tableIds);
                removeTables(gameId, mttId, tableIds);
            } 
        }, delayMs, TimeUnit.MILLISECONDS);
    }
    
    
    // --- PRIVATE CLASSES --- //
    
    private class Listener implements CommandListener {
    	
    	public Object commandReceived(CommandMessage c) {
    		if(c.command instanceof TableRemoved && ((TableRemoved)c.command).isMtt()) {
				handleTableRemoved((TableRemoved)c.command);
			} else if(c.command instanceof TableCreated && ((TableCreated)c.command).isMtt()) {
				handleTableAdded((TableCreated)c.command);
			}
			return null;
		}
    }
}
