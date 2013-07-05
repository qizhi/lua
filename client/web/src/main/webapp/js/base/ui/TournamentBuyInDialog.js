"use strict";
var Poker = Poker || {};

Poker.TournamentBuyInDialog = Poker.BuyInDialog.extend({
    init : function() {
        this._super();
    },
    show : function(tournamentId,name,buyIn,fee,balance,currencyCode) {
        var data = {
            tournamentId : tournamentId,
            buyIn : buyIn,
            fee : fee,
            balance : balance,
            name : name,
            currencyCode : currencyCode
        };
        var tournament = Poker.AppCtx.getTournamentManager().getTournamentById(tournamentId);
        var viewContainer = tournament.tournamentLayoutManager.viewElement;
        $.ga._trackEvent("tournament_registration", "open_buy_in_dialogue");
        this.render(data, viewContainer ,function(){
            new Poker.TournamentRequestHandler(tournamentId).registerToTournament();
            $.ga._trackEvent("tournament_registration", "buy_in_to_tournament");
            return true;
        });
    },
    getTemplateId : function() {
        return "tournamentBuyInContent";
    }

});