/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.activator;

import static com.cubeia.games.poker.activator.TableModifierActionType.CLOSE;
import static com.cubeia.games.poker.activator.TableModifierActionType.CREATE;
import static com.cubeia.games.poker.activator.TableModifierActionType.DESTROY;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.cubeia.games.poker.entity.TableConfigTemplate;

public class TableModifierAction {

	public static TableModifierAction create(TableConfigTemplate template) {
		return new TableModifierAction(-1, CREATE, template);
	}
	
	public static TableModifierAction close(int tableId) {
		return new TableModifierAction(tableId, CLOSE, null);
	}
	
	public static TableModifierAction destroy(int tableId) {
		return new TableModifierAction(tableId, DESTROY, null);
	}
	
	private final int tableId;
	private final TableModifierActionType type;
	private final TableConfigTemplate template;

	private TableModifierAction(int tableId, TableModifierActionType type, TableConfigTemplate template) {
		this.tableId = tableId;
		this.type = type;
		this.template = template;
	}
	
	public int getTableId() {
		return tableId;
	}
	
	public TableConfigTemplate getTemplate() {
		return template;
	}
	
	public TableModifierActionType getType() {
		return type;
	}
	
	
	// --- COMMON METHODS --- //
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(obj, this);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
