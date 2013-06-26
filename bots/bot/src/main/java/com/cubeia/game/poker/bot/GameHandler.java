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

import static com.cubeia.game.poker.util.Arithmetic.gaussianAverage;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cubeia.firebase.bot.BotState;
import com.cubeia.firebase.bot.action.Action;
import com.cubeia.firebase.bot.ai.AbstractAI;
import com.cubeia.firebase.bot.ai.MttAI;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.game.poker.bot.ai.PokerGameHandler;
import com.cubeia.games.poker.io.protocol.*;
import com.cubeia.games.poker.io.protocol.Enums.HandPhaseHoldem;
import com.cubeia.games.poker.io.protocol.Enums.PlayerTableStatus;

public class GameHandler implements PacketVisitor {

    private static final StyxSerializer styxDecoder = new StyxSerializer(new ProtocolObjectFactory());

    private final AbstractAI bot;
    private final Strategy strategy;

    private AtomicBoolean historicActionsAreBeingSent = new AtomicBoolean(false);
    
    private PokerGameHandler pokerHandler;

    public GameHandler(AbstractAI bot) {
    	this.strategy = new Strategy();
        this.bot = bot;
        pokerHandler = new PokerGameHandler(bot);
    }

    public void handleGamePacket(GameTransportPacket packet) {
        // Create the user packet
        ProtocolObject gamePacket;
        gamePacket = styxDecoder.unpack(ByteBuffer.wrap(packet.gamedata));
        gamePacket.accept(this);
    }

    public void handleTournamentPacket(MttTransportPacket packet) {
        ProtocolObject gamePacket;
        gamePacket = styxDecoder.unpack(ByteBuffer.wrap(packet.mttdata));
        gamePacket.accept(this);
    }

    public ProtocolObject unpack(GameTransportPacket packet) {
        // Create the user packet
        ProtocolObject gamePacket;
        gamePacket = styxDecoder.unpack(ByteBuffer.wrap(packet.gamedata));
        return gamePacket;
    }


    public void visit(HandStartInfo packet) {
        bot.getBot().logDebug("New Hand starting. Hand id: " + packet.handId);
    }

