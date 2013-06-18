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

package com.cubeia.games.poker.adapter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.games.poker.common.money.Currency;
import org.junit.Test;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.dto.BatchHandRequest;
import com.cubeia.games.poker.PokerConfigServiceMock;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.Result;

public class HandResultBatchFactoryTest {

    private Currency eur = new Currency("EUR",2);

    @Test
    public void testCreateBatchHandRequest() {
        HandResultBatchFactory handResultFactory = new HandResultBatchFactory();
        handResultFactory.configService = new PokerConfigServiceMock();
        String handId = "55555";

        int playerId1 = 22;
        PlayerSessionId playerSessionId1 = new PlayerSessionId(playerId1);
        PokerPlayerImpl pokerPlayer1 = mock(PokerPlayerImpl.class);
        when(pokerPlayer1.getId()).thenReturn(playerId1);
        when(pokerPlayer1.getPlayerSessionId()).thenReturn(playerSessionId1);

        int playerId2 = 33; 
        PlayerSessionId playerSessionId2 = new PlayerSessionId(playerId2);
        PokerPlayerImpl pokerPlayer2 = mock(PokerPlayerImpl.class);
        when(pokerPlayer2.getId()).thenReturn(playerId2); 
        when(pokerPlayer2.getPlayerSessionId()).thenReturn(playerSessionId2);

        TableId tableId = new TableId(1, 1);

        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();
        Result result1 = new Result(bd("9.80"), bd("10.00"), Collections.<Pot, BigDecimal>emptyMap());
        Result result2 = new Result(bd("-10.00"), bd("10.00"), Collections.<Pot, BigDecimal>emptyMap());
        results.put(pokerPlayer1, result1);
        results.put(pokerPlayer2, result2);

        RakeInfoContainer rakeInfoContainer = new RakeInfoContainer(bd("20.00"), bd("0.20"), new HashMap<Pot, BigDecimal>());
        HandResult handResult = new HandResult(results, Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), rakeInfoContainer, new ArrayList<Integer>(), eur);

        BatchHandRequest batchHandRequest = handResultFactory.createAndValidateBatchHandRequest(handResult, handId, tableId);

        assertThat(batchHandRequest, notNullValue());
        assertThat(batchHandRequest.getHandId(), is(handId));
        assertThat(batchHandRequest.getTableId(), is(tableId));
        assertThat(batchHandRequest.getHandResults().size(), is(2));

