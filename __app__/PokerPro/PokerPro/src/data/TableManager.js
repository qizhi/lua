///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../net/TableHandler.ts"/>
///<reference path="Table.ts"/>
///<reference path="GameConfig.ts"/>
var data;
(function (data) {
    var TableManager = (function () {
        function TableManager() {
            this.tables = new data.Map();
            this.tableNames = new data.Map();
        }
        TableManager.prototype.getTable = function (tableId) {
            return this.tables.get(tableId);
        };
        TableManager.prototype.tableExist = function (tableId) {
            return this.tables.contains(tableId);
        };
        TableManager.prototype.isSeated = function (tableId) {
            var table = this.getTable(tableId);
            if (table != null) {
                return table.myPlayerSeat != null;
            }
            return false;
        };
        TableManager.prototype.onPlayerLoggedIn = function () {
            console.log("Checking if there are open tables to reconnect to");
            var tables = this.tables.values();
            for (var i = 0; i < tables.length; i++) {
                this.leaveTable(tables[i].id);

                //TODO: we need snapshot to get capacity
                new net.TableRequestHandler(tables[i].id).openTable(10);
            }
        };
        TableManager.prototype.handleOpenTableAccepted = function (tableId, capacity) {
        };

        TableManager.prototype.createTable = function (tableId, capacity, name, tableLayout) {
            console.log("Creating table " + tableId + " with name = " + name);
            var table = new data.Table(tableId, capacity, name);
            table.layout = tableLayout;
            this.tables.put(tableId, table);
            tableLayout.onTableCreated(table);

            console.log("Nr of tables open = " + this.tables.size());
        };

        TableManager.prototype.handleBuyInResponse = function (tableId, status) {
            if (status == com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PENDING) {
                var table = this.getTable(tableId);
                table.layout.onBuyInCompleted();
            } else if (status != com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.OK) {
                //$.ga._trackEvent("poker_table", "buy_in_error");
                this.handleBuyInError(tableId, status);
            }
        };

        TableManager.prototype.handleBuyInError = function (tableId, status) {
            console.log("buy-in status = " + status);
            var table = this.getTable(tableId);
            table.layout.onBuyInError(i18n.t("buy-in.error"));
        };

        TableManager.prototype.handleBuyInInfo = function (tableId, balanceInWallet, balanceOnTable, maxAmount, minAmount, mandatory, currencyCode) {
            var table = this.getTable(tableId);
            table.layout.onBuyInInfo(table.name, balanceInWallet, balanceOnTable, maxAmount, minAmount, mandatory, currencyCode);
        };

        TableManager.prototype.startNewHand = function (tableId, handId) {
            var table = this.tables.get(tableId);
            table.handCount++;
            table.handId = handId;
            table.layout.onStartHand(handId);
        };

        /**
        * Called when a hand is complete and calls the TableLayoutManager
        * This method will trigger a tableManager.clearTable after
        * 15 seconds (us
        * @param {Number} tableId
        * @param {com.cubeia.games.poker.io.protocol.BestHand[]} hands
        * @param {com.cubeia.games.poker.io.protocol.PotTransfers} potTransfers
        */
        TableManager.prototype.endHand = function (tableId, hands, potTransfers) {
            for (var i = 0; i < hands.length; i++) {
                this.updateHandStrength(tableId, hands[i], true);
            }
            var table = this.tables.get(tableId);
            console.log("pot transfers:");
            console.log(potTransfers);
            var count = table.handCount;
            var self = this;

            if (potTransfers.fromPlayerToPot === false) {
                table.layout.onPotToPlayerTransfers(potTransfers.transfers);
            }

            setTimeout(function () {
                //if no new hand has started in the next 15 secs we clear the table
                self.clearTable(tableId, count);
            }, 15000);
        };

        TableManager.prototype.updateHandStrength = function (tableId, bestHand, handEnded) {
            this.showHandStrength(tableId, bestHand.player, bestHand.handType, this.getCardStrings(bestHand.cards), handEnded);
        };
        TableManager.prototype.getCardStrings = function (cards) {
            var converted = [];
            for (var i = 0; i < cards.length; i++) {
                converted.push(util.Utils.getCardString(cards[i]));
            }
            return converted;
        };
        TableManager.prototype.clearTable = function (tableId, handCount) {
            var table = this.tables.get(tableId);
            if (table.handCount == handCount) {
                console.log("No hand started clearing table");
                table.layout.onStartHand(this.dealerSeatId);
            } else {
                console.log("new hand started, skipping clear table");
            }
        };

        TableManager.prototype.showHandStrength = function (tableId, playerId, hand, cardStrings, handEnded) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onPlayerHandStrength(player, hand, cardStrings, handEnded);
        };
        TableManager.prototype.handlePlayerAction = function (tableId, playerId, actionType, amount) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onPlayerActed(player, actionType, amount);
        };
        TableManager.prototype.setDealerButton = function (tableId, seatId) {
            var table = this.tables.get(tableId);
            table.layout.onMoveDealerButton(seatId);
        };
        TableManager.prototype.addPlayer = function (tableId, seat, playerId, playerName) {
            var self = this;
            console.log("adding player " + playerName + " at seat" + seat + " on table " + tableId);
            var table = this.tables.get(tableId);
            var p = new data.UserInfo(playerId, playerName);
            table.addPlayer(seat, p);
            if (playerId == data.Player.getInstance().id) {
                table.myPlayerSeat = seat;
            }
            table.layout.onPlayerAdded(seat, p);
            if (data.Player.getInstance().loginToken != null) {
            } else {
                self.updatePlayerAvatar(playerId, table, null);
                console.log("No loginToken available to request player info from player api");
            }
        };

        TableManager.prototype.updatePlayerAvatar = function (playerId, table, profile) {
            if (profile != null) {
                table.layout.updateAvatar(playerId, profile.externalAvatarUrl);
            } else {
                table.layout.updateAvatar(playerId, null);
            }
        };

        TableManager.prototype.removePlayer = function (tableId, playerId) {
            console.log("removing player with playerId " + playerId);
            var table = this.tables.get(tableId);
            table.removePlayer(playerId);
            if (playerId == data.Player.getInstance().id) {
                table.myPlayerSeat = null;
            }
            table.layout.onPlayerRemoved(playerId);
        };

        /**
        * handle deal cards, passes a card string as parameter
        * card string i h2 (two of hearts), ck (king of spades)
        * @param {Number} tableId the id of the table
        * @param {Number} playerId  the id of the player
        * @param {Number} cardId id of the card
        * @param {String} cardString the card string identifier
        */
        TableManager.prototype.dealPlayerCard = function (tableId, playerId, cardId, cardString) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onDealPlayerCard(player, cardId, cardString);
        };
        TableManager.prototype.updatePlayerBalance = function (tableId, playerId, balance) {
            var table = this.tables.get(tableId);
            var p = table.getPlayerById(playerId);
            if (p == null) {
                console.log("Unable to find player to update balance playerId = " + playerId + ", tableId = " + tableId);
                return;
            }
            p.balance = balance;
            table.layout.onPlayerUpdated(p);
        };

        TableManager.prototype.updatePlayerStatus = function (tableId, playerId, status, away, sitOutNextHand) {
            var table = this.tables.get(tableId);
            var p = table.getPlayerById(playerId);
            if (p == null) {
                throw "Player with id " + playerId + " not found";
            }
            p.tableStatus = status;
            p.away = away;
            p.sitOutNextHand = sitOutNextHand;
            table.layout.onPlayerStatusUpdated(p);
        };

        TableManager.prototype.setNoMoreBlinds = function (tableId, enable) {
            var table = this.tables.get(tableId);
            table.noMoreBlinds = enable;
        };
        TableManager.prototype.handleRequestPlayerAction = function (tableId, playerId, allowedActions, timeToAct) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            var fixedLimit = table.betStrategy === com.cubeia.games.poker.io.protocol.BetStrategyEnum.FIXED_LIMIT;
            table.layout.onRequestPlayerAction(player, allowedActions, timeToAct, table.totalPot, fixedLimit);
        };
        TableManager.prototype.handleRebuyOffer = function (tableId, playerId, rebuyCost, chipsForRebuy, timeToAct) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onRequestRebuy(player, rebuyCost, chipsForRebuy, timeToAct);
        };
        TableManager.prototype.hideRebuyButtons = function (tableId, playerId) {
            console.log("Getting table " + tableId);
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            console.log("Player " + player);
            table.layout.hideRebuyButtons(player);
        };
        TableManager.prototype.handleAddOnOffer = function (tableId, playerId, addOnCost, chipsForAddOn) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onRequestAddOn(player, addOnCost, chipsForAddOn);
        };
        TableManager.prototype.handleAddOnPeriodClosed = function (tableId, playerId) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.hideAddOnButton(player);
        };
        TableManager.prototype.hideAddOnButton = function (tableId, playerId) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.hideAddOnButton(player);
        };
        TableManager.prototype.handleRebuyPerformed = function (tableId, playerId, addOnCost, chipsForAddOn) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onRebuyPerformed(player);
        };
        TableManager.prototype.updateTotalPot = function (tableId, amount) {
            var table = this.tables.get(tableId);
            table.totalPot = amount;
            table.layout.onTotalPotUpdate(amount);
        };
        TableManager.prototype.dealCommunityCards = function (tableId, cards) {
            var table = this.getTable(tableId);
            table.layout.onDealCommunityCards(cards);
        };
        TableManager.prototype.updatePots = function (tableId, pots) {
            var table = this.tables.get(tableId);
            var totalPot = 0;
            for (var i = 0; i < pots.length; i++) {
                totalPot += pots[i].amount;
            }
            table.layout.onTotalPotUpdate(totalPot);
            table.layout.onPotUpdate(pots);
        };

        TableManager.prototype.exposePrivateCards = function (tableId, cards) {
            var playerCardMap = new data.Map();
            var table = this.getTable(tableId);
            for (var i = 0; i < cards.length; i++) {
                var playerCards = playerCardMap.get(cards[i].player);
                if (playerCards == null) {
                    var player = table.getPlayerById(cards[i].player);
                    playerCards = { player: player, cards: [] };
                    playerCardMap.put(cards[i].player, playerCards);
                }
                var cardString = util.Utils.getCardString(cards[i].card);
                var cardId = cards[i].card.cardId;
                playerCards.cards.push({ id: cardId, cardString: cardString });
            }

            table.layout.exposePrivateCards(playerCardMap.values());
        };

        TableManager.prototype.notifyWaitingToStartBreak = function (tableId) {
        };

        /**
        * @param {Number} tableId
        * @param {com.cubeia.games.poker.io.protocol.BlindsLevel} newBlinds
        * @param {Number} secondsToNextLevel
        * @param {com.cubeia.games.poker.io.protocol.BetStrategyEnum} betStrategy
        * @param {com.cubeia.games.poker.io.protocol.Currency} currency
        */
        TableManager.prototype.notifyGameStateUpdate = function (tableId, newBlinds, secondsToNextLevel, betStrategy, currency) {
            console.log("Seconds to next level: " + secondsToNextLevel);
            console.log("notifyGameStateUpdate = " + betStrategy);
            var table = this.getTable(tableId);
            table.betStrategy = betStrategy;
            table.currency = currency;
            this.notifyBlindsUpdated(tableId, newBlinds, currency, secondsToNextLevel);
        };
        TableManager.prototype.notifyBlindsUpdated = function (tableId, newBlinds, currency, secondsToNextLevel) {
            console.log("Seconds to next level: " + secondsToNextLevel);
            if (newBlinds.isBreak) {
            }
            var table = this.getTable(tableId);
            table.layout.onBlindsLevel(newBlinds, currency, secondsToNextLevel);
        };
        TableManager.prototype.notifyTournamentDestroyed = function (tableId) {
            //var dialogManager = Poker.AppCtx.getDialogManager();
            //dialogManager.displayGenericDialog({ tableId: tableId, translationKey: "tournament-closed" });
            this.tables.get(tableId).tournamentClosed = true;
        };
        TableManager.prototype.bettingRoundComplete = function (tableId) {
            var table = this.getTable(tableId);
            table.layout.onBettingRoundComplete();
        };
        TableManager.prototype.leaveTable = function (tableId) {
            console.log("REMOVING TABLE = " + tableId);
            var table = this.tables.remove(tableId);
            if (table == null) {
                console.log("table not found when removing " + tableId);
            } else {
                table.layout.onLeaveTableSuccess();
                table.leave();
            }
        };
        TableManager.prototype.onFutureAction = function (tableId, actions, callAmount, minBetAmount) {
            var table = this.getTable(tableId);
            if (actions.length > 0) {
                var futureActions = this.getFutureActionTypes(actions);
                table.layout.displayFutureActions(futureActions, callAmount, minBetAmount);
            }
        };
        TableManager.prototype.getFutureActionTypes = function (actions) {
            var futureActions = [];
            for (var i = 0; i < actions.length; i++) {
                var act = actions[i];
                switch (act) {
                    case data.ActionType.CHECK:
                        futureActions.push(data.FutureActionType.CHECK_OR_FOLD);
                        futureActions.push(data.FutureActionType.CHECK_OR_CALL_ANY);
                        break;
                    case data.ActionType.CALL:
                        futureActions.push(data.FutureActionType.CALL_CURRENT_BET);
                        futureActions.push(data.FutureActionType.CALL_ANY);
                        break;
                    case data.ActionType.RAISE:
                        futureActions.push(data.FutureActionType.RAISE);
                        futureActions.push(data.FutureActionType.RAISE_ANY);
                        break;
                }
            }
            if (actions.length > 0) {
                futureActions.push(data.FutureActionType.FOLD);
            }
            return futureActions;
        };
        TableManager.prototype.onChatMessage = function (tableId, playerId, message) {
        };

        TableManager.getInstance = function () {
            if (TableManager._instance == null) {
                TableManager._instance = new TableManager();
            }
            return TableManager._instance;
        };
        return TableManager;
    })();
    data.TableManager = TableManager;
})(data || (data = {}));
//@ sourceMappingURL=TableManager.js.map
