///<reference path="../net/HandHistoryHandler.ts"/>
///<reference path="../ui/HandHistoryLayout.ts"/>
///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>
///<reference path="TableManager.ts"/>
var data;
(function (data) {
    var HandHistoryManager = (function () {
        function HandHistoryManager() {
            this.handHistoryLayouts = new data.Map();
        }
        HandHistoryManager.prototype.close = function () {
        };
        HandHistoryManager.prototype.requestHandHistory = function (tableId) {
            var self = this;
            var layout = this.handHistoryLayouts.get(tableId);
            if (layout == null) {
                layout = new ui.HandHistoryLayout(tableId, function (tableId) {
                    self.handHistoryLayouts.remove(tableId);
                });
                this.handHistoryLayouts.put(tableId, layout);
            } else {
                layout.activate();
            }
            new net.HandHistoryRequestHandler(tableId).requestHandSummaries(30);
        };
        HandHistoryManager.prototype.showHandSummaries = function (tableId, summaries) {
            var layout = this.handHistoryLayouts.get(tableId);
            if (layout != null) {
                var self = this;

                /*$.each(summaries, function (i, e) {
                e.startTime = self.formatDateTime(e.startTime);
                });*/
                layout.showHandSummaries(summaries);
            } else {
                console.log("undable to find layout");
            }
        };
        HandHistoryManager.prototype.showHand = function (hand) {
            var layout = this.handHistoryLayouts.get(hand.table.tableId);
            console.log(hand);
            hand = this.prepareHand(hand);
            layout.showHand(hand);
        };
        HandHistoryManager.prototype.formatDateTime = function (milis) {
            var date = new Date(milis);
            var val = function (value) {
                value = "" + value;
                if (value.length == 1) {
                    value = "0" + value;
                }
                return value;
            };
            return date.getFullYear() + "-" + val((date.getMonth() + 1)) + "-" + val(date.getDay()) + " " + val(date.getHours()) + ":" + val(date.getMinutes());
        };

        HandHistoryManager.prototype.prepareHand = function (hand) {
            console.log(hand);

            /*
            var playerMap = new Poker.Map();
            for (var i = 0; i < hand.seats.length; i++) {
            var seat = hand.seats[i];
            playerMap.put(seat.playerId, seat.name);
            $.extend(seat, { initialBalance: this.formatAmount(seat.initialBalance) });
            }
            hand.startTime = this.formatDateTime(hand.startTime);
            for (var i = 0; i < hand.events.length; i++) {
            var event = hand.events[i];
            
            if (typeof (event.playerId) != "undefined") {
            event = $.extend(event, { name: playerMap.get(event.playerId), player: true });
            }
            if (typeof (event.action) != "undefined") {
            event = $.extend(event, {
            action: this.getAction(hand.events[i].action)
            
            });
            }
            if (typeof (event.cards) != undefined) {
            event = $.extend(event, {
            cards: this.extractCards(event.cards)
            });
            
            }
            if (typeof (event.amount) != "undefined") {
            event = $.extend(event, {
            amount: {
            amount: this.formatAmount(hand.events[i].amount.amount)
            }
            });
            }
            if (event.type == "TableCardsDealt") {
            event = $.extend(event, {
            cards: this.extractCards(event.cards),
            tableCards: true
            });
            }
            if (event.type == "PlayerCardsExposed") {
            event = $.extend(event, {
            playerCardsExposed: true
            });
            }
            if (event.type == "PlayerCardsDealt") {
            event = $.extend(event, {
            playerCardsDealt: true
            });
            }
            if (event.type == "PlayerBestHand") {
            event = $.extend(event, {
            bestHandCards: this.extractCards(event.bestHandCards),
            name: playerMap.get(event.playerHand.playerId),
            handDescription: Poker.Hand.fromName(event.handInfoCommon.handType).text
            });
            }
            
            }
            var results = [];
            for (var x in hand.results.results) {
            results.push(hand.results.results[x]);
            }
            $.extend(hand.results, { res: results });
            
            for (var i = 0; i < hand.results.res.length; i++) {
            var result = hand.results.res[i];
            result = $.extend(result, {
            name: playerMap.get(result.playerId),
            totalBet: this.formatAmount(result.totalBet),
            totalWin: this.formatAmount(result.totalWin, "0")
            });
            }
            */
            return hand;
        };
        HandHistoryManager.prototype.formatAmount = function (amount, emptyStr) {
            if (amount == 0) {
                return emptyStr || "";
            } else {
                return util.Utils.formatCurrency(amount);
            }
        };
        HandHistoryManager.prototype.getAction = function (actionEnumString) {
            var act = data.ActionType[actionEnumString];
            if (typeof (act) != "undefined") {
                return act.text;
            }
            return actionEnumString;
        };
        HandHistoryManager.prototype.extractCards = function (cards) {
            if (typeof (cards) == "undefined") {
                return null;
            }
            for (var i = 0; i < cards.length; i++) {
            }
            return cards;
        };
        HandHistoryManager.prototype.getCard = function (card) {
            return this.getRank(card.rank) + this.getSuit(card.suit);
        };
        HandHistoryManager.prototype.getSuit = function (suit) {
            switch (suit) {
                case "CLUBS":
                    return "c";
                case "DIAMONDS":
                    return "d";
                case "HEARTS":
                    return "h";
                case "SPADES":
                    return "s";
            }
            return "";
        };
        HandHistoryManager.prototype.getRank = function (rank) {
            switch (rank) {
                case "TWO":
                    return "2";
                case "THREE":
                    return "3";
                case "FOUR":
                    return "4";
                case "FIVE":
                    return "5";
                case "SIX":
                    return "6";
                case "SEVEN":
                    return "7";
                case "EIGHT":
                    return "8";
                case "NINE":
                    return "9";
                case "TEN":
                    return "T";
                case "JACK":
                    return "J";
                case "QUEEN":
                    return "Q";
                case "KING":
                    return "K";
                case "ACE":
                    return "A";
            }
            return "";
        };

        HandHistoryManager.getInstance = function () {
            if (HandHistoryManager._instance == null) {
                HandHistoryManager._instance = new HandHistoryManager();
            }
            return HandHistoryManager._instance;
        };
        return HandHistoryManager;
    })();
    data.HandHistoryManager = HandHistoryManager;
})(data || (data = {}));
//@ sourceMappingURL=HandHistoryManager.js.map
