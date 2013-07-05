package com.cubeia.games.poker.admin.service;


import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;

public interface PayoutStructureViewHelper {
    /**
     * Returns the payout structure as a wicket <code>DataTable</code>
     * @param id of the payout structure
     * @return
     */
    DataTable getPayoutStructure(int id);
}
