"use strict";
var Poker = Poker || {};

Poker.BlindsActions = Class.extend({
    tableId : -1,
    /**
     * @type {Poker.CheckboxAction}
     */
    noMoreBlinds : null,
    /**
     * @type {Poker.CheckboxAction}
     */
    waitForBigBlind : null,

    actionCallback : null,
    /**
     * @constructor
     * @param view
     * @param tableId
     * @param actionCallback
     */
    init : function(view,tableId,actionCallback) {
        this.actionCallback = actionCallback;
        this.noMoreBlinds = new Poker.CheckboxAction(view,".no-more-blinds",false);
        this.waitForBigBlind = new Poker.CheckboxAction(view,".wait-for-big-blind",true);
        this.tableId = tableId;
    },

    handleBlindsAndEntryBet : function(allowedActions) {
        return this.handleBlinds(allowedActions) || this.handleEntryBigBlind(allowedActions);
    },
    onSatDown : function() {
        this.noMoreBlinds.show();
        this.waitForBigBlind.show();
    },
    onWatchingTable : function() {
        this.noMoreBlinds.hide();
        this.waitForBigBlind.hide();
    },
    onSitIn : function() {
        this.noMoreBlinds.show();
    },
    onSitOut : function() {
        this.noMoreBlinds.setEnabled(false);
        this.waitForBigBlind.hide();
    },
    entryBetPosted : function() {
        this.noMoreBlinds.show();
        this.waitForBigBlind.hide();
        this.waitForBigBlind.setEnabled(true);
    },
    hide : function() {
        this.noMoreBlinds.hide();
        this.waitForBigBlind.hide();
    },
    show : function() {
        this.noMoreBlinds.show();
        this.waitForBigBlind.show();
    },

    /**
     * @param {Poker.Action[]} allowedActions
     * @return {Boolean} whether the request action was a blind or not
     */
    handleBlinds : function(allowedActions) {
        var requestHandler = this.getRequestHandler();
        for (var i = 0; i < allowedActions.length; i++) {
            var action = allowedActions[i];
            //move to my actions manager
            if (action.type == Poker.ActionType.BIG_BLIND || action.type == Poker.ActionType.SMALL_BLIND ||
                action.type == Poker.ActionType.DEAD_SMALL_BLIND || action.type == Poker.ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND ||
                action.type == Poker.ActionType.ANTE) {
                console.log("No more blinds? = " + this.noMoreBlinds.isEnabled());
                if (this.noMoreBlinds.isEnabled()) {
                    requestHandler.onMyPlayerAction(Poker.ActionType.DECLINE_ENTRY_BET, 0);
                } else {
                    console.log("Auto posting blind of type: ");
                    console.log(action.type);
                    requestHandler.onMyPlayerAction(action.type, action.minAmount);
                }
                return true;
            }
        }
        return false;
    },
    /**
     * @return {Poker.PokerRequestHandler}
     */
    getRequestHandler : function() {
        return new Poker.PokerRequestHandler(this.tableId);
    },
    getEntryBetAction : function(actions) {
        return Poker.ActionUtils.getActionByType(Poker.ActionType.ENTRY_BET,actions);
    },
    containsWaitForBigBlind : function(actions) {
        return Poker.ActionUtils.getActionByType(Poker.ActionType.WAIT_FOR_BIG_BLIND,actions)!=null;
    },
    handleEntryBigBlind : function(actions) {
        console.log("Handle entry bet ");
        console.log(actions);
        var entryBetAction = this.getEntryBetAction(actions);
        if(this.containsWaitForBigBlind(actions) && this.waitForBigBlind.isEnabled()) {
            this.actionCallback(Poker.ActionType.WAIT_FOR_BIG_BLIND, 0);
            this.waitForBigBlind.show();
            return true;
        } else if(entryBetAction!=null) {
            console.log("Not waiting for big blind, posting entry bet");
            this.actionCallback(Poker.ActionType.ENTRY_BET, entryBetAction.minAmount);
            this.entryBetPosted();
            return true;
        }
        return false;
    }
});