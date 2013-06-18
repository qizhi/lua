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
package com.cubeia.firebase.game.table.comm;

import java.io.Serializable;

import com.cubeia.firebase.api.common.Attribute;

public final class CreationRequestData implements Serializable {

	private static final long serialVersionUID = 5341544858034196333L;
	
	private int pid;
	private Attribute[] attributes;
	private int[] invitees;
	private int seats;
	
	public CreationRequestData(int pid, int seats, Attribute[] attributes, int[] invitees) {
		this.pid = pid;
		this.seats = seats;
		this.attributes = attributes;
		this.invitees = invitees;
	}

	public int getSeats() {
		return seats;
	}

	public void setSeats(int seats) {
		this.seats = seats;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
	}

	public int[] getInvitees() {
		return invitees;
	}

	public void setInvitees(int[] invitees) {
		this.invitees = invitees;
	}

}
