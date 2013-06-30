///<reference path="../data/Player.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="SocketManager.ts"/>
module net {
    export class PokerSequence {
        private sequences: data.Map<number, any>;
        constructor() {
            this.sequences = new data.Map<number, any>();
        }
        setSequence(tableId: number, seq: any): void {
            this.sequences.put(tableId, seq);
        }
        getSequence(tableId: number): any {
            return this.sequences.get(tableId);
        }
        
        private static _instance: PokerSequence;
        public static getInstance(): PokerSequence {
            if (PokerSequence._instance == null) {
                PokerSequence._instance = new PokerSequence();
            }
            return PokerSequence._instance;
        }
    }
    export class PokerPacketHandler {
        tableManager: data.TableManager;

        constructor(public tableId: number) {
            this.tableManager = data.TableManager.getInstance();
        }
        handleRequestAction(requestAction: any): void {
            this.tableManager.updateTotalPot(this.tableId, requestAction.currentPotSize);
            PokerSequence.getInstance().setSequence(this.tableId, requestAction.seq);
            var acts:data.Action[] = util.ActionUtils.getPokerActions(requestAction.allowedActions);
            this.tableManager.handleRequestPlayerAction(this.tableId, requestAction.player, acts, requestAction.timeToAct);
        }

        handleRebuyOffer(rebuyOffer: any, playerId: number): void {
            console.log("Player " + playerId + " was offered a rebuy.");
            this.tableManager.handleRebuyOffer(this.tableId, playerId, rebuyOffer.cost, rebuyOffer.cost, 15000); // TODO: Un-hard-code
        }
        handleAddOnOffer(addOnOffer: any, playerId: number): void {
            console.log("Player " + playerId + " was offered an add-on.");
            this.tableManager.handleAddOnOffer(this.tableId, playerId, addOnOffer.cost, addOnOffer.chips);
        }
        handleAddOnPeriodClosed(playerId: number): void {
            this.tableManager.handleAddOnPeriodClosed(this.tableId, playerId);
        }
        handleRebuyPerformed(playerId: number): void {
            console.log("Player " + playerId + " performed a rebuy.");
            this.tableManager.handleRebuyPerformed(this.tableId, playerId);
        }
        handleAddOnPerformed(playerId: number): void {
            console.log("Player " + playerId + " performed an add-on.");
        }
        handlePlayerBalance(packet: any): void {
            this.tableManager.updatePlayerBalance(this.tableId, packet.player, util.Utils.formatCurrency(packet.balance));
        }
        handlePlayerHandStartStatus(packet: any): void {
            var status = data.PlayerTableStatus.SITTING_OUT;
            if (packet.status === com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITIN) {
                status = data.PlayerTableStatus.SITTING_IN;
            }
            this.tableManager.updatePlayerStatus(this.tableId, packet.player, status, packet.away, packet.sitOutNextHand);
        }
        /**
        * @param {com.cubeia.games.poker.io.protocol.BuyInInfoResponse} protocolObject
         */
        handleBuyIn(protocolObject: any): void {
            var po = protocolObject;
            console.log("BUY-IN:");
            console.log(protocolObject);
            this.tableManager.handleBuyInInfo(this.tableId, po.balanceInWallet, po.balanceOnTable, po.maxAmount, po.minAmount, po.mandatoryBuyin, po.currencyCode);
        }
        handlePerformAction(performAction: any): void {
            var actionType = util.ActionUtils.getActionType(performAction.action.type);

            var amount:string = "0";
            if (performAction.stackAmount) {
                amount = util.Utils.formatCurrency(performAction.stackAmount);
            }

            this.tableManager.handlePlayerAction(this.tableId, performAction.player, actionType, amount);
        }
        handleDealPublicCards(packet: any): void {
            this.tableManager.bettingRoundComplete(this.tableId);
            var cards = [];
            for (var i = 0; i < packet.cards.length; i++) {
                cards.push({ id: packet.cards[i].cardId, cardString: util.Utils.getCardString(packet.cards[i]) });
            }
            this.tableManager.dealCommunityCards(this.tableId, cards);
        }
        handleDealPrivateCards(protocolObject: any): void {
            var cardsToDeal = protocolObject.cards;
            for (var c in cardsToDeal) {
                var cardString = util.Utils.getCardString(cardsToDeal[c].card);
                this.tableManager.dealPlayerCard(this.tableId, cardsToDeal[c].player, cardsToDeal[c].card.cardId, cardString);
            }
        }
        handleExposePrivateCards(packet: any): void {
            this.tableManager.bettingRoundComplete(this.tableId);
            this.tableManager.exposePrivateCards(this.tableId, packet.cards)
        }
        handlePlayerPokerStatus(packet: any): void {
            var status = packet.status;
            var tableStatus = data.PlayerTableStatus.SITTING_IN;
            if (status == com.cubeia.games.poker.io.protocol.PlayerTableStatusEnum.SITOUT) {
                tableStatus = data.PlayerTableStatus.SITTING_OUT;
            }
            this.tableManager.updatePlayerStatus(this.tableId, packet.player, tableStatus, packet.away, packet.sitOutNextHand);
        }
        handlePotTransfers(packet: any): void {
            var pots:data.Pot[] = [];
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
        }
        handleFuturePlayerAction(packet: any): void {
            var futureActions:string[] = [];
            var actions = packet.actions;
            console.log("packet handler ACTIONS:");
            console.log(actions);
            for (var i = 0; i < actions.length; i++) {
                var act:string = util.ActionUtils.getActionType(actions[i].action);
                futureActions.push(act);
            }
            this.tableManager.onFutureAction(this.tableId, futureActions, packet.callAmount, packet.minBetAmount);
        }
    }

