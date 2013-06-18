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
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class CreateOperator extends BasePage {

    @SpringBean(name="operatorClient")
    private OperatorServiceClient operatorService;

    final OperatorDTO operator = new OperatorDTO();

    public CreateOperator(final PageParameters parameters) {
        super(parameters);
        Form<OperatorDTO> createForm = new Form<OperatorDTO>("createForm",
                new CompoundPropertyModel<OperatorDTO>(operator)){

            @Override
            public void onSubmit() {
                operatorService.createOperator(operator);
                setResponsePage(OperatorList.class);
            }

        };

        createForm.add(new RequiredTextField<Integer>("id"));
        createForm.add(new RequiredTextField<String>("name"));
        createForm.add(new CheckBox("enabled"));

        add(createForm);

    }

    @Override
    public String getPageTitle() {
        return "Create Operator";
    }
}
