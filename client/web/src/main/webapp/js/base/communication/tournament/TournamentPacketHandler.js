"use strict";
var Poker = Poker || {};
/**
 *
 * @type {Poker.TournamentPacketHandler}
 */
Poker.TournamentPacketHandler = Class.extend({

    /**
     * @type Poker.TournamentManager
     */
    tournamentManager: null,

    /**
     * @type Poker.TableManager
     */
    tableManager: null,

    /**
     * @type Number
     */
    tournamentId: null,

    /**
     * @constructor
     */
    init: function () {
        this.tournamentManager = Poker.AppCtx.getTournamentManager();
        this.tableManager = Poker.AppCtx.getTableManager();
    },
    handleTournamentTransport: function (packet) {
        console.log("Got tournament transport");

        var valueArray = FIREBASE.ByteArray.fromBase64String(packet.mttdata);
        var gameData = new FIREBASE.ByteArray(valueArray);
        var length = gameData.readInt(); // drugs.
        var classId = gameData.readUnsignedByte();
        var tournamentPacket = com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create(classId, gameData);

        var tournamentManager = Poker.AppCtx.getTournamentManager();
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
    },
    handleTournamentTable: function (tournamentPacket) {
        if (tournamentPacket.tableId != -1) {
            console.log(tournamentPacket);
            //TODO: we need snapshot to get capacity
            new Poker.TableRequestHandler(tournamentPacket.tableId).openTable(10);
        } else {
            console.log("Unable to find table in tournament");
        }
    },
    handleTournamentRegistrationInfo: function (tournamentId, registrationInfo) {
        console.log(registrationInfo);
        new Poker.TournamentPacketHandler().handleTournamentBuyInInfo(tournamentId, registrationInfo);
    },
    handleTournamentOut: function (packet) {
        var dialogManager = Poker.AppCtx.getDialogManager();
        if (packet.position == 1) {
            dialogManager.displayGenericDialog(
                {header: i18n.t("dialogs.tournament-won.header"), message: i18n.t("dialogs.tournament-won.message")}
            );
        } else {
            dialogManager.displayGenericDialog({header: i18n.t("dialogs.tournament-out.header"),
                message: i18n.t("dialogs.tournament-out.message", { sprintf : [packet.position]})});
        }

    },
    handleRemovedFromTournamentTable: function (packet) {
        console.log("Removed from table " + packet.tableid + " in tournament " + packet.mttid + " keep watching? " + packet.keepWatching);
        this.tournamentManager.onRemovedFromTournament(packet.tableid, Poker.MyPlayer.id);
    },
    handleSeatedAtTournamentTable: function (seated) {
        console.log("I was seated in a tournament, opening table");
        console.log(seated);
        this.tournamentManager.setTournamentTable(seated.mttid, seated.tableid);
        new Poker.TableRequestHandler(seated.tableid).joinTable();

        this.tableManager.handleOpenTableAccepted(seated.tableid, 10);
    },
    handleRegistrationResponse: function (registrationResponse) {
        console.log("Registration response:");
        console.log(registrationResponse);

        if (registrationResponse.status == FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK) {
            this.tournamentManager.handleRegistrationSuccessful(registrationResponse.mttid);
        } else {
            this.tournamentManager.handleRegistrationFailure(registrationResponse.mttid);
        }
    },
    handleUnregistrationResponse: function (unregistrationResponse) {
        console.log("Unregistration response:");
        console.log(unregistrationResponse);
        if (unregistrationResponse.status == FB_PROTOCOL.TournamentRegisterResponseStatusEnum.OK) {
            this.tournamentManager.handleUnregistrationSuccessful(unregistrationResponse.mttid);
        } else {
            this.tournamentManager.handleUnregistrationFailure(unregistrationResponse.mttid)

        }
    },
    handleNotifyRegistered: function (packet) {
        this.tournamentManager.openTournamentLobbies(packet.tournaments);
    },
    handleTournamentBuyInInfo: function (tournamentId, packet) {
        this.tournamentManager.onBuyInInfo(tournamentId, packet.buyIn, packet.fee, packet.currency, packet.balanceInWallet, packet.sufficientFunds);
    }


});