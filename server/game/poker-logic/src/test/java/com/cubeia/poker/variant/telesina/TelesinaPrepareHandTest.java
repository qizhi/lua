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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Card;

public class TelesinaPrepareHandTest {

    @Mock
    private PokerContext context;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;
    
    @Before
    public void setup() {
        initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPrepareNewHand() {
        List<Card> communityCards = mock(List.class);
        when(context.getCommunityCards()).thenReturn(communityCards);
        Telesina telesina = new Telesina(null, null, null);
        telesina.setPokerContextAndServerAdapter(context, serverAdapterHolder);
        telesina.prepareNewHand();
        verify(communityCards).clear();
    }

}
