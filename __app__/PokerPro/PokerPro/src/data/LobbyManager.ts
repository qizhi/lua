///<reference path="LobbyData.ts"/>
///<reference path="GameConfig.ts"/>
///<reference path="../ui/LobbyLayoutManager.ts"/>
module data {
    export class LobbyManager {

        private cashGamesLobbyData: LobbyData;
        private tournamentLobbyData: LobbyData;
        private sitAndGoState: boolean;

        private lobbyLayoutManager: ui.LobbyLayoutManager;

        constructor() {
            this.lobbyLayoutManager = ui.LobbyLayoutManager.getInstance();
            var self: LobbyManager = this;
            this.cashGamesLobbyData = new LobbyData(new TableLobbyDataValidator(),
                (items) => {
                    self.lobbyLayoutManager.createTableList(items);
                },
                (itemId) => {
                    self.lobbyLayoutManager.tableRemoved(itemId);
                });
            this.tournamentLobbyData = new LobbyData(new TournamentLobbyDataValidator(),
                (items) => {
                    self.lobbyLayoutManager.createTournamentList(items);
                },
                (itemId) => {
                    self.lobbyLayoutManager.tournamentRemoved(itemId);
                });
        }

        public handleTableSnapshotList(tableSnapshotList: any[]): void {
            var items: any[] = [];
            for (var i: number = 0; i < tableSnapshotList.length; i++) {
                items.push(util.ProtocolUtils.extractTableData(tableSnapshotList[i]));
            }
            this.cashGamesLobbyData.addOrUpdateItems(items);
        }
        public handleTournamentSnapshotList(tournamentSnapshotList: any[]): void {
            if (tournamentSnapshotList.length > 0 && tournamentSnapshotList[0].address.indexOf("/sitandgo") != -1) {
                this.sitAndGoState = true;
            } else {
                this.sitAndGoState = false;
            }

            var items: any[] = [];
            for (var i = 0; i < tournamentSnapshotList.length; i++) {
                items.push(util.ProtocolUtils.extractTournamentData(tournamentSnapshotList[i]));
            }
            this.tournamentLobbyData.addOrUpdateItems(items);
        }

        public handleTournamentUpdates(tournamentUpdateList: any[]): void {
            var items = [];
            for (var i = 0; i < tournamentUpdateList.length; i++) {
                items.push(util.ProtocolUtils.extractTournamentData(tournamentUpdateList[i]));
            }
            this.tournamentLobbyData.addOrUpdateItems(items);
        }

        public getTableStatus(seated: number, capacity: number): string {
            if (seated == capacity) {
                return "full";
            }
            return "open";
        }
        public getBettingModel(model: string): string {
            if (model == "NO_LIMIT") {
                return "NL"
            } else if (model == "POT_LIMIT") {
                return "PL";
            } else if (model == "FIXED_LIMIT") {
                return "FL";
            }
            return model;
        }
        public handleTableUpdateList(tableUpdateList: any[]): void {
            var items = [];
            for (var i = 0; i < tableUpdateList.length; i++) {
                items.push(util.ProtocolUtils.extractTableData(tableUpdateList[i]));
            }
            this.cashGamesLobbyData.addOrUpdateItems(items);
        }
        public handleTableRemoved(tableId: number): void {
            this.cashGamesLobbyData.addOrUpdateItem({ id: tableId, showInLobby: 0 });
        }
        public handleTournamentRemoved(tournamentId: number): void {
            this.tournamentLobbyData.addOrUpdateItem({ id: tournamentId, showInLobby: 0 });
        }
        public clearLobby(): void {
            this.cashGamesLobbyData.clear();
            this.tournamentLobbyData.clear();
            //$("#tableListContainer").empty();
        }

        public getCapacity(id: number): number {
            var tableData: any = this.cashGamesLobbyData.getItem(id);
            return tableData.capacity;
        }

        private static _instance: LobbyManager;
        public static getInstance(): LobbyManager {
            if (LobbyManager._instance == null) {
                LobbyManager._instance = new LobbyManager();
            }
            return LobbyManager._instance;
        }
    }
}