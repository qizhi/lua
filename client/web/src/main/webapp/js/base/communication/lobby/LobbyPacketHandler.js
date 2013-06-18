"use strict";
var Poker = Poker || {};
Poker.LobbyPacketHandler = Class.extend({

    /**
     * @type Poker.LobbyManager
     */
    lobbyManager : null,

    init : function() {
        this.lobbyManager = Poker.AppCtx.getLobbyManager();
    },
    handleTableSnapshotList : function(snapshots) {
        this.lobbyManager.handleTableSnapshotList(snapshots);
    },
    handleTableUpdateList : function(updates) {
        this.lobbyManager.handleTableUpdateList(updates);
    },
    handleTableRemoved : function(tableId) {
        this.lobbyManager.handleTableRemoved(tableId);
    },
    handleTournamentSnapshotList : function(snapshots){
        this.lobbyManager.handleTournamentSnapshotList(snapshots);
    },
    handleTournamentUpdates : function(updates) {
        this.lobbyManager.handleTournamentUpdates(updates);
    },
    handleTournamentRemoved : function(tournamentId) {
        this.lobbyManager.handleTournamentRemoved(tournamentId);
    }

});