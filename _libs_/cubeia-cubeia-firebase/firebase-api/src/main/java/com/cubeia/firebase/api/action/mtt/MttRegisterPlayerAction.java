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
package com.cubeia.firebase.api.action.mtt;

import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.MttActionVisitor;
import com.cubeia.firebase.api.common.Attribute;

/**
 * A player wants to sign up for a tournament.
 *
 * @author Fredrik
 */
public class MttRegisterPlayerAction extends MttPlayerAction {

	/** Version ID */
	private static final long serialVersionUID = 1L;

	private String screenname = "n/a";
	
	private List<Attribute> parameters;
	
	public MttRegisterPlayerAction(int mttId, int playerId) {
		super(mttId, playerId);
	}

	public void accept(MttActionVisitor visitor) {
		visitor.visit(this);
	}
	
	public String getScreenname() {
		return screenname;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public String toString() {
		return "MttRegisterPlayerAction - "+super.toString();
	}
	
	/**
	 * Returns a list of supplied attribute parameters
	 * to be used with the join request.
	 * 
	 * @return a list of attributes, never null.
	 */
	@SuppressWarnings("unchecked")
	public List<Attribute> getParameters() {
		if (parameters == null) {
			return Collections.EMPTY_LIST;
		} else {
			return parameters;
		}
	}

	public void setParameters(List<Attribute> parameters) {
		this.parameters = parameters;
	}
}
