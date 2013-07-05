"use strict";
var Poker = Poker || {};
Poker.DevSettingsView = Poker.View.extend({
    init : function(viewElementId,name) {
        this._super(viewElementId,name);
    },
    onViewActivated : function() {
        Poker.Settings.bindSettingToggle($("#swipeEnabled"),Poker.Settings.Param.SWIPE_ENABLED);
        Poker.Settings.bindSettingToggle($("#freezeComEnabled"),Poker.Settings.Param.FREEZE_COMMUNICATION);
    },
    onDeactivateView : function() {
        $("#swipeEnabled").unbind();
        $("#freezeComEnabled").unbind();
    }
});