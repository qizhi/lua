"use strict";
var Poker = Poker || {};

/**
 * Handles all table related requests
 *
 * Usage:
 *
 *  new Poker.TableRequestHandler(tableId).joinTable();
 *
 * @type {Poker.TableRequestHandler}
 */
Poker.TableRequestHandler = Class.extend({
    /**
     * @type FIREBASE.Connector
     */
    connector : null,

    /**
     *  @type Number
     */
    tableId : null,

    /**
     * @type Poker.TableManager
     */
    tableManager : null,

    init : function(tableId){
        if(typeof(tableId) == "undefined") {
            throw "Poker.TableRequestHandler Table id must be set"
        }
        this.tableId = tableId;
        this.connector = Poker.AppCtx.getConnector();
        this.tableManager = Poker.AppCtx.getTableManager();
    },
    joinTable : function() {
        this.connector.joinTable(this.tableId, -1);
    },

    openTableWithName : function(capacity,name){
        this.tableManager.tableNames.put(this.tableId,name);
        this.openTable(capacity);
    },
    openTable : function (capacity) {
        var t = this.tableManager.getTable(this.tableId);
        if(t!=null) {
            Poker.AppCtx.getViewManager().activateViewByTableId(this.tableId);
        } else {
            this.tableManager.handleOpenTableAccepted(this.tableId,capacity);
            this.connector.watchTable(this.tableId);
        }
    },
    reactivateTable : function() {
        this.connector.watchTable(this.tableId);
    },
    leaveTable : function () {
        this.connector.leaveTable(this.tableId);
    },
    unwatchTable : function () {
        var unwatchRequest = new FB_PROTOCOL.UnwatchRequestPacket();
        unwatchRequest.tableid = this.tableId;
        this.connector.sendProtocolObject(unwatchRequest);
    },
    sendChatMessage : function(message) {
        message = Poker.Utils.filterMessage(message);
        if(message!=null && $.trim(message).length>0) {
            var chatPacket = new FB_PROTOCOL.TableChatPacket();
            chatPacket.pid = Poker.MyPlayer.id;
            chatPacket.message = message;
            chatPacket.tableid = this.tableId;
            Poker.AppCtx.getConnector().sendProtocolObject(chatPacket);
            $.ga._trackEvent("table_chat", "send_message");
        }

    }
});