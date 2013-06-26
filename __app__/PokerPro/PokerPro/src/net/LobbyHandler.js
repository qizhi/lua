///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="SocketManager.ts"/>
///<reference path="../data/LobbyManager.ts"/>
var net;
(function (net) {
    var LobbyPacketHandler = (function () {
        function LobbyPacketHandler() {
            this.lobbyManager = data.LobbyManager.getInstance();
        }
        LobbyPacketHandler.prototype.handleTableSnapshotList = function (snapshots) {
            this.lobbyManager.handleTableSnapshotList(snapshots);
        };
        LobbyPacketHandler.prototype.handleTableUpdateList = function (updates) {
            this.lobbyManager.handleTableUpdateList(updates);
        };
        LobbyPacketHandler.prototype.handleTableRemoved = function (tableId) {
            this.lobbyManager.handleTableRemoved(tableId);
        };
        LobbyPacketHandler.prototype.handleTournamentSnapshotList = function (snapshots) {
            this.lobbyManager.handleTournamentSnapshotList(snapshots);
        };
        LobbyPacketHandler.prototype.handleTournamentUpdates = function (updates) {
            this.lobbyManager.handleTournamentUpdates(updates);
        };
        LobbyPacketHandler.prototype.handleTournamentRemoved = function (tournamentId) {
            this.lobbyManager.handleTournamentRemoved(tournamentId);
        };
        return LobbyPacketHandler;
    })();
    net.LobbyPacketHandler = LobbyPacketHandler;

    var LobbyRequestHandler = (function () {
        function LobbyRequestHandler() {
            this.connector = net.SocketManager.getInstance().getConnector();
        }
        LobbyRequestHandler.prototype.subscribeToCashGames = function () {
            this.unsubscribe();

            this.connector.lobbySubscribe(1, "/texas");

            data.LobbyManager.getInstance().clearLobby();
            Unsubscribe.unsubscribe = function () {
                console.log("Unsubscribing from cash games.");
                var unsubscribeRequest = new FB_PROTOCOL.LobbyUnsubscribePacket();
                unsubscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.REGULAR;
                unsubscribeRequest.gameid = 1;
                unsubscribeRequest.address = "/texas";
                net.SocketManager.getInstance().getConnector().sendProtocolObject(unsubscribeRequest);
            };
        };

        LobbyRequestHandler.prototype.subscribeToSitAndGos = function () {
            data.LobbyManager.getInstance().clearLobby();
            this.subscribeToTournamentsWithPath("/sitandgo");
        };

        LobbyRequestHandler.prototype.subscribeToTournaments = function () {
            data.LobbyManager.getInstance().clearLobby();
            this.subscribeToTournamentsWithPath("/scheduled");
        };

        LobbyRequestHandler.prototype.subscribeToTournamentsWithPath = function (path) {
            this.unsubscribe();

            var subscribeRequest = new FB_PROTOCOL.LobbySubscribePacket();
            subscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.MTT;
            subscribeRequest.gameid = 1;
            subscribeRequest.address = path;
            this.connector.sendProtocolObject(subscribeRequest);

            console.log("Subscribing to tournaments with path " + path, subscribeRequest);

            Unsubscribe.unsubscribe = function () {
                console.log("Unsubscribing from tournaments, path  = " + path);
                var unsubscribeRequest = new FB_PROTOCOL.LobbyUnsubscribePacket();
                unsubscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.MTT;
                unsubscribeRequest.gameid = 1;
                unsubscribeRequest.address = path;
                net.SocketManager.getInstance().getConnector().sendProtocolObject(unsubscribeRequest);
            };
        };

        LobbyRequestHandler.prototype.unsubscribe = function () {
            if (Unsubscribe.unsubscribe != null) {
                data.LobbyManager.getInstance().clearLobby();
                Unsubscribe.unsubscribe();
            } else {
                console.log("No unsubscribe function defined.");
            }
        };
        return LobbyRequestHandler;
    })();
    net.LobbyRequestHandler = LobbyRequestHandler;

    var Unsubscribe = (function () {
        function Unsubscribe() {
        }
        Unsubscribe.unsubscribe = null;
        return Unsubscribe;
    })();
})(net || (net = {}));
//@ sourceMappingURL=LobbyHandler.js.map
