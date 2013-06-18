"use strict";
var Poker = Poker || {};
/**
 * Handles lobby related requests
 * @type {Poker.LobbyRequestHandler}
 */
Poker.LobbyRequestHandler = Class.extend({

    /**
     * @type FIREBASE.Connector
     */
    connector : null,

    init : function() {
        this.connector = Poker.AppCtx.getConnector();
    },
    unsubscribe : function() {
        if (Poker.Unsubscribe!=null) {
            Poker.AppCtx.getLobbyManager().clearLobby();
            Poker.Unsubscribe();
        } else {
            console.log("No unsubscribe function defined.");
        }
    },
    subscribeToCashGames : function() {
        this.unsubscribe();

        this.connector.lobbySubscribe(1, "/texas");
        Poker.AppCtx.getLobbyManager().clearLobby();
        Poker.Unsubscribe  = function() {
            console.log("Unsubscribing from cash games.");
            var unsubscribeRequest = new FB_PROTOCOL.LobbyUnsubscribePacket();
            unsubscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.REGULAR;
            unsubscribeRequest.gameid = 1;
            unsubscribeRequest.address = "/texas";
            Poker.AppCtx.getConnector().sendProtocolObject(unsubscribeRequest);
        }
    },

    subscribeToSitAndGos : function() {
        Poker.AppCtx.getLobbyManager().clearLobby();
        this.subscribeToTournamentsWithPath("/sitandgo")
    },

    subscribeToTournaments : function() {
        Poker.AppCtx.getLobbyManager().clearLobby();
        this.subscribeToTournamentsWithPath("/scheduled");
    },

    subscribeToTournamentsWithPath : function(path) {
        this.unsubscribe();
        console.log("Subscribing to tournaments with path " + path);
        var subscribeRequest = new FB_PROTOCOL.LobbySubscribePacket();
        subscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.MTT;
        subscribeRequest.gameid = 1;
        subscribeRequest.address = path;
        Poker.AppCtx.getConnector().sendProtocolObject(subscribeRequest);
        console.log(subscribeRequest);
        Poker.Unsubscribe  = function() {
            console.log("Unsubscribing from tournaments, path  = " + path);
            var unsubscribeRequest = new FB_PROTOCOL.LobbyUnsubscribePacket();
            unsubscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.MTT;
            unsubscribeRequest.gameid = 1;
            unsubscribeRequest.address = path;
            Poker.AppCtx.getConnector().sendProtocolObject(unsubscribeRequest);
        }
    }
});