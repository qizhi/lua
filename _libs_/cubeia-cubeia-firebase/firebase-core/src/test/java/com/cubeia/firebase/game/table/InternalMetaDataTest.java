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

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.ActionId;
import com.cubeia.firebase.api.action.ProbeAction;

public class InternalMetaDataTest extends TestCase {

	public void testDoubleExecution() throws Exception {
		InternalMetaData meta = new InternalMetaData();
		
		Action a1 = newAction(1);
		Action a2 = newAction(2);
		
		assertFalse(meta.isDoubleExecution(a1));
		
		meta.setLastExecuted(a1);
		
		assertTrue(meta.isDoubleExecution(a1));
		assertFalse(meta.isDoubleExecution(a2));
		
		meta.setLastExecuted(a2);
		
		assertTrue(meta.isDoubleExecution(a2));
		assertFalse(meta.isDoubleExecution(a1));
		
	}
	
	private Action newAction(long seq) {
		ProbeAction p = new ProbeAction(1, 1, 1);
		p.setActionId(new Id(seq));
		return p;
	}
	
	private static class Id extends ActionId {
		
		private static final long serialVersionUID = 5192419554705021522L;

		public Id(long seq) {
			super("kalle", seq);
		}
	}
}
