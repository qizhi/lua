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

package com.cubeia.games.poker.admin.wicket.pages.tournaments.payouts;

import com.cubeia.games.poker.admin.service.PayoutStructureViewHelper;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class ViewPayoutStructure extends BasePage {

    @SpringBean
    private PayoutStructureViewHelper payoutStructureService;

    public ViewPayoutStructure(PageParameters p) {
        super(p);
        DataTable table = payoutStructureService.getPayoutStructure(p.get("structureId").toInt());
        add(table);
    }


    @Override
    public String getPageTitle() {
        return "Viewing Payout Structure";
    }


}
