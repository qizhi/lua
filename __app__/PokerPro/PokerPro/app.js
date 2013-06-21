///<reference path="dtd/firebase.d.ts"/>
///<reference path="src/net/SocketManager.ts"/>
window.onload = function () {
    var requestHost = window.location.hostname;
    var webSocketUrl = requestHost ? requestHost : "localhost";
    var webSocketPort = 9191;
    var tournamentLobbyUpdateInterval = 10000;

    var by = new FIREBASE.ByteArray();
    console.log("connecting to WS: ", webSocketUrl, ",", webSocketPort, ",", FIREBASE.ErrorCodes.IO_ADAPTER_ERROR, ",", FIREBASE.ConnectionStatus.CANCELLED, ",", FIREBASE.ConnectionStatus.toString(FIREBASE.ConnectionStatus.CONNECTED), ",", by.getBuffer(), ",", by.remaining(), ",", utf8.fromByteArray([122]));

    net.SocketManager.getInstance().initialize(webSocketUrl, webSocketPort);
};
//@ sourceMappingURL=app.js.map
