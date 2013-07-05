"user strict";
var Poker = Poker || {};

Poker.HandHistoryManager = Class.extend({
    handHistoryLayouts : null,
    view : null,
    init : function() {
        this.handHistoryLayouts = new Poker.Map();
    },
    close : function() {
        Poker.AppCtx.getViewManager().removeView(this.view);

    },
    requestHandHistory : function(tableId) {
        var self = this;
        var layout = this.handHistoryLayouts.get(tableId);
        if(layout==null) {
            layout = new Poker.HandHistoryLayout($(".view-container"),tableId, function(tableId){
                self.handHistoryLayouts.remove(tableId);
            });
            this.handHistoryLayouts.put(tableId,layout);
        } else {
            layout.activate();
        }
        new Poker.HandHistoryRequestHandler(tableId).requestHandSummaries(30);
    },
    showHandSummaries : function(tableId,summaries) {
        var layout = this.handHistoryLayouts.get(tableId);
        if(layout!=null) {
            var self = this;
            $.each(summaries,function(i,e){
                e.startTime = self.formatDateTime(e.startTime);
            });
            layout.showHandSummaries(summaries);
        } else {
            console.log("undable to find layout");
        }

    },
    showHand : function(hand) {
        var layout = this.handHistoryLayouts.get(hand.table.tableId);
        console.log(hand);
        hand = this.prepareHand(hand);
        layout.showHand(hand);

    },
    formatDateTime : function(milis) {
        var date = new Date(milis);
        var val = function(value) {
            value = ""+value;
            if(value.length==1) {
                value="0"+value;
            }
            return value;
        };
        return  date.getFullYear() +"-" + val((date.getMonth()+1)) + "-"
            + val(date.getDay()) + " " + val(date.getHours()) + ":" + val(date.getMinutes());
    },

    prepareHand : function(hand) {
        console.log(hand);
        var playerMap = new Poker.Map();
        for(var i = 0; i<hand.seats.length; i++)  {
            var seat = hand.seats[i];
            playerMap.put(seat.playerId,seat.name);
            $.extend(seat,{initialBalance : this.formatAmount(seat.initialBalance)});
        }
        hand.startTime = this.formatDateTime(hand.startTime);
        for(var i = 0; i<hand.events.length; i++) {
            var event = hand.events[i];

            if(typeof(event.playerId)!="undefined") {
                event = $.extend(event,{name : playerMap.get(event.playerId), player : true});
            }
            if(typeof(event.action)!="undefined") {
                event = $.extend(event,{
                    action : this.getAction(hand.events[i].action)

                });
            }
            if(typeof(event.cards)!=undefined) {
                event = $.extend(event,{
                    cards : this.extractCards(event.cards)
                });

            }
            if(typeof(event.amount)!="undefined"){
                event= $.extend(event,{
                    amount :  {
                        amount : this.formatAmount(hand.events[i].amount.amount)
                    }
                });
            }
            if(event.type == "TableCardsDealt") {
                event = $.extend(event,{
                    cards : this.extractCards(event.cards),
                    tableCards : true
                });
            }
            if(event.type == "PlayerCardsExposed") {
                event = $.extend(event,{
                    playerCardsExposed : true
                });
            }
            if(event.type == "PlayerCardsDealt") {
                event = $.extend(event,{
                    playerCardsDealt : true
                });
            }
            if(event.type == "PlayerBestHand") {
                event = $.extend(event,{
                    bestHandCards : this.extractCards(event.bestHandCards),
                    name : playerMap.get(event.playerHand.playerId),
                    handDescription : Poker.Hand.fromName(event.handInfoCommon.handType).text
                });
            }

        }
        var results = [];
        for(var x in hand.results.results) {
            results.push(hand.results.results[x]);
        }
        $.extend(hand.results, { res : results});

        for(var i = 0; i<hand.results.res.length; i++) {
            var result = hand.results.res[i];
            result = $.extend(result,{
                name : playerMap.get(result.playerId),
                totalBet : this.formatAmount(result.totalBet),
                totalWin : this.formatAmount(result.totalWin,"0")
            });
        }

        return hand;

    },
    formatAmount : function(amount,emptyStr) {
        if(amount==0) {
            return emptyStr || "";
        } else {
            return Poker.Utils.formatCurrency(amount);
        }
    },
    getAction : function(actionEnumString) {
        var act = Poker.ActionType[actionEnumString];
        if(typeof(act)!="undefined"){
            return act.text;
        }
        return actionEnumString;
    },
    extractCards : function(cards) {
        if(typeof(cards)=="undefined") {
            return null;
        }
        for(var i = 0; i<cards.length; i++) {
            cards[i] = $.extend(cards[i],{text : this.getCard(cards[i])});
        }
        return cards;
    },
    getCard : function(card) {
        return this.getRank(card.rank) + this.getSuit(card.suit);
    },
    getSuit : function(suit){
        switch(suit) {
            case "CLUBS":
                return "c";
            case "DIAMONDS":
                return "d";
            case "HEARTS" :
                return "h";
            case "SPADES":
                return "s";
        }
    },
    getRank : function(rank) {
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
    }


});
