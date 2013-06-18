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

package com.cubeia.games.poker.client;

import com.cubeia.firebase.clients.java.connector.text.IOContext;
import com.cubeia.games.poker.io.protocol.AddOnOffer;
import com.cubeia.games.poker.io.protocol.AddOnPeriodClosed;
import com.cubeia.games.poker.io.protocol.BestHand;
import com.cubeia.games.poker.io.protocol.BlindsAreUpdated;
import com.cubeia.games.poker.io.protocol.BlindsLevel;
import com.cubeia.games.poker.io.protocol.BlindsStructure;
import com.cubeia.games.poker.io.protocol.BuyInInfoRequest;
import com.cubeia.games.poker.io.protocol.BuyInInfoResponse;
import com.cubeia.games.poker.io.protocol.BuyInRequest;
import com.cubeia.games.poker.io.protocol.BuyInResponse;
import com.cubeia.games.poker.io.protocol.CardToDeal;
import com.cubeia.games.poker.io.protocol.ChipStatistics;
import com.cubeia.games.poker.io.protocol.Currency;
import com.cubeia.games.poker.io.protocol.DealPrivateCards;
import com.cubeia.games.poker.io.protocol.DealPublicCards;
import com.cubeia.games.poker.io.protocol.DealerButton;
import com.cubeia.games.poker.io.protocol.DeckInfo;
import com.cubeia.games.poker.io.protocol.ErrorPacket;
import com.cubeia.games.poker.io.protocol.ExposePrivateCards;
import com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket;
import com.cubeia.games.poker.io.protocol.FuturePlayerAction;
import com.cubeia.games.poker.io.protocol.GameCard;
import com.cubeia.games.poker.io.protocol.GameState;
import com.cubeia.games.poker.io.protocol.HandCanceled;
import com.cubeia.games.poker.io.protocol.HandEnd;
import com.cubeia.games.poker.io.protocol.HandStartInfo;
import com.cubeia.games.poker.io.protocol.InformFutureAllowedActions;
import com.cubeia.games.poker.io.protocol.LevelInfo;
import com.cubeia.games.poker.io.protocol.PacketVisitor;
import com.cubeia.games.poker.io.protocol.Payout;
import com.cubeia.games.poker.io.protocol.PayoutInfo;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.PerformAddOn;
import com.cubeia.games.poker.io.protocol.PingPacket;
import com.cubeia.games.poker.io.protocol.PlayerAction;
import com.cubeia.games.poker.io.protocol.PlayerBalance;
import com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket;
import com.cubeia.games.poker.io.protocol.PlayerHandStartStatus;
import com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn;
import com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy;
import com.cubeia.games.poker.io.protocol.PlayerPokerStatus;
import com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket;
import com.cubeia.games.poker.io.protocol.PlayerSitinRequest;
import com.cubeia.games.poker.io.protocol.PlayerSitoutRequest;
import com.cubeia.games.poker.io.protocol.PlayerState;
import com.cubeia.games.poker.io.protocol.PlayersLeft;
import com.cubeia.games.poker.io.protocol.PongPacket;
import com.cubeia.games.poker.io.protocol.Pot;
import com.cubeia.games.poker.io.protocol.PotTransfer;
import com.cubeia.games.poker.io.protocol.PotTransfers;
import com.cubeia.games.poker.io.protocol.RakeInfo;
import com.cubeia.games.poker.io.protocol.RebuyOffer;
import com.cubeia.games.poker.io.protocol.RebuyResponse;
import com.cubeia.games.poker.io.protocol.RequestAction;
import com.cubeia.games.poker.io.protocol.RequestBlindsStructure;
import com.cubeia.games.poker.io.protocol.RequestPayoutInfo;
import com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData;
import com.cubeia.games.poker.io.protocol.RequestTournamentPlayerList;
import com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo;
import com.cubeia.games.poker.io.protocol.RequestTournamentStatistics;
import com.cubeia.games.poker.io.protocol.RequestTournamentTable;
import com.cubeia.games.poker.io.protocol.StartHandHistory;
import com.cubeia.games.poker.io.protocol.StopHandHistory;
import com.cubeia.games.poker.io.protocol.TakeBackUncalledBet;
import com.cubeia.games.poker.io.protocol.TournamentDestroyed;
import com.cubeia.games.poker.io.protocol.TournamentInfo;
import com.cubeia.games.poker.io.protocol.TournamentLobbyData;
import com.cubeia.games.poker.io.protocol.TournamentOut;
import com.cubeia.games.poker.io.protocol.TournamentPlayer;
import com.cubeia.games.poker.io.protocol.TournamentPlayerList;
import com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo;
import com.cubeia.games.poker.io.protocol.TournamentStatistics;
import com.cubeia.games.poker.io.protocol.TournamentTable;
import com.cubeia.games.poker.io.protocol.WaitingForPlayers;
import com.cubeia.games.poker.io.protocol.WaitingToStartBreak;

public class ManualGameHandler implements PacketVisitor {

    private final IOContext context;

    private PokerFormatter formatter = new PokerFormatter();

    public ManualGameHandler(IOContext context) {
        this.context = context;
    }

    public void visit(DealerButton packet) {
        System.out.println("Player[" + packet.seat + "] is dealer");
    }

    public void visit(DealPublicCards packet) {
        System.out.println("Public cards dealt: " + formatter.format(packet.cards));
    }

