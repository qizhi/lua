"use strict";
var Poker = Poker || {};
/**
 * Handles the table UI, extends the Poker.TableListener
 * interface to receive events about the table
 * @type {Poker.TableLayoutManager}
 */
Poker.TableLayoutManager = Class.extend({
    tableViewContainer : null,
    capacity : 10,
    seatTemplate : null,
    templateManager : null,
    cardElements : null,
    tableInfoElement : null,
    myActionsManager : null,
    myPlayerSeatId : -1,
    seats : null,
    dealerButton : null,
    currentDealer : -1,
    soundManager : null,
    tableId : -1,

    /**
     * @type {Poker.BuyInDialog}
     */
    buyInDialog : null,
    communityCardsContainer : null,
    mainPotContainer : null,
    tableView : null,
    animationManager : null,
    totalPotContainer : null,
    viewContainerOffsetTop : 0,

    tableLog : null,

    /**
     * @type Poker.Clock
     */
    clock : null,

    /**
     *
     * @param {Number} tableId
     * @param tableViewContainer
     * @param {Poker.TemplateManager} templateManager
     * @param {Number} capacity
     * @constructor
     */
    init : function(tableId, tableViewContainer, templateManager, capacity, soundManager) {
        if (!tableViewContainer) {
            throw "TableLayoutManager requires a tableViewContainer";
        }
        var self = this;
        this.tableViewContainer = tableViewContainer;
        this.seats = new Poker.Map();
        this.animationManager = new Poker.AnimationManager();
        var tableViewHtml = templateManager.render("tableViewTemplate",{tableId : tableId, capacity : capacity});
        this.viewContainerOffsetTop = tableViewContainer.offset().top;
        tableViewContainer.append(tableViewHtml);
        var viewId = "#tableView-"+tableId;
        this.tableView = $(viewId);

        new Poker.ChatInput(this.tableView.find(".chat-input"),function(message){
            new Poker.TableRequestHandler(tableId).sendChatMessage(message);
        });

        this.tableId = tableId;
        this.soundManager = soundManager;


        var actionCallback = function(actionType,amount){
           self.handleAction(actionType,amount);

        };
        this.buyInDialog = new Poker.BuyInDialog();
        this.myActionsManager = new Poker.MyActionsManager(this.tableView, tableId, actionCallback, false);
        this.templateManager = templateManager;
        this.capacity = capacity || this.capacity;
        this.seatTemplate = $("#seatTemplate").html();
        this.totalPotContainer = this.tableView.find(".total-pot").hide();

        for(var i = 0; i<this.capacity; i++){
            this.addEmptySeatContent(i,i,true);
        }

        this.dealerButton = new Poker.DealerButton(this.tableView.find(".dealer-button"),this.animationManager);
        $(this.tableView).show();
        this.communityCardsContainer = this.tableView.find(".community-cards");
        this.mainPotContainer = this.tableView.find(".main-pot");
        this.tableInfoElement = this.tableView.find(".table-info");
        tableViewContainer.show();
        this.cardElements = new Poker.Map();
        this.clock = new Poker.Clock(this.tableInfoElement.find(".time-to-next-level-value"));

        this.tableLog = new Poker.TableEventLog(this.tableView.find(".table-log-container"));

        $(".future-action").show();
    },
    onChatMessage : function(player, message) {
        this.tableLog.appendChatMessage(player,message);
    },
    handleAction : function(actionType,amount) {
        var self = this;
        if (actionType.id == Poker.ActionType.SIT_IN.id) {
            this.handleSitIn();
        } else if (actionType.id == Poker.ActionType.LEAVE.id && this.isConfirmLeave()) {

            Poker.AppCtx.getDialogManager().displayGenericDialog({
                container : self.tableView,
                translationKey : "leave-table",
                displayCancelButton : true
            }, function(){
                new Poker.PokerRequestHandler(self.tableId).onMyPlayerAction(actionType,amount);
                return true;
            });
            return;
        }
        new Poker.PokerRequestHandler(this.tableId).onMyPlayerAction(actionType,amount);
    },
    updateAvatar : function(playerId,avatarUrl) {
        this.getSeatByPlayerId(playerId).updateAvatar(avatarUrl);
    },
    isConfirmLeave : function() {
        if(this.myPlayerSeatId!=-1) {
            var seat = this.seats.get(this.myPlayerSeatId);
            if(seat!=null && seat.player.tableStatus == Poker.PlayerTableStatus.SITTING_IN) {
                return true;
            }
        }
        return false;
    },
    handleSitIn : function() {
        this.myActionsManager.onRequestToSitIn();
    },
    onActivateView : function() {
        this.animationManager.setActive(true);
        this.tableLog.scrollDown();
    },
    onDeactivateView : function() {
        this.animationManager.setActive(false);
    },
    onBestHands : function(hands) {

    },
    /**
     * Adds an empty seat div to a seat id and if position supplied
     * also the position css class
     * @param seatId - the seat id to add the empty seat div to
     * @param pos - position if supplied adds the corresponding position css class
     * @param active - {boolean} boolean to indicate if the seat is active or not (active == occupied)
     */
    addEmptySeatContent : function(seatId,pos,active) {
        var seat = $("#seat"+seatId+"-"+this.tableId);
        seat.addClass("seat-empty").html(this.templateManager.render("emptySeatTemplate",{}));
        seat.removeClass("seat-sit-out").removeClass("seat-folded");
        if (typeof(pos) != "undefined" && pos != -1) {
            seat.addClass("seat-pos-"+pos);
        }
        if (!active) {
            seat.addClass("seat-inactive");
        }
    },
    onBuyInCompleted : function() {
        this.buyInDialog.close();
    },
    onBuyInError : function(msg) {
        this.buyInDialog.onError(msg);
    },
    onBuyInInfo : function(tableName,balanceInWallet, balanceOnTable, maxAmount, minAmount, mandatory,currencyCode) {
        this.buyInDialog.show(this.tableId,tableName,balanceInWallet,maxAmount,minAmount,currencyCode);
    },
    /**
     * Called when a player is added to the table
     * @param seatId  - the seat id of the player
     * @param player  - the player that was added
     */
    onPlayerAdded : function(seatId,player) {
        console.log("Player " + player.name + " added at seat " + seatId);

        var seat = null;
        var elementId = null;
        if (player.id == Poker.MyPlayer.id) {
            elementId = "myPlayerSeat-"+this.tableId;
            seat = new Poker.MyPlayerSeat(this.tableId,elementId,seatId,player,this.myActionsManager,this.animationManager);
            this.myPlayerSeatId = seatId;
            this._calculateSeatPositions();
            if(this.currentDealer!=-1) {
                this.onMoveDealerButton(this.currentDealer);
            }
            this.seats.put(seatId,seat);
            this.tableView.find(".seat-pos-0").hide();
            var self = this;
            this.tableView.find(".hand-history").show().click(function(){
                Poker.AppCtx.getHandHistoryManager().requestHandHistory(self.tableId);
            });
            this.tableView.find(".click-area-0").touchSafeClick(function(){
                new Poker.PokerRequestHandler(self.tableId).requestBuyInInfo();
            });
            this.soundManager.playerAction({id:"action-join"}, this.tableId);

        } else {

            elementId = "seat"+seatId+"-"+this.tableId;
            seat = new Poker.Seat(elementId, seatId, player, this.animationManager);
            seat.setSeatPos(-1,this._getNormalizedSeatPosition(seatId));

            this.seats.put(seatId,seat);
        }

    },
    /**
     * Called when a player left the table,
     * removes the player from the table UI and resets
     * the seat to open
     * @param playerId - the id of the player
     */
    onPlayerRemoved : function(playerId) {
        var seat = this.getSeatByPlayerId(playerId);
        if (this.myPlayerSeatId == seat.seatId) {
            this.myPlayerSeatId = -1;
            var tournamentTable = Poker.AppCtx.getTournamentManager().isTournamentTable(this.tableId);
            console.log("TOURNAMENT TABLE = " + tournamentTable);
            console.log(Poker.AppCtx.getTournamentManager().tournamentTables);
            if(tournamentTable==false) {
                Poker.AppCtx.getDialogManager().displayGenericDialog(
                    {header: "Seating info", message : "You have been removed from table "});
            }

        }
        seat.clearSeat();
        this.seats.remove(seat.seatId);
        this.addEmptySeatContent(seat.seatId,-1,(this.myPlayerSeatId==-1));
        this.soundManager.playerAction({id:"action-leave"}, this.tableId);
    },

    /**
     * Retrieves the seat by player id
     * @param id  of the player
     * @return {Poker.Seat} the players seat or null if not found
     */
    getSeatByPlayerId : function(id) {
        var seats = this.seats.values();
        for (var i = 0; i < seats.length; i++) {
            if (seats[i].player.id == id) {
                return seats[i];
            }
        }
        return null;
    },
    onPlayerUpdated : function(p) {
        var seat = this.getSeatByPlayerId(p.id);
        if(seat==null) {
            console.log("Unable to find player " + p.name + " seat");
            return;
        }
        seat.updatePlayer(p);
    },
    onPlayerStatusUpdated : function(p) {
        var seat = this.getSeatByPlayerId(p.id);
        if(seat==null) {
            console.log("Unable to find player " + p.name + " seat");
            return;
        }
        seat.updatePlayerStatus(p);
    },
    onTableCreated : function(table) {
        this.currentDealer = -1;
        this.dealerButton.move(0,0);
        this.dealerButton.hide();
        Poker.Sharing.bindShareTable(this.tableView.find(".share-button")[0],table.id,table.name);
    },
    /**
     *
     * @param {String} handId
     */
    onStartHand : function(handId) {
        this._resetSeats();
        this._resetCommunity();
        this.tableView.find(".pot-transfer").remove();
        this.cardElements = new Poker.Map();
        this.myActionsManager.onStartHand();
        if(this.myPlayerSeatId!=-1) {
            this.seats.get(this.myPlayerSeatId).handlePlayerStatus();
        }
        console.log("Hand " + handId + " started");
        this.tableLog.appendNewHand(handId);
    },
    /**
     * Updates the blinds info given a new level.
     *
     * @param {com.cubeia.games.poker.io.protocol.BlindsLevel} level
     * @param {Number} secondsToNextLevel
     */
    onBlindsLevel : function(level, currency, secondsToNextLevel) {
        if (level.smallBlind != null && level.bigBlind != null) {
            this.tableInfoElement.show();
            this.tableInfoElement.find(".table-blinds-value").html(Poker.Utils.formatCurrency(level.smallBlind) +
                "/" + Poker.Utils.formatCurrency(level.bigBlind));
            this.myActionsManager.setBigBlind(Math.floor(parseFloat(level.bigBlind.replace(",",""))),currency);
            if (secondsToNextLevel >= 0){
                this.clock.sync(secondsToNextLevel);
                this.tableInfoElement.find(".time-to-next-level").show();
            } else {
                this.tableInfoElement.find(".time-to-next-level").hide();
            }
        }
    },
    onPlayerActed : function(player,actionType,amount) {
        var seat = this.getSeatByPlayerId(player.id);
        if (seat == null) {
            throw "unable to find seat for player " + player.id;
        }
        //make icons gray and hide the action text
        if(actionType == Poker.ActionType.BET || actionType == Poker.ActionType.RAISE) {
             $(".player-action-icon").addClass("action-inactive");
             this._hideSeatActionText();
        }
        this.soundManager.playerAction(actionType, this.tableId, player, amount);
        seat.onAction(actionType,amount);

        this.tableLog.appendAction(player,actionType,amount);
    },
    onDealPlayerCard : function(player,cardId,cardString) {
        this.playSound(Poker.Sounds.DEAL_PLAYER);
        var seat = this.getSeatByPlayerId(player.id);
        var card = new Poker.Card(cardId,this.tableId,cardString,this.templateManager);
        seat.dealCard(card);
        this._storeCard(card);
    },
    exposePrivateCards : function(playerCards) {
        for(var i = 0; i<playerCards.length; i++) {
            var cards = playerCards[i].cards;
            for(var j = 0; j<cards.length; j++) {
                this.onExposePrivateCard(cards[j].id, cards[j].cardString);
            }
            this.tableLog.appendExposedCards(playerCards[i]);
        }
    },
    onExposePrivateCard : function(cardId,cardString){
        this.playSound(Poker.Sounds.REVEAL);
        var card = this.cardElements.get(cardId);
        if(cardString == card.cardString) {
            return;
        }

        var animManager = this.animationManager;

        // callback to ensure that the image is loaded before firing the animation
        var imageLoadedCallback = function() {
            setTimeout(function() {
                new Poker.CSSClassAnimation(card.getJQElement()).addClass("exposed").start(animManager);
            }, 50)
        };

        card.exposeCard(cardString, imageLoadedCallback);




    },
    onMoveDealerButton : function(seatId) {
        var newDealer = this.currentDealer!=seatId;
        this.currentDealer = seatId;
        this.positionDealerButton(newDealer);
        this.playSound(Poker.Sounds.MOVE_DEALER_BUTTON);
    },
    positionDealerButton : function(newDealer) {
        if(this.currentDealer==-1) {
            return;
        }
        if(typeof(newDealer)=="undefined") {
            newDealer = false;
        }
        var seat = this.seats.get(this.currentDealer);
        var off = seat.getDealerButtonOffsetElement().relativeOffset(this.tableView);

        var pos = {
            left : Math.round(off.left + seat.getDealerButtonOffsetElement().width()*0.95),
            top : Math.round(off.top)
        };
        if(newDealer==true) {
            this.dealerButton.move(pos.top,pos.left);
        } else {
            this.dealerButton.instantMove(pos.top,pos.left);
        }
    },
    onBettingRoundComplete :function() {
        var seats =  this.seats.values();
        for(var x = 0; x<seats.length; x++) {
            seats[x].onBettingRoundComplete();
        }
    },
    onPlayerHandStrength : function(player, hand,cardStrings, handEnded) {
        var seat = this.getSeatByPlayerId(player.id);
        seat.showHandStrength(hand);
        if(handEnded == true) {
            this.tableLog.appendHandStrength(player,hand,cardStrings);
        }
    },
    onDealCommunityCards : function(cards) {
        for(var i = 0; i<cards.length; i++) {
            this.onDealCommunityCard(cards[i].id, cards[i].cardString);
        }
        this.tableLog.appendCommunityCards(cards);
    },
    onDealCommunityCard : function(cardId, cardString) {
        this.playSound(Poker.Sounds.DEAL_COMMUNITY);
        var card = new Poker.CommunityCard(cardId,this.tableId,cardString,this.templateManager);
        var html = card.render();
        this.communityCardsContainer.append(html);

        // Animate the cards.
        var div = $('#' + card.getCardDivId());

        new Poker.TransformAnimation(div).addTranslate3d(0,0,0,"").start(this.animationManager);

        this._storeCard(card);
        this._moveToPot();
    },
    onTotalPotUpdate : function(amount) {
       this.totalPotContainer.show().find(".amount").html(Poker.Utils.formatCurrency(amount));
    },
    /**
     *
     * @param {Poker.Pot[]} pots
     */
    onPotUpdate : function(pots) {
        for(var i = 0; i<pots.length; i++) {
            var potElement = this.mainPotContainer.find(".pot-"+pots[i].id);
            if(potElement.length>0) {
                potElement.html(Poker.Utils.formatCurrency(pots[i].amount));
            } else {
                this.mainPotContainer.append(this.templateManager.render("mainPotTemplate",
                    { potId: pots[i].id, amount : Poker.Utils.formatCurrency(pots[i].amount) }));
            }

        }
    },
    onRequestPlayerAction : function(player,allowedActions,timeToAct,mainPot,fixedLimit){
        var seats = this.seats.values();
        for (var s = 0; s<seats.length; s++) {
            seats[s].inactivateSeat();
        }
        var seat = this.getSeatByPlayerId(player.id);

        if (player.id == Poker.MyPlayer.id) {
            this.playSound(Poker.Sounds.REQUEST_ACTION);
        }

        seat.activateSeat(allowedActions,timeToAct,mainPot,fixedLimit);
    },
    onRequestRebuy : function(player, rebuyCost, chipsForRebuy, timeToAct){
//        var seats = this.seats.values();
//        for (var s = 0; s < seats.length; s++) {
//            seats[s].inactivateSeat();
//        }
        var seat = this.getSeatByPlayerId(player.id);
        seat.rebuyRequested(rebuyCost, chipsForRebuy, timeToAct);
    },
    onRequestAddOn : function(player, addOnCost, chipsForAddOn){
        var seat = this.getSeatByPlayerId(player.id);
        seat.addOnRequested(addOnCost, chipsForAddOn);
    },
    onRebuyPerformed : function(player, addOnCost, chipsForAddOn){
        var seat = this.getSeatByPlayerId(player.id);
        seat.rebuyPerformed();
    },
    hideRebuyButtons : function(player) {
        var seat = this.getSeatByPlayerId(player.id);
        seat.hideRebuyButtons();
    },
    hideAddOnButton : function(player) {
        var seat = this.getSeatByPlayerId(player.id);
        seat.hideAddOnButton();
    },
    onLeaveTableSuccess : function() {
        $(this.tableView).hide();
        for(var i = 0; i<this.capacity; i++) {
            var s = $("#seat"+i+"-"+this.tableId);
            s.empty();
            s.attr("class","seat");
        }
        if(this.myPlayerSeatId != -1) {
            this.seats.get(this.myPlayerSeatId).clear();
        }
        this.myPlayerSeatId = -1;
        this._resetCommunity();
        var cards = this.cardElements.values();
        for(var x = 0; x<cards.length; x++) {
            $("#"+cards[x].getCardDivId()).remove();
        }
        this.myActionsManager.clear();
        this.soundManager.playerAction({id:"action-leave"}, this.tableId);
    },
    _hideSeatActionText : function() {
        var seats = this.seats.values();
        for(var s in seats) {
            seats[s].hideActionText();
        }
    },
    _resetSeats : function() {
        var seats = this.seats.values();
        for(var s in seats){
            seats[s].reset();
        }
    },
    _storeCard : function(card){
        this.cardElements.put(card.id,card);
    },
    _calculateSeatPositions : function() {
        //my player seat position should always be 0
        console.log("seat length on calculate = " + this.seats.size());
        var seats = this.seats.values();
        for(var s in seats){
            seats[s].setSeatPos(seats[s].seatId,this._getNormalizedSeatPosition(seats[s].seatId));
        }
        //do empty seats, question is if we want them or not, looked a bit empty without them
        for(var i = 0; i<this.capacity; i++){
            var seat = $("#seat"+i+"-"+this.tableId);
            if(seat.hasClass("seat-empty")){
                seat.removeClass("seat-pos-"+i).addClass("seat-inactive").addClass("seat-pos-"+this._getNormalizedSeatPosition(i));
            }
        }

    },
    _getNormalizedSeatPosition : function(seatId){
        if(this.myPlayerSeatId != -1) {
            return ( this.capacity + seatId - this.myPlayerSeatId ) % this.capacity;
        } else {
            return seatId;
        }
    },
    _resetCommunity : function() {
        this.communityCardsContainer.empty();
        this.mainPotContainer.empty();
        this.totalPotContainer.hide().find(".amount").empty();
    },
    _hideSeatActionInfo : function() {
        var seats = this.seats.values();
        for(var s = 0; s<seats.length; s++) {
            seats[s].hideActionInfo();
        }
    },
    _moveToPot : function() {
        var seats = this.seats.values();
        for(var s=0; s<seats.length; s++) {
            seats[s].moveAmountToPot(this.tableView, this.mainPotContainer);
        }
    },
    onPotToPlayerTransfers : function(transfers) {

        var transferAnimator = new Poker.PotTransferAnimator(this.tableId, this.animationManager, $("#seatContainer-"+this.tableId),
            this.mainPotContainer);

        for(var i = 0; i<transfers.length; i++) {
            var trans = transfers[i];
            if(trans.amount<=0) {
                continue;
            }
            var seat = this.getSeatByPlayerId(trans.playerId);
            seat.onPotWon(trans.potId,trans.amount);
            transferAnimator.addTransfer(seat, trans.potId, trans.amount);
            this.tableLog.appendPotTransfer(seat.player,trans.potId, trans.amount);
        }
        transferAnimator.start();
        this.playSound(Poker.Sounds.POT_TO_PLAYERS);
    },

    playSound : function(sound) {
        this.soundManager.handleTableUpdate(sound, this.tableId);
    },
    /**
     * @param {Poker.FutureActionType[]} actions
     */
    displayFutureActions : function(actions,callAmount,minBetAmount) {
        this.myActionsManager.displayFutureActions(actions,callAmount,minBetAmount);
    }
});
