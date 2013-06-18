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

/**
 * 
 */
package com.cubeia.games.poker.admin.wicket.pages.util;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.util.Date;

/**
 * A property column that formats dates according to the applications standard data format.
 * @author w
 */
public final class DatePropertyColumn<T,K> extends PropertyColumn<T,K> {
    private static final long serialVersionUID = 1L;

    public DatePropertyColumn(IModel<String> displayModel, String propertyExpression) {
        super(displayModel, propertyExpression);
    }

    public DatePropertyColumn(IModel<String> displayModel, K sortProperty, String propertyExpression) {
        super(displayModel, sortProperty, propertyExpression);
    }
    
    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> model) {
        PropertyModel<T> propModel = new PropertyModel<T>(model, getPropertyExpression());
        item.add(new Label(componentId, FormatUtil.formatDate((Date) propModel.getObject()))); 
    }
}