        com.cubeia.backend.cashgame.dto.HandResult hr1 = findByPlayerSessionId(playerSessionId1, batchHandRequest.getHandResults());
        assertThat(hr1.getAggregatedBet().getAmount(), is(result1.getWinningsIncludingOwnBets().subtract(result1.getNetResult())));
        assertThat(hr1.getAggregatedBet().getCurrencyCode(), is("EUR"));
        assertThat(hr1.getAggregatedBet().getFractionalDigits(), is(2));
        assertThat(hr1.getWin().getAmount(), is(result1.getWinningsIncludingOwnBets()));
        assertThat(hr1.getRake().getAmount(), is(bd("0.10")));
        assertThat(hr1.getPlayerSession(), is(playerSessionId1));
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }
    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateUnbalancedBatchHandRequest() {
        HandResultBatchFactory handResultFactory = new HandResultBatchFactory();
        handResultFactory.configService = new PokerConfigServiceMock();
        String handId = "55555";

        int playerId1 = 22;
        PlayerSessionId playerSessionId1 = new PlayerSessionId(playerId1);
        PokerPlayerImpl pokerPlayer1 = mock(PokerPlayerImpl.class);
        when(pokerPlayer1.getId()).thenReturn(playerId1);
        when(pokerPlayer1.getPlayerSessionId()).thenReturn(playerSessionId1);

        int playerId2 = 33;
        PlayerSessionId playerSessionId2 = new PlayerSessionId(playerId2);
        PokerPlayerImpl pokerPlayer2 = mock(PokerPlayerImpl.class);
        when(pokerPlayer2.getId()).thenReturn(playerId2);
        when(pokerPlayer2.getPlayerSessionId()).thenReturn(playerSessionId2);

        TableId tableId = new TableId(1, 1);

        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();
        Result result1 = new Result(bd(981), bd(1000), Collections.<Pot, BigDecimal>emptyMap());
        Result result2 = new Result(bd(-1000), bd(1000), Collections.<Pot, BigDecimal>emptyMap());
        results.put(pokerPlayer1, result1);
        results.put(pokerPlayer2, result2);

        RakeInfoContainer rakeInfoContainer = new RakeInfoContainer(bd(1000 * 2), bd((1000 * 2) / 100), new HashMap<Pot, BigDecimal>());
        HandResult handResult = new HandResult(results, Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), rakeInfoContainer, new ArrayList<Integer>(),eur);

        handResultFactory.createAndValidateBatchHandRequest(handResult, handId, tableId);
    }


    @Test
    public void testCreateHandBatch() {
        HandResultBatchFactory handResultFactory = new HandResultBatchFactory();
        handResultFactory.configService = new PokerConfigServiceMock();
        String handId = "12345";

        PokerPlayerImpl pokerPlayer8 = createMockPlayer(8);
        PokerPlayerImpl pokerPlayer2 = createMockPlayer(2);
        PokerPlayerImpl pokerPlayer0 = createMockPlayer(0);
        PokerPlayerImpl pokerPlayer5 = createMockPlayer(5);
        PokerPlayerImpl pokerPlayer9 = createMockPlayer(9);

        TableId tableId = new TableId(1, 1);

        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();
        Result result8 = new Result(bd(-26), bd(26), Collections.<Pot, BigDecimal>emptyMap());

        Pot pot = new Pot(0);
        pot.bet(pokerPlayer8, bd(26));
        pot.bet(pokerPlayer2, bd(146));
        pot.bet(pokerPlayer0, bd(14));
        pot.bet(pokerPlayer5, bd(2));
        pot.bet(pokerPlayer9, bd(331));
        Map<Pot, BigDecimal> pots = new HashMap<Pot, BigDecimal>();
        pots.put(pot, bd(331));
        Result result2 = new Result(bd(185), bd(146), pots);

        Result result0 = new Result(bd(-14), bd(14), Collections.<Pot, BigDecimal>emptyMap());
        Result result5 = new Result(bd(-2), bd(2), Collections.<Pot, BigDecimal>emptyMap());
        Result result9 = new Result(bd(-146), bd(146), Collections.<Pot, BigDecimal>emptyMap());

        results.put(pokerPlayer8, result8);
        results.put(pokerPlayer2, result2);
        results.put(pokerPlayer0, result0);
        results.put(pokerPlayer5, result5);
        results.put(pokerPlayer9, result9);

        HashMap<Pot, BigDecimal> potRakes = new HashMap<Pot, BigDecimal>();
        potRakes.put(pot, bd(3));
        RakeInfoContainer rakeInfoContainer = new RakeInfoContainer(bd(334), bd(3), potRakes);
        HandResult handResult = new HandResult(results, Collections.<RatedPlayerHand>emptyList(), Collections.<PotTransition>emptyList(), rakeInfoContainer, new ArrayList<Integer>(),eur);

        handResultFactory.createAndValidateBatchHandRequest(handResult, handId, tableId);
    }

    private PokerPlayerImpl createMockPlayer(int playerId) {
        PlayerSessionId playerSessionId = new PlayerSessionId(playerId);
        PokerPlayerImpl pokerPlayer1 = mock(PokerPlayerImpl.class);
        when(pokerPlayer1.getId()).thenReturn(playerId);
        when(pokerPlayer1.getPlayerSessionId()).thenReturn(playerSessionId);
        return pokerPlayer1;
    }


    private com.cubeia.backend.cashgame.dto.HandResult findByPlayerSessionId(PlayerSessionId playerSessionId,
                                                                             List<com.cubeia.backend.cashgame.dto.HandResult> handResults) {
        for (com.cubeia.backend.cashgame.dto.HandResult hr : handResults) {
            if (hr.getPlayerSession().equals(playerSessionId)) {
                return hr;
            }
        }
        return null;
    }

}
