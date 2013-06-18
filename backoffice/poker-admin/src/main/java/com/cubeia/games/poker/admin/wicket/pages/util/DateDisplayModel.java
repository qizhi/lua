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

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.util.Date;


/**
 * Date formatting display property model. Read only.
 * @author w
 */
public final class DateDisplayModel extends AbstractReadOnlyModel<String> {
    private static final long serialVersionUID = 1L;
    private IModel<Date> model;

    public DateDisplayModel(Object modelObject, String expression) {
        this(new PropertyModel<Date>(modelObject, expression));
    }
    
    public DateDisplayModel(IModel<Date> dateModel) {
        this.model = dateModel;
    }

    @Override
    public String getObject() {
        return FormatUtil.formatDate(model.getObject());
    }
}