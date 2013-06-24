var net;
(function (net) {
    var HandHistoryPacketHandler = (function () {
        function HandHistoryPacketHandler() {
        }
        HandHistoryPacketHandler.prototype.handleHandIds = function (tableId, handIds) {
        };
        HandHistoryPacketHandler.prototype.handleHandSummaries = function (tableId, handSummaries) {
        };

        HandHistoryPacketHandler.prototype.handleHands = function (tableId, hands) {
        };
        HandHistoryPacketHandler.prototype.handleHand = function (hand) {
        };
        return HandHistoryPacketHandler;
    })();
    net.HandHistoryPacketHandler = HandHistoryPacketHandler;
})(net || (net = {}));
//@ sourceMappingURL=HandHistoryHandler.js.map
