"use strict";
var Poker = Poker || {};

/**
 * Action that a player does, such as Call, Raise etc.
 * @type {Poker.Action}
 */
Poker.Action = Class.extend({

    /**
     * @type Poker.ActionType
     */
    type : null,
    minAmount : 0,
    maxAmount : 0,

    /**
     *
     * @param {Poker.ActionType} type
     * @param {Number} minAmount
     * @param {Number} maxAmount
     */
    init : function(type,minAmount,maxAmount){
        this.type = type;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }
});

Poker.ActionType = {
    CALL : {
        text : "Call",
        id : "action-call"
    },
    CHECK : {
        text : "Check",
        id : "action-check"
    },
    FOLD : {
        text : "Fold",
        id : "action-fold"
    },
    BET : {
        text : "Bet",
        id : "action-bet"
    },
    RAISE : {
        text : "Raise",
        id : "action-raise"
    },
    SMALL_BLIND : {
        text : "Small Blind",
        id : "action-small-blind"
    },
    BIG_BLIND : {
        text : "Big Blind",
        id : "action-big-blind"
    },
    JOIN :{
        text : "Join",
        id : "action-join"
    },
    LEAVE : {
        text : "leave",
        id : "action-leave"
    },
    SIT_OUT : {
        text : "Sit-out",
        id : "action-sit-out"
    },
    SIT_IN : {
        text : "Sit-in",
        id : "action-sit-in"
    },
    ENTRY_BET : {
        text : "Big Blind",
        id : "entry-bet"
    },
    DECLINE_ENTRY_BET : {
        text : "Decline Blind",
        id : "decline-entry-bet"
    },
    WAIT_FOR_BIG_BLIND : {
        text : "Waiting",
        id : "wait-for-big-blind"
    },
    ANTE : {
        text : "Ante",
        id : "ante"
    },
    BIG_BLIND_PLUS_DEAD_SMALL_BLIND : {
        text : "BB + Dead Small",
        id : "big-and-small-blind"
    },
    DEAD_SMALL_BLIND : {
        text : "Dead Small",
        id : "dead-small-blind"
    },
    REBUY : {
        text : "Rebuy",
        id : "rebuy"
    },
    DECLINE_REBUY : {
        text : "Decline Rebuy",
        id : "decline-rebuy"
    },
    ADD_ON : {
        text : "Add-on",
        id : "add-on"
    }


};