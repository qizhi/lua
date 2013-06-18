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
package com.cubeia.firebase.mock;

import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.game.table.trans.TransactionalTableFactory;

public class TableCreator {
	
	private static TransactionalTableFactory factory = new TransactionalTableFactory(null, 10240);
	
	public static TableData createTableData(int id, int capacity) {
		InternalMetaData meta = new InternalMetaData();
		meta.setTableId(id);
		meta.setName("MockTable_"+id);
		TableData data = factory.createTableData(meta, capacity);
		return data;
	}
	
	public static FirebaseTable createTable(int id, int capacity) {
		return createTable(createTableData(id, capacity));
	}

	public static FirebaseTable createTable(TableData tableData) {
		FirebaseTable table = factory.createTable(tableData);
		table.getGameState().setState(table.getMetaData().getName());
		table.commit();
		return table;
	}
	
}
