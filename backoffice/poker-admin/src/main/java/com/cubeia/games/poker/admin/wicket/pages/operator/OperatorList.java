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
package com.cubeia.games.poker.admin.wicket.pages.operator;

import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class OperatorList extends BasePage {

    @SpringBean(name="operatorClient")
    private OperatorServiceClient operatorServiceClient;

    public OperatorList(final PageParameters parameters) {
        super(parameters);
        final List<OperatorDTO> operators = operatorServiceClient.getOperators();
        final DataView<OperatorDTO> operatorList = new DataView<OperatorDTO>("operatorList",
                new ListDataProvider<OperatorDTO>(operators)
               ) {
            @Override
            protected void populateItem(Item<OperatorDTO> item) {
                OperatorDTO operator = item.getModelObject();
                item.add(new Label("id", "" + operator.getId()));
                item.add(new Label("name", operator.getName()));
                item.add(new Label("enabled", operator.isEnabled() ? "Enabled" : "Disabled"));
                String status = operator.getAccountStatus() == null ? "" : operator.getAccountStatus().toString();
                item.add(new Label("accountStatus", status));
                PageParameters parameters = new PageParameters();
                parameters.add("id", operator.getId());
                item.add(new BookmarkablePageLink<Void>("editLink", EditOperator.class, parameters));
            }
        };
        add(operatorList);
    }

    @Override
    public String getPageTitle() {
        return "Operators";
    }
}
