"use strict";
var Poker = Poker || {};

Poker.DevTools = Class.extend({
    tableId : 999999,
    tableManager : null,
    cards : null,
    cardIdSeq : 0,
    mockEventManager : null,

    init : function() {
        var self = this;
        this.initCards();
    },
    initCards : function() {
       var suits = "hsdc ";
       var rank = "0"
    },
    launch : function() {
        var self = this;
        Poker.AppCtx.getViewManager().onLogin();
        setTimeout(function(){
            self.createTable();
        },1000);
    },

    createTable : function() {
        var self = this;

        var tableName = "Dev Table";
        this.tableManager = Poker.AppCtx.getTableManager();
        var tableViewContainer = $(".table-view-container");
        var templateManager = new Poker.TemplateManager();


        var beforeFunction = function() {
            var tableLayoutManager = new Poker.TableLayoutManager(self.tableId, tableViewContainer,
                templateManager,10,new Poker.SoundManager());

            self.tableManager.createTable(self.tableId, 10, tableName , tableLayoutManager);
            Poker.AppCtx.getViewManager().addTableView(tableLayoutManager,tableName);
            new Poker.PositionEditor("#tableView-"+self.tableId);
        };

        var cleanUpFunction = function() {
            self.tableManager.leaveTable(self.tableId);
            Poker.AppCtx.getViewManager().removeTableView(self.tableId);
        };

        this.mockEventManager = new Poker.MockEventManager(beforeFunction,cleanUpFunction);

        var mockEvent = function(name,func,delay) {
            return new Poker.MockEvent(name,func,delay);
        };
        Poker.MyPlayer.id = 0;
        Poker.MyPlayer.name= "test";
        $(".table-view-container").show();
        this.mockEventManager.addEvent(
            mockEvent("Add players",function(){
                for(var i = 0; i<10; i++) {
                    self.addPlayer(i,i,"CoolPlayer"+i);
                }
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Update state",function(){
                var bl = new com.cubeia.games.poker.io.protocol.BlindsLevel();
                bl.bigBlind = "1";
                bl.smallBlind = "0.5";
                bl.isBreak = false;
                var bs =
                self.tableManager.notifyGameStateUpdate(self.tableId, bl ,0,com.cubeia.games.poker.io.protocol.BetStrategyEnum.FIXED_LIMIT);
        }));
        this.mockEventManager.addEvent(
            mockEvent("Deal cards",function(){
                for(var i = 0; i<10; i++) {
                    self.dealCards(i,i);
                }
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Player 0 small blind",function(){
                self.playerAction(0,Poker.ActionType.SMALL_BLIND);
            })
        );

        this.mockEventManager.addEvent(
            mockEvent("Update main pots", function(){
                self.tableManager.updateTotalPot(self.tableId,10000);
                self.tableManager.updatePots(self.tableId,[new Poker.Pot(0,Poker.PotType.MAIN,10000)]);
            })
        );


        this.mockEventManager.addEvent(
            mockEvent("Player 1 big blind",function(){
                self.playerAction(1,Poker.ActionType.BIG_BLIND);
            })
        );

        this.mockEventManager.addEvent(
            mockEvent("Activate player 2", function(){
                self.tableManager.handleRequestPlayerAction(self.tableId,2,
                    [
                        new Poker.Action(Poker.ActionType.FOLD,0,0),
                        new Poker.Action(Poker.ActionType.CALL,10,10),
                        new Poker.Action(Poker.ActionType.RAISE,10,1000000)
                    ],15000)
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Player 2 call",function(){
                self.playerAction(2,Poker.ActionType.CALL);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Player 3 fold",function(){
                self.playerAction(3,Poker.ActionType.FOLD,0);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Player 4 raise",function(){
                self.playerAction(4,Poker.ActionType.RAISE);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Player 5 raise",function(){
                self.playerAction(5,Poker.ActionType.RAISE);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("All players call",function(){
                self.playerAction(6,Poker.ActionType.CALL);
                self.playerAction(7,Poker.ActionType.CALL);
                self.playerAction(8,Poker.ActionType.CALL);
                self.playerAction(9,Poker.ActionType.CALL);
                self.playerAction(0,Poker.ActionType.CALL);
                self.playerAction(1,Poker.ActionType.CALL);
                self.playerAction(2,Poker.ActionType.CALL);
                self.playerAction(4,Poker.ActionType.CALL);
            })
        );

        this.mockEventManager.addEvent(
            mockEvent("Deal flop", function(){
                self.tableManager.dealCommunityCards(self.tableId,[
                    { id : self.cardIdSeq++, cardString : "as" },
                    { id : self.cardIdSeq++, cardString : "ad" },
                    { id : self.cardIdSeq++, cardString : "ks" }

                ]);
            })
        );


        this.mockEventManager.addEvent(
            mockEvent("Deal turn", function(){
                self.tableManager.dealCommunityCards(self.tableId,
                [{ id : self.cardIdSeq++, cardString : "qs" }]);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Deal river", function(){
                self.tableManager.dealCommunityCards(self.tableId,[{ id : self.cardIdSeq++, cardString : "js" }]);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Future actions",function(){
              self.tableManager.onFutureAction(self.tableId,[
                  Poker.ActionType.FOLD,
                  Poker.ActionType.CALL,
                  Poker.ActionType.RAISE
              ],100,200);
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Request Player Action",function(){
                self.tableManager.handleRequestPlayerAction(self.tableId,0,
                    [
                        new Poker.Action(Poker.ActionType.FOLD,0,0),
                        new Poker.Action(Poker.ActionType.CALL,10,10),
                        new Poker.Action(Poker.ActionType.RAISE,10,1000000)
                    ],15000)
            })
        );
        this.mockEventManager.addEvent(
            mockEvent("Player 1 bet blind",function(){
                self.playerAction(0,Poker.ActionType.BET);
            })
        );
        this.mockEventManager.addEvent(mockEvent("Update pots", function(){
            self.tableManager.updatePots(self.tableId, [new Poker.Pot(0,Poker.PotType.MAIN,9000),
                new Poker.Pot(1,Poker.PotType.SIDE,1000)] );
        }));
        this.mockEventManager.addEvent(mockEvent("End hand", function(){
            var bestHands = [];

            var bh = new com.cubeia.games.poker.io.protocol.BestHand();
            bh.handType =  com.cubeia.games.poker.io.protocol.HandTypeEnum.HIGH_CARD;
            bh.player = 1;
            bh.cards = [self.getCard(1,"s"),self.getCard(2,"d")];
            bestHands.push(bh);


            var potTransfers = new com.cubeia.games.poker.io.protocol.PotTransfers();
            potTransfers.fromPlayerToPot = false;

            potTransfers.transfers = [self.getPotTransfer(0,1,8000),self.getPotTransfer(0,2,1000),self.getPotTransfer(1,2,1000)];

            self.tableManager.endHand(self.tableId,bestHands,potTransfers);
        }));
    },
    getPotTransfer : function(potId,playerId,amount) {
        var pt = new com.cubeia.games.poker.io.protocol.PotTransfer();
        pt.amount = amount;
        pt.playerId = playerId;
        pt.potId = potId;
        return pt;
    },
    getCard : function(rank,suit) {
        var card1 = new com.cubeia.games.poker.io.protocol.GameCard();
        card1.rank = rank;
        card1.suit = suit;
        return card1;
    },

    addPlayer : function(seat,playerId,name) {
        this.tableManager.addPlayer(this.tableId,seat,playerId, name);
        this.tableManager.updatePlayerStatus(this.tableId, playerId, Poker.PlayerTableStatus.SITTING_IN);
        this.tableManager.updatePlayerBalance(this.tableId,playerId, 100000);
    },
    dealCards : function(seat,playerId) {
        this.tableManager.dealPlayerCard(this.tableId,playerId,this.cardIdSeq++,"  ");
        this.tableManager.dealPlayerCard(this.tableId,playerId,this.cardIdSeq++,"  ");
    },
    playerAction : function(playerId,action,amount) {
        if(!amount) {
            amount = 10000
        }
        this.tableManager.handlePlayerAction(this.tableId,playerId,action,amount);
    },

    getRandomCard : function() {

    }
});

$(document).ready(function(){

    if(document.location.hash.indexOf("dev")!=-1){
        console.log("dev mode enabled");
        var dt = new Poker.DevTools();
        setTimeout(function(){dt.launch();},1000);
    }
});
