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

package com.cubeia.games.poker.handler;

import com.cubeia.games.poker.io.protocol.*;

public class DefaultPokerHandler implements PacketVisitor {

    public void visit(PerformAction packet) {
    }

    // -----  TO CLIENTS
    @Override
    public void visit(GameCard packet) {
    }

    @Override
    public void visit(DealPublicCards packet) {
    }

    @Override
    public void visit(DealPrivateCards packet) {
    }

    @Override
    public void visit(HandStartInfo packet) {
    }

    @Override
    public void visit(ExposePrivateCards packet) {
    }

    @Override
    public void visit(HandEnd packet) {
    }

    @Override
    public void visit(BestHand packet) {
    }

    @Override
    public void visit(PlayerState packet) {
    }

    @Override
    public void visit(PlayerAction packet) {
    }

    @Override
    public void visit(RequestAction packet) {
    }

    @Override
    public void visit(DealerButton packet) {
    }

    @Override
    public void visit(StartHandHistory packet) {
    }

    @Override
    public void visit(StopHandHistory packet) {
    }

    @Override
    public void visit(TournamentOut packet) {
    }

    @Override
    public void visit(PlayerBalance packet) {
    }

    @Override
    public void visit(Pot packet) {
    }

    @Override
    public void visit(PlayerSitinRequest packet) {
    }

    @Override
    public void visit(PlayerPokerStatus packet) {
    }

    @Override
    public void visit(PlayerSitoutRequest packet) {
    }

    @Override
    public void visit(CardToDeal packet) {
    }

    @Override
    public void visit(ExternalSessionInfoPacket packet) {
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
    public void visit(PotTransfer packet) {
    }

    @Override
    public void visit(PotTransfers packet) {
    }

    @Override
    public void visit(BuyInInfoRequest packet) {
    }

    @Override
    public void visit(BuyInInfoResponse packet) {
    }

    @Override
    public void visit(BuyInRequest packet) {
    }

    @Override
    public void visit(BuyInResponse packet) {
    }

    @Override
    public void visit(HandCanceled packet) {
    }

    @Override
    public void visit(RakeInfo packet) {
    }

    @Override
    public void visit(ErrorPacket packet) {
    }

    @Override
    public void visit(TakeBackUncalledBet packet) {
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
    public void visit(RebuyResponse packet) {
    }

    @Override
    public void visit(AddOnOffer packet) {

    }

    @Override
    public void visit(PerformAddOn packet) {

    }

    @Override
    public void visit(PlayerPerformedRebuy packet) {}

    @Override
    public void visit(PlayerPerformedAddOn packet) {}

    @Override
    public void visit(AddOnPeriodClosed packet) {}

	@Override
	public void visit(AchievementNotificationPacket packet) {}

}
