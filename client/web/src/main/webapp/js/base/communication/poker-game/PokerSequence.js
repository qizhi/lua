"use strict";
var Poker = Poker || {};


Poker.PokerSequence = Class.extend({
    sequences : null,
    init : function() {
        this.sequences  = new Poker.Map();
    },
    setSequence : function(tableId,seq) {
        this.sequences.put(tableId,seq);
    },
    getSequence : function(tableId) {
        return this.sequences.get(tableId);
    }
});
Poker.PokerSequence = new Poker.PokerSequence();
