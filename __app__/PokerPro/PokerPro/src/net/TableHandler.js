///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/Table.ts"/>
///<reference path="SocketManager.ts"/>
var net;
(function (net) {
    var TablePacketHandler = (function () {
        function TablePacketHandler(tableId) {
            this.tableId = tableId;
            this.tableManager = data.TableManager.getInstance();
        }
        TablePacketHandler.prototype.handleSeatInfo = function (seatInfoPacket) {
            console.log(seatInfoPacket);
            console.log("seatInfo pid[" + seatInfoPacket.player.pid + "]  seat[" + seatInfoPacket.seat + "]");
            console.log(seatInfoPacket);
            this.tableManager.addPlayer(seatInfoPacket.tableid, seatInfoPacket.seat, seatInfoPacket.player.pid, seatInfoPacket.player.nick);
        };
        TablePacketHandler.prototype.handleNotifyLeave = function (notifyLeavePacket) {
            this.tableManager.removePlayer(notifyLeavePacket.tableid, notifyLeavePacket.pid);
        };

        //When you are notified that you are already sitting at a table when logging in
        TablePacketHandler.prototype.handleSeatedAtTable = function (packet) {
            new TableRequestHandler(packet.tableid).joinTable();
            var data = util.ProtocolUtils.extractTableData(packet.snapshot);
            this.tableManager.tableNames.put(packet.tableid, util.ProtocolUtils.getTableName(data));
            this.tableManager.handleOpenTableAccepted(packet.tableid, data.capacity);
        };
        TablePacketHandler.prototype.handleNotifyJoin = function (notifyJoinPacket) {
            this.tableManager.addPlayer(notifyJoinPacket.tableid, notifyJoinPacket.seat, notifyJoinPacket.pid, notifyJoinPacket.nick);
        };
        TablePacketHandler.prototype.handleJoinResponse = function (joinResponsePacket) {
            console.log(joinResponsePacket);
            console.log("join response seat = " + joinResponsePacket.seat + " player id = " + data.Player.getInstance().id);
            if (joinResponsePacket.status === FB_PROTOCOL.JoinResponseStatusEnum.OK) {
                this.tableManager.addPlayer(joinResponsePacket.tableid, joinResponsePacket.seat, data.Player.getInstance().id, data.Player.getInstance().name);
            } else {
                console.log("Join failed. Status: " + joinResponsePacket.status);
            }
        };
        TablePacketHandler.prototype.handleUnwatchResponse = function (unwatchResponse) {
            console.log("Unwatch response = ");
            console.log(unwatchResponse);
            this.tableManager.leaveTable(unwatchResponse.tableid);
        };
        TablePacketHandler.prototype.handleLeaveResponse = function (leaveResponse) {
            console.log("leave response: ");
            console.log(leaveResponse);
            this.tableManager.leaveTable(leaveResponse.tableid);
        };
        TablePacketHandler.prototype.handleWatchResponse = function (watchResponse) {
            if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.DENIED_ALREADY_SEATED) {
                new TableRequestHandler(this.tableId).joinTable();
            } else if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.OK) {
            } else if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.FAILED) {
            }
        };
        TablePacketHandler.prototype.handleChatMessage = function (chatPacket) {
            console.log("Handle chat message");
            console.log(chatPacket);
            this.tableManager.onChatMessage(this.tableId, chatPacket.pid, chatPacket.message);
        };
        return TablePacketHandler;
    })();
    net.TablePacketHandler = TablePacketHandler;

    var TableRequestHandler = (function () {
        function TableRequestHandler(tableId) {
            this.tableId = tableId;
            this.connector = net.SocketManager.getInstance().getConnector();
            this.tableManager = data.TableManager.getInstance();
        }
        TableRequestHandler.prototype.joinTable = function () {
            this.connector.joinTable(this.tableId, -1);
        };

        TableRequestHandler.prototype.openTableWithName = function (capacity, name) {
            this.tableManager.tableNames.put(this.tableId, name);
            this.openTable(capacity);
        };
        TableRequestHandler.prototype.openTable = function (capacity) {
            var t = this.tableManager.getTable(this.tableId);
            if (t != null) {
            } else {
                this.tableManager.handleOpenTableAccepted(this.tableId, capacity);
                this.connector.watchTable(this.tableId);
            }
        };
        TableRequestHandler.prototype.reactivateTable = function () {
            this.connector.watchTable(this.tableId);
        };
        TableRequestHandler.prototype.leaveTable = function () {
            this.connector.leaveTable(this.tableId);
        };
        TableRequestHandler.prototype.unwatchTable = function () {
            var unwatchRequest = new FB_PROTOCOL.UnwatchRequestPacket();
            unwatchRequest.tableid = this.tableId;
            this.connector.sendProtocolObject(unwatchRequest);
        };
        TableRequestHandler.prototype.sendChatMessage = function (message) {
        };
        return TableRequestHandler;
    })();
    net.TableRequestHandler = TableRequestHandler;
})(net || (net = {}));
//@ sourceMappingURL=TableHandler.js.map
