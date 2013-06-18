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

package com.cubeia.games.poker.tournament.state;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.games.poker.common.money.Money;
import com.cubeia.games.poker.tournament.configuration.payouts.PayoutStructure;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class PokerTournamentStateTest {

    private PokerTournamentState state = new PokerTournamentState();
    @Mock
    private PayoutStructure prizeStricture;
    private Currency eur  = new Currency("EUR",2);

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testGuaranteedPrizePool() {
        // Given a tournament with $200 in guaranteed money.
        state.setCurrency(eur);
        state.setBuyIn(BigDecimal.valueOf(10));
        state.setGuaranteedPrizePool(BigDecimal.valueOf(200));
        state.setPayoutStructure(prizeStricture, 10);

        // Where there are 12 players registered.
        for (int i = 0; i < 12; i++) state.addBuyInToPrizePool();

        // When we calculate the payouts.
        state.setPayouts(10, 12);

        // Then the prize pool should be $200
        assertThat(state.getPrizePool(), is(BigDecimal.valueOf(200)));
        // And that prize pool should be used while calculating the payouts.
        verify(prizeStricture, atLeastOnce()).getPayoutsForEntrantsAndPrizePool(12, new BigDecimal(200),eur);
        // And the guaranteed prize pool used should be eighty.
        assertThat(state.getGuaranteedPrizePoolUsedAsMoney(), is(new Money(new BigDecimal(80),eur)));
    }

    @Test
    public void testGuaranteedPrizePoolNotUsedIfEnoughRegistrations() {
        // Given a tournament with $200 in guaranteed money.
        state.setCurrency(eur);
        state.setBuyIn(BigDecimal.valueOf(10));
        state.setGuaranteedPrizePool(BigDecimal.valueOf(200));
        state.setPayoutStructure(prizeStricture, 10);

        // Where there are 21 players.
        for (int i = 0; i < 21; i++) state.addBuyInToPrizePool();

        // When we calculate the payouts.
        state.setPayouts(10, 21);

        // Then the prize pool should be $200
        assertThat(state.getPrizePool(), is(BigDecimal.valueOf(210)));
        // And that prize pool should be used while calculating the payouts.
        verify(prizeStricture, atLeastOnce()).getPayoutsForEntrantsAndPrizePool(21, new BigDecimal(210),eur);
        // And the guaranteed prize pool used should be zero.
        assertThat(state.getGuaranteedPrizePoolUsedAsMoney(), is(new Money(BigDecimal.ZERO, new Currency( "EUR", 2))));
    }

}
