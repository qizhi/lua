/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.network.example.rps;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.activator.ActivatorContext;
import com.cubeia.firebase.api.game.activator.DefaultCreationParticipant;
import com.cubeia.firebase.api.game.activator.GameActivator;
import com.cubeia.firebase.api.game.activator.TableFactory;
import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.server.SystemException;

public class Activator implements GameActivator {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private ScheduledExecutorService executor;

	@Override
	public void init(final ActivatorContext ctx) throws SystemException {
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				destroyFinishedTables(ctx);
				createTablesIfNeeded(ctx);
			}
		}, 1000, 1000, TimeUnit.MILLISECONDS);
	}

	private void destroyFinishedTables(final ActivatorContext ctx) {
		TableFactory tableFactory = ctx.getTableFactory();
		for (LobbyTable tbl : tableFactory.listTables()) {
			if (tbl.getAttributes().containsKey("FINISHED")) {
				log.info("destroying finished table {}", tbl.getTableId());
				tableFactory.destroyTable(tbl, true);
			}
		}
	}
	
	private void createTablesIfNeeded(final ActivatorContext ctx) {
		int tablesToCreate = Math.max(0, 10 - ctx.getTableFactory().listTables().length);
		
		if (tablesToCreate > 0) {
			DefaultCreationParticipant cp = new DefaultCreationParticipant() {
				@Override
				public void tableCreated(Table table, LobbyTableAttributeAccessor acc) {
					super.tableCreated(table, acc);
					table.getGameState().setState(new com.cubeia.network.example.rps.State());
				}
				
				@Override
				public String getTableName(GameDefinition def, Table t) {
					return "Heads Up";
				}
			};
			ctx.getTableFactory().createTables(tablesToCreate, 2, cp);
		}
	}

	@Override
	public void destroy() {
		executor.shutdownNow();
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

}
