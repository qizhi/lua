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
package com.cubeia.firebase.api.action.visitor;

import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinResponseAction;
import com.cubeia.firebase.api.action.local.FilteredJoinTableAvailableAction;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryResponseAction;
import com.cubeia.firebase.api.action.local.SystemInfoRequestAction;
import com.cubeia.firebase.api.action.local.SystemInfoResponseAction;

public interface LocalActionVisitor {
	public void handle(FilteredJoinAction action);
	public void handle(LoginRequestAction action);
	public void handle(LoginResponseAction action);
	public void handle(FilteredJoinResponseAction action);
	public void handle(FilteredJoinCancelAction action);
	public void handle(FilteredJoinCancelResponseAction action);
	public void handle(FilteredJoinTableAvailableAction action);
	public void handle(PlayerQueryRequestAction action);
	public void handle(PlayerQueryResponseAction action);
	public void handle(LocalServiceAction action);
    public void handle(SystemInfoRequestAction systemInfoRequestAction);
    public void handle(SystemInfoResponseAction systemInfoResponseAction);
}
