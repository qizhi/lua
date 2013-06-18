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

package com.cubeia.poker.variant.factory;

import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.PokerVariant;
import com.cubeia.poker.variant.telesina.Telesina;
import com.cubeia.poker.variant.telesina.TelesinaDealerButtonCalculator;
import com.cubeia.poker.variant.telesina.TelesinaDeckFactory;
import com.cubeia.poker.variant.telesina.TelesinaRoundFactory;
import com.cubeia.poker.variant.texasholdem.TexasHoldem;

public class GameTypeFactory {

    public static GameType createGameType(PokerVariant variant) {
        GameType gameType;

        switch (variant) {
            case TEXAS_HOLDEM:
                gameType = new TexasHoldem();
                break;
            case TELESINA:
                gameType = new Telesina(new TelesinaDeckFactory(), new TelesinaRoundFactory(), new TelesinaDealerButtonCalculator());
                break;
            default:
                throw new UnsupportedOperationException("unsupported poker variant: " + variant);
        }

        return gameType;
    }

}
