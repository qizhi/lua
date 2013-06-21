///<reference path="../../dtd/firebase.d.ts"/>
var net;
(function (net) {
    var TournamentPacketHandler = (function () {
        function TournamentPacketHandler(tournamentId) {
            this.tournamentId = tournamentId;
        }
        TournamentPacketHandler.prototype.handleTournamentTransport = function (packet) {
        };
        TournamentPacketHandler.prototype.handleTournamentTable = function (tournamentPacket) {
        };
        TournamentPacketHandler.prototype.handleTournamentRegistrationInfo = function (tournamentId, registrationInfo) {
        };
        TournamentPacketHandler.prototype.handleTournamentOut = function (packet) {
        };
        TournamentPacketHandler.prototype.handleRemovedFromTournamentTable = function (packet) {
        };
        TournamentPacketHandler.prototype.handleSeatedAtTournamentTable = function (seated) {
        };
        TournamentPacketHandler.prototype.handleRegistrationResponse = function (registrationResponse) {
        };
        TournamentPacketHandler.prototype.handleUnregistrationResponse = function (unregistrationResponse) {
        };
        TournamentPacketHandler.prototype.handleNotifyRegistered = function (packet) {
        };
        TournamentPacketHandler.prototype.handleTournamentBuyInInfo = function (tournamentId, packet) {
        };
        return TournamentPacketHandler;
    })();
    net.TournamentPacketHandler = TournamentPacketHandler;
})(net || (net = {}));
//@ sourceMappingURL=TournamentHandler.js.map
