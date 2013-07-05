"use strict";
var Poker = Poker || {};
Poker.AccountPageManager = Class.extend({
    templateManager : null,
    menuItemTemplate : null,
    activeView : null,
    userPanel : null,
    userOverlay : null,
    buyCreditsView : null,
    editProfileView : null,
    init : function() {
        this.templateManager = new Poker.TemplateManager();
        this.menuItemTemplate = "menuItemTemplate";
        this.userPanel = $(".user-panel");
        this.userOverlay = $(".user-overlay-container");
        this.setupUserPanel();
        var self = this;
        var vm =  Poker.AppCtx.getViewManager();

        $("#editProfileButton").click(function(e){
            self.closeAccountOverlay();
            if(self.editProfileView==null) {
                var url = Poker.OperatorConfig.getProfilePageUrl();
                self.editProfileView = new Poker.ExternalPageView(
                    "editProfileView","Edit Profile","C",self.addToken(url),function(){
                        vm.removeView(self.editProfileView);
                        self.editProfileView = null;
                    });
                self.editProfileView.fixedSizeView = true;
               vm.addView(self.editProfileView);

            }
            Poker.AppCtx.getViewManager().activateView(self.editProfileView);
        });
        $("#buyCreditsButton").click(function(e){
            self.closeAccountOverlay();
            if(self.buyCreditsView==null) {
                var url = Poker.OperatorConfig.getBuyCreditsUrl();
                self.buyCreditsView = new Poker.ExternalPageView(
                    "buyCreditsView","Buy credits","C",self.addToken(url),
                    function(){
                        vm.removeView(self.buyCreditsView);
                        self.buyCreditsView = null;
                    });
                self.buyCreditsView.fixedSizeView = true;
                Poker.AppCtx.getViewManager().addView(self.buyCreditsView);

            }
            Poker.AppCtx.getViewManager().activateView(self.buyCreditsView);

        });

        $(".logout-link").click(function() {
            self.closeAccountOverlay();
            self.logout();
        });

    },
    onLogin : function(playerId,name) {
        var self = this;
        $(".username").html(name);
        $(".user-id").html(playerId);
        if(Poker.MyPlayer.sessionToken!=null) {
            Poker.AppCtx.getPlayerApi().requestPlayerProfile(playerId,Poker.MyPlayer.sessionToken,
                function(profile){
                    console.log("PROFILE");
                    console.log(profile);
                    if (profile !=null && profile.externalAvatarUrl != null) {
                        $(".user-panel-avatar").addClass("user-panel-custom-avatar").css("backgroundImage","url('"+profile.externalAvatarUrl+"')");
                    } else {
                        self.displayDefaultAvatar(playerId);
                    }
                },
                function(){
                    self.displayDefaultAvatar(playerId);
                })
        } else {
            this.displayDefaultAvatar(playerId);
        }
    },
    displayDefaultAvatar : function(playerId){
        $(".user-panel-avatar").addClass("avatar" + (playerId % 9));
    },
    logout : function() {
        $.ga._trackEvent("user_navigation", "clicked_logout");
        Poker.AppCtx.getCommunicationManager().getConnector().logout(true);
        Poker.Utils.removeStoredUser();
        var logout_url = Poker.OperatorConfig.getLogoutUrl();
        if(!logout_url) {
            document.location.reload();
        } else {
            var dialogManager = Poker.AppCtx.getDialogManager();
            dialogManager.displayGenericDialog({
                container:  Poker.AppCtx.getViewManager().getActiveView().getViewElement(),
                header: i18n.t("account.logout"),
                message: i18n.t("account.logout-warning"),
                displayCancelButton: true
            }, function() {
                document.location = logout_url;
            });
        }
    },
    addToken : function(url) {
        return url + "?userSessionToken="+Poker.MyPlayer.sessionToken+"&playerId="+Poker.MyPlayer.id + "&skin=" + Poker.SkinConfiguration.name
            +"&operatorId=" + Poker.SkinConfiguration.operatorId + "&operatorAuthToken=" + Poker.MyPlayer.loginToken + "&r="+Math.random();
    },
    closeAccountOverlay : function() {
        this.userOverlay.hide();
        this.userPanel.removeClass("active");
    },
    openAccountOverlay : function() {
        this.userOverlay.show();
        this.userPanel.addClass("active");
        this.openAccountFrame();
    },
    setupUserPanel : function() {
        var self = this;
        this.userPanel.click(function(e){
            if(self.userOverlay.is(":visible")) {
                self.closeAccountOverlay();
                $(document).off("mouseup.account");
            } else {
                self.openAccountOverlay();
            }
            $(document).on("mouseup.account",function(e){
                if(self.userPanel.has(e.target).length === 0
                    && self.userOverlay.has(e.target).length === 0) {
                    self.closeAccountOverlay();
                    $(document).off("mouseup.account");
                }
            });
        });
    },
    openAccountFrame : function() {
        $.ga._trackEvent("user_navigation", "open_account_frame");
        var iframe = $("#accountIframe");
        var url = Poker.OperatorConfig.getAccountInfoUrl();
        iframe.attr("src",this.addToken(url));
    }
});

