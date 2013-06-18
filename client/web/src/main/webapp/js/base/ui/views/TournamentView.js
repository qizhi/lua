var Poker = Poker || {};
Poker.TournamentView = Poker.TabView.extend({
    layoutManager : null,
    init :function(viewElementId,name,layoutManager) {
        this._super(viewElementId,name, "T");
        this.layoutManager = layoutManager;
    },
    getTournamentId : function() {
        return this.layoutManager.tournamentId;
    },
    activate : function() {
        this._super();
        Poker.AppCtx.getTournamentManager().activateTournamentUpdates(this.layoutManager.tournamentId);
    },
    deactivate : function() {
        this._super();
        Poker.AppCtx.getTournamentManager().deactivateTournamentUpdates(this.layoutManager.tournamentId);
    }
});