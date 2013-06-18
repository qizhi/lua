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
package com.cubeia.firebase.server.processor;

import static com.cubeia.firebase.api.util.Arguments.notNull;

import java.util.ArrayList;
import java.util.List;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.processor.filter.DefaultFilterStackCreatorContext;
import com.cubeia.firebase.server.processor.filter.DoubleExecutionCheckFilter;
import com.cubeia.firebase.server.processor.filter.FirebaseGameActionFilter;
import com.cubeia.firebase.server.processor.filter.FirebaseLobbyUpdateFilter;
import com.cubeia.firebase.server.processor.filter.FirebaseMttRoundActionFilter;
import com.cubeia.firebase.server.processor.filter.FirebaseSeatingFilter;
import com.cubeia.firebase.server.processor.filter.FirebaseTableSetupFilter;
import com.cubeia.firebase.server.processor.filter.JbcTransactionAttachFilter;
import com.cubeia.firebase.server.processor.filter.JtaTransactionAttachFilter;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/*
 * Base stack:
 * 
 *  1) Wire JBC transactions
 *  2) Wire JTA transactions
 *  3) Double execution check
 *  4) Table setup
 *  5) Lobby update (post exec only)
 *  
 * Firebase stack:
 * 
 *  6) Seating rules
 *  7) MTT round start/end
 *  8) Process action
 *  
 */
public class DefaultFilterStackCreator {

	private final DefaultFilterStackCreatorContext context;

	public DefaultFilterStackCreator(DefaultFilterStackCreatorContext context) {
		notNull(context, "context");
		this.context = context;
	}
	
	public List<ProcessorFilter<FirebaseTable, Action>> createBaseStack() {
		List<ProcessorFilter<FirebaseTable, Action>> list = new ArrayList<ProcessorFilter<FirebaseTable, Action>>();
		addBaseStack(list);
		return list;
	}
	
	public List<ProcessorFilter<FirebaseTable, GameAction>> createGameStack() {
		List<ProcessorFilter<FirebaseTable, GameAction>> list = new ArrayList<ProcessorFilter<FirebaseTable, GameAction>>();
		addGameStack(list);
		return list;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ProcessorChain<FirebaseTable, Action> createFullStack() {
		FirebaseProcessorChain chain = new FirebaseProcessorChain();
		chain.addAll(createBaseStack());
		chain.addAll(createGameStack());
		return chain;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void addBaseStack(List<ProcessorFilter<FirebaseTable, Action>> list) {
		list.add(new JbcTransactionAttachFilter<FirebaseTable, Action>(context.getServiceRegistry().getServiceInstance(CoreTransactionManager.class)));
		list.add(new JtaTransactionAttachFilter<FirebaseTable, Action>(context.getServiceRegistry().getServiceInstance(TransactionManagerProvider.class)));
		list.add(new DoubleExecutionCheckFilter<FirebaseTable, Action>());
		list.add(new FirebaseTableSetupFilter<FirebaseTable, Action>(context));
		list.add(new FirebaseLobbyUpdateFilter<FirebaseTable, Action>(context.getServiceRegistry().getServiceInstance(SystemStateServiceContract.class)));
	}

	private void addGameStack(List<ProcessorFilter<FirebaseTable, GameAction>> list) {
		list.add(new FirebaseSeatingFilter<FirebaseTable, GameAction>(context.getGameConfig(), context.getServiceRegistry()));
		list.add(new FirebaseMttRoundActionFilter<FirebaseTable, GameAction>(context.getGameRegistry()));
		list.add(new FirebaseGameActionFilter<FirebaseTable, GameAction>(context.getGameRegistry()));
	}
}
