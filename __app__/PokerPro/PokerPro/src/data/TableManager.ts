///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../net/TableHandler.ts"/>
///<reference path="Table.ts"/>
///<reference path="GameConfig.ts"/>
module data {
    export class TableManager {
        public tables: Map<number, Table>;
        public tableNames: Map<number, string>;
        public dealerSeatId: number;
        constructor() {
            this.tables = new Map<number, Table>();
            this.tableNames = new Map<number, string>();
        }
        
        public getTable(tableId: number): Table {
            return this.tables.get(tableId);
        }
        public tableExist(tableId: number): boolean {
            return this.tables.contains(tableId)
        }
        public isSeated   (tableId:number):boolean {
            var table:Table = this.getTable(tableId);
            if (table != null) {
                return table.myPlayerSeat != null;
            }
            return false;
        }
        public onPlayerLoggedIn  ():void {
            console.log("Checking if there are open tables to reconnect to");
            var tables:Table[] = this.tables.values();
            for (var i = 0; i < tables.length; i++) {
                this.leaveTable(tables[i].id);
                //TODO: we need snapshot to get capacity
                new net.TableRequestHandler(tables[i].id).openTable(10);
            }
        }
        public handleOpenTableAccepted(tableId: number, capacity: number): void {
            /*var viewManager = Poker.AppCtx.getViewManager();
            if (viewManager.findViewByTableId(tableId) != null) {
                viewManager.activateViewByTableId(tableId);
            } else {
                var name = this.tableNames.get(tableId);
                if (name == null) {
                    name = "Table"; //TODO: fix
                }
                var tableViewContainer = $(".table-view-container");
                var templateManager = new Poker.TemplateManager();
                var soundManager = new Poker.SoundManager(Poker.AppCtx.getSoundRepository(), tableId);
                var tableLayoutManager = new Poker.TableLayoutManager(tableId, tableViewContainer, templateManager, capacity, soundManager);
                this.createTable(tableId, capacity, name, tableLayoutManager);
                Poker.AppCtx.getViewManager().addTableView(tableLayoutManager, name);
            }*/
        }

        createTable(tableId:number, capacity:number, name:string, tableLayout:ui.TableLayout):void {
            console.log("Creating table " + tableId + " with name = " + name);
            var table:Table = new Table(tableId, capacity, name);
            table.layout = tableLayout;
            this.tables.put(tableId, table);
            tableLayout.onTableCreated(table);

            console.log("Nr of tables open = " + this.tables.size());
        }

        handleBuyInResponse(tableId:number, status:number):void {
            if (status == com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.PENDING) {
                var table:Table = this.getTable(tableId);
                table.layout.onBuyInCompleted();
                //$.ga._trackEvent("poker_table", "buy_in_success");
            } else if (status != com.cubeia.games.poker.io.protocol.BuyInResultCodeEnum.OK) {
                //$.ga._trackEvent("poker_table", "buy_in_error");
                this.handleBuyInError(tableId, status);
            }
        }
        
        handleBuyInError(tableId:number, status:number):void {
            console.log("buy-in status = " + status);
            var table:Table = this.getTable(tableId);
            table.layout.onBuyInError(i18n.t("buy-in.error"));
        }

        handleBuyInInfo(tableId:number, balanceInWallet:number, balanceOnTable:number, maxAmount:number, minAmount:number, mandatory:number, currencyCode:number):void {
            var table = this.getTable(tableId);
            table.layout.onBuyInInfo(table.name, balanceInWallet, balanceOnTable, maxAmount, minAmount, mandatory, currencyCode);
        }

