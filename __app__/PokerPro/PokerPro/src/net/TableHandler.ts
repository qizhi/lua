///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/Table.ts"/>
///<reference path="SocketManager.ts"/>
module net {
    export class TablePacketHandler {
        tableManager: data.TableManager;

        constructor(public tableId: number) {
            this.tableManager = data.TableManager.getInstance();
        }

        handleSeatInfo(seatInfoPacket: any): void {
            console.log(seatInfoPacket);
            console.log("seatInfo pid[" + seatInfoPacket.player.pid + "]  seat[" + seatInfoPacket.seat + "]");
            console.log(seatInfoPacket);
            this.tableManager.addPlayer(seatInfoPacket.tableid, seatInfoPacket.seat, seatInfoPacket.player.pid, seatInfoPacket.player.nick);
        }
        handleNotifyLeave(notifyLeavePacket: any) {
            this.tableManager.removePlayer(notifyLeavePacket.tableid, notifyLeavePacket.pid);
        }
        //When you are notified that you are already sitting at a table when logging in
        handleSeatedAtTable(packet: any) {
            new TableRequestHandler(packet.tableid).joinTable();
            var data = util.ProtocolUtils.extractTableData(packet.snapshot);
            this.tableManager.tableNames.put(packet.tableid, util.ProtocolUtils.getTableName(data));
            this.tableManager.handleOpenTableAccepted(packet.tableid, data.capacity);
        }
        handleNotifyJoin(notifyJoinPacket: any) {
            this.tableManager.addPlayer(notifyJoinPacket.tableid, notifyJoinPacket.seat, notifyJoinPacket.pid, notifyJoinPacket.nick);
        }
        handleJoinResponse(joinResponsePacket: any) {
            console.log(joinResponsePacket);
            console.log("join response seat = " + joinResponsePacket.seat + " player id = " + data.Player.getInstance().id);
            if (joinResponsePacket.status === FB_PROTOCOL.JoinResponseStatusEnum.OK) {
                this.tableManager.addPlayer(joinResponsePacket.tableid, joinResponsePacket.seat, data.Player.getInstance().id, data.Player.getInstance().name);
            } else {
                console.log("Join failed. Status: " + joinResponsePacket.status);
            }
        }
        handleUnwatchResponse(unwatchResponse: any) {
            console.log("Unwatch response = ");
            console.log(unwatchResponse);
            this.tableManager.leaveTable(unwatchResponse.tableid);
        }
        handleLeaveResponse(leaveResponse: any) {
            console.log("leave response: ");
            console.log(leaveResponse);
            this.tableManager.leaveTable(leaveResponse.tableid);
            //Poker.AppCtx.getViewManager().removeTableView(leaveResponse.tableid);
        }
        handleWatchResponse(watchResponse: any) {
            if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.DENIED_ALREADY_SEATED) {
                new TableRequestHandler(this.tableId).joinTable();
            } else if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.OK) {
                //this.tableManager.clearTable()
            } else if (watchResponse.status == FB_PROTOCOL.WatchResponseStatusEnum.FAILED) {
                /*Poker.AppCtx.getDialogManager().displayGenericDialog({
                    tableId: this.tableId,
                    translationKey: "watch-table-failed"
                }, function () {
                        Poker.AppCtx.getTableManager().leaveTable(self.tableId);
                    });
                */
            }
        }
        handleChatMessage(chatPacket: any) {
            console.log("Handle chat message");
            console.log(chatPacket);
            this.tableManager.onChatMessage(this.tableId, chatPacket.pid, chatPacket.message);
        }
    }

    export class TableRequestHandler {
        private connector: FIREBASE.Connector;
        private tableManager: data.TableManager;
        constructor(public tableId: number) {
            this.connector = net.SocketManager.getInstance().getConnector();
            this.tableManager = data.TableManager.getInstance();
        }

        public joinTable(): void {
            this.connector.joinTable(this.tableId, -1);
        }

        public openTableWithName(capacity:number, name:string):void {
            this.tableManager.tableNames.put(this.tableId, name);
            this.openTable(capacity);
        }
        public openTable(capacity: number): void {
            var t: data.Table = this.tableManager.getTable(this.tableId);
            if (t != null) {
                //Poker.AppCtx.getViewManager().activateViewByTableId(this.tableId);
            } else {
                this.tableManager.handleOpenTableAccepted(this.tableId, capacity);
                this.connector.watchTable(this.tableId);
            }
        }
        public reactivateTable(): void {
            this.connector.watchTable(this.tableId);
        }
        public leaveTable(): void {
            this.connector.leaveTable(this.tableId);
        }
        public unwatchTable(): void {
            var unwatchRequest = new FB_PROTOCOL.UnwatchRequestPacket();
            unwatchRequest.tableid = this.tableId;
            this.connector.sendProtocolObject(unwatchRequest);
        }
        public sendChatMessage(message: any): void {
            /*message = util.Utils.filterMessage(message);
            if (message != null && $.trim(message).length > 0) {
                var chatPacket = new FB_PROTOCOL.TableChatPacket();
                chatPacket.pid = data.Player.getInstance().id;
                chatPacket.message = message;
                chatPacket.tableid = this.tableId;
                
                SocketManager.getInstance().getConnector.sendProtocolObject(chatPacket);
                $.ga._trackEvent("table_chat", "send_message");
            }
*/
        }
    }
}