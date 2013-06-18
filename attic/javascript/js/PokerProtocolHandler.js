var POKER_PROTOCOL = POKER_PROTOCOL || {};

/**
 * Construct a poker protocol object handler
 * @constructor
 * @param {com.cubeia.games.poker.PokerGameHandler} pokerGameHandler
 */
POKER_PROTOCOL.PokerProtocolHandler = function(pokerGameHandler) {

	this.pokerGameHandler = pokerGameHandler;

	this.handleGameTransportPacket = function(gameTransportPacket) {

		var valueArray =  FIREBASE.ByteArray.fromBase64String(gameTransportPacket.gamedata);
		var gameData = new FIREBASE.ByteArray(valueArray);
		var length = gameData.readInt();
		var classId = gameData.readUnsignedByte();
		
		//console.log("received protocolObject - classId=" + classId);
		
		var protocolObject = com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create(classId, gameData);
		
		//console.log(protocolObject);
		
		switch ( protocolObject.classId() ) {
			case com.cubeia.games.poker.io.protocol.BestHand.CLASSID:
				this.pokerGameHandler.handleBestHand(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID:
				this.pokerGameHandler.handleBuyInInfoRequest(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID:
				this.pokerGameHandler.handleBuyInInfoResponse(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID:
				this.pokerGameHandler.handleBuyInResponse(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID:
				this.pokerGameHandler.handleCardToDeal(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.DealerButton.CLASSID:
				this.pokerGameHandler.handleDealerButton(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID:
				this.pokerGameHandler.handleDealPrivateCards(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID:
				this.pokerGameHandler.handleDealPublicCards(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID:
				this.pokerGameHandler.handleDeckInfo(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID:
				this.pokerGameHandler.handleErrorPacket(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID:
				this.pokerGameHandler.handleExposePrivateCards(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID:
				this.pokerGameHandler.handleExternalSessionInfoPacket(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID:
				this.pokerGameHandler.handleFuturePlayerAction(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.GameCard.CLASSID:
				this.pokerGameHandler.handleGameCard(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID:
				this.pokerGameHandler.handleHandCanceled(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.HandEnd.CLASSID:
				this.pokerGameHandler.handleHandEnd(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID:
				this.pokerGameHandler.handleInformFutureAllowedActions(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PerformAction.CLASSID:
				this.pokerGameHandler.handlePerformAction(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PingPacket.CLASSID:
				this.pokerGameHandler.handlePingPacket(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID:
				this.pokerGameHandler.handlePlayerAction(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID:
				this.pokerGameHandler.handlePlayerBalance(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID:
				this.pokerGameHandler.handlePlayerDisconnectedPacket(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID:
				this.pokerGameHandler.handlePlayerHandStartStatus(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID:
				this.pokerGameHandler.handlePlayerPokerStatus(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID:
				this.pokerGameHandler.handlePlayerReconnectedPacket(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PlayerState.CLASSID:
				this.pokerGameHandler.handlePlayerState(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PingPacket.CLASSID:
				this.pokerGameHandler.handlePingPacket(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.PongPacket.CLASSID:
				this.pokerGameHandler.handlePongPacket(protocolObject);
				break;
            case com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID:
                this.pokerGameHandler.handlePotTransfers(protocolObject);
                break;
			case com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID:
				this.pokerGameHandler.handleRakeInfo(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.RequestAction.CLASSID:
				this.pokerGameHandler.handleRequestAction(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID:
				this.pokerGameHandler.handleStartHandHistory(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.StartNewHand.CLASSID:
				this.pokerGameHandler.handleStartNewHand(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID:
				this.pokerGameHandler.handleStopHandHistory(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID:
				this.pokerGameHandler.handleTakeBackUncalledBet(protocolObject);
				break;
			case com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID:
				this.pokerGameHandler.handleTournamentOut(protocolObject);
				break;
            default:
                console.log("Ignoring packet: " + protocolObject);
                break;
		}
	};
};