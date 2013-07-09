"use strict";
var Poker = Poker || {};

Poker.AchievementManager = Class.extend({

    init : function() {

    },
    handleAchievement : function(tableId, playerId, message) {
        console.log("player " + playerId + " received", message);
        if(message.type=="achievement" && playerId == Poker.MyPlayer.id) {
            var n = new Poker.TextNotifcation(message.achievement.name + ' ' + i18n.t("achievement.completed"),message.achievement.description,message.achievement.imageUrl);
            Poker.AppCtx.getNotificationsManager().notify(n);
        }
    }
});