///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/TournamentManager.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/Player.ts"/>
///<reference path="TableHandler.ts"/>
///<reference path="SocketManager.ts"/>
module net {
    export class TournamentPacketHandler {
        tournamentManager: data.TournamentManager;
        tableManager: data.TableManager;

        constructor() {
            this.tournamentManager = data.TournamentManager.getInstance();
            this.tableManager = data.TableManager.getInstance();
        }

        handleTournamentTransport(packet: any): void {
            console.log("Got tournament transport");

            var valueArray = FIREBASE.ByteArray.fromBase64String(packet.mttdata);
            var gameData = new FIREBASE.ByteArray(valueArray);
            var length = gameData.readInt(); // drugs.
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
        }
        handleTournamentTable(tournamentPacket: any): void {
            if (tournamentPacket.tableId != -1) {
                console.log(tournamentPacket);
                //TODO: we need snapshot to get capacity
                new TableRequestHandler(tournamentPacket.tableId).openTable(10);
            } else {
                console.log("Unable to find table in tournament");
            }
        }
        handleTournamentRegistrationInfo(tournamentId: number, registrationInfo: any): void {
            console.log(registrationInfo);
            new TournamentPacketHandler().handleTournamentBuyInInfo(tournamentId, registrationInfo);
        }
        handleTournamentOut(packet: any): void {
            /*var dialogManager = Poker.AppCtx.getDialogManager();
            if (packet.position == 1) {
                dialogManager.displayGenericDialog(
                    { header: i18n.t("dialogs.tournament-won.header"), message: i18n.t("dialogs.tournament-won.message") }
                    );
            } else {
                dialogManager.displayGenericDialog({
                    header: i18n.t("dialogs.tournament-out.header"),
                    message: i18n.t("dialogs.tournament-out.message", { sprintf: [packet.position] })
                });
            }
            */
        }
        handleRemovedFromTournamentTable(packet: any): void {
            console.log("Removed from table " + packet.tableid + " in tournament " + packet.mttid + " keep watching? " + packet.keepWatching);
            this.tournamentManager.onRemovedFromTournament(packet.tableid, data.Player.getInstance().id);
        }
        handleSeatedAtTournamentTable(seated: any): void {
            console.log("I was seated in a tournament, opening table");
            console.log(seated);
            this.tournamentManager.setTournamentTable(seated.mttid, seated.tableid);
            new TableRequestHandler(seated.tableid).joinTable();

            this.tableManager.handleOpenTableAccepted(seated.tableid, 10);
        }
        handleRegistrationResponse(registrationResponse: any): void {
            console.log("Registration response:");
            console.log(registrationResponse);

            if (registrationResponse.status == FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK) {
                this.tournamentManager.handleRegistrationSuccessful(registrationResponse.mttid);
            } else {
                this.tournamentManager.handleRegistrationFailure(registrationResponse.mttid);
            }
        }
        handleUnregistrationResponse(unregistrationResponse: any): void {
            console.log("Unregistration response:");
            console.log(unregistrationResponse);
            if (unregistrationResponse.status == FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK) {
                this.tournamentManager.handleUnregistrationSuccessful(unregistrationResponse.mttid);
            } else {
                this.tournamentManager.handleUnregistrationFailure(unregistrationResponse.mttid)

        }
        }
        handleNotifyRegistered(packet: any): void {
            this.tournamentManager.openTournamentLobbies(packet.tournaments);
        }
        handleTournamentBuyInInfo(tournamentId: number, packet: any): any {
            this.tournamentManager.onBuyInInfo(tournamentId, packet.buyIn, packet.fee, packet.currency, packet.balanceInWallet, packet.sufficientFunds);
        }
    }
    export class TournamentRequestHandler {
        private connector: FIREBASE.Connector;
        constructor(public tournamentId: number) {
            this.connector = SocketManager.getInstance().getConnector();
        }

        registerToTournament(): void {
            console.log("TournamentRequestHandler.registerToTournament");
            var registrationRequest = new FB_PROTOCOL.MttRegisterRequestPacket();
            registrationRequest.mttid = this.tournamentId;
            this.connector.sendProtocolObject(registrationRequest);
        }
        unregisterFromTournament(): void {
            console.log("TournamentRequestHandler.registerToTournament");
            var unregistrationRequest = new FB_PROTOCOL.MttUnregisterRequestPacket();
            unregistrationRequest.mttid = this.tournamentId;
            this.connector.sendProtocolObject(unregistrationRequest);
        }
        requestTournamentInfo(): void {
            this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData());
        }
        createMttPacket(): any {
            var mtt = new FB_PROTOCOL.MttTransportPacket();
            mtt.mttid = this.tournamentId;
            mtt.pid =data.Player.getInstance().id;
            return mtt;
        }
        leaveTournamentLobby(): void {
            data.TournamentManager.getInstance().removeTournament(this.tournamentId);
            //Poker.AppCtx.getViewManager().removeTournamentView(this.tournamentId);
        }
        takeSeat(): void {
            console.log("sending request tournament table");
            this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentTable());
        }
        requestBuyInInfo(): void {
            console.log("sending request for registration info");
            this.sendEmptyPacketToTournament(new com.cubeia.games.poker.io.protocol.RequestTournamentRegistrationInfo());
        }
        /**
         * Sends an empty packet to a tournament. Because the packet is empty we will only store the classId of the packet (there's a bug in Styx).
         * @param packet
         */
        sendEmptyPacketToTournament(packet: any): void {
            var mtt = this.createMttPacket();
            var byteArray = new FIREBASE.ByteArray(); //not using  playerListRequest.save() because of a styx bug
            mtt.mttdata = FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(packet.classId()));
            console.log(mtt);
            this.connector.sendProtocolObject(mtt);
        }
    }
}