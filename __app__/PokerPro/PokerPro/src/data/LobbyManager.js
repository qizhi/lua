///<reference path="LobbyData.ts"/>
///<reference path="GameConfig.ts"/>
///<reference path="../ui/LobbyLayoutManager.ts"/>
var data;
(function (data) {
    var LobbyManager = (function () {
        function LobbyManager() {
            this.lobbyLayoutManager = ui.LobbyLayoutManager.getInstance();
            var self = this;
            this.cashGamesLobbyData = new data.LobbyData(new data.TableLobbyDataValidator(), function (items) {
                self.lobbyLayoutManager.createTableList(items);
            }, function (itemId) {
                self.lobbyLayoutManager.tableRemoved(itemId);
            });
            this.tournamentLobbyData = new data.LobbyData(new data.TournamentLobbyDataValidator(), function (items) {
                self.lobbyLayoutManager.createTournamentList(items);
            }, function (itemId) {
                self.lobbyLayoutManager.tournamentRemoved(itemId);
            });
        }
        LobbyManager.prototype.handleTableSnapshotList = function (tableSnapshotList) {
            var items = [];
            for (var i = 0; i < tableSnapshotList.length; i++) {
                items.push(util.ProtocolUtils.extractTableData(tableSnapshotList[i]));
            }
            this.cashGamesLobbyData.addOrUpdateItems(items);
        };
        LobbyManager.prototype.handleTournamentSnapshotList = function (tournamentSnapshotList) {
            if (tournamentSnapshotList.length > 0 && tournamentSnapshotList[0].address.indexOf("/sitandgo") != -1) {
                this.sitAndGoState = true;
            } else {
                this.sitAndGoState = false;
            }

            var items = [];
            for (var i = 0; i < tournamentSnapshotList.length; i++) {
                items.push(util.ProtocolUtils.extractTournamentData(tournamentSnapshotList[i]));
            }
            this.tournamentLobbyData.addOrUpdateItems(items);
        };

        LobbyManager.prototype.handleTournamentUpdates = function (tournamentUpdateList) {
            var items = [];
            for (var i = 0; i < tournamentUpdateList.length; i++) {
                items.push(util.ProtocolUtils.extractTournamentData(tournamentUpdateList[i]));
            }
            this.tournamentLobbyData.addOrUpdateItems(items);
        };

        LobbyManager.prototype.getTableStatus = function (seated, capacity) {
            if (seated == capacity) {
                return "full";
            }
            return "open";
        };
        LobbyManager.prototype.getBettingModel = function (model) {
            if (model == "NO_LIMIT") {
                return "NL";
            } else if (model == "POT_LIMIT") {
                return "PL";
            } else if (model == "FIXED_LIMIT") {
                return "FL";
            }
            return model;
        };
        LobbyManager.prototype.handleTableUpdateList = function (tableUpdateList) {
            var items = [];
            for (var i = 0; i < tableUpdateList.length; i++) {
                items.push(util.ProtocolUtils.extractTableData(tableUpdateList[i]));
            }
            this.cashGamesLobbyData.addOrUpdateItems(items);
        };
        LobbyManager.prototype.handleTableRemoved = function (tableId) {
            this.cashGamesLobbyData.addOrUpdateItem({ id: tableId, showInLobby: 0 });
        };
        LobbyManager.prototype.handleTournamentRemoved = function (tournamentId) {
            this.tournamentLobbyData.addOrUpdateItem({ id: tournamentId, showInLobby: 0 });
        };
        LobbyManager.prototype.clearLobby = function () {
            this.cashGamesLobbyData.clear();
            this.tournamentLobbyData.clear();
        };

        LobbyManager.prototype.getCapacity = function (id) {
            var tableData = this.cashGamesLobbyData.getItem(id);
            return tableData.capacity;
        };

        LobbyManager.getInstance = function () {
            if (LobbyManager._instance == null) {
                LobbyManager._instance = new LobbyManager();
            }
            return LobbyManager._instance;
        };
        return LobbyManager;
    })();
    data.LobbyManager = LobbyManager;
})(data || (data = {}));
//@ sourceMappingURL=LobbyManager.js.map
