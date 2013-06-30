var ui;
(function (ui) {
    var TournamentLayout = (function () {
        function TournamentLayout(tournamentId, name, registered, leaveFunction) {
            this.tournamentId = tournamentId;
            this.name = name;
            this.registered = registered;
            this.leaveFunction = leaveFunction;
        }
        TournamentLayout.prototype.hideTournamentStatistics = function () {
        };
        TournamentLayout.prototype.updatePlayerList = function (players) {
        };
        TournamentLayout.prototype.updateBlindsStructure = function (blindsStructure) {
        };
        TournamentLayout.prototype.updatePayoutInfo = function (payoutInfo) {
        };
        TournamentLayout.prototype.updateTournamentStatistics = function (statistics) {
        };
        TournamentLayout.prototype.setPlayerRegisteredState = function () {
        };
        TournamentLayout.prototype.updateTournamentInfo = function (info) {
        };
        TournamentLayout.prototype.setTournamentNotRegisteringState = function (registered) {
        };
        TournamentLayout.prototype.setPlayerUnregisteredState = function () {
        };
        TournamentLayout.prototype.showBuyInInfo = function (buyIn, fee, currency, balanceInWallet) {
        };
        return TournamentLayout;
    })();
    ui.TournamentLayout = TournamentLayout;
})(ui || (ui = {}));
//@ sourceMappingURL=TournamentLayout.js.map
