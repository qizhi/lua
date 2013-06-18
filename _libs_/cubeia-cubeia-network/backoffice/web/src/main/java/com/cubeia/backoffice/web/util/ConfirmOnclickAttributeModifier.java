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

package com.cubeia.backoffice.web.util;

import static org.apache.wicket.util.string.Strings.escapeMarkup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Confirm dialog attribute modifier for onclick events. This class wraps any existing javascript on the onclick handler
 * with an confirm dialog call. This is useful when Wicket generates it's own javascript and an ordinary attribute
 * appender/modifier cannot be used.
 * @author w
 *
 */
public class ConfirmOnclickAttributeModifier extends AttributeModifier {
    private static final long serialVersionUID = 1L;
    private final IModel<String> msgModel;

    /**
     * Constructor.
     * @param message message to display
     */
    public ConfirmOnclickAttributeModifier(String message) {
        this(Model.of(message));
    }
    
    public ConfirmOnclickAttributeModifier(IModel<String> msgModel) {
        super("onclick", null);
        this.msgModel = msgModel;
    }
        
    @Override
    public boolean isEnabled(Component component) {
        return super.isEnabled(component)  &&  component.isEnabled();
    }
    
    @Override
    protected String newValue(String currentValue, String replacementValue) {
        if (currentValue == null) {
            currentValue = "";
        }
        
        return "if (confirm('" + filterMessage(msgModel.getObject()) + "')) {" + currentValue + "} else return false;"; 
    }
    
    private String filterMessage(String message) {
        return escapeMarkup(message.replace("'", "\"")).toString();
    }
    
}