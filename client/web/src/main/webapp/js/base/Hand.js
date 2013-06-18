"use strict";
var Poker = Poker || {};

/**
 * The poker hands that a player can get
 * @type {Poker.Hand}
 */
Poker.Hand = {
    UNKNOWN : { text: "Unknown", id : 0},
    HIGH_CARD : { text: "High card", id : 1},
    PAIR : { text: "Pair", id : 2},
    TWO_PAIRS : { text :"Two Pairs", id : 3},
    THREE_OF_A_KIND : { text : "Three of a kind",id : 4},
    STRAIGHT : {text : "Straight",id : 5},
    FLUSH : { text : "Flush",id : 6},
    FULL_HOUSE : {text : "Full house",id : 7},
    FOUR_OF_A_KIND : {text : "Four of a kind",id : 8},
    STRAIGHT_FLUSH : { text : "Straight Flush",id : 9},
    ROYAL_STRAIGHT_FLUSH : { text : "Royal straight flush",id : 10},

    fromName : function(name) {
        for(var x in Poker.Hand) {
            if(x == name) {
                return Poker.Hand[x];
            }
        }
        return null;
    },
    fromId : function(id) {
        for(var x in Poker.Hand) {
            if(Poker.Hand[x].id && Poker.Hand[x].id == id) {
                  return Poker.Hand[x];
            }
        }
        return null;
    }
};