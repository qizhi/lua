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

package com.cubeia.game.poker.bot;

import com.cubeia.firebase.api.util.ParameterUtil;
import com.cubeia.firebase.bot.BotGroupConfig;
import com.cubeia.firebase.bot.model.Table;
import org.apache.log4j.Logger;

public class DiceArenaGroupConfig implements BotGroupConfig {

    private static final int DA_MIN_LENGTH = 6;
    private static final int DA_MAX_LENGTH = 12;

    private static transient Logger log = Logger.getLogger(DiceArenaGroupConfig.class);

    public String createBotScreenName(int id) {
        String s = "Bot_" + id;
        // Must be 6 characters...
        while (s.length() < DA_MIN_LENGTH) {
            s += "_";
        }
        // Must be less than 12
        while (s.length() > DA_MAX_LENGTH) {
            s = s.substring(1);
        }
        return s;
    }

    public String createLobbyBotScreenName(int id) {
        return createBotScreenName(id);
    }

    public boolean isTableJoinable(Table table) {
        for (String key : table.getAttributes().keySet()) {
            if (key.equals("VISIBLE_IN_LOBBY")) {
                int visible = ParameterUtil.convertAsInt(table.getAttributes().get(key)).getValue().intValue();
                return (visible == 1);
            }
        }
        log.debug("Not joinable table: " + table);
        return false;
    }
}
