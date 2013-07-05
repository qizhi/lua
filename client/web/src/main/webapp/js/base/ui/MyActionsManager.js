"use strict";
var Poker = Poker || {};
/**
 * Handles the displaying of user buttons,
 * handles what shows when
 * @type {Poker.MyActionsManager}
 */
Poker.MyActionsManager  = Class.extend({
    /**
     * @type {Poker.ActionButtons}
     */
    actionButtons : null,

    /**
     * @type {Poker.TableButtons}
     */
    tableButtons : null,

    /**
     * @type {Poker.Action[]}
     */
    currentActions : null,

    /**
     * @type {Poker.BetSlider}
     */
    slider : null,

    /**
     * @type {Number}
     */
    tableId : null,

    /**
     * @type {Number}
     */
    bigBlind : 0,

    currency : null,

    actionCallback : null,

    /**
     * @type {Poker.CheckboxAction}
     */
    sitOutNextHand : null,

    /**
     * @type {Poker.BlindsActions}
     */
    blindsActions : null,

    /**
     * @type {Poker.FutureActions}
     */
    futureActions : null,

    userActionsContainer : null,

    /**
     * @constructor
     * @param view
     * @param tableId
     * @param actionCallback
     */
    init : function(view,tableId, actionCallback) {
        var self = this;
        this.actionCallback = actionCallback;
        this.tableId = tableId;
        var tournamentTable = Poker.AppCtx.getTournamentManager().isTournamentTable(tableId);
        this.tableButtons = new Poker.TableButtons(view,actionCallback,tournamentTable);
        this.currentActions = [];
        this.userActionsContainer = $(".user-actions",view);
        this.futureActions = new Poker.FutureActions($(".future-actions",view));
        this.blindsActions = new Poker.BlindsActions(view,tableId,actionCallback);

        var betCallback = function(minAmount,maxAmount,mainPot){
            self.onClickBetButton(minAmount,maxAmount,mainPot);
        };

        var raiseCallback = function(minAmount,maxAmount,mainPot) {
            self.onClickRaiseButton(minAmount,maxAmount,mainPot);
        };
        var amountFunc = function(){return self.slider.getValue();};

        this.actionButtons = new Poker.ActionButtons(view,actionCallback,
            raiseCallback,betCallback, amountFunc, function(){
                self.onClickCancelButton();
            });

        this.sitOutNextHand =  new Poker.CheckboxAction(view,".sit-out-next-hand",false);

        this.sitOutNextHand.onChange(function(enabled){
            var requestHandler = new Poker.PokerRequestHandler(self.tableId);
            if (enabled == true) {
                requestHandler.sitOut();
            } else {
                self.onRequestToSitIn();
                requestHandler.sitIn();
            }
        });
        if(tournamentTable==true) {
            this.sitOutNextHand.hide();
        }

        this.onWatchingTable();

    },
    onRequestToSitIn : function() {
        this.tableButtons.hide(Poker.ActionType.SIT_IN);
        this.blindsActions.noMoreBlinds.setEnabled(false);
        this.sitOutNextHand.setEnabled(false);
    },
    setNoMoreBlinds : function(enabled) {
        console.log("setting no more blinds = " + enabled);
        this.blindsActions.noMoreBlinds.setEnabled(enabled);
    },
    onClickBetButton : function(minAmount,maxAmount,mainPot) {
        this.handleBetSliderButtons(minAmount,maxAmount,mainPot);
        this.actionButtons.doBetActionButton.show();
    },
    onClickRaiseButton : function(minAmount,maxAmount,mainPot) {
        this.handleBetSliderButtons(minAmount,maxAmount,mainPot);
        this.actionButtons.doRaiseActionButton.show();
    },
    handleBetSliderButtons : function(minAmount,maxAmount,mainPot) {
        this.hideActionElements();
        this.actionButtons.cancelBetActionButton.show();
        this.showSlider(minAmount,maxAmount,mainPot);
    },
    onClickCancelButton : function() {
        this.hideActionElements();
        this.showActionButtons(this.currentActions);
        this.actionButtons.doBetActionButton.hide();
        this.actionButtons.cancelBetActionButton.hide();
        this.hideSlider();
    },
    onSatDown : function() {
        this.hideActionElements();
        this.sitOutNextHand.show();
        this.blindsActions.onSatDown();
        this.futureActions.hide();
        this.showWaitForBigBlind();
        this.tableButtons.hideAll();
        this.tableButtons.show(Poker.ActionType.LEAVE);
    },
    hideSlider : function() {
        if (this.slider) {
            this.slider.hide();
        }
    },
    setBigBlind : function(bigBlind,currency) {

        console.log("SETTING BB");
        this.bigBlind = bigBlind;
        if(typeof(currency)!="undefined") {
            console.log("setting currency ", this.currency);
            this.currency = currency;
        }
    },
    getMinSliderStep : function() {
        return (1/Math.pow(10,this.currency.fractionalDigits));
    },
    showSlider : function(minAmount,maxAmount,mainPot) {
        var self = this;
        this.slider = new Poker.BetSlider(this.tableId,"betSlider", function(){
            self.actionButtons.betOrRaise();
        }, this.getMinSliderStep());
        this.slider.clear();

        this.slider.setMinBet(minAmount);
        this.slider.setMaxBet(maxAmount);
        this.slider.setBigBlind(this.bigBlind);
        this.slider.addMarker("Min", minAmount);
        this.slider.addMarker("All in", maxAmount);
        this.slider.addMarker("Pot",mainPot);
        this.slider.draw();
    },
    showWaitForBigBlind : function() {
        this.blindsActions.waitForBigBlind.show();
    },
    hideWaitForBigBlind : function() {
        this.blindsActions.waitForBigBlind.hide();
    },
    /**
     * Called when the user is required to act (certain actions, are handled automatically)
     *
     * @param {Poker.Action[]} actions
     * @param {Number} mainPot
     * @param {Boolean} fixedLimit
     * @param {CircularProgressBar} progressBar
     * @return {Boolean} whether the action was handled automatically
     */
    onRequestPlayerAction : function(actions, mainPot, fixedLimit, progressBar){

        if (this.blindsActions.handleBlindsAndEntryBet(actions)) {
            return true;
        }

        this.blindsActions.entryBetPosted();

        this.currentActions = actions;

        this.hideActionElements();

        var fromFutureAction = this.futureActions.getAction(actions);
        this.futureActions.clear();
        this.futureActions.hide();

        if (fromFutureAction!=null) {
            this.actionCallback(fromFutureAction.type,fromFutureAction.minAmount);
            return true;
        }
        var self = this;

        //to avoid users clicking the action buttons by mistake
        setTimeout(function(){
            self.showActionButtons(actions, mainPot, fixedLimit);
            progressBar.show();
            progressBar.render();
        }, 500);
        return false;
    },
    showActionButtons : function(actions, mainPot, fixedLimit) {
        this.userActionsContainer.show();
        this.actionButtons.showButtons(actions, mainPot, fixedLimit);
    },
    showRebuyButtons : function(rebuyCost, chipsForRebuy) {
        this.actionButtons.showRebuyButtons();
    },
    hideRebuyButtons : function() {
        this.actionButtons.hideRebuyButtons();
    },
    showAddOnButton : function(addOnCost, chipsForAddOn) {
        this.actionButtons.showAddOnButton();
    },
    hideAddOnButton : function() {
        this.actionButtons.hideAddOnButton();
    },
    onStartHand : function() {
        this.futureActions.clear();
        this.futureActions.hide();
        $.ga._trackEvent("poker_table", "start_hand_participate");
    },
    onTournamentOut : function(){
        this.tableButtons.hideAll();
        this.hideActionElements();
        this.display(Poker.ActionType.LEAVE);
    },
    onSitIn : function() {
        this.hideActionElements();
        this.sitOutNextHand.setEnabled(false);
        this.sitOutNextHand.show();
        this.blindsActions.onSitIn();
        this.futureActions.hide();
        this.tableButtons.hideAll();
        this.tableButtons.show(Poker.ActionType.LEAVE);
    },
    setSitOutNextHand : function(sitOut) {
        console.log("setSitOutNextHand = " + sitOut);
        this.sitOutNextHand.setEnabled(sitOut);
    },
    onSitOut : function() {
        console.log("ON SIT OUT");
        this.blindsActions.onSitOut();
        this.tableButtons.hideAll();
        this.sitOutNextHand.setEnabled(true);
        this.hideActionElements();
        this.userActionsContainer.show();
        this.display(Poker.ActionType.LEAVE);
        this.display(Poker.ActionType.SIT_IN);
    },
    onWatchingTable : function() {
        this.hideActionElements();
        this.tableButtons.hideAll();
        this.futureActions.hide();
        this.sitOutNextHand.hide();
        this.blindsActions.onWatchingTable();
        this.display(Poker.ActionType.JOIN);
        this.display(Poker.ActionType.LEAVE);
    },
    clear : function() {
        this.tableButtons.clear();
        this.actionButtons.clear();
    },
    onFold : function() {
      this.futureActions.hide();
      this.blindsActions.entryBetPosted();
      this.hideActionElements();
    },
    display : function(actionType) {
        if(this.actionButtons.contains(actionType)) {
            this.actionButtons.show(actionType);
        } else {
            this.tableButtons.show(actionType);
        }
    },
    hideActionElements : function() {
        this.actionButtons.hideAll();
        this.hideSlider();

    },
    /**
     * @param {Poker.FutureActionType[]} actions
     */
    displayFutureActions : function(actions, callAmount, minBetAmount) {
        console.log("DISPLAY FUTURE ACTIONS");
        console.log(actions);
        this.futureActions.setFutureActions(actions,callAmount,minBetAmount);
        this.blindsActions.entryBetPosted();
        this.userActionsContainer.hide();
    }
});
