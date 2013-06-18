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

import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.CreateTableResponseAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.NotifyInvitedAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesRemovedAction;
import com.cubeia.firebase.api.game.activator.CreationRequestDeniedException;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.TableFactory;
import com.cubeia.firebase.game.table.comm.TableCreationRequest;
import com.cubeia.firebase.mtt.comm.MttCommand;
import com.cubeia.firebase.mtt.comm.TableCreation;
import com.cubeia.firebase.mtt.comm.TableRemoval;
import com.cubeia.firebase.server.commands.TableCommandData;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;

public final class CommandAwareInvokator {
	
	private final DefaultTableFactory factory;
	private final TableFactory<FirebaseTable> tables;
	private final Sender<MttEvent> mttSender;

	CommandAwareInvokator(DefaultTableFactory factory, TableFactory<FirebaseTable> tables, Sender<MttEvent> mttSender) {
		this.mttSender = mttSender;
		this.factory = factory;
		this.tables = tables;
	}
	
	/**
	 * This method assumes that the activator is aware of the table
	 * creation interfaces. If this is not checked before this method 
	 * there might be a class cast exception during runtime.
	 * 
	 * @param comm Command to handle, never null
	 * @throws ChannelNotFoundException 
	 */
	public void handleCommand(TableCreationRequest comm, Sender<ClientEvent<?>> clientSender) throws ChannelNotFoundException {
		try {
			TmpResponse invitees = factory.createRequestedTable(comm.getAttachment());
			dispatchCreationResponse(comm, invitees, clientSender);
			dispatchInvitation(comm, invitees, clientSender);
		} catch (CreationRequestDeniedException e) {
			Logger.getLogger(getClass()).debug("Creation request denied by activator");
			dispatchDenied(comm, e.getCode(), clientSender);
		} catch (SeatingFailedException e) {
			Logger.getLogger(getClass()).debug("Seating failed for creation request");
			dispatchFailed(comm, clientSender);
		}
	}


	/**
	 * This method assumes that the activator is MTT aware. If this
	 * is not checked before this method there might be a class cast 
	 * exception during runtime.
	 * 
	 * @param comm Command to handle, never null
	 * @throws ChannelNotFoundException 
	 */
	public void handleCommand(MttCommand<?> comm) throws ChannelNotFoundException {
		if(comm instanceof TableCreation) {
			handleTableCreation((TableCreation)comm);
		} else if(comm instanceof TableRemoval) {
			handleTableRemoval((TableRemoval)comm);
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void dispatchInvitation(TableCreationRequest comm, TmpResponse resp, Sender<ClientEvent<?>> sender) throws ChannelNotFoundException {
		for (Invite inv : resp.invitees) {
			NotifyInvitedAction act = new NotifyInvitedAction(inv.id, comm.getAttachment().getPid(), resp.tableId, inv.seat);
			ClientEvent<GameAction> ev = new ClientEvent<GameAction>();
			ev.setPlayerId(inv.id);
			ev.setAction(act);
			sender.dispatch(ev);
		}
	}

	private void dispatchCreationResponse(TableCreationRequest comm, TmpResponse resp, Sender<ClientEvent<?>> sender) throws ChannelNotFoundException {
		int pid = comm.getAttachment().getPid();
		CreateTableResponseAction act = new CreateTableResponseAction(pid, resp.tableId, resp.seat, Status.OK, -1);
		act.setSeq(comm.getSeq());
		ClientEvent<GameAction> ev = new ClientEvent<GameAction>();
		ev.setPlayerId(pid);
		ev.setAction(act);
		sender.dispatch(ev);
	}
	
	private void dispatchFailed(TableCreationRequest comm, Sender<ClientEvent<?>> sender) throws ChannelNotFoundException {
		int pid = comm.getAttachment().getPid();
		CreateTableResponseAction act = new CreateTableResponseAction(pid, -1, -1, Status.FAILED, -1);
		ClientEvent<GameAction> ev = new ClientEvent<GameAction>();
		ev.setPlayerId(pid);
		ev.setAction(act);
		sender.dispatch(ev);
	}
	
	private void dispatchDenied(TableCreationRequest comm, int code, Sender<ClientEvent<?>> sender) throws ChannelNotFoundException {
		int pid = comm.getAttachment().getPid();
		CreateTableResponseAction act = new CreateTableResponseAction(pid, -1, -1, Status.DENIED, code);
		ClientEvent<GameAction> ev = new ClientEvent<GameAction>();
		ev.setPlayerId(pid);
		ev.setAction(act);
		sender.dispatch(ev);
	}

	private void handleTableRemoval(TableRemoval comm) throws ChannelNotFoundException {
		Set<Integer> set = new TreeSet<Integer>();
		int[] tableIds = comm.getAttachment();
		for(int i = 0; tableIds != null && i < tableIds.length; i++) {
			int tableId = tableIds[i];
			if(factory.destroyMttTable(tableId, comm.getMttId())) {
				set.add(tableId);
			}
		}
		dispatchRemovedEvent(comm.getMttId(), set);
	}

	private void handleTableCreation(TableCreation comm) throws ChannelNotFoundException  {
		TableCommandData[] attachment = comm.getAttachment();
		ClassLoader loader = tables.getGameClassLoader(factory.getGameId());
		Object att = comm.getGameAttachment(loader);
		Set<Integer> ids = factory.createMttTables(comm.getMttId(), attachment, att);
		dispatchCreatedEvent(comm.getMttId(), ids);
	}
	
	private void dispatchRemovedEvent(int mttId, Set<Integer> ids) {
		MttTablesRemovedAction action = new MttTablesRemovedAction(mttId);
		MttEvent ev = new MttEvent();
		ev.setAction(action);
		ev.setMttId(mttId);
		action.setTables(ids);
		sendMttEvent(ev);
	}
	
	private void dispatchCreatedEvent(int mttId, Set<Integer> ids) {
		MttTablesCreatedAction action = new MttTablesCreatedAction(mttId);
		MttEvent ev = new MttEvent();
		ev.setAction(action);
		ev.setMttId(mttId);
		action.setTables(ids);
		sendMttEvent(ev);
	}
	
	private void sendMttEvent(MttEvent ev) {
		try {
			mttSender.dispatch(ev);
		} catch (ChannelNotFoundException e) {
			Logger.getLogger(getClass()).error("Failed to dispatch MTT event!", e);
		}
	}
}
