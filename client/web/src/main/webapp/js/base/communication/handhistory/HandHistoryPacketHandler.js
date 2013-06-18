"use strict";
var Poker = Poker || {};
Poker.ServicePacketHandler = Class.extend({
    /**
     * @type {Poker.HandHistoryManager}
     */
    handHistoryManager : null,
    init: function () {
        this.handHistoryManager = Poker.AppCtx.getHandHistoryManager();
    },

    handleHandIds : function(tableId, handIds){
    },

    handleHandSummaries : function(tableId, handSummaries) {
        var jsonData = JSON.parse(handSummaries);
        this.handHistoryManager.showHandSummaries(tableId, jsonData);
    },

    handleHands : function(tableId, hands) {
    },

    handleHand : function(hand) {
        var jsonData = JSON.parse(hand);
        this.handHistoryManager.showHand(jsonData[0]);
    }
});