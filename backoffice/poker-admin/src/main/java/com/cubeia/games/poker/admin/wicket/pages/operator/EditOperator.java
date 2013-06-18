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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

import com.cubeia.backoffice.operator.api.OperatorAccountStatus;
import com.cubeia.backoffice.operator.api.OperatorConfigParamDTO;
import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.backoffice.operator.client.OperatorServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.network.shared.web.wicket.list.EditableListItem;
import com.cubeia.network.shared.web.wicket.list.ListEditor;


public class EditOperator extends BasePage {

    @SpringBean(name="operatorClient")
    private OperatorServiceClient operatorService;

    private final List<ParameterValuePair> values = new ArrayList<ParameterValuePair>();

    public EditOperator(final PageParameters parameters) {
        super(parameters);
        final Long operatorId = getOperatorId(parameters);
        final OperatorDTO operator = operatorService.getOperator(operatorId);
        Form<OperatorDTO> editOperatorForm = new Form<OperatorDTO>("editOperatorForm", new CompoundPropertyModel<OperatorDTO>(operator)){
            @Override
            public void onSubmit() {
                operatorService.updateOperator(operator);
            }
        };
        editOperatorForm.add(new Label("id"));
        editOperatorForm.add(new RequiredTextField<String>("name"));
        editOperatorForm.add(new CheckBox("enabled"));
        editOperatorForm.add(new DropDownChoice<OperatorAccountStatus>("accountStatus", Arrays.asList(OperatorAccountStatus.values())));
        add(editOperatorForm);

        final Map<OperatorConfigParamDTO,String> config = operatorService.getConfig(operatorId);


        for(OperatorConfigParamDTO p : OperatorConfigParamDTO.values()){
            String val = config.get(p);
            values.add(new ParameterValuePair(p,val));
        }
        Form<List<ParameterValuePair>> configForm = new Form<List<ParameterValuePair>>("configForm",
                new CompoundPropertyModel<List<ParameterValuePair>>(values)){
            @Override
            public void onSubmit() {
                Map<OperatorConfigParamDTO, String> operatorConfigMap = getOperatorConfigMap();
                if(operatorConfigMap.size()>0) {
                    operatorService.updateConfig(operatorId, operatorConfigMap);
                }
            }
        };

        ListEditor<ParameterValuePair> pl = new ListEditor<ParameterValuePair>("paramList",
                new PropertyModel(this,"values")) {
            @Override
            protected void onPopulateItem(EditableListItem<ParameterValuePair> item) {
                item.setModel(new CompoundPropertyModel(item.getModel()));
                item.add(new Label("name",item.getModelObject().param.name()));
                item.add(new TextField<String>("value"));
            }
        };
        configForm.add(pl);
        add(configForm);
    }

    private Map<OperatorConfigParamDTO, String> getOperatorConfigMap() {
        Map<OperatorConfigParamDTO,String> paramMap = new HashMap<OperatorConfigParamDTO, String>();
        for(ParameterValuePair p : values) {
            if(p.value!=null) {
                paramMap.put(p.param, p.value);
            }
        }
        return paramMap;
    }

    private Long getOperatorId(PageParameters params) {
        StringValue id = params.get("id");
        return id.toLong();
    }

    @Override
    public String getPageTitle() {
        return "Edit Operator";
    }

    private class ParameterValuePair implements Serializable {

        OperatorConfigParamDTO param;
        String value;
        public ParameterValuePair(OperatorConfigParamDTO param, String value) {
            this.param = param;
            this.value = value;
        }
    }
}
