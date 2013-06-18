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

import com.cubeia.firebase.api.action.MttRegisterResponseAction;
import com.cubeia.firebase.api.action.MttUnregisterResponseAction;
import com.cubeia.firebase.api.action.mtt.MttCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttDestroyedAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRegisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesRemovedAction;
import com.cubeia.firebase.api.action.mtt.MttUnregisterPlayerAction;
import com.cubeia.firebase.api.action.mtt.ScheduledMttAction;

public class DefaultMttActionVisitor implements MttActionVisitor {
	public void visit(MttRegisterPlayerAction action) {}
	public void visit(MttUnregisterPlayerAction action) {}
	public void visit(MttRoundReportAction action) {}
	public void visit(MttTablesCreatedAction action) {}
	public void visit(MttTablesRemovedAction action) {}
	public void visit(ScheduledMttAction action) {}
    public void visit(MttObjectAction mttObjectAction) {}
    public void visit(MttDataAction mttDataAction) {}
    public void visit(MttRegisterResponseAction mttRegisterResponseAction) {}
	public void visit(MttUnregisterResponseAction mttUnregisterResponseAction) {}
    public void visit(MttCreatedAction mttCreatedAction) {}
    public void visit(MttDestroyedAction mttDestroyedAction) { }
    public void visit(MttSeatingFailedAction mttSeatingFailedAction) {}
}
