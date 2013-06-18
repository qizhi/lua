"use strict";
var Poker = Poker || {};
Poker.SoundSettingsView = Poker.View.extend({
    init : function(viewElementId,name) {
        this._super(viewElementId,name);
    },
    activate : function() {
        this._super();
        Poker.Settings.bindSettingToggle($("#soundEnabled"),Poker.Settings.Param.SOUND_ENABLED);
        $.ga._trackEvent("user_navigation", "open_sfx_page");
    },
    deactivate : function() {
        this._super();
        $("#soundEnabled").unbind();
        $.ga._trackEvent("user_navigation", "close_sfx_page");
    }
});