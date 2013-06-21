"use strict";
var Poker = Poker || {};

/**
 * Handles the UI for the logged in player
 *
 * @extends {Poker.Seat}
 * @type {Poker.MyPlayerSeat}
 */
Poker.MyPlayerSeat = Poker.Seat.extend({

    /**
     * @type Poker.MyActionsManager
     */
    myActionsManager : null,

    /**
     * @type CircularProgressBar
     */
    circularProgressBar : null,

    /**
     * @type Number
     */
    tableId : null,

    seatBalance : null,

    avatarElement : null,

    infoElement : null,

    init : function(tableId,elementId, seatId, player, myActionsManager, animationManager) {
        this._super(elementId,seatId, player,animationManager);
        this.tableId = tableId;
        this.myActionsManager = myActionsManager;
        this.seatElement = $("#"+elementId);
        this.renderSeat();
        this.infoElement = $("#"+elementId+"Info").show();
        this.circularProgressBar = new CircularProgressBar("#"+elementId+"Progressbar",this.animationManager);
        this.circularProgressBar.hide();
        this.seatBalance = this.seatElement.find(".seat-balance");
        this.myActionsManager.onSatDown();
    },
    setSeatPos : function(prev,pos) {
        //do nothing
    },
    renderSeat : function(){
        var output = this.templateManager.render("myPlayerSeatTemplate",this.player);
        this.seatElement.html(output);
        this.cardsContainer = this.seatElement.find(".cards-container");
        this.actionAmount = this.seatElement.find(".action-amount");
        this.actionText = this.seatElement.find(".action-text");
        this.handStrength = this.seatElement.find(".hand-strength");
        this.avatarElement = this.seatElement.find(".avatar");

        this.reset();
        $("#myPlayerName-"+this.tableId).html(this.player.name);
    },
    activateSeat : function(allowedActions, timeToAct,mainPot,fixedLimit) {
        console.log("ON REQUEST ACTION FOR table = " + this.tableId);
        this.showTimer(timeToAct);
        this.myActionsManager.onRequestPlayerAction(allowedActions, mainPot, fixedLimit, this.circularProgressBar);
        Poker.AppCtx.getViewManager().requestTableFocus(this.tableId);
    },
    rebuyRequested : function(rebuyCost, chipsForRebuy, timeToAct) {
        console.log("Showing rebuy timer for " + timeToAct + " millis");
        this.showTimer(timeToAct);
        this.circularProgressBar.show();
        this.circularProgressBar.render();
        this.myActionsManager.showRebuyButtons(rebuyCost, chipsForRebuy);
    },
    addOnRequested : function(addOnCost, chipsForAddOn) {
        this.myActionsManager.showAddOnButton(addOnCost, chipsForAddOn);
    },
    hideRebuyButtons : function() {
        this.myActionsManager.hideRebuyButtons();
        this.circularProgressBar.hide();
    },
    hideAddOnButton : function() {
        this.myActionsManager.hideAddOnButton();
    },
    showTimer: function(timeToAct) {
        if (this.circularProgressBar != null) {
            this.circularProgressBar.detach();
        }
        this.circularProgressBar = new CircularProgressBar("#" + this.seatElement.attr("id") + "Progressbar", this.animationManager);
        this.circularProgressBar.setTime(timeToAct);
    },
    onAction : function(actionType,amount){
        this.running = false;
        this.circularProgressBar.hide();
        this.showActionData(actionType,amount);
        this.myActionsManager.hideActionElements();
        this.clearProgressBar();
        if(actionType.id == Poker.ActionType.FOLD.id) {
            this.fold();
            Poker.AppCtx.getViewManager().updateTableInfo(this.tableId,{});
        } else if(actionType.id == Poker.ActionType.SIT_IN.id) {

        }
    },
    clearSeat : function() {
        this.seatElement.html("");
        $("#myPlayerBalance-"+this.tableId).html("");
        $("#myPlayerName-"+this.tableId).html("");
        this.myActionsManager.onWatchingTable();
        this.infoElement.hide();
    },
    showHandStrength : function(hand) {
        if(hand.id != Poker.Hand.UNKNOWN.id) {
            this.handStrength.visible = true;
            this.handStrength.html(hand.text).show();
        }
    },
    updatePlayer : function(player) {
        console.log("UPDATE MY PLAYER ");
        console.log(player);
        var updated = false;
        if(player.tableStatus.id != this.player.tableStatus.id || player.away != this.player.away ||
            player.sitOutNextHand != this.player.sitOutNextHand) {
            updated = true;
        }
        this.player = player;
        $("#myPlayerBalance-"+this.tableId).html(this.player.balance);
        this.seatBalance.html(this.player.balance);

        if (updated) {
            this.handlePlayerStatus();
        }
    },
    updatePlayerStatus : function(player) {
        this.player = player;
        this.handlePlayerStatus();
    },
    handlePlayerStatus : function() {
        console.log("player status update");
        console.log(this.player);
        if(this.player.tableStatus == Poker.PlayerTableStatus.SITTING_OUT) {
            this.seatElement.addClass("seat-sit-out");
            this.seatElement.find(".player-status").show().html(this.player.tableStatus.text);
            this.myActionsManager.onSitOut();
        } else if (this.player.tableStatus == Poker.PlayerTableStatus.TOURNAMENT_OUT){
            this.myActionsManager.onTournamentOut();
        } else if(this.player.tableStatus == Poker.PlayerTableStatus.SITTING_IN){
            this.seatElement.find(".player-status").html("").hide();
            this.seatElement.removeClass("seat-sit-out");
            if(this.player.away == true || this.player.sitOutNextHand == true) {
                this.myActionsManager.setSitOutNextHand(true);
            } else {
                this.myActionsManager.onSitIn();
            }
        }
    },
    hideActionText : function() {
        this.actionText.html("").hide();
    },
    onCardDealt : function(card) {
        var div = card.getJQElement();
        new Poker.CSSClassAnimation(div).addClass("dealt").start(this.animationManager);
        Poker.AppCtx.getViewManager().updateTableInfo(this.tableId,{card:card});
    },
    onReset : function() {
        Poker.AppCtx.getViewManager().updateTableInfo(this.tableId,{});
    },
    fold : function() {

        this.seatElement.addClass("seat-folded");
        this.seatElement.find(".player-card-container").addClass("seat-folded");
        this.myActionsManager.onFold();
        this.handStrength.visible = false;
        if(this.player.tableStatus == Poker.PlayerTableStatus.SITTING_OUT) {
            this.hideActionText();
        }

    },
    clear : function() {
        this.seatElement.empty();
        $("#myPlayer-"+this.tableId).hide();
        this.circularProgressBar.detach();
    },
    getDealerButtonOffsetElement : function() {
        return this.cardsContainer;
    },
    isMySeat : function() {
        return true;
    }
});
