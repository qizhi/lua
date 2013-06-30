///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/Table.ts"/>
///<reference path="../data/Player.ts"/>
var ui;
(function (ui) {
    var TableLayout = (function () {
        function TableLayout(tableId, capacity) {
            this.tableId = tableId;
            this.capacity = capacity;
        }
        TableLayout.prototype.onTableCreated = function (table) {
        };

        //buy in
        TableLayout.prototype.onBuyInCompleted = function () {
        };
        TableLayout.prototype.onBuyInError = function (msg) {
        };
        TableLayout.prototype.onBuyInInfo = function (tableName, balanceInWallet, balanceOnTable, maxAmount, minAmount, mandatory, currencyCode) {
        };

        //hand
        TableLayout.prototype.onStartHand = function (handId) {
            console.log("Hand " + handId + " started");
        };

        TableLayout.prototype.onPotToPlayerTransfers = function (transfers) {
        };

        TableLayout.prototype.onPlayerHandStrength = function (player, hand, cardStrings, handEnded) {
            if (handEnded == true) {
            }
        };
        TableLayout.prototype.onPlayerActed = function (player, actionType, amount) {
        };
        TableLayout.prototype.onMoveDealerButton = function (seatId) {
        };

        TableLayout.prototype.onPlayerAdded = function (seatId, player) {
            console.log("Player " + player.name + " added at seat " + seatId);
        };

        TableLayout.prototype.updateAvatar = function (playerId, avatarUrl) {
        };

        TableLayout.prototype.onPlayerRemoved = function (playerId) {
        };

        TableLayout.prototype.onDealPlayerCard = function (player, cardId, cardString) {
        };

        TableLayout.prototype.onPlayerUpdated = function (p) {
        };

        TableLayout.prototype.onPlayerStatusUpdated = function (p) {
        };

        TableLayout.prototype.onBettingRoundComplete = function () {
        };

        //action
        TableLayout.prototype.displayFutureActions = function (actions, callAmount, minBetAmount) {
        };

        //leave
        TableLayout.prototype.onLeaveTableSuccess = function () {
        };

        //
        //request
        TableLayout.prototype.onRequestPlayerAction = function (player, allowedActions, timeToAct, mainPot, fixedLimit) {
        };

        //
        //rebuy
        TableLayout.prototype.onRequestRebuy = function (player, rebuyCost, chipsForRebuy, timeToAct) {
        };
        TableLayout.prototype.hideRebuyButtons = function (player) {
        };
        TableLayout.prototype.onRebuyPerformed = function (player, addOnCost, chipsForAddOn) {
        };

        //
        //addon
        TableLayout.prototype.onRequestAddOn = function (player, addOnCost, chipsForAddOn) {
        };
        TableLayout.prototype.hideAddOnButton = function (player) {
        };

        //
        //pot
        TableLayout.prototype.onTotalPotUpdate = function (amount) {
        };
        TableLayout.prototype.onPotUpdate = function (pots) {
            for (var i = 0; i < pots.length; i++) {
            }
        };

        //card
        TableLayout.prototype.onDealCommunityCards = function (cards) {
            for (var i = 0; i < cards.length; i++) {
            }
        };
        TableLayout.prototype.exposePrivateCards = function (playerCards) {
            for (var i = 0; i < playerCards.length; i++) {
                var cards = playerCards[i].cards;
                for (var j = 0; j < cards.length; j++) {
                    this.onExposePrivateCard(cards[j].id, cards[j].cardString);
                }
            }
        };
        TableLayout.prototype.onExposePrivateCard = function (cardId, cardString) {
        };

        //
        //blind
        TableLayout.prototype.onBlindsLevel = function (level, currency, secondsToNextLevel) {
            if (level.smallBlind != null && level.bigBlind != null) {
            }
        };
        return TableLayout;
    })();
    ui.TableLayout = TableLayout;
})(ui || (ui = {}));
//@ sourceMappingURL=TableLayout.js.map
