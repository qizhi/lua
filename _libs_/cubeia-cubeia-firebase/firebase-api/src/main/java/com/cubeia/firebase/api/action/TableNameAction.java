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
package com.cubeia.firebase.api.action;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;


/**
 * This is an action for changing the name of a table.
 * It is used for automated tests of the Game Object Space.
 * 
 * The sleep time will be used as Thread.sleep in the processor 
 * so we can properly test blocking of the Space.
 * 
 * @author fredrik.johansson
 *
 */
public class TableNameAction extends AbstractGameAction {

	private static final long serialVersionUID = 2267305416199490968L;
	
    /**
     * The new name of the table
     */
	private String name;
	private long sleep;
	
    
	public TableNameAction(int tableId, String name, long sleep) {
		super(tableId);
		this.name = name;
        this.sleep = sleep;
	}

	
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}	


	public String toString() {
		return "TableNameAction: table: " + getTableId() + " name: " + name;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getSleep() {
        return sleep;
    }


    public void setSleep(long sleep) {
        this.sleep = sleep;
    }



}
