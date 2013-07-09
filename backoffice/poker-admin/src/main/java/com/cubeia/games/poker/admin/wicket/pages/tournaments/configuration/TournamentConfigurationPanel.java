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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.configuration;

import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.games.poker.admin.db.AdminDAO;
import com.cubeia.games.poker.admin.network.NetworkClient;
import com.cubeia.games.poker.admin.wicket.components.TournamentPlayersValidator;
import com.cubeia.games.poker.tournament.configuration.TournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.blinds.BlindsStructure;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.timing.TimingProfile;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class TournamentConfigurationPanel extends Panel {

    private static final Logger log = Logger.getLogger(TournamentConfigurationPanel.class);

    @SpringBean(name="adminDAO")
    private AdminDAO adminDAO;

    @SpringBean
    private NetworkClient networkClient;

    private PropertyModel<TournamentConfiguration> model;

    public TournamentConfigurationPanel(String id, Form<?> form, PropertyModel<TournamentConfiguration> propertyModel, boolean sitAndGo) {
        super(id, propertyModel);
        this.model = propertyModel;
        add(new RequiredTextField<String>("name", new PropertyModel(model, "name")));
        add(new RequiredTextField<Integer>("seatsPerTable", new PropertyModel(model, "seatsPerTable")));
        DropDownChoice<TimingProfile> timing = new DropDownChoice<TimingProfile>("timingType", model("timingType"), adminDAO.getTimingProfiles(), renderer("name"));
        timing.setRequired(true);
        add(timing);
        TextField<Integer> minPlayers = new TextField<Integer>("minPlayers", new PropertyModel(model, "minPlayers"));
        minPlayers.add(RangeValidator.minimum(2));
        add(minPlayers);
        TextField<Integer> maxPlayers = new TextField<Integer>("maxPlayers", new PropertyModel(model, "maxPlayers"));
        maxPlayers.add(RangeValidator.minimum(2));
        add(maxPlayers);
        add(new RequiredTextField<BigDecimal>("buyIn", new PropertyModel(model, "buyIn")));
        add(new RequiredTextField<BigDecimal>("fee", new PropertyModel(model, "fee")));
        add(new RequiredTextField<BigDecimal>("guaranteedPrizePool", new PropertyModel(model, "guaranteedPrizePool")));
        add(new RequiredTextField<BigDecimal>("startingChips", new PropertyModel(model, "startingChips")));
        DropDownChoice<BetStrategyType> strategy = new DropDownChoice<BetStrategyType>("betStrategy", new PropertyModel(model, "betStrategy"), asList(BetStrategyType.values()), renderer("name"));
        strategy.setRequired(true);
        add(strategy);
        form.add(new TournamentPlayersValidator(minPlayers,maxPlayers));

        final List<OperatorDTO> operators = networkClient.getOperators();


        add(new ListMultipleChoice<Long>("operatorIds", model("operatorIds"), getOperatorIds(operators), new IChoiceRenderer<Long>() {

            @Override
            public Object getDisplayValue(Long id) {
                return getOperatorName(operators,id);
            }

            @Override
            public String getIdValue(Long object, int index) {
                return object.toString();
            }
        }));
        
        DropDownChoice<String> currency = new DropDownChoice<String>("currency", model("currency"), networkClient.getCurrencies(), new ChoiceRenderer<String>());
        currency.setRequired(true);
        add(currency);
        DropDownChoice<BlindsStructure> blindsStructure = new DropDownChoice<BlindsStructure>("blindsStructure", model("blindsStructure"), adminDAO.getBlindsStructures(), renderer("name"));
        blindsStructure.setRequired(true);
        add(blindsStructure);
        DropDownChoice<PayoutStructure> payoutStructure = new DropDownChoice<PayoutStructure>("payoutStructure", model("payoutStructure"), adminDAO.getPayoutStructures(), renderer("name"));
        payoutStructure.setRequired(true);
        add(payoutStructure);

        if (sitAndGo) {
            maxPlayers.setVisible(false);
        }
    }

	private List<Long> getOperatorIds(List<OperatorDTO> operators) {
		List<Long> l = new ArrayList<Long>();
		for (OperatorDTO op :operators) {
			l.add(op.getId());
		}
		return l;
	}

	private String getOperatorName(List<OperatorDTO> operators, Long id) {
		for (OperatorDTO op : operators) {
			if(op.getId() == id) {
				return op.getName();
			}
		}
		return "n/a";
	}

    private PropertyModel model(String expression) {
        return new PropertyModel(model, expression);
    }

    private <T> ChoiceRenderer<T> renderer(String name) {
        return new ChoiceRenderer<T>(name);
    }
}
