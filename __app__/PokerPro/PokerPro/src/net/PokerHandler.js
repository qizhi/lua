///<reference path="../data/Player.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="SocketManager.ts"/>
var net;
(function (net) {
    var PokerSequence = (function () {
        function PokerSequence() {
            this.sequences = new data.Map();
        }
        PokerSequence.prototype.setSequence = function (tableId, seq) {
            this.sequences.put(tableId, seq);
        };
        PokerSequence.prototype.getSequence = function (tableId) {
            return this.sequences.get(tableId);
        };

        PokerSequence.getInstance = function () {
            if (PokerSequence._instance == null) {
                PokerSequence._instance = new PokerSequence();
            }
            return PokerSequence._instance;
        };
        return PokerSequence;
    })();
    net.PokerSequence = PokerSequence;
    var PokerPacketHandler = (function () {
        function PokerPacketHandler(tableId) {
            this.tableId = tableId;
            this.tableManager = data.TableManager.getInstance();
        }
        PokerPacketHandler.prototype.handleRequestAction = function (requestAction) {
            this.tableManager.updateTotalPot(this.tableId, requestAction.currentPotSize);
            PokerSequence.getInstance().setSequence(this.tableId, requestAction.seq);
            var acts = util.ActionUtils.getPokerActions(requestAction.allowedActions);
            this.tableManager.handleRequestPlayerAction(this.tableId, requestAction.player, acts, requestAction.timeToAct);
        };

        PokerPacketHandler.prototype.handleRebuyOffer = function (rebuyOffer, playerId) {
            console.log("Player " + playerId + " was offered a rebuy.");
            this.tableManager.handleRebuyOffer(this.tableId, playerId, rebuyOffer.cost, rebuyOffer.cost, 15000);
        };
        PokerPacketHandler.prototype.handleAddOnOffer = function (addOnOffer, playerId) {
            console.log("Player " + playerId + " was offered an add-on.");
            this.tableManager.handleAddOnOffer(this.tableId, playerId, addOnOffer.cost, addOnOffer.chips);
        };
        PokerPacketHandler.prototype.handleAddOnPeriodClosed = function (playerId) {
            this.tableManager.handleAddOnPeriodClosed(this.tableId, playerId);
        };
        PokerPacketHandler.prototype.handleRebuyPerformed = function (playerId) {
            console.log("Player " + playerId + " performed a rebuy.");
            this.tableManager.handleRebuyPerformed(this.tableId, playerId);
        };
        PokerPacketHandler.prototype.handleAddOnPerformed = function (playerId) {
            console.log("Player " + playerId + " performed an add-on.");
        };
        PokerPacketHandler.prototype.handlePlayerBalance = function (packet) {
            this.tableManager.updatePlayerBalance(this.tableId, packet.player, util.Utils.formatCurrency(packet.balance));
        };
        PokerPacketHandler.prototype.handlePlayerHandStartStatus = function (packet) {
            var status = data.PlayerTableStatus.SITTING_OUT;
            if (packet.status === com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITIN) {
                status = data.PlayerTableStatus.SITTING_IN;
            }
            this.tableManager.updatePlayerStatus(this.tableId, packet.player, status, packet.away, packet.sitOutNextHand);
        };

        /**
        * @param {com.cubeia.games.poker.io.protocol.BuyInInfoResponse} protocolObject
        */
        PokerPacketHandler.prototype.handleBuyIn = function (protocolObject) {
            var po = protocolObject;
            console.log("BUY-IN:");
            console.log(protocolObject);
            this.tableManager.handleBuyInInfo(this.tableId, po.balanceInWallet, po.balanceOnTable, po.maxAmount, po.minAmount, po.mandatoryBuyin, po.currencyCode);
        };
        PokerPacketHandler.prototype.handlePerformAction = function (performAction) {
            var actionType = util.ActionUtils.getActionType(performAction.action.type);

            var amount = "0";
            if (performAction.stackAmount) {
                amount = util.Utils.formatCurrency(performAction.stackAmount);
            }

            this.tableManager.handlePlayerAction(this.tableId, performAction.player, actionType, amount);
        };
        PokerPacketHandler.prototype.handleDealPublicCards = function (packet) {
            this.tableManager.bettingRoundComplete(this.tableId);
            var cards = [];
            for (var i = 0; i < packet.cards.length; i++) {
                cards.push({ id: packet.cards[i].cardId, cardString: util.Utils.getCardString(packet.cards[i]) });
            }
            this.tableManager.dealCommunityCards(this.tableId, cards);
        };
        PokerPacketHandler.prototype.handleDealPrivateCards = function (protocolObject) {
            var cardsToDeal = protocolObject.cards;
            for (var c in cardsToDeal) {
                var cardString = util.Utils.getCardString(cardsToDeal[c].card);
                this.tableManager.dealPlayerCard(this.tableId, cardsToDeal[c].player, cardsToDeal[c].card.cardId, cardString);
            }
        };
        PokerPacketHandler.prototype.handleExposePrivateCards = function (packet) {
            this.tableManager.bettingRoundComplete(this.tableId);
            this.tableManager.exposePrivateCards(this.tableId, packet.cards);
        };
        PokerPacketHandler.prototype.handlePlayerPokerStatus = function (packet) {
            var status = packet.status;
            var tableStatus = data.PlayerTableStatus.SITTING_IN;
            if (status == com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITOUT) {
                tableStatus = data.PlayerTableStatus.SITTING_OUT;
            }
            this.tableManager.updatePlayerStatus(this.tableId, packet.player, tableStatus, packet.away, packet.sitOutNextHand);
        };
        PokerPacketHandler.prototype.handlePotTransfers = function (packet) {
            var pots = [];
            for (var i in packet.pots) {
                var p = packet.pots[i];
                var type = data.PotType.MAIN;
                if (com.cubeia.games.poker.io.protocol.PotTypeEnum.SIDE == p.type) {
                    type = data.PotType.SIDE;
                }
                pots.push(new data.Pot(p.id, type, p.amount));
            }
            if (pots.length > 0) {
                this.tableManager.updatePots(this.tableId, pots);
            }
        };
        PokerPacketHandler.prototype.handleFuturePlayerAction = function (packet) {
            var futureActions = [];
            var actions = packet.actions;
            console.log("packet handler ACTIONS:");
            console.log(actions);
            for (var i = 0; i < actions.length; i++) {
                var act = util.ActionUtils.getActionType(actions[i].action);
                futureActions.push(act);
            }
            this.tableManager.onFutureAction(this.tableId, futureActions, packet.callAmount, packet.minBetAmount);
        };
        return PokerPacketHandler;
    })();
    net.PokerPacketHandler = PokerPacketHandler;

    var PokerRequestHandler = (function () {
        function PokerRequestHandler(tableId) {
            this.tableId = tableId;
            this.tableManager = data.TableManager.getInstance();
        }
        PokerRequestHandler.prototype.onMyPlayerAction = function (actionType, amount) {
            console.log("ON my player action");
            console.log(actionType);
            console.log(amount);
            var tableRequestHandler = new net.TableRequestHandler(this.tableId);
            if (actionType == data.ActionType.JOIN) {
                tableRequestHandler.joinTable();
            } else if (actionType == data.ActionType.LEAVE) {
                var table = this.tableManager.getTable(this.tableId);
                if (table.tournamentClosed) {
                    console.log("Tournament is closed, will close the table without telling the server.");
                    this.tableManager.leaveTable(this.tableId);
                } else if (this.tableManager.isSeated(this.tableId)) {
                    tableRequestHandler.leaveTable();
                    this.tableManager.leaveTable(this.tableId);
                } else {
                    tableRequestHandler.unwatchTable();
                    this.tableManager.leaveTable(this.tableId);
                }
            } else if (actionType == data.ActionType.SIT_IN) {
                this.sitIn();
            } else if (actionType == data.ActionType.SIT_OUT) {
                this.sitOut();
            } else if (actionType == data.ActionType.REBUY) {
                this.sendRebuyResponse(true);
                console.log("Hiding rebuy buttons for player " + data.Player.getInstance().id + " table " + this.tableId);
                this.tableManager.hideRebuyButtons(this.tableId, data.Player.getInstance().id);
            } else if (actionType == data.ActionType.DECLINE_REBUY) {
                this.sendRebuyResponse(false);
                console.log("Hiding rebuy buttons for player " + data.Player.getInstance().id + " table " + this.tableId);
                this.tableManager.hideRebuyButtons(this.tableId, data.Player.getInstance().id);
            } else if (actionType == data.ActionType.ADD_ON) {
                this.sendAddOnRequest();
                this.tableManager.hideAddOnButton(this.tableId, data.Player.getInstance().id);
            } else {
                this.sendAction(util.ActionUtils.getActionEnumType(actionType), amount, 0);
            }
        };
        PokerRequestHandler.prototype.sendAction = function (actionType, betAmount, raiseAmount) {
            var action = util.ActionUtils.getPlayerAction(this.tableId, PokerSequence.getInstance().getSequence(this.tableId), actionType, betAmount, raiseAmount);
            this.sendGameTransportPacket(action);
        };
        PokerRequestHandler.prototype.sendGameTransportPacket = function (gamedata) {
            var connector = net.SocketManager.getInstance().getConnector();
            connector.sendStyxGameData(0, this.tableId, gamedata);
            console.log("package sent to table " + this.tableId);
            console.log(gamedata);
        };
        PokerRequestHandler.prototype.buyIn = function (amount) {
            var buyInRequest = new com.cubeia.games.poker.io.protocol.BuyInRequest();
            buyInRequest.amount = amount;
            buyInRequest.sitInIfSuccessful = true;
            this.sendGameTransportPacket(buyInRequest);
        };
        PokerRequestHandler.prototype.requestBuyInInfo = function () {
            console.log("REQUEST BUY-IN INFO!");
            var buyInInfoRequest = new com.cubeia.games.poker.io.protocol.BuyInInfoRequest();
            this.sendGameTransportPacket(buyInInfoRequest);
        };
        PokerRequestHandler.prototype.sitOut = function () {
            var sitOut = new com.cubeia.games.poker.io.protocol.PlayerSitoutRequest();
            sitOut.player = data.Player.getInstance().id;
            this.sendGameTransportPacket(sitOut);
        };
        PokerRequestHandler.prototype.sitIn = function () {
            var sitIn = new com.cubeia.games.poker.io.protocol.PlayerSitinRequest();
            sitIn.player = data.Player.getInstance().id;
            this.sendGameTransportPacket(sitIn);
        };
        PokerRequestHandler.prototype.sendRebuyResponse = function (answer) {
            var rebuy = new com.cubeia.games.poker.io.protocol.RebuyResponse();
            rebuy.answer = answer;
            this.sendGameTransportPacket(rebuy);
        };
        PokerRequestHandler.prototype.sendAddOnRequest = function () {
            this.sendGameTransportPacket(new com.cubeia.games.poker.io.protocol.PerformAddOn());
        };
        return PokerRequestHandler;
    })();
    net.PokerRequestHandler = PokerRequestHandler;
})(net || (net = {}));
//@ sourceMappingURL=PokerHandler.js.map
