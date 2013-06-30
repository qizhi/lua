///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/Table.ts"/>
///<reference path="../data/Player.ts"/>
module ui {
    export class TableLayout {
        constructor(public tableId:number, public capacity:number) {
        }

        public onTableCreated(table:data.Table):void {
        }

        //buy in
        public onBuyInCompleted(): void {
            //this.buyInDialog.close();
        }
        public onBuyInError(msg: string): void {
            //this.buyInDialog.onError(msg);
        }
        public onBuyInInfo(tableName: string, balanceInWallet: number, balanceOnTable: number, maxAmount: number, minAmount: number, mandatory?: any, currencyCode?: any): void {
            //this.buyInDialog.show(this.tableId,tableName,balanceInWallet,maxAmount,minAmount,currencyCode);
        }

        //hand
        public onStartHand(handId: number): void {
            console.log("Hand " + handId + " started");
        }

        public onPotToPlayerTransfers(transfers: any[]): void {
            /*var transferAnimator = new Poker.PotTransferAnimator(this.tableId, this.animationManager, $("#seatContainer-" + this.tableId), this.mainPotContainer);

            for (var i = 0; i < transfers.length; i++) {
                var trans = transfers[i];
                if (trans.amount <= 0) {
                    continue;
                }
                var seat = this.getSeatByPlayerId(trans.playerId);
                seat.onPotWon(trans.potId, trans.amount);
                //transferAnimator.addTransfer(seat, trans.potId, trans.amount);
                //this.tableLog.appendPotTransfer(seat.player, trans.potId, trans.amount);
            }
            //transferAnimator.start();
            this.playSound(Poker.Sounds.POT_TO_PLAYERS);*/
        }

        public onPlayerHandStrength(player: data.IPlayer, hand: number, cardStrings: string[], handEnded: boolean): void {
            //var seat = this.getSeatByPlayerId(player.id);
            //seat.showHandStrength(hand);
            if (handEnded == true) {
                //this.tableLog.appendHandStrength(player, hand, cardStrings);
            }
        }
        public onPlayerActed(player:data.IPlayer, actionType:string, amount:string): void {
        }
        public onMoveDealerButton(seatId:number): void {
        }

        public onPlayerAdded(seatId: number, player: data.IPlayer): void {
            console.log("Player " + player.name + " added at seat " + seatId);

        }

        public updateAvatar(playerId: number, avatarUrl: string): void {
        }

        public onPlayerRemoved(playerId: number): void {
            /*var seat = this.getSeatByPlayerId(playerId);
        if (this.myPlayerSeatId == seat.seatId) {
            this.myPlayerSeatId = -1;
            var tournamentTable = Poker.AppCtx.getTournamentManager().isTournamentTable(this.tableId);
            console.log("TOURNAMENT TABLE = " + tournamentTable);
            console.log(Poker.AppCtx.getTournamentManager().tournamentTables);
            if(tournamentTable==false) {
                Poker.AppCtx.getDialogManager().displayGenericDialog(
                    {header: "Seating info", message : "You have been removed from table "});
            }

        }
        seat.clearSeat();
        this.seats.remove(seat.seatId);
        this.addEmptySeatContent(seat.seatId,-1,(this.myPlayerSeatId==-1));
        this.soundManager.playerAction({id:"action-leave"}, this.tableId);*/
        }

        public onDealPlayerCard(player: data.IPlayer, cardId: number, cardString: string): void {
            /*this.playSound(Poker.Sounds.DEAL_PLAYER);
        var seat = this.getSeatByPlayerId(player.id);
        var card = new Poker.Card(cardId,this.tableId,cardString,this.templateManager);
        seat.dealCard(card);
        this._storeCard(card);*/
        }

        public onPlayerUpdated(p: data.IPlayer): void {
            /*var seat = this.getSeatByPlayerId(p.id);
        if(seat==null) {
            console.log("Unable to find player " + p.name + " seat");
            return;
        }
        seat.updatePlayer(p);*/
        }

        public onPlayerStatusUpdated(p: data.IPlayer): void {
            /*var seat = this.getSeatByPlayerId(p.id);
        if(seat==null) {
            console.log("Unable to find player " + p.name + " seat");
            return;
        }
        seat.updatePlayerStatus(p);*/
        }

        public onBettingRoundComplete(): void {
            /*var seats = this.seats.values();
            for (var x = 0; x < seats.length; x++) {
                seats[x].onBettingRoundComplete();
            }*/
        }

        //action
        public displayFutureActions  (actions:string[], callAmount:any, minBetAmount:any):void {
            //this.myActionsManager.displayFutureActions(actions, callAmount, minBetAmount);
        }

