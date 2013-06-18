"use strict";
var Poker = Poker || {};
/**
 * Handles tournament lobby related data for the
 * tournaments the user is currently watching
 * @type {Poker.TournamentManager}
 */
Poker.TournamentManager = Class.extend({

    /**
     * @type Poker.Map
     */
    tournaments : null,

    /**
     * @type Poker.Map
     */
    registeredTournaments : null,

    /**
     *  @type Poker.Map
     */
    tournamentTables : null,

    /**
     * @type Poker.DialogManager
     */
    dialogManager : null,

    /**
     * @type Poker.PeriodicalUpdater
     */
    tournamentUpdater : null,

    /**
     * @type Poker.TableManager
     */
    tableManager : null,

    /**
     *
     * @param {Number} tournamentLobbyUpdateInterval
     * @constructor
     */
    init : function(tournamentLobbyUpdateInterval) {
        this.tournaments = new Poker.Map();
        this.tournamentTables = new Poker.Map();
        this.registeredTournaments = new Poker.Map();
        this.dialogManager = Poker.AppCtx.getDialogManager();
        this.tableManager = Poker.AppCtx.getTableManager();
        var self = this;
        this.tournamentUpdater = new Poker.PeriodicalUpdater(function(){
            self.updateTournamentData();
        },tournamentLobbyUpdateInterval);
    },
    createTournament : function(id, name) {
        var viewManager = Poker.AppCtx.getViewManager();
        if(this.getTournamentById(id)!=null) {
            viewManager.activateViewByTournamentId(id);
        } else {
            var self = this;
            var viewContainer = $(".view-container");

            var layoutManager = new Poker.TournamentLayoutManager(id, name, this.isRegisteredForTournament(id),
                viewContainer,function(){
                        self.removeTournament(id);
                    }
            );
            viewManager.addTournamentView(layoutManager.getViewElementId(), name, layoutManager);

            this.tournaments.put(id,new Poker.Tournament(id, name, layoutManager));
            new Poker.TournamentRequestHandler(id).requestTournamentInfo();
            this.activateTournamentUpdates(id);
            this.tournamentUpdater.start();
        }
    },
    onRemovedFromTournament : function(tableId, playerId) {
        this.tableManager.updatePlayerStatus(tableId,playerId,
            Poker.PlayerTableStatus.TOURNAMENT_OUT);
        this.tableManager.removePlayer(tableId, playerId);
    },
    setTournamentTable : function(tournamentId, tableId) {
        this.tournamentTables.put(tournamentId,tableId);
    },
    isTournamentTable : function(tableId) {
        var tables = this.tournamentTables.values();
        for(var i = 0; i<tables;i++) {
            if(tables[i]===tableId) {
                return true;
            }
        }
        return false;
    },
    getTableByTournament : function(tournamentId) {
        return this.tournamentTables.get(tournamentId);
    },
    removeTournament : function(tournamentId) {
        var tournament = this.tournaments.remove(tournamentId);
        if (tournament!=null) {
            if (this.tournaments.size() == 0) {
                console.log("Stopping updates of lobby for tournament: " + tournamentId);
                this.tournamentUpdater.stop();
            }
        }
    },
    onPlayerLoggedIn : function() {
        var tournaments = this.tournaments.values();
        for(var i = 0; i<tournaments.length; i++){
            var t = tournaments[i];
            new Poker.TournamentRequestHandler(t.id).leaveTournamentLobby();
            this.createTournament(t.id,t.name);
        }

    },
    /**
     * @param id
     * @return {Poker.Tournament}
     */
    getTournamentById : function(id) {
        return this.tournaments.get(id);
    },
    /**
     * @param {Number} tournamentId
     * @param {com.cubeia.games.poker.io.protocol.TournamentLobbyData} tournamentData
     */
    handleTournamentLobbyData : function(tournamentId, tournamentData) {
        var tournament = this.getTournamentById(tournamentId);
        this.handlePlayerList(tournament,tournamentData.players);
        this.handleBlindsStructure(tournament,tournamentData.blindsStructure);
        this.handlePayoutInfo(tournament,tournamentData.payoutInfo);
        this.handleTournamentInfo(tournament, tournamentData.tournamentInfo);
        if (this.isTournamentRunning(tournamentData.tournamentInfo.tournamentStatus)) {
            this.handleTournamentStatistics(tournament, tournamentData.tournamentStatistics);
        } else {
            tournament.tournamentLayoutManager.hideTournamentStatistics();
        }
    },
    handlePlayerList : function(tournament,playerList) {
        var players = [];
        if(playerList) {
           players = playerList.players;
        }
        tournament.tournamentLayoutManager.updatePlayerList(players);
    },
    handleBlindsStructure : function(tournament,blindsStructure) {
        tournament.tournamentLayoutManager.updateBlindsStructure(blindsStructure);
    },
    handlePayoutInfo : function(tournament, payoutInfo) {
        tournament.tournamentLayoutManager.updatePayoutInfo(payoutInfo);
    },
    handleTournamentStatistics : function(tournament,statistics) {
        tournament.tournamentLayoutManager.updateTournamentStatistics(statistics);
    },
    handleRegistrationSuccessful : function(tournamentId) {
        this.registeredTournaments.put(tournamentId,true);
        var tournament = this.tournaments.get(tournamentId);
        if(tournament!=null) {
            tournament.tournamentLayoutManager.setPlayerRegisteredState();
        }
        this.dialogManager.displayGenericDialog({
            tournamentId : tournamentId,
            header:i18n.t("dialogs.tournament-register-success.header"),
            message:i18n.t("dialogs.tournament-register-success.message", { sprintf : [tournamentId]})
        });

    },
    /**
     * @param {Poker.Tournament} tournament
     * @param {com.cubeia.games.poker.io.protocol.TournamentInfo} info
     */
    handleTournamentInfo : function(tournament, info) {
        console.log("registered tournaments " + this.registeredTournaments.contains(tournament.id));
        console.log(this.registeredTournaments);
        var view = Poker.AppCtx.getViewManager().findViewByTournamentId(tournament.id);
        if(view!=null){
            view.updateName(info.tournamentName);
        }
        tournament.tournamentLayoutManager.updateTournamentInfo(info);
        var registered = this.registeredTournaments.contains(tournament.id);
        if (this.isTournamentRunning(info.tournamentStatus)) {
            tournament.tournamentLayoutManager.setTournamentNotRegisteringState(registered);
        } else if (info.tournamentStatus != com.cubeia.games.poker.io.protocol.TournamentStatusEnum.REGISTERING) {
            tournament.tournamentLayoutManager.setTournamentNotRegisteringState(false);
        } else if (registered == true) {
            tournament.tournamentLayoutManager.setPlayerRegisteredState();
        } else {
            tournament.tournamentLayoutManager.setPlayerUnregisteredState();
        }
        // TODO: we could update the name here, at least if it's the dummy name (Tourney). (I tried, but couldn't figure out the Mustache stuff.)
        // tournament.tournamentLayoutManager.updateName(info.tournamentName);
    },
    handleRegistrationFailure : function(tournamentId) {
        this.dialogManager.displayGenericDialog({
            tournamentId : tournamentId,
            header: i18n.t("dialogs.tournament-register-failure.header"),
            message: i18n.t("dialogs.tournament-register-failure.message", { sprintf : [tournamentId]})
        });

    },
    handleUnregistrationSuccessful : function(tournamentId) {
        this.registeredTournaments.remove(tournamentId);
        var tournament = this.tournaments.get(tournamentId);
        if(tournament!=null) {
            tournament.tournamentLayoutManager.setPlayerUnregisteredState();
        }
        this.dialogManager.displayGenericDialog({
            tournamentId : tournamentId,
            header:i18n.t("dialogs.tournament-unregister-success.header"),
            message:i18n.t("dialogs.tournament-unregister-success.message", { sprintf : [tournamentId]})
        });

    },
    handleUnregistrationFailure : function(tournamentId) {
        this.dialogManager.displayGenericDialog({
            tournamentId : tournamentId,
            header:i18n.t("dialogs.tournament-unregister-failure.header"),
            message:i18n.t("dialogs.tournament-unregister-failure.message", { sprintf : [tournamentId]})
        });

    },
    isRegisteredForTournament : function(tournamentId) {
        return this.registeredTournaments.get(tournamentId) != null;
    },
    activateTournamentUpdates : function(tournamentId) {
        var tournament = this.tournaments.get(tournamentId);
        if (tournament != null) {
            tournament.updating = true;
        }
        this.tournamentUpdater.rushUpdate();
    },
    deactivateTournamentUpdates : function(tournamentId) {
        var tournament = this.tournaments.get(tournamentId);
        if (tournament != null) {
            tournament.updating = false;
        }
    },
    updateTournamentData : function() {
        var tournaments =  this.tournaments.values();
        for (var i = 0; i < tournaments.length; i++) {
            if (tournaments[i].updating == true && tournaments[i].finished == false) {
                console.log("found updating tournament retrieving tournament data");
                new Poker.TournamentRequestHandler(tournaments[i].id).requestTournamentInfo();
            }
        }
    },
    openTournamentLobbies : function(tournamentIds) {
        //TODO: the name of the tournament needs to be fetched from somewhere!
        for (var i = 0; i < tournamentIds.length; i++) {
            this.registeredTournaments.put(tournamentIds[i],true);
            this.createTournament(tournamentIds[i],"Tourney");
        }
    },
    tournamentFinished : function(tournamentId) {
        var tournament = this.tournaments.get(tournamentId);
        if(tournament!=null){
            console.log("Tournament finished rushing update");
            tournament.finished = true;
            new Poker.TournamentRequestHandler(tournamentId).requestTournamentInfo();
        } else {
            console.log("Tournament finished but not found");
        }
    },
    /**
     * Checks if this tournament is running (which it is if the status is running, on_break or preparing_break).
     * @param {Number} status
     * @return {boolean}
     */
    isTournamentRunning : function(status) {
        var running = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.RUNNING;
        var onBreak = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ON_BREAK;
        var preparingForBreak = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.PREPARING_BREAK;
        return status == running || status == onBreak || status == preparingForBreak;
    },
    onBuyInInfo : function(tournamentId, buyIn, fee, currency, balanceInWallet, sufficientFunds) {
        console.log("on buy info " + tournamentId);
        var tournament = this.getTournamentById(tournamentId);
        console.log(tournament);
        if (sufficientFunds == true) {
            tournament.tournamentLayoutManager.showBuyInInfo(buyIn,fee,currency,balanceInWallet);
        }
    }
});