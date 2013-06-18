"use strict";
var Poker = Poker || {};
Poker.TournamentRequestHandler = Class.extend({

    /**
     * @type FIREBASE.Connector
     */
    connector : null,

    /**
     * @type Number
     */
    tournamentId : null,

    /**
     *
     * @param {Number} tournamentId
     * @constructor
     */
    init : function(tournamentId) {
        this.tournamentId = tournamentId;
        this.connector = Poker.AppCtx.getConnector();
    },
    registerToTournament : function(){
        console.log("TournamentRequestHandler.registerToTournament");
        var registrationRequest = new FB_PROTOCOL.MttRegisterRequestPacket();
        registrationRequest.mttid = this.tournamentId;
        this.connector.sendProtocolObject(registrationRequest);
    },
    unregisterFromTournament : function(){
        console.log("TournamentRequestHandler.registerToTournament");
        var unregistrationRequest = new FB_PROTOCOL.MttUnregisterRequestPacket();
        unregistrationRequest.mttid = this.tournamentId;
        this.connector.sendProtocolObject(unregistrationRequest);
    },
    requestTournamentInfo : function() {
        this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData());
    },
    createMttPacket : function() {
        var mtt = new FB_PROTOCOL.MttTransportPacket();
        mtt.mttid = this.tournamentId;
        mtt.pid = Poker.MyPlayer.id;
        return mtt;
    },
    leaveTournamentLobby : function() {
        Poker.AppCtx.getTournamentManager().removeTournament(this.tournamentId);
        Poker.AppCtx.getViewManager().removeTournamentView(this.tournamentId);
    },
    takeSeat : function() {
        console.log("sending request tournament table");
        this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentTable());
    },
    requestBuyInInfo : function() {
        console.log("sending request for registration info");
        this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo());
    },
    /**
     * Sends an empty packet to a tournament. Because the packet is empty we will only store the classId of the packet (there's a bug in Styx).
     * @param packet
     */
    sendEmptyPacketToTournament : function(packet) {
        var mtt = this.createMttPacket();
        var byteArray = new FIREBASE.ByteArray(); //not using  playerListRequest.save() because of a styx bug
        mtt.mttdata  = FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(packet.classId()));
        console.log(mtt);
        this.connector.sendProtocolObject(mtt);
    }
});