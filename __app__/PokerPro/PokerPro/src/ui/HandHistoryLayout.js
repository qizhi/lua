var ui;
(function (ui) {
    var HandHistoryLayout = (function () {
        function HandHistoryLayout(tableId, closeFunction) {
            this.tableId = tableId;
            this.closeFunction = closeFunction;
        }
        HandHistoryLayout.prototype.prepareUI = function (tableId) {
        };

        HandHistoryLayout.prototype.activate = function () {
        };
        HandHistoryLayout.prototype.close = function () {
        };

        HandHistoryLayout.prototype.showHandSummaries = function (summaries) {
        };

        HandHistoryLayout.prototype.showHand = function (hand) {
        };
        return HandHistoryLayout;
    })();
    ui.HandHistoryLayout = HandHistoryLayout;
})(ui || (ui = {}));
//@ sourceMappingURL=HandHistoryLayout.js.map