    export class PokerRequestHandler {
        private tableManager: data.TableManager;
        constructor(public tableId: number) {
            this.tableManager = data.TableManager.getInstance();
        }
        onMyPlayerAction(actionType: string, amount: string) {
            console.log("ON my player action");
            console.log(actionType);
            console.log(amount);
            var tableRequestHandler = new TableRequestHandler(this.tableId);
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
        }
        sendAction(actionType: any, betAmount: any, raiseAmount: any): void {
            var action = util.ActionUtils.getPlayerAction(this.tableId, PokerSequence.getInstance().getSequence(this.tableId),
                actionType, betAmount, raiseAmount);
            this.sendGameTransportPacket(action);
        }
        sendGameTransportPacket(gamedata: any): void {
            var connector = SocketManager.getInstance().getConnector();
            connector.sendStyxGameData(0, this.tableId, gamedata);
            console.log("package sent to table " + this.tableId);
            console.log(gamedata);
        }
        buyIn(amount: any): void {
            var buyInRequest = new com.cubeia.games.poker.io.protocol.BuyInRequest();
            buyInRequest.amount = amount;
            buyInRequest.sitInIfSuccessful = true;
            this.sendGameTransportPacket(buyInRequest);
        }
        requestBuyInInfo(): void {
            console.log("REQUEST BUY-IN INFO!");
            var buyInInfoRequest = new com.cubeia.games.poker.io.protocol.BuyInInfoRequest();
            this.sendGameTransportPacket(buyInInfoRequest);
        }
        sitOut(): void {
            var sitOut = new com.cubeia.games.poker.io.protocol.PlayerSitoutRequest();
            sitOut.player = data.Player.getInstance().id;
            this.sendGameTransportPacket(sitOut);
        }
        sitIn():void {
            var sitIn = new com.cubeia.games.poker.io.protocol.PlayerSitinRequest();
            sitIn.player = data.Player.getInstance().id;
            this.sendGameTransportPacket(sitIn);
        }
        sendRebuyResponse(answer: any): void {
            var rebuy = new com.cubeia.games.poker.io.protocol.RebuyResponse();
            rebuy.answer = answer;
            this.sendGameTransportPacket(rebuy);
        }
        sendAddOnRequest(): void {
            this.sendGameTransportPacket(new com.cubeia.games.poker.io.protocol.PerformAddOn());
        }
    }
}