"use strict";
var Poker = Poker || {};

/**
 *
 * @type {Poker.TournamentManager}
 */
Poker.TournamentLayoutManager = Class.extend({
    viewContainer : null,
    tournamentId :-1,

    /**
     * @type Poker.TemplateManager
     */
    templateManager : null,

    viewElement : null,
    playerListBody : null,
    registerButton : null,
    unregisterButton : null,
    loadingButton : null,
    leaveButton : null,
    leaveFunction : null,
    takeSeatButton : null,
    name : null,

    init : function(tournamentId, name, registered, viewContainer,leaveFunction) {
        this.leaveFunction = leaveFunction;
        this.tournamentId = tournamentId;
        this.viewContainer = viewContainer;
        this.name = name;
        this.templateManager = Poker.AppCtx.getTemplateManager();
        var viewHTML = this.templateManager.render("tournamentTemplate",{tournamentId : tournamentId, name : name});

        viewContainer.append(viewHTML);

        var viewId = "#tournamentView"+tournamentId;
        this.viewElement = $(viewId);
        this.playerListBody = this.viewElement.find(".player-list tbody");
        this.initActions();
        if(registered==true) {
            this.setPlayerRegisteredState();
        }
        Poker.Sharing.bindShareTournament(this.viewElement.find(".share-button")[0],tournamentId,name);
    },
    updatePlayerList : function(players) {
        var template = this.templateManager.getRenderTemplate("tournamentPlayerListItem");
        this.playerListBody.empty();
        var self = this;
        $.each(players,function(i,p) {
            self.playerListBody.append(template.render(p));
        });
        if(players.length==0) {
            this.playerListBody.append("<td/>").attr("colspan","3").
                append(i18n.t("tournament-lobby.players.no-players"));
        }
    },
    updateBlindsStructure : function(blindsStructure) {
        var blindsTemplate = this.templateManager.getRenderTemplate("tournamentBlindsStructureTemplate");
        this.viewElement.find(".blinds-structure").html(blindsTemplate.render(blindsStructure));

    },
    updateTournamentInfo : function(info) {
        this.viewElement.find(".tournament-name-title").html(info.tournamentName);
        var sitAndGo = false;
        if(info.maxPlayers == info.minPlayers) {
            sitAndGo = true;
        }
        $.extend(info,{sitAndGo : sitAndGo});
        var infoTemplate = this.templateManager.getRenderTemplate("tournamentInfoTemplate");
        this.viewElement.find(".tournament-info").html(infoTemplate.render(info));

        if(info.sitAndGo==false) {
            console.log(this.viewElement);
            console.log(this.viewElement.find(".tournament-start-date"));
            var m = moment(parseInt(info.startTime));
            this.viewElement.find(".tournament-start-date").html(m.format("lll") + " ("+ m.fromNow()+")");

        }

    },
    updateTournamentStatistics : function(statistics) {
        var statsTemplate = this.templateManager.getRenderTemplate("tournamentStatsTemplate");
        this.viewElement.find(".tournament-stats").show().html(statsTemplate.render(statistics));
    },
    hideTournamentStatistics : function()  {
        this.viewElement.find(".tournament-stats").hide();
    },
    updatePayoutInfo : function(payoutInfo) {
        var payoutTemplate = this.templateManager.getRenderTemplate("tournamentPayoutStructureTemplate");
        this.viewElement.find(".payout-structure").html(payoutTemplate.render(payoutInfo));
    },
    initActions : function() {
        this.leaveButton = this.viewElement.find(".leave-action");
        this.registerButton = this.viewElement.find(".register-action");
        this.unregisterButton = this.viewElement.find(".unregister-action");
        this.loadingButton =  this.viewElement.find(".loading-action").hide();
        this.takeSeatButton =  this.viewElement.find(".take-seat-action").hide();
        var tournamentRequestHandler = new Poker.TournamentRequestHandler(this.tournamentId);
        var self = this;
        this.leaveButton.touchSafeClick(function(e){
            self.leaveLobby();
        });
        this.registerButton.touchSafeClick(function(e){
            tournamentRequestHandler.requestBuyInInfo();
        });
        this.unregisterButton.hide().touchSafeClick(function(e){
            $(this).hide();
            self.loadingButton.show();
            tournamentRequestHandler.unregisterFromTournament();

        });
        this.takeSeatButton.touchSafeClick(function(e){
            tournamentRequestHandler.takeSeat();
        });
    },

    onFailedRegistration : function() {
        this.setPlayerUnregisteredState();
    },
    onFailedUnregistraion : function() {
        this.setPlayerRegisteredState();
    },
    setTournamentNotRegisteringState : function(registered){
        if(registered) {
            this.takeSeatButton.show();
        } else {
            this.takeSeatButton.hide();
        }
        this.loadingButton.hide();
        this.registerButton.hide();
        this.unregisterButton.hide();
    },
    setPlayerRegisteredState : function() {
        this.loadingButton.hide();
        this.registerButton.hide();
        this.unregisterButton.show();
    },
    setPlayerUnregisteredState : function() {
        this.loadingButton.hide();
        this.registerButton.show();
        this.unregisterButton.hide();
    },
    getViewElementId : function() {
        return this.viewElement.attr("id");
    },
    leaveLobby : function() {
        new Poker.TournamentRequestHandler(this.tournamentId).leaveTournamentLobby();
    },
    showBuyInInfo : function(buyIn, fee, currency, balanceInWallet) {
        var buyInDialog = new Poker.TournamentBuyInDialog();
        buyInDialog.show(this.tournamentId,this.name,buyIn,fee,balanceInWallet,currency);
    }

});
