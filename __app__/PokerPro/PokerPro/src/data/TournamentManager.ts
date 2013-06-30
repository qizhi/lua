///<reference path="../net/PeriodicalUpdater.ts"/>
///<reference path="../ui/TournamentLayout.ts"/>
///<reference path="../net/TournamentHandler.ts"/>
///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>
///<reference path="TableManager.ts"/>
module data {
    export class Tournament {
        updating: boolean;
        finished: boolean;
        constructor(public id: number, public name: string, public layout: ui.TournamentLayout) {
        }
    }
    export class TournamentManager {
        public tournaments: Map<number, Tournament>;
        public registeredTournaments: Map<number, boolean>;
        public tournamentTables: Map<number, number>;

        private tournamentUpdater: net.PeriodicalUpdater;
        private tableManager: TableManager;

        constructor(tournamentLobbyUpdateInterval: number = 5000) {
            this.tournaments = new Map<number, Tournament>();
            this.registeredTournaments = new Map<number, boolean>();
            this.tournamentTables = new Map<number, number>();
            this.tableManager = TableManager.getInstance();
            
            var self: TournamentManager = this;
            this.tournamentUpdater = new net.PeriodicalUpdater(() => {
                self.updateTournamentData();
            }, tournamentLobbyUpdateInterval);
        }

