"use strict";
var Poker = Poker || {};

Poker.LoginView = Poker.TabView.extend({
    init: function(viewElementId,name){
        this._super(viewElementId,name);
        var self = this;
        $("#loginButton").touchSafeClick(function(e){
            console.log("logging in");
            if ($('#user').val() != "username" && $('#pwd').val() != "Password") {
                var usr = $('#user').val();
                var pwd = $('#pwd').val();
                $.ga._trackEvent("user_navigation", "use_login_button", usr);
                Poker.AppCtx.getCommunicationManager().doLogin(usr,pwd);
            }
        });
        $("#user").keyup(function(e){
            self.handleKeyUp(e.keyCode);
        });
        $("#pwd").keyup(function(e){
            self.handleKeyUp(e.keyCode);
        });
    },
    handleKeyUp : function(keyCode) {
        if(keyCode == 13) {
            $("#loginButton").click();
        }
    }
});