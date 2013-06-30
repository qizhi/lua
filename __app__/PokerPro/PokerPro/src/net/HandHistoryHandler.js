///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/HandHistoryManager.ts"/>
///<reference path="../data/Player.ts"/>
///<reference path="SocketManager.ts"/>
var net;
(function (net) {
    var HandHistoryPacketHandler = (function () {
        function HandHistoryPacketHandler() {
            this.handHistoryManager = data.HandHistoryManager.getInstance();
        }
        HandHistoryPacketHandler.prototype.handleHandIds = function (tableId, handIds) {
        };
        HandHistoryPacketHandler.prototype.handleHandSummaries = function (tableId, handSummaries) {
            var jsonData = JSON.parse(handSummaries);
            this.handHistoryManager.showHandSummaries(tableId, jsonData);
        };

        HandHistoryPacketHandler.prototype.handleHands = function (tableId, hands) {
        };

        HandHistoryPacketHandler.prototype.handleHand = function (hand) {
            var jsonData = JSON.parse(hand);
            this.handHistoryManager.showHand(jsonData[0]);
        };
        return HandHistoryPacketHandler;
    })();
    net.HandHistoryPacketHandler = HandHistoryPacketHandler;

    var HandHistoryRequestHandler = (function () {
        function HandHistoryRequestHandler(tableId) {
            this.tableId = tableId;
        }
        HandHistoryRequestHandler.prototype.requestHandIds = function (count) {
            console.log("Requesting hands for table " + this.tableId);
            var handIdRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHandIds();
            handIdRequest.tableId = this.tableId;
            handIdRequest.count = count;
            handIdRequest.time = "" + new Date().getTime();
            this.sendPacket(handIdRequest);
        };
        HandHistoryRequestHandler.prototype.requestHandSummaries = function (count) {
            console.log("Requesting hands for table " + this.tableId);
            var handIdRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHandSummaries();
            handIdRequest.tableId = this.tableId;
            handIdRequest.count = count;
            handIdRequest.time = "" + new Date().getTime();
            this.sendPacket(handIdRequest);
        };
        HandHistoryRequestHandler.prototype.requestHands = function (count) {
            var handsRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHands();
            handsRequest.tableId = this.tableId;
            handsRequest.count = count;
            handsRequest.time = "" + new Date().getTime();
            this.sendPacket(handsRequest);
        };
        HandHistoryRequestHandler.prototype.requestHand = function (handId) {
            var handRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHand();
            handRequest.handId = handId;
            this.sendPacket(handRequest);
        };
        HandHistoryRequestHandler.prototype.sendPacket = function (historyRequest) {
            var packet = new FB_PROTOCOL.ServiceTransportPacket();
            packet.pid = data.Player.getInstance().id;
            packet.seq = 0;
            packet.idtype = 0;
            packet.service = "ns://www.cubeia.com/poker/handhistory/provider-service";
            packet.servicedata = FIREBASE.ByteArray.toBase64String(historyRequest.save().createGameDataArray(historyRequest.classId()));

            net.SocketManager.getInstance().getConnector().sendProtocolObject(packet);
        };
        return HandHistoryRequestHandler;
    })();
    net.HandHistoryRequestHandler = HandHistoryRequestHandler;
})(net || (net = {}));
//@ sourceMappingURL=HandHistoryHandler.js.map