        startNewHand(tableId:number, handId:number):void {
            var table:Table = this.tables.get(tableId);
            table.handCount++;
            table.handId = handId;
            table.layout.onStartHand(handId);
        }
        /**
         * Called when a hand is complete and calls the TableLayoutManager
         * This method will trigger a tableManager.clearTable after
         * 15 seconds (us
         * @param {Number} tableId
         * @param {com.cubeia.games.poker.io.protocol.BestHand[]} hands
         * @param {com.cubeia.games.poker.io.protocol.PotTransfers} potTransfers
         */
        endHand(tableId: number, hands: any[], potTransfers: any): void {
            for (var i = 0; i < hands.length; i++) {
                this.updateHandStrength(tableId, hands[i], true);
            }
            var table:Table = this.tables.get(tableId);
            console.log("pot transfers:");
            console.log(potTransfers);
            var count:number = table.handCount;
            var self:TableManager = this;

            if (potTransfers.fromPlayerToPot === false) {
                table.layout.onPotToPlayerTransfers(potTransfers.transfers);
            }

            setTimeout(function () {
                //if no new hand has started in the next 15 secs we clear the table
                self.clearTable(tableId, count);
            }, 15000);
        }

        updateHandStrength(tableId:number, bestHand:any, handEnded:any):void {
            this.showHandStrength(tableId, bestHand.player,
                bestHand.handType,
                this.getCardStrings(bestHand.cards),
                handEnded);//Hand.fromId(bestHand.handType),
        }
        getCardStrings(cards:any):string[] {
            var converted:string[] = [];
            for (var i = 0; i < cards.length; i++) {
                converted.push(util.Utils.getCardString(cards[i]));
            }
            return converted;
        }
        clearTable(tableId: number, handCount: number): void {
            var table:Table = this.tables.get(tableId);
            if (table.handCount == handCount) {
                console.log("No hand started clearing table");
                table.layout.onStartHand(this.dealerSeatId);
            } else {
                console.log("new hand started, skipping clear table")
            }
        }

        showHandStrength(tableId:number, playerId:number, hand:number, cardStrings:string[], handEnded:boolean):void {
            var table:Table = this.tables.get(tableId);
            var player:IPlayer = table.getPlayerById(playerId);
            table.layout.onPlayerHandStrength(player, hand, cardStrings, handEnded);
        }
        handlePlayerAction(tableId:number, playerId:number, actionType:string, amount:string) {
            var table: Table = this.tables.get(tableId);
            var player: IPlayer = table.getPlayerById(playerId);
            table.layout.onPlayerActed(player, actionType, amount);
        }
        setDealerButton(tableId:number, seatId:number):void {
            var table: Table = this.tables.get(tableId);
            table.layout.onMoveDealerButton(seatId);
        }
        addPlayer(tableId: number, seat: number, playerId: number, playerName: string): void {
            var self: TableManager = this;
            console.log("adding player " + playerName + " at seat" + seat + " on table " + tableId);
            var table: Table = this.tables.get(tableId);
            var p: UserInfo = new UserInfo(playerId, playerName);
            table.addPlayer(seat, p);
            if (playerId == Player.getInstance().id) {
                table.myPlayerSeat = seat;
            }
            table.layout.onPlayerAdded(seat, p);
            if (Player.getInstance().loginToken != null) {
                /*Poker.AppCtx.getPlayerApi().requestPlayerProfile(playerId, Poker.MyPlayer.loginToken,
                    function (profile) {
                        self.updatePlayerAvatar(playerId, table, profile);
                    },
                    function () {
                        self.updatePlayerAvatar(playerId, table, null);
                    }
                    );
        */
            } else {
                self.updatePlayerAvatar(playerId, table, null);
                console.log("No loginToken available to request player info from player api");
            }
        }

        updatePlayerAvatar(playerId:number, table:Table, profile:any):void {
            if (profile != null) {
                table.layout.updateAvatar(playerId, profile.externalAvatarUrl);
            } else {
                table.layout.updateAvatar(playerId, null);
            }
        }

        removePlayer(tableId:number, playerId:number):void {
            console.log("removing player with playerId " + playerId);
            var table: Table = this.tables.get(tableId);
            table.removePlayer(playerId);
            if (playerId ==Player.getInstance().id) {
                table.myPlayerSeat = null;
            }
            table.layout.onPlayerRemoved(playerId);
        }