    public void visit(DealPrivateCards packet) {
        for (CardToDeal card : packet.cards) {
            card.accept(this);
        }
    }

//    public void visit(StartNewHand packet) {
//        System.out.println("Start a new hand. Dealer: " + packet.dealerSeatId);
//    }

    public void visit(ExposePrivateCards packet) {
        for (CardToDeal card : packet.cards) {
            System.out.println("Player " + card.player + " shows: " + formatter.format(card.card));
        }
    }

    public void visit(HandEnd packet) {
        String out = "\nHand over. Hands:\n";
        for (BestHand hand : packet.hands) {
            out += "\t" + hand.player + " - " + hand + " (" + hand.handType + ")\n";
        }
        System.out.println(out);
    }

    public void visit(RequestAction packet) {
        if (packet.player == context.getPlayerId()) {
            System.out.println("I was requested to do something: " + packet.allowedActions);
            PokerTextClient.seq = packet.seq;
        } else {
            System.out.println("Player[" + packet.player + "] was requested to act.");
        }
    }

    public void visit(PerformAction packet) {
        if (packet.player == context.getPlayerId()) {
            // System.out.println("I acted with: "+packet.action.type.name()+"  bet: "+packet.bet);
        } else {
            System.out.println("Player[" + packet.player + "] acted: " + packet.action.type.name() + "  bet: " + packet.betAmount);
        }
    }

    public void visit(StartHandHistory packet) {
        System.out.println("-- Start History");
    }

    public void visit(StopHandHistory packet) {
        System.out.println("-- Stop History");
    }

    public void visit(TournamentOut packet) {
        System.out.println("Player: " + packet.player + " was out of tournament");
    }

    public void visit(PlayerBalance packet) {
        if (packet.player == context.getPlayerId()) {
            System.out.println("I got balance: " + packet.balance + ", pending: " + packet.pendingBalance);
        } else {
            System.out.println("Player[" + packet.player + "] balance: " + packet.balance);
        }
    }

    @Override
    public void visit(CardToDeal packet) {
        if (packet.player == context.getPlayerId()) {
            System.out.println("I was dealt: " + formatter.format(packet.card));
        } else {
            System.out.println("Player[" + packet.player + "] was dealt: " + formatter.format(packet.card));
        }
    }

    @Override
    public void visit(PlayerPokerStatus packet) {
        if (packet.player == context.getPlayerId()) {
            System.out.println("My status has changed to: " + packet.status);
        } else {
            System.out.println("Player[" + packet.player + "]'s status has changed to: " + packet.status);
        }
    }

    @Override
    public void visit(BuyInInfoResponse packet) {
        System.out.println("Buy in info - min[" + packet.minAmount + "] max[" + packet.maxAmount + "] mandatory[" + packet.mandatoryBuyin + "]");
    }

    public void visit(GameCard packet) {
    }

    public void visit(BestHand packet) {
    }

    public void visit(PlayerState packet) {
    }

    public void visit(PlayerAction packet) {
    }

    public void visit(Pot packet) {
    }

    @Override
    public void visit(PlayerSitinRequest packet) {
    }

    @Override
    public void visit(PlayerSitoutRequest packet) {
    }

    @Override
    public void visit(HandCanceled packet) {
    }

    @Override
    public void visit(BuyInInfoRequest packet) {
        System.out.println("Buy-in info: " + packet);
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
    public void visit(BlindsLevel packet) {
    }

    @Override
    public void visit(ErrorPacket packet) {
    }

    @Override
    public void visit(TakeBackUncalledBet packet) {
    }

    @Override
    public void visit(ExternalSessionInfoPacket packet) {
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
    public void visit(HandStartInfo packet) {

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
        System.out.println("Received tour player list: " + packet);
    }

    @Override
    public void visit(TournamentPlayer packet) {
    }

    @Override
    public void visit(RequestBlindsStructure packet) {
    }

    @Override
    public void visit(BlindsStructure packet) {
        System.out.println("Received blinds structure: " + packet);
    }

    @Override
    public void visit(RequestPayoutInfo packet) {
    }

    @Override
    public void visit(PayoutInfo packet) {
        System.out.println("Received payout info: " + packet);
    }

    @Override
    public void visit(Payout packet) {
    }

    @Override
    public void visit(RequestTournamentStatistics packet) {

    }

    @Override
    public void visit(TournamentStatistics packet) {

    }

    @Override
    public void visit(RequestTournamentLobbyData packet) {
    }

    @Override
    public void visit(TournamentLobbyData packet) {
        System.out.println("Received tournament lobby data: " + packet);
    }

    @Override
    public void visit(RequestTournamentTable packet) {

    }

    @Override
    public void visit(TournamentTable packet) {

    }

    @Override
    public void visit(RebuyOffer packet) {
        System.out.println("Someone was offered a rebuy. Pay " + packet.cost + " to get " + packet.chips + " chips.");
    }

    @Override
    public void visit(RebuyResponse packet) {

    }

    @Override
    public void visit(AddOnOffer packet) {
        System.out.println("Add-ons are available. Pay " + packet.cost + " to get " + packet.chips + " chips.");
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
    public void visit(ChipStatistics packet) {

    }

    @Override
    public void visit(LevelInfo packet) {

    }

    @Override
    public void visit(PlayersLeft packet) {

    }

    @Override
    public void visit(TournamentInfo packet) {}
    
	@Override
	public void visit(Currency packet) {}

}
