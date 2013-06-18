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

import com.cubeia.firebase.api.action.ActionRequest;
import com.cubeia.firebase.api.action.CleanupPlayerAction;
import com.cubeia.firebase.api.action.CreateTableResponseAction;
import com.cubeia.firebase.api.action.DealerAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.JoinResponseAction;
import com.cubeia.firebase.api.action.KickPlayerAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.MttPickedUpAction;
import com.cubeia.firebase.api.action.MttRegisterResponseAction;
import com.cubeia.firebase.api.action.MttSeatedAction;
import com.cubeia.firebase.api.action.NotifyInvitedAction;
import com.cubeia.firebase.api.action.PlayerInfoAction;
import com.cubeia.firebase.api.action.PlayerStatusAction;
import com.cubeia.firebase.api.action.ProbeAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.RequestStatusAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.ReserveSeatResponseAction;
import com.cubeia.firebase.api.action.ScheduledGameAction;
import com.cubeia.firebase.api.action.SeatInfoAction;
import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.action.StartMttRoundAction;
import com.cubeia.firebase.api.action.StopMttRoundAction;
import com.cubeia.firebase.api.action.SystemMessageAction;
import com.cubeia.firebase.api.action.TableChatAction;
import com.cubeia.firebase.api.action.TableNameAction;
import com.cubeia.firebase.api.action.TableQueryRequestAction;
import com.cubeia.firebase.api.action.TableQueryResponseAction;
import com.cubeia.firebase.api.action.UnWatchAction;
import com.cubeia.firebase.api.action.UnWatchResponseAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction;
import com.cubeia.firebase.api.action.WatchAction;
import com.cubeia.firebase.api.action.WatchResponseAction;
import com.cubeia.firebase.api.action.chat.ChannelChatAction;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;

/** 
 * 
 * Will contain a "visit" method for each action type
 * in the system.
 * 
 * A default implementation will be provided for each action in the 
 * DefaultVisitor which will do nothing.
 * 
 */
public interface GameActionVisitor extends Visitor {
	public void visit(JoinRequestAction action);
    public void visit(WatchAction action);
    public void visit(PlayerInfoAction action);
    public void visit(LeaveAction action);
    public void visit(DealerAction action);
    public void visit(ActionRequest action);
    public void visit(TableNameAction action);
    public void visit(ProbeAction action);
    public void visit(RequestStatusAction action);
	public void visit(GameDataAction action);
	public void visit(ChannelChatAction action);
	public void visit(TableChatAction action);
	public void visit(UnWatchAction action);
	public void visit(GameObjectAction action);
	public void visit(JoinResponseAction action);
	public void visit(LeaveResponseAction action);
    public void visit(ScheduledGameAction action);
	public void visit(PlayerStatusAction action);
    public void visit(WatchResponseAction action);
	public void visit(UnWatchResponseAction action);
    public void visit(KickPlayerAction action);
	public void visit(ClientServiceAction action);
    public void visit(SeatInfoAction action);
    public void visit(SeatPlayersMttAction seatPlayersMttAction);
	public void visit(StartMttRoundAction action);
	public void visit(StopMttRoundAction action);
	public void visit(UnseatPlayersMttAction action);
	public void visit(MttPickedUpAction action);
	public void visit(MttSeatedAction action);
	public void visit(TableQueryRequestAction action);
	public void visit(TableQueryResponseAction action);
	public void visit(RemovePlayerAction action);
    public void visit(MttRegisterResponseAction mttRegisterResponseAction);
    public void visit(MttDataAction mttDataAction);
	public void visit(ReserveSeatRequestAction action);
	public void visit(ReserveSeatResponseAction action);
	public void visit(CleanupPlayerAction action);
	public void visit(CreateTableResponseAction action);
	public void visit(NotifyInvitedAction action);
	public void visit(SystemMessageAction action);
}
