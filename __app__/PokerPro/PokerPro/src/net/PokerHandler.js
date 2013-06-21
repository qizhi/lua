var net;
(function (net) {
    var PokerPacketHandler = (function () {
        function PokerPacketHandler(tableId) {
            this.tableId = tableId;
        }
        PokerPacketHandler.prototype.handleRequestAction = function (requestAction) {
        };

        PokerPacketHandler.prototype.handleRebuyOffer = function (rebuyOffer, playerId) {
        };
        PokerPacketHandler.prototype.handleAddOnOffer = function (addOnOffer, playerId) {
        };
        PokerPacketHandler.prototype.handleAddOnPeriodClosed = function (playerId) {
        };
        PokerPacketHandler.prototype.handleRebuyPerformed = function (playerId) {
        };
        PokerPacketHandler.prototype.handleAddOnPerformed = function (playerId) {
        };
        PokerPacketHandler.prototype.handlePlayerBalance = function (packet) {
        };
        PokerPacketHandler.prototype.handlePlayerHandStartStatus = function (packet) {
        };
        PokerPacketHandler.prototype.handleBuyIn = function (protocolObject) {
        };
        PokerPacketHandler.prototype.handlePerformAction = function (performAction) {
        };
        PokerPacketHandler.prototype.handleDealPublicCards = function (packet) {
        };
        PokerPacketHandler.prototype.handleDealPrivateCards = function (protocolObject) {
        };
        PokerPacketHandler.prototype.handleExposePrivateCards = function (packet) {
        };
        PokerPacketHandler.prototype.handlePlayerPokerStatus = function (packet) {
        };
        PokerPacketHandler.prototype.handlePotTransfers = function (packet) {
        };
        PokerPacketHandler.prototype.handleFuturePlayerAction = function (packet) {
        };
        return PokerPacketHandler;
    })();
    net.PokerPacketHandler = PokerPacketHandler;
})(net || (net = {}));
//@ sourceMappingURL=PokerHandler.js.map
