var com = com || {};
com.cubeia = com.cubeia || {};
com.cubeia.games = com.cubeia.games || {};
com.cubeia.games.poker = com.cubeia.games.poker || {};

com.cubeia.games.poker.PokerGameHandler = function(callbackInstance) {

	/**
	 * Handle BestHand
	 * @param {POKER_PROTOCOL.BestHand} 
	*/
	this.handleBestHand = function(bestHand) {
		
	};

	/**
	 * Handle BuyInInfoRequest
	 * @param {POKER_PROTOCOL.BuyInResponse} buyInResponse
	 */
	 this.handleBuyInInfoRequest = function(buyInInfoRequest) {
	 };
	 
	/**
	 * Handle BuyInInfoResponse
	 * @param {POKER_PROTOCOL.BuyInInfoResponse} buyInInfoResponse
	 */
	this.handleBuyInInfoResponse = function(buyInInfoResponse) {
		var buyInRequest = new com.cubeia.games.poker.io.protocol.BuyInRequest();
		buyInRequest.amount = buyInInfoResponse.maxAmount;
		buyInRequest.sitInIfSuccessful = true;
		sendGameTransportPacket(buyInRequest);
	};

	/**
	 * Handle BuyInResponse
	 * @param {POKER_PROTOCOL.BuyInResponse} buyInResponse
	 */
	 this.handleBuyInResponse = function(buyInResponse) {
	 };
		
	/**
	 * Handle CardToDeal
	 * @param {POKER_PROTOCOL.CardToDeal} cardToDeal
	 */
	this.handleCardToDeal= function(cardToDeal) {
	};
	
	/**
	 * Handle DealerButton
	 * @param {POKER_PROTOCOL.DealerButton} dealerButton
	 */
	this.handleDealerButton= function(dealerButton) {
		 pokerDealer.moveDealerButton(dealerButton.seat);
	};
	
	/**
	 * Handle DealPrivateCards
	 * @param {POKER_PROTOCOL.DealPrivateCards} dealPrivateCards
	 */
	this.handleDealPrivateCards= function(dealPrivateCards) {
		console.log("handleDealPrivateCards");
		for ( var i = 0; i < dealPrivateCards.cards.length; i ++ ) {
		 	pokerDealer.dealCardIdToPid(dealPrivateCards.cards[i]);
		}
	};
	
	/**
	 * Handle DealPublicCards
	 * @param {POKER_PROTOCOL.DealPublicCards} dealPublicCards
	 */
	this.handleDealPublicCards= function(dealPublicCards) {
		for ( var i = 0; i < dealPublicCards.cards.length; i ++ ) {
		 	pokerDealer.dealCommunityCard(dealPublicCards.cards[i]);
		}
	};
	
	/**
	 * Handle DeckInfo
	 * @param {POKER_PROTOCOL.DeckInfo} deckInfo
	 */
	this.handleDeckInfo= function(deckInfo) {
	};
	
	/**
	 * Handle ErrorPacket
	 * @param {POKER_PROTOCOL.ErrorPacket} errorPacket
	 */
	this.handleErrorPacket= function(errorPacket) {
	};
	
	/**
	 * Handle ExposePrivateCards
	 * @param {POKER_PROTOCOL.ExposePrivateCards} exposePrivateCards
	 */
	this.handleExposePrivateCards= function(exposePrivateCards) {
		console.log("handleExposePrivateCards");
		console.log(exposePrivateCards);
		for ( var i = 0; i < exposePrivateCards.cards.length; i ++ ) {
		 	pokerDealer.exposePrivateCards(exposePrivateCards.cards[i]);
		}
	};
	
	/**
	 * Handle ExternalSessionInfoPacket
	 * @param {POKER_PROTOCOL.ExternalSessionInfoPacket} externalSessionInfoPacket
	 */
	this.handleExternalSessionInfoPacket= function(externalSessionInfoPacket) {
	};
	
	/**
	 * Handle FuturePlayerAction
	 * @param {POKER_PROTOCOL.FuturePlayerAction} futurePlayerAction
	 */
	this.handleFuturePlayerAction= function(futurePlayerAction) {
	};
	
	/**
	 * Handle GameCard
	 * @param {POKER_PROTOCOL.GameCard} gameCard
	 */
	this.handleGameCard= function(gameCard) {
	};
	
	/**
	 * Handle HandCanceled
	 * @param {POKER_PROTOCOL.HandCanceled} handCanceled
	 */
	this.handleHandCanceled= function(handCanceled) {
	};
	
	/**
	 * Handle HandEnd
	 * @param {POKER_PROTOCOL.HandEnd} handEnd
	 */
	this.handleHandEnd= function(handEnd) {
		for ( var i = 0; i < handEnd.hands.length; i ++ ) {
			view.table.handleBestHand(handEnd.hands[i]);
		}
	};
	
	/**
	 * Handle InformFutureAllowedActions
	 * @param {POKER_PROTOCOL.InformFutureAllowedActions} informFutureAllowedActions
	 */
	this.handleInformFutureAllowedActions= function(informFutureAllowedActions) {
	};
	
	/**
	 * Handle PerformAction
	 * @param {POKER_PROTOCOL.PerformAction} performAction
	 */
	this.handlePerformAction= function(performAction) {
		view.table.handlePerformAction(performAction);
	};
	
	/**
	 * Handle PingPacket
	 * @param {POKER_PROTOCOL.PingPacket} pingPacket
	 */
	this.handlePingPacket= function(pingPacket) {
	};
	
	/**
	 * Handle PlayerAction
	 * @param {POKER_PROTOCOL.PlayerAction} playerAction
	 */
	this.handlePlayerAction= function(playerAction) {

	};
	
	/**
	 * Handle PlayerBalance
	 * @param {POKER_PROTOCOL.PlayerBalance} playerBalance
	 */
	this.handlePlayerBalance = function(playerBalance) {
		playerHandler.updateSeatBalance(playerBalance.player, currencyFormatted(playerBalance.balance));
	};
	
	/**
	 * Handle PlayerDisconnectedPacket
	 * @param {POKER_PROTOCOL.PlayerDisconnectedPacket} playerDisconnectedPacket
	 */
	this.handlePlayerDisconnectedPacket= function(playerDisconnectedPacket) {
	};
	
	/**
	 * Handle PlayerHandStartStatus
	 * @param {POKER_PROTOCOL.PlayerHandStartStatus} playerHandStartStatus
	 */
	this.handlePlayerHandStartStatus= function(playerHandStartStatus) {
	};
	
	/**
	 * Handle PlayerPokerStatus
	 * @param {POKER_PROTOCOL.PlayerPokerStatus} playerPokerStatus
	 */
	this.handlePlayerPokerStatus= function(playerPokerStatus) {
		playerHandler.handlePlayerStatus(playerPokerStatus.player, playerPokerStatus.status);
	};
	
	/**
	 * Handle PlayerReconnectedPacket
	 * @param {POKER_PROTOCOL.PlayerReconnectedPacket} playerReconnectedPacket
	 */
	this.handlePlayerReconnectedPacket= function(playerReconnectedPacket) {
	};
	
	/**
	 * Handle PlayerState
	 * @param {POKER_PROTOCOL.PlayerState} playerState
	 */
	this.handlePlayerState= function(playerState) {
	};
	
	/**
	 * Handle PingPacket
	 * @param {POKER_PROTOCOL.PingPacket} pingPacket
	 */
	this.handlePingPacket= function(pingPacket) {
	};
	
	/**
	 * Handle PongPacket
	 * @param {POKER_PROTOCOL.PongPacket} pongPacket
	 */
	this.handlePongPacket= function(pongPacket) {
	};

	/**
	 * Handle PotTransfer
	 * @param {POKER_PROTOCOL.PotTransfer} potTransfer
	 */
	this.handlePotTransfers= function(potTransfers) {
        view.table.handlePotTransfers(potTransfers);
	};
	
	/**
	 * Handle RakeInfo
	 * @param {POKER_PROTOCOL.RakeInfo} rakeInfo
	 */
	this.handleRakeInfo= function(rakeInfo) {
	};
	
	/**
	 * Handle RequestAction
	 * @param {POKER_PROTOCOL.RequestAction} requestAction
	 */
	this.handleRequestAction= function(requestAction) {
		view.table.handleRequestAction(requestAction);
	};
	
	/**
	 * Handle StartHandHistory
	 * @param {POKER_PROTOCOL.StartHandHistory} startHandHistory
	 */
	this.handleStartHandHistory= function(startHandHistory) {
		//handleStartHandHistory();
	};
	
	/**
	 * Handle StartNewHand
	 * @param {POKER_PROTOCOL.StartNewHand} startNewHand
	 */
	this.handleStartNewHand= function(startNewHand) {
		pokerDealer.startNewHand();
	};
	
	/**
	 * Handle StopHandHistory
	 * @param {POKER_PROTOCOL.StopHandHistory} stopHandHistory
	 */
	this.handleStopHandHistory= function(stopHandHistory) {
	//	handleStopHandHistory();
	};
	
	/**
	 * Handle TakeBackUncalledBet
	 * @param {POKER_PROTOCOL.TakeBackUncalledBet} takeBackUncalledBet
	 */
	this.handleTakeBackUncalledBet= function(takeBackUncalledBet) {
	};
	
	/**
	 * Handle TournamentOut
	 * @param {POKER_PROTOCOL.TournamentOut} tournamentOut
	 */
	this.handleTournamentOut= function(tournamentOut) {
	};

};