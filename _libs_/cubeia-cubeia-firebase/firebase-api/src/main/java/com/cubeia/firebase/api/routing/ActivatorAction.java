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
package com.cubeia.firebase.api.routing;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.action.AbstractAction;
import com.cubeia.firebase.api.action.Attribute;

public class ActivatorAction<T extends Serializable> extends AbstractAction implements Serializable {

	private static final long serialVersionUID = -4327563943293657409L;

	private T payload;
	private final List<Attribute> attributes = new LinkedList<Attribute>();

	public ActivatorAction() { }
	
	public ActivatorAction(T data) {
		this(data, null);
	}
	
	public ActivatorAction(T data, List<Attribute> attributes) {
		payload = data;
		if(attributes != null) {
			this.attributes.addAll(attributes);
		}
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	public T getData() {
		return payload;
	}
	
	public void setData(T payload) {
		this.payload = payload;
	}
}
