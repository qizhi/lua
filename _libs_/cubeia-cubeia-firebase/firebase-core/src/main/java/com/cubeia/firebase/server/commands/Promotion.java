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

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.server.master.ClusterParticipant;


/**
 * This command is sent from the master when a new participant
 * is elected coordinator for its type. If any node caches the 
 * coordinator values it should reset everytime this command is
 * received. 
 * 
 * @author lars.j.nilsson
 * @date 2007 apr 4
 */
public class Promotion extends Command<ClusterParticipant> {

	private static final long serialVersionUID = -6244212955404179857L;
	
	public Promotion(ClusterParticipant part) {
		super(Types.PROMOTION.ordinal());
		setAttachment(part);
	}
}