        /**
         * handle deal cards, passes a card string as parameter
         * card string i h2 (two of hearts), ck (king of spades)
         * @param {Number} tableId the id of the table
         * @param {Number} playerId  the id of the player
         * @param {Number} cardId id of the card
         * @param {String} cardString the card string identifier
         */
        dealPlayerCard(tableId:number, playerId:number, cardId:number, cardString:string):void {
            var table: Table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onDealPlayerCard(player, cardId, cardString);
        }
        updatePlayerBalance(tableId: number, playerId: number, balance: string): void {
            var table: Table = this.tables.get(tableId);
            var p = table.getPlayerById(playerId);
            if (p == null) {
                console.log("Unable to find player to update balance playerId = " + playerId + ", tableId = " + tableId);
                return;
            }
            p.balance = balance;
            table.layout.onPlayerUpdated(p);
        }

        
        updatePlayerStatus(tableId: number, playerId: number, status: number, away?: boolean, sitOutNextHand?: boolean): void {
            var table = this.tables.get(tableId);
            var p: IPlayer = table.getPlayerById(playerId);
            if (p == null) {
                throw "Player with id " + playerId + " not found";
            }
            p.tableStatus = status;
            p.away = away;
            p.sitOutNextHand = sitOutNextHand;
            table.layout.onPlayerStatusUpdated(p);
        }
        
