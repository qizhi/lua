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
package com.cubeia.firebase.server.commands;

import java.io.Serializable;

import com.cubeia.firebase.server.layout.ClusterLayout;

public class ResumeMessage implements Serializable {

	private static final long serialVersionUID = 2753873640830640817L;
	
	private long id;
	private ClusterLayout layout;
	
	public ResumeMessage(long id, ClusterLayout layout) {
		this.id = id;
		this.layout = layout;
	}
	
	public ResumeMessage() { }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ClusterLayout getLayout() {
		return layout;
	}

	public void setLayout(ClusterLayout layout) {
		this.layout = layout;
	}
	
	@Override
	public String toString() {
		return id + "; layout: " + layout;
	}
}
