/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.rebuy;

import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.tournament.configuration.RebuyConfiguration;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;

public class RebuyConfigurationPanel extends Panel {

    private static final Logger log = Logger.getLogger(RebuyConfigurationPanel.class);
    private final CheckBox addOnsEnabled;
    private final TextField<Integer> numberOfLevelsWithRebuys;
    private final TextField<BigDecimal> rebuyCost;
    private final TextField<Integer> chipsForRebuy;
    private final TextField<BigDecimal> addOnCost;
    private final TextField<Integer> chipsForAddOn;
    private final TextField<Long> maxStackForRebuy;

    @SpringBean(name="adminDAO")
    private AdminDAO adminDAO;
    private final RebuyConfiguration rebuyConfiguration;
    private final TextField<Integer> rebuys;

    public RebuyConfigurationPanel(String id, RebuyConfiguration rebuyConfiguration, boolean enabled) {
        super(id);
        this.rebuyConfiguration = rebuyConfiguration;
        rebuys = add("numberOfRebuysAllowed");
        addOnsEnabled = checkBox("addOnsEnabled");
        numberOfLevelsWithRebuys = add("numberOfLevelsWithRebuys");
        rebuyCost = add("rebuyCost");
        chipsForRebuy = add("chipsForRebuy");
        addOnCost = add("addOnCost");
        chipsForAddOn = add("chipsForAddOn");
        maxStackForRebuy = add("maxStackForRebuy");
        setEnabled(enabled);
        log.debug("Setting enabled to: " + enabled);
    }

    public void clear() {
        rebuys.setModelObject(0);
        addOnsEnabled.setModelObject(false);
        numberOfLevelsWithRebuys.setModelObject(0);
        rebuyCost.setModelObject(BigDecimal.ZERO);
        chipsForRebuy.setModelObject(0);
        addOnCost.setModelObject(BigDecimal.ZERO);
        chipsForAddOn.setModelObject(0);
        maxStackForRebuy.setModelObject(0L);
    }

    public void setRebuysEnabled(boolean enabled) {
        setEnabled(enabled);
        if (!enabled) {
            clear();
        }
    }

    private CheckBox checkBox(String expression) {
        CheckBox checkBox = new CheckBox(expression, model(expression));
        add(checkBox);
        return checkBox;
    }

    private <T> TextField<T> add(String expression) {
        TextField<T> textField = new TextField<T>(expression, model(expression));
        add(textField);
        return textField;
    }

    private PropertyModel model(String expression) {
        return new PropertyModel(rebuyConfiguration, expression);
    }

}
