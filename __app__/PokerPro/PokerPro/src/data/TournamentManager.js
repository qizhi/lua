///<reference path="../net/PeriodicalUpdater.ts"/>
///<reference path="../ui/TournamentLayout.ts"/>
///<reference path="../net/TournamentHandler.ts"/>
///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>
///<reference path="TableManager.ts"/>
var data;
(function (data) {
    var Tournament = (function () {
        function Tournament(id, name, layout) {
            this.id = id;
            this.name = name;
            this.layout = layout;
        }
        return Tournament;
    })();
    data.Tournament = Tournament;
    var TournamentManager = (function () {
        function TournamentManager(tournamentLobbyUpdateInterval) {
            if (typeof tournamentLobbyUpdateInterval === "undefined") { tournamentLobbyUpdateInterval = 5000; }
            this.tournaments = new data.Map();
            this.registeredTournaments = new data.Map();
            this.tournamentTables = new data.Map();
            this.tableManager = data.TableManager.getInstance();

            var self = this;
            this.tournamentUpdater = new net.PeriodicalUpdater(function () {
                self.updateTournamentData();
            }, tournamentLobbyUpdateInterval);
        }
        TournamentManager.prototype.createTournament = function (id, name) {
        };
        TournamentManager.prototype.onRemovedFromTournament = function (tableId, playerId) {
            this.tableManager.updatePlayerStatus(tableId, playerId, data.PlayerTableStatus.TOURNAMENT_OUT);
            this.tableManager.removePlayer(tableId, playerId);
        };
        TournamentManager.prototype.setTournamentTable = function (tournamentId, tableId) {
            this.tournamentTables.put(tournamentId, tableId);
        };
        TournamentManager.prototype.isTournamentTable = function (tableId) {
            var tables = this.tournamentTables.values();
            for (var i = 0; i < tables.length; i++) {
                if (tables[i] === tableId) {
                    return true;
                }
            }
            return false;
        };
        TournamentManager.prototype.getTableByTournament = function (tournamentId) {
            return this.tournamentTables.get(tournamentId);
        };
        TournamentManager.prototype.removeTournament = function (tournamentId) {
            var tournament = this.tournaments.remove(tournamentId);
            if (tournament != null) {
                if (this.tournaments.size() == 0) {
                    console.log("Stopping updates of lobby for tournament: " + tournamentId);
                    this.tournamentUpdater.stop();
                }
            }
        };
        TournamentManager.prototype.onPlayerLoggedIn = function () {
            var tournaments = this.tournaments.values();
            for (var i = 0; i < tournaments.length; i++) {
                var t = tournaments[i];
                new net.TournamentRequestHandler(t.id).leaveTournamentLobby();
                this.createTournament(t.id, t.name);
            }
        };
        TournamentManager.prototype.getTournamentById = function (id) {
            return this.tournaments.get(id);
        };

        /**
        * @param {Number} tournamentId
        * @param {com.cubeia.games.poker.io.protocol.TournamentLobbyData} tournamentData
        */
        TournamentManager.prototype.handleTournamentLobbyData = function (tournamentId, tournamentData) {
            var tournament = this.getTournamentById(tournamentId);
            this.handlePlayerList(tournament, tournamentData.players);
            this.handleBlindsStructure(tournament, tournamentData.blindsStructure);
            this.handlePayoutInfo(tournament, tournamentData.payoutInfo);
            this.handleTournamentInfo(tournament, tournamentData.tournamentInfo);
            if (this.isTournamentRunning(tournamentData.tournamentInfo.tournamentStatus)) {
                this.handleTournamentStatistics(tournament, tournamentData.tournamentStatistics);
            } else {
                tournament.layout.hideTournamentStatistics();
            }
        };
        TournamentManager.prototype.handlePlayerList = function (tournament, playerList) {
            var players = [];
            if (playerList) {
                players = playerList.players;
            }
            tournament.layout.updatePlayerList(players);
        };
        TournamentManager.prototype.handleBlindsStructure = function (tournament, blindsStructure) {
            tournament.layout.updateBlindsStructure(blindsStructure);
        };
        TournamentManager.prototype.handlePayoutInfo = function (tournament, payoutInfo) {
            tournament.layout.updatePayoutInfo(payoutInfo);
        };
        TournamentManager.prototype.handleTournamentStatistics = function (tournament, statistics) {
            tournament.layout.updateTournamentStatistics(statistics);
        };
        TournamentManager.prototype.handleRegistrationSuccessful = function (tournamentId) {
            this.registeredTournaments.put(tournamentId, true);
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.layout.setPlayerRegisteredState();
            }
        };

        /**
        * @param {com.cubeia.games.poker.io.protocol.TournamentInfo} info
        */
        TournamentManager.prototype.handleTournamentInfo = function (tournament, info) {
            console.log("registered tournaments " + this.registeredTournaments.contains(tournament.id));
            console.log(this.registeredTournaments);

            /*var view = Poker.AppCtx.getViewManager().findViewByTournamentId(tournament.id);
            if (view != null) {
            view.updateName(info.tournamentName);
            }*/
            tournament.layout.updateTournamentInfo(info);
            var registered = this.registeredTournaments.contains(tournament.id);
            if (this.isTournamentRunning(info.tournamentStatus)) {
                tournament.layout.setTournamentNotRegisteringState(registered);
            } else if (info.tournamentStatus != com.cubeia.games.poker.io.protocol.TournamentStatusEnum.REGISTERING) {
                tournament.layout.setTournamentNotRegisteringState(false);
            } else if (registered == true) {
                tournament.layout.setPlayerRegisteredState();
            } else {
                tournament.layout.setPlayerUnregisteredState();
            }
        };
        TournamentManager.prototype.handleRegistrationFailure = function (tournamentId) {
        };
        TournamentManager.prototype.handleUnregistrationSuccessful = function (tournamentId) {
            this.registeredTournaments.remove(tournamentId);
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.layout.setPlayerUnregisteredState();
            }
        };
        TournamentManager.prototype.handleUnregistrationFailure = function (tournamentId) {
        };
        TournamentManager.prototype.isRegisteredForTournament = function (tournamentId) {
            return this.registeredTournaments.get(tournamentId) != null;
        };
        TournamentManager.prototype.activateTournamentUpdates = function (tournamentId) {
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.updating = true;
            }
            this.tournamentUpdater.rushUpdate();
        };
        TournamentManager.prototype.deactivateTournamentUpdates = function (tournamentId) {
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.updating = false;
            }
        };
        TournamentManager.prototype.updateTournamentData = function () {
            var tournaments = this.tournaments.values();
            for (var i = 0; i < tournaments.length; i++) {
                if (tournaments[i].updating == true && tournaments[i].finished == false) {
                    console.log("found updating tournament retrieving tournament data");
                    new net.TournamentRequestHandler(tournaments[i].id).requestTournamentInfo();
                }
            }
        };
        TournamentManager.prototype.openTournamentLobbies = function (tournamentIds) {
            for (var i = 0; i < tournamentIds.length; i++) {
                this.registeredTournaments.put(tournamentIds[i], true);
                this.createTournament(tournamentIds[i], "Tourney");
            }
        };
        TournamentManager.prototype.tournamentFinished = function (tournamentId) {
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                console.log("Tournament finished rushing update");
                tournament.finished = true;
                new net.TournamentRequestHandler(tournamentId).requestTournamentInfo();
            } else {
                console.log("Tournament finished but not found");
            }
        };

        /**
        * Checks if this tournament is running (which it is if the status is running, on_break or preparing_break).
        * @param {Number} status
        * @return {boolean}
        */
        TournamentManager.prototype.isTournamentRunning = function (status) {
            var running = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.RUNNING;
            var onBreak = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ON_BREAK;
            var preparingForBreak = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.PREPARING_BREAK;
            return status == running || status == onBreak || status == preparingForBreak;
        };
        TournamentManager.prototype.onBuyInInfo = function (tournamentId, buyIn, fee, currency, balanceInWallet, sufficientFunds) {
            console.log("on buy info " + tournamentId);
            var tournament = this.getTournamentById(tournamentId);
            console.log(tournament);
            if (sufficientFunds == true) {
                tournament.layout.showBuyInInfo(buyIn, fee, currency, balanceInWallet);
            }
        };

        TournamentManager.getInstance = function () {
            if (TournamentManager._instance == null) {
                TournamentManager._instance = new TournamentManager();
            }
            return TournamentManager._instance;
        };
        return TournamentManager;
    })();
    data.TournamentManager = TournamentManager;
})(data || (data = {}));
//@ sourceMappingURL=TournamentManager.js.map