        setNoMoreBlinds(tableId: number, enable: boolean) {
            var table: Table = this.tables.get(tableId);
            table.noMoreBlinds = enable;
        }
        handleRequestPlayerAction(tableId: number, playerId: number, allowedActions:any[], timeToAct:number) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            var fixedLimit = table.betStrategy === com.cubeia.games.poker.io.protocol.BetStrategyEnum.FIXED_LIMIT;
            table.layout.onRequestPlayerAction(player, allowedActions, timeToAct, table.totalPot, fixedLimit); //this.totalPot???
        }
        handleRebuyOffer(tableId, playerId, rebuyCost, chipsForRebuy, timeToAct) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onRequestRebuy(player, rebuyCost, chipsForRebuy, timeToAct);
        }
        hideRebuyButtons(tableId, playerId) {
            console.log("Getting table " + tableId);
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            console.log("Player " + player);
            table.layout.hideRebuyButtons(player);
        }
        handleAddOnOffer(tableId, playerId, addOnCost, chipsForAddOn) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onRequestAddOn(player, addOnCost, chipsForAddOn);
        }
        handleAddOnPeriodClosed(tableId, playerId) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.hideAddOnButton(player);
        }
        hideAddOnButton(tableId, playerId) {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.hideAddOnButton(player);
        }
        handleRebuyPerformed(tableId: number, playerId: number, addOnCost?: number, chipsForAddOn?: number): void {
            var table = this.tables.get(tableId);
            var player = table.getPlayerById(playerId);
            table.layout.onRebuyPerformed(player);
        }
        updateTotalPot(tableId:number, amount:number):void {
            var table:Table = this.tables.get(tableId);
            table.totalPot = amount;
            table.layout.onTotalPotUpdate(amount);
        }
        dealCommunityCards(tableId, cards) {
            var table = this.getTable(tableId);
            table.layout.onDealCommunityCards(cards);
        }
        updatePots(tableId:number, pots:data.Pot[]):void {
            var table:Table = this.tables.get(tableId);
            var totalPot:number = 0;
            for (var i = 0; i < pots.length; i++) {
                totalPot += pots[i].amount;
            }
            table.layout.onTotalPotUpdate(totalPot);
            table.layout.onPotUpdate(pots);
        }

        exposePrivateCards(tableId, cards) {
            var playerCardMap: Map<IPlayer, any> = new Map<IPlayer, any>();
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
        }

        notifyWaitingToStartBreak(tableId) {
            //var dialogManager = Poker.AppCtx.getDialogManager();
            //dialogManager.displayGenericDialog({ tableId: tableId, translationKey: "break-is-starting" });
        }
        /**
         * @param {Number} tableId
         * @param {com.cubeia.games.poker.io.protocol.BlindsLevel} newBlinds
         * @param {Number} secondsToNextLevel
         * @param {com.cubeia.games.poker.io.protocol.BetStrategyEnum} betStrategy
         * @param {com.cubeia.games.poker.io.protocol.Currency} currency
         */
        notifyGameStateUpdate(tableId:number, newBlinds:any, secondsToNextLevel:number, betStrategy:any, currency:any):void {
            console.log("Seconds to next level: " + secondsToNextLevel);
            console.log("notifyGameStateUpdate = " + betStrategy);
            var table = this.getTable(tableId);
            table.betStrategy = betStrategy;
            table.currency = currency;
            this.notifyBlindsUpdated(tableId, newBlinds, currency, secondsToNextLevel);
        }
        notifyBlindsUpdated(tableId:number, newBlinds:any, currency?:any, secondsToNextLevel?:number) {
            console.log("Seconds to next level: " + secondsToNextLevel);
            if (newBlinds.isBreak) {
                /*var dialogManager = Poker.AppCtx.getDialogManager();
                dialogManager.displayGenericDialog({
                    tableId: tableId,
                    header: i18n.t("dialogs.on-break.header"),
                    message: i18n.t("dialogs.on-break.message", { sprintf: [secondsToNextLevel] })
                });*/
            }
            var table = this.getTable(tableId);
            table.layout.onBlindsLevel(newBlinds, currency, secondsToNextLevel);
        }
        notifyTournamentDestroyed(tableId:number):void {
            //var dialogManager = Poker.AppCtx.getDialogManager();
            //dialogManager.displayGenericDialog({ tableId: tableId, translationKey: "tournament-closed" });
            this.tables.get(tableId).tournamentClosed = true;
        }
        bettingRoundComplete(tableId:number):void {
            var table = this.getTable(tableId);
            table.layout.onBettingRoundComplete();

        }
        leaveTable(tableId:number):void {
            console.log("REMOVING TABLE = " + tableId);
            var table = this.tables.remove(tableId);
            if (table == null) {
                console.log("table not found when removing " + tableId);
            } else {
                table.layout.onLeaveTableSuccess();
                table.leave();
            }
            //Poker.AppCtx.getViewManager().removeTableView(tableId);
        }
        onFutureAction(tableId, actions:string[], callAmount, minBetAmount) {
            var table:Table = this.getTable(tableId);
            if (actions.length > 0) {
                var futureActions:string[] = this.getFutureActionTypes(actions);
                table.layout.displayFutureActions(futureActions, callAmount, minBetAmount);
            }
        }
        getFutureActionTypes(actions:string[]):string[] {
            var futureActions: string[] = [];
            for (var i = 0; i < actions.length; i++) {
                var act = actions[i];
                switch (act) {
                    case ActionType.CHECK:
                        futureActions.push(FutureActionType.CHECK_OR_FOLD);
                        futureActions.push(FutureActionType.CHECK_OR_CALL_ANY);
                        break;
                    case ActionType.CALL:
                        futureActions.push(FutureActionType.CALL_CURRENT_BET);
                        futureActions.push(FutureActionType.CALL_ANY);
                        break;
                    case ActionType.RAISE:
                        futureActions.push(FutureActionType.RAISE);
                        futureActions.push(FutureActionType.RAISE_ANY);
                        break;
                }
            }
            if (actions.length > 0) {
                futureActions.push(FutureActionType.FOLD);
            }
            return futureActions;
        }
        onChatMessage(tableId, playerId, message) {
            /*var table = this.getTable(tableId);
            if (table != null) {
                var player = table.getPlayerById(playerId);
                if (player != null) {
                    message = util.Utils.filterMessage(message);
                    table.layout.onChatMessage(player, message);
                } else {
                    console.log("onChatMessage: player not found at table");
                }
            }*/
        }

        private static _instance: TableManager;
        public static getInstance(): TableManager {
            if (TableManager._instance == null) {
                TableManager._instance = new TableManager();
            }
            return TableManager._instance;
        }

    }
}