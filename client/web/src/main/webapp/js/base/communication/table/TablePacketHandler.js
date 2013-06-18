"use strict";


Poker.TablePacketHandler = Class.extend({

    /**
     * @type Poker.TableManager
     */
    tableManager : null,

    tableId : null,

    /**
     * @constructor
     * @param {Number} tableId
     */
    init : function(tableId) {
        this.tableManager = Poker.AppCtx.getTableManager();
        this.tableId = tableId;
    },
    handleSeatInfo:function (seatInfoPacket) {
        console.log(seatInfoPacket);
        console.log("seatInfo pid[" + seatInfoPacket.player.pid + "]  seat[" + seatInfoPacket.seat + "]");
        console.log(seatInfoPacket);
        this.tableManager.addPlayer(seatInfoPacket.tableid,seatInfoPacket.seat, seatInfoPacket.player.pid, seatInfoPacket.player.nick);
    },
    handleNotifyLeave:function (notifyLeavePacket) {
        this.tableManager.removePlayer(notifyLeavePacket.tableid,notifyLeavePacket.pid);
    },
    /**
     * When you are notified that you are already sitting at a table when logging in
     * @param packet
     */
    handleSeatedAtTable : function(packet){
        new Poker.TableRequestHandler(packet.tableid).joinTable();
        var data = Poker.ProtocolUtils.extractTableData(packet.snapshot);
        this.tableManager.tableNames.put(packet.tableid,Poker.ProtocolUtils.getTableName(data));
        this.tableManager.handleOpenTableAccepted(packet.tableid, data.capacity);
    },
    handleNotifyJoin:function (notifyJoinPacket) {
        this.tableManager.addPlayer(notifyJoinPacket.tableid,notifyJoinPacket.seat, notifyJoinPacket.pid, notifyJoinPacket.nick);
    },
    handleJoinResponse : function (joinResponsePacket) {
        console.log(joinResponsePacket);
        console.log("join response seat = " + joinResponsePacket.seat + " player id = " + Poker.MyPlayer.id);
        if (joinResponsePacket.status === FB_PROTOCOL.JoinResponseStatusEnum.OK) {
            this.tableManager.addPlayer(joinResponsePacket.tableid,joinResponsePacket.seat, Poker.MyPlayer.id, Poker.MyPlayer.name);
        } else {
            console.log("Join failed. Status: " + joinResponsePacket.status);
        }
    },
    handleUnwatchResponse:function (unwatchResponse) {
        console.log("Unwatch response = ");
        console.log(unwatchResponse);
        this.tableManager.leaveTable(unwatchResponse.tableid);

    },
    handleLeaveResponse:function (leaveResponse) {
        console.log("leave response: ");
        console.log(leaveResponse);
        this.tableManager.leaveTable(leaveResponse.tableid);
        Poker.AppCtx.getViewManager().removeTableView(leaveResponse.tableid);

    },
    handleWatchResponse:function (watchResponse) {
        var self = this;
        if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.DENIED_ALREADY_SEATED) {
            new Poker.TableRequestHandler(this.tableId).joinTable();
        } else if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.OK) {
            //this.tableManager.clearTable()
        } else if(watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.FAILED) {
            Poker.AppCtx.getDialogManager().displayGenericDialog({tableId: this.tableId,
                translationKey : "watch-table-failed"}, function(){
                Poker.AppCtx.getTableManager().leaveTable(self.tableId);
            });
        }
    },
    handleChatMessage : function(chatPacket) {
        console.log("Handle chat message");
        console.log(chatPacket);
        this.tableManager.onChatMessage(this.tableId, chatPacket.pid, chatPacket.message);
    }
});