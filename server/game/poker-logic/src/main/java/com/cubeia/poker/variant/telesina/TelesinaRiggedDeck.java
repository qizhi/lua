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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.hand.Card;

import java.util.List;
import java.util.Random;

/**
 * Rigged Telesina deck. The size of the deck will vary depending on the
 * number of participants.
 *
 * @author w
 */
public class TelesinaRiggedDeck extends TelesinaDeck {
    private static final long serialVersionUID = -5030565526818602010L;

    @SuppressWarnings("serial")
    private static final Random ALWAYS_ZERO_RNG = new Random() {
        protected int next(int bits) {
            return 0;
        }
    };

    public TelesinaRiggedDeck(TelesinaDeckUtil telesinaDeckUtil, int numberOfParticipants, String riggedDeckString) {
        super(telesinaDeckUtil, ALWAYS_ZERO_RNG, numberOfParticipants);
        List<Card> riggedCards = telesinaDeckUtil.createRiggedDeck(numberOfParticipants, riggedDeckString);
        resetCards(riggedCards);
    }

}
