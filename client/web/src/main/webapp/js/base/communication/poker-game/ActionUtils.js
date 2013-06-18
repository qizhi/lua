"use strict";
var Poker = Poker || {};


Poker.ActionUtils = Class.extend({
    init : function() {
    },
    /**
     *
     * @param {Number} actType
     * @return {Poker.ActionType}
     */
    getActionType : function(actType){
        var type = null;
        switch (actType) {
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK:
                type = Poker.ActionType.CHECK;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL:
                type = Poker.ActionType.CALL;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET:
                type = Poker.ActionType.BET;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE:
                type = Poker.ActionType.RAISE;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD:
                type = Poker.ActionType.FOLD;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE:
                type = Poker.ActionType.ANTE;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND:
                type = Poker.ActionType.SMALL_BLIND;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND:
                type = Poker.ActionType.BIG_BLIND;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET:
                type = Poker.ActionType.DECLINE_ENTRY_BET;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND:
                type = Poker.ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND:
                type = Poker.ActionType.DEAD_SMALL_BLIND;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.ENTRY_BET:
                type = Poker.ActionType.ENTRY_BET;
                break;
            case com.cubeia.games.poker.io.protocol.ActionTypeEnum.WAIT_FOR_BIG_BLIND:
                type = Poker.ActionType.WAIT_FOR_BIG_BLIND;
                break;
            default:
                console.log("Unhandled ActionTypeEnum " + actType);
                break;
        }
        return type;
    },

    /**
     *
     * @param {com.cubeia.games.poker.io.protocol.PlayerAction} act
     * @return {Poker.Action}
     */
    getAction : function(act) {

        var type = this.getActionType(act.type);
        return new Poker.Action(type,parseFloat(act.minAmount), parseFloat(act.maxAmount));
    },
    getPokerActions : function(allowedActions){
        var actions = [];
        for(var a in allowedActions) {
            var ac = this.getAction(allowedActions[a]);
            if(ac!=null) {
                actions.push(ac);
            }
        }
        return actions;
    },

    /**
     *
     * @param {Number} tableId
     * @param {Number} seq
     * @param {Number} actionType
     * @param {Number} betAmount
     * @param {Number}raiseAmount
     * @return {com.cubeia.games.poker.io.protocol.PerformAction}
     */
    getPlayerAction : function(tableId, seq, actionType, betAmount, raiseAmount) {

        var performAction = new com.cubeia.games.poker.io.protocol.PerformAction();
        performAction.player = Poker.MyPlayer.id;
        performAction.action = new com.cubeia.games.poker.io.protocol.PlayerAction();
        performAction.action.type = actionType;
        performAction.action.minAmount = "0";
        performAction.action.maxAmount = "0";
        performAction.betAmount = ""+ betAmount;
        performAction.raiseAmount = "" + (raiseAmount || 0);
        performAction.timeOut = 0;
        performAction.seq = seq;
        performAction.cardsToDiscard = [];
        return performAction;
    },
    /**
     *
     * @param actionType
     * @return {Number}
     */
    getActionEnumType : function (actionType) {
        switch (actionType.id) {
            case Poker.ActionType.ANTE.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE;
            case Poker.ActionType.SMALL_BLIND.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND;
            case Poker.ActionType.BIG_BLIND.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND;
            case Poker.ActionType.CALL.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL;
            case Poker.ActionType.CHECK.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK;
            case Poker.ActionType.BET.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET;
            case Poker.ActionType.RAISE.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE;
            case Poker.ActionType.FOLD.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD;
            case Poker.ActionType.DECLINE_ENTRY_BET.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET;
            case Poker.ActionType.DEAD_SMALL_BLIND.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND;
            case Poker.ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
            case Poker.ActionType.ENTRY_BET.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ENTRY_BET;
            case Poker.ActionType.WAIT_FOR_BIG_BLIND.id:
                return com.cubeia.games.poker.io.protocol.ActionTypeEnum.WAIT_FOR_BIG_BLIND;
            default:
                console.log("Unhandled action " + actionType.text);
                return null;

        }
    },
    getActionByType : function(actionType,actions) {
        for(var i = 0; i<actions.length; i++) {
            if(actions[i].type.id == actionType.id) {
                return actions[i];
            }
        }
        return null;
    }

});
Poker.ActionUtils = new Poker.ActionUtils();
