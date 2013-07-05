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

package com.cubeia.games.poker;

import com.cubeia.firebase.api.game.TournamentProcessor;
import com.cubeia.firebase.api.game.table.TournamentTableListener;
import com.cubeia.firebase.guice.game.EventScoped;
import com.cubeia.games.poker.adapter.*;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.common.time.SystemTime;
import com.cubeia.games.poker.common.time.DefaultSystemTime;
import com.cubeia.games.poker.handler.ActionTransformer;
import com.cubeia.games.poker.handler.BackendCallHandler;
import com.cubeia.games.poker.handler.BackendPlayerSessionHandler;
import com.cubeia.games.poker.handler.PokerHandler;
import com.cubeia.games.poker.logic.TimeoutCache;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class IntegrationGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StateInjector.class).in(EventScoped.class);
        bind(ActionCache.class).in(Singleton.class);
        bind(HandHistoryReporter.class).in(EventScoped.class);
        bind(FirebaseServerAdapter.class).in(EventScoped.class);
        bind(GameStateSender.class).in(Singleton.class);
        bind(BackendCallHandler.class).in(EventScoped.class);
        bind(PokerHandler.class).in(EventScoped.class);
        bind(TournamentProcessor.class).to(Processor.class);
        bind(TournamentTableListener.class).to(PokerTableListener.class);
        bind(ActionTransformer.class).in(Singleton.class);
        bind(ActionSequenceGenerator.class).in(Singleton.class);
        bind(TimeoutCache.class).in(Singleton.class);
        bind(BackendPlayerSessionHandler.class).in(Singleton.class);
        bind(TableCloseHandlerImpl.class).in(EventScoped.class);
        bind(LobbyUpdater.class).in(Singleton.class);
        bind(PlayerUnseater.class).in(Singleton.class);
        bind(BuyInCalculator.class).in(Singleton.class);
        bind(SystemTime.class).to(DefaultSystemTime.class).in(Singleton.class);
    }
}