        //leave
        public onLeaveTableSuccess() {
            /*$(this.tableView).hide();
            for (var i = 0; i < this.capacity; i++) {
                var s = $("#seat" + i + "-" + this.tableId);
                s.empty();
                s.attr("class", "seat");
            }
            if (this.myPlayerSeatId != -1) {
                this.seats.get(this.myPlayerSeatId).clear();
            }
            this.myPlayerSeatId = -1;
            this._resetCommunity();
            var cards = this.cardElements.values();
            for (var x = 0; x < cards.length; x++) {
                $("#" + cards[x].getCardDivId()).remove();
            }
            this.myActionsManager.clear();
            this.soundManager.playerAction({ id: "action-leave" }, this.tableId);*/
        }

        //
        //request
        public onRequestPlayerAction(player: data.IPlayer, allowedActions: any[], timeToAct: number, mainPot: number, fixedLimit: boolean): void {
            /*var seats = this.seats.values();
            for (var s = 0; s < seats.length; s++) {
                seats[s].inactivateSeat();
            }
            var seat = this.getSeatByPlayerId(player.id);

            if (player.id == Poker.MyPlayer.id) {
                this.playSound(Poker.Sounds.REQUEST_ACTION);
            }

            seat.activateSeat(allowedActions, timeToAct, mainPot, fixedLimit);*/
        }

        //
        //rebuy
        public onRequestRebuy(player: data.IPlayer, rebuyCost: number, chipsForRebuy: number, timeToAct: number): void {
            //var seat = this.getSeatByPlayerId(player.id);
            //seat.rebuyRequested(rebuyCost, chipsForRebuy, timeToAct);
        }
        public hideRebuyButtons(player: data.IPlayer): void {
            //var seat = this.getSeatByPlayerId(player.id);
            //seat.hideRebuyButtons();
        }
        public onRebuyPerformed(player: data.IPlayer, addOnCost?: number, chipsForAddOn?: number): void {
            //var seat = this.getSeatByPlayerId(player.id);
            //seat.rebuyPerformed();
        }

        //
        //addon
        public onRequestAddOn(player:data.IPlayer, addOnCost:number, chipsForAddOn:number): void {
            //var seat = this.getSeatByPlayerId(player.id);
            //seat.addOnRequested(addOnCost, chipsForAddOn);
        }
        public hideAddOnButton(player: data.IPlayer): void {
            //var seat = this.getSeatByPlayerId(player.id);
            //seat.hideAddOnButton();
        }

        //
        //pot
        public onTotalPotUpdate(amount: number): void {
            //  this.totalPotContainer.show().find(".amount").html(Poker.Utils.formatCurrency(amount));
        }
        public onPotUpdate(pots: data.Pot[]): void {
            for (var i = 0; i < pots.length; i++) {
                /*var potElement = this.mainPotContainer.find(".pot-" + pots[i].id);
                if (potElement.length > 0) {
                    potElement.html(Poker.Utils.formatCurrency(pots[i].amount));
                } else {
                    this.mainPotContainer.append(this.templateManager.render("mainPotTemplate",
                        { potId: pots[i].id, amount: Poker.Utils.formatCurrency(pots[i].amount) }));
                }
                */
            }
        }

        //card
        public onDealCommunityCards(cards: any[]): void {
            for (var i = 0; i < cards.length; i++) {
                //this.onDealCommunityCard(cards[i].id, cards[i].cardString);
            }
            //this.tableLog.appendCommunityCards(cards);
        }
        public exposePrivateCards(playerCards: any[]): void {
            for (var i = 0; i < playerCards.length; i++) {
                var cards = playerCards[i].cards;
                for (var j = 0; j < cards.length; j++) {
                    this.onExposePrivateCard(cards[j].id, cards[j].cardString);
                }
               // this.tableLog.appendExposedCards(playerCards[i]);
            }
        }
        public onExposePrivateCard(cardId: number, cardString: string): void {
            /*this.playSound(Poker.Sounds.REVEAL);
            var card = this.cardElements.get(cardId);
            if (cardString == card.cardString) {
                return;
            }

            var animManager = this.animationManager;

            // callback to ensure that the image is loaded before firing the animation
            var imageLoadedCallback = function () {
                setTimeout(function () {
                    new Poker.CSSClassAnimation(card.getJQElement()).addClass("exposed").start(animManager);
                }, 50)
        };

            card.exposeCard(cardString, imageLoadedCallback);
            */
        }

        //
        //blind
        public onBlindsLevel  (level:any, currency:any, secondsToNextLevel:number):void {
            if (level.smallBlind != null && level.bigBlind != null) {
                /*this.tableInfoElement.show();
                this.tableInfoElement.find(".table-blinds-value").html(Poker.Utils.formatCurrency(level.smallBlind) +
                    "/" + Poker.Utils.formatCurrency(level.bigBlind));
                this.myActionsManager.setBigBlind(Math.floor(parseFloat(level.bigBlind.replace(",", ""))), currency);
                if (secondsToNextLevel >= 0) {
                    this.clock.sync(secondsToNextLevel);
                    this.tableInfoElement.find(".time-to-next-level").show();
                } else {
                    this.tableInfoElement.find(".time-to-next-level").hide();
                }*/
            }
        }

    }
}