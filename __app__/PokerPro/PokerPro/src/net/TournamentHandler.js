///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/TournamentManager.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/Player.ts"/>
///<reference path="TableHandler.ts"/>
///<reference path="SocketManager.ts"/>
var net;
(function (net) {
    var TournamentPacketHandler = (function () {
        function TournamentPacketHandler() {
            this.tournamentManager = data.TournamentManager.getInstance();
            this.tableManager = data.TableManager.getInstance();
        }
        TournamentPacketHandler.prototype.handleTournamentTransport = function (packet) {
            console.log("Got tournament transport");

            var valueArray = FIREBASE.ByteArray.fromBase64String(packet.mttdata);
            var gameData = new FIREBASE.ByteArray(valueArray);
            var length = gameData.readInt();
            var classId = gameData.readUnsignedByte();
            var tournamentPacket = com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create(classId, gameData);

            var tournamentManager = this.tournamentManager;
            switch (tournamentPacket.classId()) {
                case com.cubeia.games.poker.io.protocol.TournamentOut.CLASSID:
                    this.handleTournamentOut(tournamentPacket);
                    break;
                case com.cubeia.games.poker.io.protocol.TournamentLobbyData.CLASSID:
                    tournamentManager.handleTournamentLobbyData(packet.mttid, tournamentPacket);
                    break;
                case com.cubeia.games.poker.io.protocol.TournamentTable.CLASSID:
                    this.handleTournamentTable(tournamentPacket);
                    break;
                case com.cubeia.games.poker.io.protocol.TournamentRegistrationInfo.CLASSID:
                    this.handleTournamentRegistrationInfo(packet.mttid, tournamentPacket);
                    break;
                default:
                    console.log("Unhandled tournament packet");
                    console.log(tournamentPacket);
            }
        };
        TournamentPacketHandler.prototype.handleTournamentTable = function (tournamentPacket) {
            if (tournamentPacket.tableId != -1) {
                console.log(tournamentPacket);

                //TODO: we need snapshot to get capacity
                new net.TableRequestHandler(tournamentPacket.tableId).openTable(10);
            } else {
                console.log("Unable to find table in tournament");
            }
        };
        TournamentPacketHandler.prototype.handleTournamentRegistrationInfo = function (tournamentId, registrationInfo) {
            console.log(registrationInfo);
            new TournamentPacketHandler().handleTournamentBuyInInfo(tournamentId, registrationInfo);
        };
        TournamentPacketHandler.prototype.handleTournamentOut = function (packet) {
        };
        TournamentPacketHandler.prototype.handleRemovedFromTournamentTable = function (packet) {
            console.log("Removed from table " + packet.tableid + " in tournament " + packet.mttid + " keep watching? " + packet.keepWatching);
            this.tournamentManager.onRemovedFromTournament(packet.tableid, data.Player.getInstance().id);
        };
        TournamentPacketHandler.prototype.handleSeatedAtTournamentTable = function (seated) {
            console.log("I was seated in a tournament, opening table");
            console.log(seated);
            this.tournamentManager.setTournamentTable(seated.mttid, seated.tableid);
            new net.TableRequestHandler(seated.tableid).joinTable();

            this.tableManager.handleOpenTableAccepted(seated.tableid, 10);
        };
        TournamentPacketHandler.prototype.handleRegistrationResponse = function (registrationResponse) {
            console.log("Registration response:");
            console.log(registrationResponse);

            if (registrationResponse.status == FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK) {
                this.tournamentManager.handleRegistrationSuccessful(registrationResponse.mttid);
            } else {
                this.tournamentManager.handleRegistrationFailure(registrationResponse.mttid);
            }
        };
        TournamentPacketHandler.prototype.handleUnregistrationResponse = function (unregistrationResponse) {
            console.log("Unregistration response:");
            console.log(unregistrationResponse);
            if (unregistrationResponse.status == FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK) {
                this.tournamentManager.handleUnregistrationSuccessful(unregistrationResponse.mttid);
            } else {
                this.tournamentManager.handleUnregistrationFailure(unregistrationResponse.mttid);
            }
        };
        TournamentPacketHandler.prototype.handleNotifyRegistered = function (packet) {
            this.tournamentManager.openTournamentLobbies(packet.tournaments);
        };
        TournamentPacketHandler.prototype.handleTournamentBuyInInfo = function (tournamentId, packet) {
            this.tournamentManager.onBuyInInfo(tournamentId, packet.buyIn, packet.fee, packet.currency, packet.balanceInWallet, packet.sufficientFunds);
        };
        return TournamentPacketHandler;
    })();
    net.TournamentPacketHandler = TournamentPacketHandler;
    var TournamentRequestHandler = (function () {
        function TournamentRequestHandler(tournamentId) {
            this.tournamentId = tournamentId;
            this.connector = net.SocketManager.getInstance().getConnector();
        }
        TournamentRequestHandler.prototype.registerToTournament = function () {
            console.log("TournamentRequestHandler.registerToTournament");
            var registrationRequest = new FB_PROTOCOL.MttRegisterRequestPacket();
            registrationRequest.mttid = this.tournamentId;
            this.connector.sendProtocolObject(registrationRequest);
        };
        TournamentRequestHandler.prototype.unregisterFromTournament = function () {
            console.log("TournamentRequestHandler.registerToTournament");
            var unregistrationRequest = new FB_PROTOCOL.MttUnregisterRequestPacket();
            unregistrationRequest.mttid = this.tournamentId;
            this.connector.sendProtocolObject(unregistrationRequest);
        };
        TournamentRequestHandler.prototype.requestTournamentInfo = function () {
            this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData());
        };
        TournamentRequestHandler.prototype.createMttPacket = function () {
            var mtt = new FB_PROTOCOL.MttTransportPacket();
            mtt.mttid = this.tournamentId;
            mtt.pid = data.Player.getInstance().id;
            return mtt;
        };
        TournamentRequestHandler.prototype.leaveTournamentLobby = function () {
            data.TournamentManager.getInstance().removeTournament(this.tournamentId);
        };
        TournamentRequestHandler.prototype.takeSeat = function () {
            console.log("sending request tournament table");
            this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentTable());
        };
        TournamentRequestHandler.prototype.requestBuyInInfo = function () {
            console.log("sending request for registration info");
            this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo());
        };

        /**
        * Sends an empty packet to a tournament. Because the packet is empty we will only store the classId of the packet (there's a bug in Styx).
        * @param packet
        */
        TournamentRequestHandler.prototype.sendEmptyPacketToTournament = function (packet) {
            var mtt = this.createMttPacket();
            var byteArray = new FIREBASE.ByteArray();
            mtt.mttdata = FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(packet.classId()));
            console.log(mtt);
            this.connector.sendProtocolObject(mtt);
        };
        return TournamentRequestHandler;
    })();
    net.TournamentRequestHandler = TournamentRequestHandler;
})(net || (net = {}));
//@ sourceMappingURL=TournamentHandler.js.map
