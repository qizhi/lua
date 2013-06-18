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
package com.cubeia.firebase.mtt.comm;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.server.commands.Types;

/**
 * This object is sent last in the chain of command in an
 * mtt creation chain. In effect, the mtt "tournamentCreated"
 * should be called on receiving this event.
 * 
 * @author larsan
 */
public class MttFinalized extends Command<int[]> {

	private static final long serialVersionUID = 8844228052839789725L;

	public MttFinalized(int[] mttIds) {
		super(Types.MTT_CREATED.ordinal());
		setAttachment(mttIds);
	}
}