    @SuppressWarnings("static-access")
    public void visit(final RequestAction request) {
        if (request.player == bot.getBot().getPid() && !historicActionsAreBeingSent.get()) {
        	Action action = pokerHandler.onActionRequest(request);

            int wait = 0;
            if (strategy.useDelay(request.allowedActions)) {
                int expected = request.timeToAct / 6;
                int deviation = request.timeToAct / 3;
                wait = gaussianAverage(expected, deviation);
                wait = wait < 0 ? 0 : wait;
            }

            bot.executor.schedule(action, wait, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void visit(StartHandHistory packet) {
        historicActionsAreBeingSent.set(true);
    }

    @Override
    public void visit(StopHandHistory packet) {
        historicActionsAreBeingSent.set(false);
    }


    @Override
    public void visit(TournamentOut packet) {
        bot.getBot().logDebug("I was out from tournament. Position: " + packet.position);
        if (bot instanceof MttAI) {
            bot.getBot().setState(BotState.MTT_OUT);
        }
    }

    @Override
    public void visit(PlayerPokerStatus packet) {
        if (packet.player == bot.getBot().getPid() && packet.status.equals(PlayerTableStatus.SITOUT)) {
            // I am in sitout state, schedule a sitin again
            int wait = 20;
            bot.getBot().logDebug("I am sitting out. Scheduling sitin in " + wait + " seconds.");
            Action action = new Action(bot.getBot()) {
                public void run() {
                    PlayerSitinRequest sitin = new PlayerSitinRequest();
                    bot.getBot().sendGameData(bot.getTable().getId(), bot.getBot().getPid(), sitin);
                }
            };
            AbstractAI.executor.schedule(action, wait, TimeUnit.SECONDS);
        }
    }

    /**
     * Handle buy in
     */
    @Override
    public void visit(BuyInInfoResponse packet) {
        bot.getBot().logDebug("I will make a buy in with the maximum amount of " + packet.maxAmount);
        BuyInRequest request = new BuyInRequest();
        request.amount = packet.maxAmount;
        request.sitInIfSuccessful = true;
        bot.getBot().sendGameData(bot.getTable().getId(), bot.getBot().getPid(), request);
    }

    @Override
    public void visit(TakeBackUncalledBet packet) {
    }

    @Override
    public void visit(GameCard packet) {
    }

    @Override
    public void visit(BestHand packet) {
    }

    @Override
    public void visit(DealPublicCards packet) {
    	for (GameCard card : packet.cards) {
    		pokerHandler.addCommunityCard(card);
    	}
    	pokerHandler.getState().advancePhase();
    }

    @Override
    public void visit(DealPrivateCards packet) {
    	for (CardToDeal deal : packet.cards) {
    		visit(deal);
    	}
    }
    
    @Override
    public void visit(CardToDeal packet) {
    	if (packet.player == bot.getBot().getPid()) {
    		bot.getBot().logDebug("I got a card: "+packet.card);
    		pokerHandler.addPrivateCard(packet.card);
    		pokerHandler.getState().setPhase(HandPhaseHoldem.PREFLOP);
    	}
    }

    @Override
    public void visit(ExposePrivateCards packet) {
    }

    @Override
    public void visit(HandEnd packet) {
    	pokerHandler.clear();
    }

    @Override
    public void visit(PlayerState packet) {
    }

    @Override
    public void visit(PerformAction packet) {
    }

    @Override
    public void visit(PlayerAction packet) {
    }

    @Override
    public void visit(DealerButton packet) {
    }

    @Override
    public void visit(PlayerBalance packet) {
    }

    @Override
    public void visit(Pot packet) {}

    @Override
    public void visit(PlayerSitinRequest packet) {
    }

    @Override
    public void visit(PlayerSitoutRequest arg0) {
    }

    @Override
    public void visit(ExternalSessionInfoPacket packet) {
    }

    @Override
    public void visit(HandCanceled packet) {
    }

    @Override
    public void visit(BuyInInfoRequest packet) {
    }

    @Override
    public void visit(BuyInRequest packet) {
    }

    @Override
    public void visit(BuyInResponse packet) {
    }

    @Override
    public void visit(PotTransfer packet) {
    }

    @Override
    public void visit(PotTransfers packet) {
    }

    @Override
    public void visit(RakeInfo packet) {
    }

    @Override
    public void visit(DeckInfo packet) {
    }

    @Override
    public void visit(WaitingToStartBreak packet) {
    }

    @Override
    public void visit(WaitingForPlayers packet) {
    }

    @Override
    public void visit(BlindsAreUpdated packet) {

    }

    @Override
    public void visit(ErrorPacket packet) {
    }

    @Override
    public void visit(FuturePlayerAction packet) {
    }

    @Override
    public void visit(GameState packet) {

    }

    @Override
    public void visit(InformFutureAllowedActions packet) {
    }

    @Override
    public void visit(PlayerHandStartStatus packet) {
    }

    @Override
    public void visit(PlayerDisconnectedPacket packet) {
    }

    @Override
    public void visit(PlayerReconnectedPacket packet) {
    }

    @Override
    public void visit(PingPacket packet) {
    }

    @Override
    public void visit(PongPacket packet) {
    }

    @Override
    public void visit(RequestTournamentPlayerList packet) {
    }

    @Override
    public void visit(TournamentPlayerList packet) {
    }

    @Override
    public void visit(TournamentPlayer packet) {
    }

    @Override
    public void visit(RequestBlindsStructure packet) {
    }

    @Override
    public void visit(BlindsStructure packet) {
    }

    @Override
    public void visit(BlindsLevel packet) {
    }

    @Override
    public void visit(RequestPayoutInfo packet) {
    }

    @Override
    public void visit(PayoutInfo packet) {
    }

    @Override
    public void visit(Payout packet) {
    }

    @Override
    public void visit(RequestTournamentStatistics packet) {
    }

    @Override
    public void visit(ChipStatistics packet) {

    }

    @Override
    public void visit(LevelInfo packet) {

    }

    @Override
    public void visit(PlayersLeft packet) {

    }

    @Override
    public void visit(TournamentStatistics packet) {
    }

    @Override
    public void visit(TournamentInfo packet) {

    }

    @Override
    public void visit(RequestTournamentLobbyData packet) {
    }

    @Override
    public void visit(TournamentLobbyData packet) {
    }

    @Override
    public void visit(RequestTournamentTable packet) {

    }

    @Override
    public void visit(TournamentTable packet) {

    }

    @Override
    public void visit(RebuyOffer packet) {

    }

    @Override
    public void visit(RebuyResponse packet) {

    }

    @Override
    public void visit(AddOnOffer packet) {

    }

    @Override
    public void visit(PerformAddOn packet) {

    }

    @Override
    public void visit(PlayerPerformedRebuy packet) {

    }

    @Override
    public void visit(PlayerPerformedAddOn packet) {

    }

    @Override
    public void visit(AddOnPeriodClosed packet) {

    }

    @Override
    public void visit(TournamentDestroyed packet) {

    }

    @Override
    public void visit(RequestTournamentRegistrationInfo packet) {
    }

    @Override
    public void visit(TournamentRegistrationInfo packet) {
    }

    @Override
    public void visit(Currency packet) {
    }

    @Override
    public void visit(AchievementNotificationPacket packet) {

    }
}