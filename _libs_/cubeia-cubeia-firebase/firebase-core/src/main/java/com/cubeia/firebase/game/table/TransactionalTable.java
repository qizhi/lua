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
package com.cubeia.firebase.game.table;

import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.game.Transactional;

/**
 * An internal representation of a table. This table
 * contains {@link Transactional} methods. It will not put
 * changes to the original underlying data object unless
 * "committed" and contains a lock for thread safety. It
 * should be used like so:
 * 
 * <pre>
 *    boolean locked = false;
 *    TransactionalTable table = // .. get table
 *    try {
 * 	  	locked = table.begin(1000); // obtain lock
 * 		if(!locked) throw Exception("could not obtain lock!");
 *    	// .. change table data here
 *    	table.commit();
 *      [...]
 *    } catch(Exception e) {
 *      table.rollback();
 *    } finally {
 *      if(locked) {
 *        table.release(); // release lock
 *      }
 *    }
 * </pre>
 * 
 * @author Larsan
 */
public interface TransactionalTable extends Table, Transactional { }
