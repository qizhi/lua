var net;
(function (net) {
    var LobbyPacketHandler = (function () {
        function LobbyPacketHandler() {
        }
        LobbyPacketHandler.prototype.handleTableSnapshotList = function (snapshots) {
        };
        LobbyPacketHandler.prototype.handleTableUpdateList = function (updates) {
        };
        LobbyPacketHandler.prototype.handleTableRemoved = function (tableId) {
        };
        LobbyPacketHandler.prototype.handleTournamentSnapshotList = function (snapshots) {
        };
        LobbyPacketHandler.prototype.handleTournamentUpdates = function (updates) {
        };
        LobbyPacketHandler.prototype.handleTournamentRemoved = function (tournamentId) {
        };
        return LobbyPacketHandler;
    })();
    net.LobbyPacketHandler = LobbyPacketHandler;
})(net || (net = {}));
//@ sourceMappingURL=LobbyHandler.js.map
