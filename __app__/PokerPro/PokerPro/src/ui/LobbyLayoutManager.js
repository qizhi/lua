var ui;
(function (ui) {
    var LobbyLayoutManager = (function () {
        function LobbyLayoutManager() {
        }
        LobbyLayoutManager.prototype.createTableList = function (items) {
            console.log(JSON.stringify(items));
        };
        LobbyLayoutManager.prototype.tableRemoved = function (itemId) {
        };

        LobbyLayoutManager.prototype.createTournamentList = function (item) {
        };
        LobbyLayoutManager.prototype.tournamentRemoved = function (itemId) {
        };

        LobbyLayoutManager.getInstance = function () {
            if (LobbyLayoutManager._instance == null) {
                LobbyLayoutManager._instance = new LobbyLayoutManager();
            }
            return LobbyLayoutManager._instance;
        };
        return LobbyLayoutManager;
    })();
    ui.LobbyLayoutManager = LobbyLayoutManager;
})(ui || (ui = {}));
//@ sourceMappingURL=LobbyLayoutManager.js.map
