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
package com.cubeia.network.shared.web.wicket.list;

import org.apache.wicket.markup.html.form.Button;

import java.util.List;

public abstract class EditorButton extends Button {
    private transient EditableListItem<?> parent;

    public EditorButton(String id) {
        super(id);
    }

    protected final EditableListItem<?> getItem() {
        if (parent == null) {
            parent = findParent(EditableListItem.class);
        }
        return parent;
    }

    protected final List<?> getList() {
        return getEditor().getItems();
    }

    protected final ListEditor<?> getEditor() {
        return (ListEditor<?>) getItem().getParent();
    }


    @Override
    protected void onDetach() {
        parent = null;
        super.onDetach();
    }

}