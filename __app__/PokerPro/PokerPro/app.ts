///<reference path="dtd/firebase.d.ts"/>
///<reference path="src/net/SocketManager.ts"/>



window.onload = () => {
    var requestHost:string = window.location.hostname;
    var webSocketUrl:string = requestHost ? requestHost : "localhost";
    var webSocketPort: number = 9191;
    var tournamentLobbyUpdateInterval: number = 10000;

    var by:FIREBASE.ByteArray = new FIREBASE.ByteArray();
    console.log("connecting to WS: ", webSocketUrl, ",", webSocketPort, ",",
        FIREBASE.ErrorCodes.IO_ADAPTER_ERROR, ",", FIREBASE.ConnectionStatus.CANCELLED, ",",
        FIREBASE.ConnectionStatus.toString(FIREBASE.ConnectionStatus.CONNECTED),
        ",", by.getBuffer(), ",", by.remaining() , ",", utf8.fromByteArray([122]));

    net.SocketManager.getInstance().initialize(webSocketUrl, webSocketPort);
};