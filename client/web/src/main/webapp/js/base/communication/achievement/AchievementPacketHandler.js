"use strict";
var Poker = Poker || {};

Poker.AchievementPacketHandler = Class.extend({
    tableId: null,
    init : function(tableId){
        this.tableId = tableId;
    },
    handleAchievementNotification : function(playerId, message) {
        var achievementManager = Poker.AppCtx.getAchievementManager();
        achievementManager.handleAchievement(this.tableId, playerId, JSON.parse(message));
    }

});