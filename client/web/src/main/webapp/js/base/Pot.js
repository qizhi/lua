"use strict";
var Poker = Poker || {};

Poker.PotType = {
    MAIN : 1,
    SIDE : 2
};
/**
 * Representation of a pot, there are two types
 * of pots MAIN and SIDE
 *
 * @type {Poker.Pot}
 */
Poker.Pot = Class.extend({
    id : -1,
    type : null,
    amount : 0,
    /**
     * @constructor
     * @param id - the id of the pot
     * @param type - the Poker.PotType type of the pot
     * @param amount - the pot amount
     */
    init : function(id,type,amount) {
        this.id = id;
        this.type = type;
        this.amount = amount;

    }
});