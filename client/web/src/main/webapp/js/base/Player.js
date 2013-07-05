"use strict";
var Poker = Poker || {};
/**
 * Holds the player states including id, name, tableStatus and cards
 * @type Poker.Player
 */
Poker.Player = Class.extend({
    name : null,
    id :-1,
    balance : 0,
    /**
     * @type Poker.PlayerTableStatus
     */
    tableStatus : null,
    away : false,
    sitOutNextHand : false,
    lastActionType : null,
    init : function(id,name) {
        this.name = name;
        this.id = id;
        this.tableStatus = Poker.PlayerTableStatus.SITTING_IN;
    }
});