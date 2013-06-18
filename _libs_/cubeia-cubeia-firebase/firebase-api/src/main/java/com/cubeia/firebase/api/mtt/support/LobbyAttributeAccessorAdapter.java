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
package com.cubeia.firebase.api.mtt.support;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;

/**
 * <b>NB: </b> This class is within the public API because of build reasons,
 * it should only be used for testing! It will be moved shortly. See Trac issue
 * #417.
 */
//FIXME: Move to test, if you can get Maven to support it, see Trac issue #417
public class LobbyAttributeAccessorAdapter implements LobbyAttributeAccessor {

	private Map<String, AttributeValue> atts = new HashMap<String, AttributeValue>();
	
	public Map<String, AttributeValue> getAllAttributes() {
		return atts;
	}

	public AttributeValue getAttribute(String attribute) {
		return atts.get(attribute);
	}

	public Date getDateAttribute(String attribute) throws ClassCastException {
		AttributeValue att = atts.get(attribute);
		return att == null ? null : att.getDateValue();
	}

	public int getIntAttribute(String attribute) throws ClassCastException {
		AttributeValue att = atts.get(attribute);
		return att == null ? null : att.getIntValue();
	}

	public String getStringAttribute(String attribute) throws ClassCastException {
		AttributeValue att = atts.get(attribute);
		return att == null ? null : att.getStringValue();
	}

	public void removeAttribute(String attribute) {
		atts.remove(attribute);
	}

	public void setAttribute(String attribute, AttributeValue value) {
		atts.put(attribute, value);
	}

	public void setAttributes(Map<String, AttributeValue> attributes) {
		atts.putAll(attributes);
	}

	public void setDateAttribute(String attribute, Date value) {
		atts.put(attribute, AttributeValue.wrap(value));
	}

	public void setIntAttribute(String attribute, int value) {
		atts.put(attribute, AttributeValue.wrap(value));
	}

	public void setStringAttribute(String attribute, String value) {
		atts.put(attribute, AttributeValue.wrap(value));
	}
}
