"use strict";
var Poker = Poker || {};
/**
 * Handles all interactions and UI for a players seat
 * @type {Poker.Seat}
 */
Poker.Seat = Class.extend({
    /**
     * @type Poker.TemplateManager
     */
    templateManager: null,
    seatId: -1,
    player: null,
    seatElement: null,
    progressBarElement: null,
    cards: null,
    cardsContainer: null,
    avatarElement: null,
    actionAmount: null,
    actionText: null,
    handStrength: null,
    seatBalance: null,
    seatBase: null,
    animationManager: null,
    currentProgressBarAnimation: null,
    init: function(elementId, seatId, player, animationManager) {
        this.animationManager = animationManager;
        this.seatId = seatId;
        this.player = player;
        this.templateManager = Poker.AppCtx.getTemplateManager();
        this.seatElement = $("#" + elementId);
        this.renderSeat();
    },
    setSeatPos: function(previousPos, position) {
        this.seatElement.removeClass("seat-empty").removeClass("seat-pos-" + previousPos).removeClass("seat-inactive").addClass("seat-pos-" + position);
    },
    renderSeat: function() {
        var output = this.templateManager.render("seatTemplate", this.player);
        this.seatElement.html(output);
        this.progressBarElement = this.seatElement.find(".progress-bar");
        this.avatarElement = this.seatElement.find(".avatar");
        this.cardsContainer = this.seatElement.find(".cards-container");
        this.actionAmount = this.seatElement.find(".action-amount");
        this.actionText = this.seatElement.find(".action-text");
        this.seatBalance = this.seatElement.find(".seat-balance");
        this.handStrength = this.seatElement.find(".hand-strength");
        this.seatBase = this.seatElement.find(".avatar-base");

        this.reset();
    },
    updateAvatar : function(url) {
        if(url!=null) {
            this.avatarElement.css("backgroundImage","url('"+url+"')");
            this.avatarElement.addClass("custom-avatar");
        } else {
            this.avatarElement.addClass("avatar" + (this.player.id % 9));
        }


    },
    getDealerButtonOffsetElement: function() {
        return this.seatBase;
    },
    clearSeat: function() {
        this.seatElement.html("");
    },
    updatePlayer: function(player) {
        this.player = player;
        var balanceDiv = this.seatBalance;
        if (this.player.balance == 0) {
            balanceDiv.html("All in");
            balanceDiv.removeClass("balance");
        } else {
            balanceDiv.html(this.player.balance);
            balanceDiv.addClass("balance");
        }
        this.handlePlayerStatus();
    },
    updatePlayerStatus : function(player) {
        this.player = player;
        this.handlePlayerStatus();
    },
    handlePlayerStatus: function() {
        if (this.player.tableStatus == Poker.PlayerTableStatus.SITTING_OUT) {
            this.reset();
            this.seatElement.addClass("seat-sit-out");
            this.seatElement.find(".player-status").show().html(this.player.tableStatus.text);
        } else {
            this.seatElement.find(".player-status").html("").hide();
            this.seatElement.removeClass("seat-sit-out");
        }
    },
    reset: function() {
        this.hideActionInfo();
        this.handStrength.html("").removeClass("won").hide();
        this.clearProgressBar();
        if (this.cardsContainer) {
            this.cardsContainer.empty();
        }
        this.seatElement.removeClass("seat-folded");
        this.onReset();
    },
    onReset: function() {

    },
    hideActionInfo: function() {
        this.hideActionText();
        if (this.actionAmount != null) {
            this.actionAmount.html("");
        }
    },
    hideActionText: function() {
        this.actionText.html("").hide();
    },
    onAction: function(actionType, amount) {
        this.inactivateSeat();
        this.showActionData(actionType, amount);
        if (actionType == Poker.ActionType.FOLD) {
            this.fold();
        }
    },
    showActionData: function(actionType, amount) {
        this.actionText.html(actionType.text).show();
        var icon = $("<div/>").addClass("player-action-icon").addClass(actionType.id + "-icon");
        if (amount!="0") {
            this.actionAmount.removeClass("placed");
            this.actionAmount.empty().append($("<span/>").append(amount));
            this.actionAmount.append(icon).show();
            this.animateActionAmount();
        }
    },
    animateActionAmount: function() {
        new Poker.CSSClassAnimation(this.actionAmount).addClass("placed").start(this.animationManager);
    },
    fold: function() {
        this.seatElement.addClass("seat-folded");
        this.seatElement.removeClass("active-seat");
        this.seatElement.find(".player-card-container img").attr("src", contextPath + "/skins/" + Poker.SkinConfiguration.name + "/images/cards/gray-back.svg");
    },
    dealCard: function(card) {
        this.cardsContainer.append(card.render());
        this.onCardDealt(card);
    },
    onCardDealt: function(card) {
        var div = card.getJQElement();
        //animate deal card
        new Poker.CSSClassAnimation(div).addClass("dealt").start(this.animationManager);

    },
    inactivateSeat: function() {
        this.seatElement.removeClass("active-seat");
        this.clearProgressBar();
    },
    clearProgressBar: function() {
        if (this.progressBarElement) {
            this.progressBarElement.attr("style", "").hide();
        }
        if (this.currentProgressBarAnimation != null) {
            this.animationManager.removeAnimation(this.currentProgressBarAnimation);
            this.currentProgressBarAnimation = null;
        }
    },
    /**
     * When a betting round is complete (community cards are dealt/shown);
     */
   onBettingRoundComplete : function(){
       this.inactivateSeat();
   },
   activateSeat : function(allowedActions, timeToAct,mainPot,fixedLimit) {
       this.seatElement.addClass("active-seat");
       this.progressBarElement.show();
       this.currentProgressBarAnimation = new Poker.TransformAnimation(this.progressBarElement)
           .addTransition("transform",timeToAct/1000,"linear")
           .addScale3d(1,0.01,1).addOrigin("bottom")
           .setTimed(true)
           .start(this.animationManager);
    },
    rebuyRequested: function(rebuyCost, chipsForRebuy, timeToAct) {
        this.showTimer(timeToAct);
    },
    addOnRequested: function(addOnCost, chipsForAddOn) {
        // Nothing to do.
    },
    hideRebuyButtons: function() {
        // Nothing to do.
    },
    hideAddOnButton: function() {
        // Nothing to do.
    },
    rebuyPerformed: function() {
        this.clearProgressBar();
    },
    showTimer: function(timeToAct) {
        this.seatElement.addClass("active-seat");
        this.progressBarElement.show();
        this.currentProgressBarAnimation = new Poker.TransformAnimation(this.progressBarElement)
                .addTransition("transform", timeToAct / 1000, "linear")
                .addScale3d(1, 0.01, 1).addOrigin("bottom")
                .setTimed(true)
                .start(this.animationManager);
    },
    showHandStrength: function(hand) {
        this.actionAmount.html("");
        this.actionText.html("").hide();
        if (hand.id != Poker.Hand.UNKNOWN.id) {
            this.handStrength.visible = true;
            this.handStrength.html(hand.text).show();
        }

    },
    clear: function() {

    },
    moveAmountToPot: function(view, mainPotContainer) {
        this.hideActionInfo();
        return;
        //before enabling animations for bet amounts going into the pot we need a better
        //handling of animations
        var self = this;
        var amount = this.actionAmount.get(0);
        var pos = self.calculatePotOffset(view, mainPotContainer);

        this.moveToPotComplete = false;
        new Poker.TransformAnimation(amount).
                addTranslate3d(pos.left, pos.top, 0, "px").
                addCallback(function() {
                    self.onMoveToPotEnd();
                });
    },
    moveToPotComplete: true,
    onMoveToPotEnd: function() {
        if (this.moveToPotComplete == false) {
            this.moveToPotComplete = true;
            this.hideActionInfo();
            this.actionAmount.attr("style", "");
        }
    },
    calculatePotOffset: function(view, mainPotContainer) {
        var width = view.width();
        var height = view.height();
        var amountOffset = this.actionAmount.offset();
        var mainPotOffset = mainPotContainer.offset();
        var left = mainPotOffset.left - amountOffset.left;
        var top = mainPotOffset.top - amountOffset.top;
        return { top: Math.round(top) + "px", left: Math.round(left) + "px" };
    },
    isMySeat: function() {
        return false;
    },
    onPotWon: function(potId, amount) {
        this.handStrength.addClass("won");
        this.hideActionInfo();
    }
});