        createTournament(id: number, name: string): void {
            /*var viewManager = Poker.AppCtx.getViewManager();
            if (this.getTournamentById(id) != null) {
                viewManager.activateViewByTournamentId(id);
            } else {
                var self = this;
                var viewContainer = $(".view-container");

                var layoutManager = new Poker.TournamentLayoutManager(id, name, this.isRegisteredForTournament(id),
                    viewContainer, function () {
                        self.removeTournament(id);
                    }
                    );
                viewManager.addTournamentView(layoutManager.getViewElementId(), name, layoutManager);

                this.tournaments.put(id, new Poker.Tournament(id, name, layoutManager));
                new Poker.TournamentRequestHandler(id).requestTournamentInfo();
                this.activateTournamentUpdates(id);
                this.tournamentUpdater.start();
            }*/
        }
        onRemovedFromTournament(tableId: number, playerId: number): void {
            this.tableManager.updatePlayerStatus(tableId, playerId, PlayerTableStatus.TOURNAMENT_OUT);
            this.tableManager.removePlayer(tableId, playerId);
        }
        setTournamentTable(tournamentId: number, tableId: number): void {
            this.tournamentTables.put(tournamentId, tableId);
        }
        isTournamentTable(tableId: number): boolean {
            var tables: number[] = this.tournamentTables.values();
            for (var i = 0; i < tables.length; i++) {
                if (tables[i] === tableId) {
                    return true;
                }
            }
            return false;
        }
        getTableByTournament(tournamentId: number): number {
            return this.tournamentTables.get(tournamentId);
        }
        removeTournament(tournamentId: number): void {
            var tournament: Tournament = this.tournaments.remove(tournamentId);
            if (tournament != null) {
                if (this.tournaments.size() == 0) {
                    console.log("Stopping updates of lobby for tournament: " + tournamentId);
                    this.tournamentUpdater.stop();
                }
            }
        }
        onPlayerLoggedIn(): void {
            var tournaments = this.tournaments.values();
            for (var i = 0; i < tournaments.length; i++) {
                var t = tournaments[i];
                new net.TournamentRequestHandler(t.id).leaveTournamentLobby();
                this.createTournament(t.id, t.name);
            }

        }
        getTournamentById(id: number): Tournament {
            return this.tournaments.get(id);
        }
        /**
         * @param {Number} tournamentId
         * @param {com.cubeia.games.poker.io.protocol.TournamentLobbyData} tournamentData
         */
        handleTournamentLobbyData(tournamentId: number, tournamentData: any): void {
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
        }
        handlePlayerList(tournament: Tournament, playerList: any): void {
            var players = [];
            if (playerList) {
                players = playerList.players;
            }
            tournament.layout.updatePlayerList(players);
        }
        handleBlindsStructure(tournament: Tournament, blindsStructure: any): void {
            tournament.layout.updateBlindsStructure(blindsStructure);
        }
        handlePayoutInfo(tournament: Tournament, payoutInfo: any): void {
            tournament.layout.updatePayoutInfo(payoutInfo);
        }
        handleTournamentStatistics(tournament: Tournament, statistics: any): void {
            tournament.layout.updateTournamentStatistics(statistics);
        }
        handleRegistrationSuccessful(tournamentId: number): void {
            this.registeredTournaments.put(tournamentId, true);
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.layout.setPlayerRegisteredState();
            }
            /*this.dialogManager.displayGenericDialog({
                tournamentId: tournamentId,
                header: i18n.t("dialogs.tournament-register-success.header"),
                message: i18n.t("dialogs.tournament-register-success.message", { sprintf: [tournamentId] })
            });*/

        }
        /**
         * @param {com.cubeia.games.poker.io.protocol.TournamentInfo} info
         */
        handleTournamentInfo(tournament: Tournament, info: any): void {
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
            // TODO: we could update the name here, at least if it's the dummy name (Tourney). (I tried, but couldn't figure out the Mustache stuff.)
            // tournament.layout.updateName(info.tournamentName);
        }
        handleRegistrationFailure(tournamentId: number): void {
            /*this.dialogManager.displayGenericDialog({
                tournamentId: tournamentId,
                header: i18n.t("dialogs.tournament-register-failure.header"),
                message: i18n.t("dialogs.tournament-register-failure.message", { sprintf: [tournamentId] })
            });*/

        }
        handleUnregistrationSuccessful(tournamentId: number): void {
            this.registeredTournaments.remove(tournamentId);
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.layout.setPlayerUnregisteredState();
            }
            /*this.dialogManager.displayGenericDialog({
                tournamentId: tournamentId,
                header: i18n.t("dialogs.tournament-unregister-success.header"),
                message: i18n.t("dialogs.tournament-unregister-success.message", { sprintf: [tournamentId] })
            });*/

        }
        handleUnregistrationFailure(tournamentId: number): void {
            /*this.dialogManager.displayGenericDialog({
                tournamentId: tournamentId,
                header: i18n.t("dialogs.tournament-unregister-failure.header"),
                message: i18n.t("dialogs.tournament-unregister-failure.message", { sprintf: [tournamentId] })
            });*/

        }
        isRegisteredForTournament(tournamentId: number): boolean {
            return this.registeredTournaments.get(tournamentId) != null;
        }
        activateTournamentUpdates(tournamentId: number): void {
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.updating = true;
            }
            this.tournamentUpdater.rushUpdate();
        }
        deactivateTournamentUpdates(tournamentId: number): void {
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                tournament.updating = false;
            }
        }
        updateTournamentData(): void {
            var tournaments = this.tournaments.values();
            for (var i = 0; i < tournaments.length; i++) {
                if (tournaments[i].updating == true && tournaments[i].finished == false) {
                    console.log("found updating tournament retrieving tournament data");
                    new net.TournamentRequestHandler(tournaments[i].id).requestTournamentInfo();
                }
            }
        }
        openTournamentLobbies(tournamentIds: number[]): void {
            //TODO: the name of the tournament needs to be fetched from somewhere!
            for (var i = 0; i < tournamentIds.length; i++) {
                this.registeredTournaments.put(tournamentIds[i], true);
                this.createTournament(tournamentIds[i], "Tourney");
            }
        }
        tournamentFinished(tournamentId: number): void {
            var tournament = this.tournaments.get(tournamentId);
            if (tournament != null) {
                console.log("Tournament finished rushing update");
                tournament.finished = true;
                new net.TournamentRequestHandler(tournamentId).requestTournamentInfo();
            } else {
                console.log("Tournament finished but not found");
            }
        }
        /**
         * Checks if this tournament is running (which it is if the status is running, on_break or preparing_break).
         * @param {Number} status
         * @return {boolean}
         */
        isTournamentRunning(status: number): boolean {
            var running = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.RUNNING;
            var onBreak = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.ON_BREAK;
            var preparingForBreak = com.cubeia.games.poker.io.protocol.TournamentStatusEnum.PREPARING_BREAK;
            return status == running || status == onBreak || status == preparingForBreak;
        }
        onBuyInInfo(tournamentId: number, buyIn: number, fee: number, currency: number, balanceInWallet: number, sufficientFunds: boolean): void {
            console.log("on buy info " + tournamentId);
            var tournament = this.getTournamentById(tournamentId);
            console.log(tournament);
            if (sufficientFunds == true) {
                tournament.layout.showBuyInInfo(buyIn, fee, currency, balanceInWallet);
            }
        }
        
        private static _instance: TournamentManager;
        public static getInstance(): TournamentManager {
            if (TournamentManager._instance == null) {
                TournamentManager._instance = new TournamentManager();
            }
            return TournamentManager._instance;
        }
    }
}