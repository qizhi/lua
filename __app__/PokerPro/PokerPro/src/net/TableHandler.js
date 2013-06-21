var net;
(function (net) {
    var TablePacketHandler = (function () {
        function TablePacketHandler(tableId) {
            this.tableId = tableId;
        }
        TablePacketHandler.prototype.handleSeatInfo = function (seatInfoPacket) {
        };
        TablePacketHandler.prototype.handleNotifyLeave = function (notifyLeavePacket) {
        };
        TablePacketHandler.prototype.handleSeatedAtTable = function (packet) {
        };
        TablePacketHandler.prototype.handleNotifyJoin = function (notifyJoinPacket) {
        };
        TablePacketHandler.prototype.handleJoinResponse = function (joinResponsePacket) {
        };
        TablePacketHandler.prototype.handleUnwatchResponse = function (unwatchResponse) {
        };
        TablePacketHandler.prototype.handleLeaveResponse = function (leaveResponse) {
        };
        TablePacketHandler.prototype.handleWatchResponse = function (watchResponse) {
        };
        TablePacketHandler.prototype.handleChatMessage = function (chatPacket) {
        };
        return TablePacketHandler;
    })();
    net.TablePacketHandler = TablePacketHandler;

    var TableRequestHandler = (function () {
        function TableRequestHandler() {
        }
        return TableRequestHandler;
    })();
    net.TableRequestHandler = TableRequestHandler;
})(net || (net = {}));
//@ sourceMappingURL=TableHandler.js.map
