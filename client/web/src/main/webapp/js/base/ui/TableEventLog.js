var Poker = Poker || {};

/**
 *
 * @type {Poker.TableEventLog}
 * @extends {Poker.Log}
 */
Poker.TableEventLog = Poker.Log.extend({

    chatEnabled : true,
    pokerEventsEnabled : true,

    init : function(container) {
        var tableLogElement = container.find(".table-event-log");
        this._super(tableLogElement);

        var self = this;
        container.find(".table-event-log-settings").click(function(e){
            new Poker.ContextMenu(e,self._getItems());
        });

    },
    _getItems : function() {
        var self = this;
        var items = [];
        var chatCallback = function(){
            self.chatEnabled = !self.chatEnabled;
        };
        if(this.chatEnabled == true) {
          items.push({ title :  i18n.t("table.log.hide-chat"), callback : chatCallback});
        } else {
          items.push({ title :  i18n.t("table.log.show-chat"), callback : chatCallback});
        }
        var pokerEventsCallback = function(){
            self.pokerEventsEnabled = !self.pokerEventsEnabled;
        };
        if(this.pokerEventsEnabled == true) {
            items.push({ title :  i18n.t("table.log.hide-events"), callback : pokerEventsCallback });
        } else {
            items.push({ title :  i18n.t("table.log.show-events"), callback : pokerEventsCallback });
        }

       return items;
    },
    appendAction : function(player, actionType, amount) {
        var data = {
            name : player.name,
            action : actionType.text,
            amount : amount,
            showAmount : (amount!="0")
        };
        this.appendPokerEvent("playerActionLogTemplate",data);
    },
    appendCommunityCards : function(cards) {
        this.appendPokerEvent("communityCardsLogTemplate", { cards : cards });
    },
    appendExposedCards : function(playerCards) {
        this.appendPokerEvent("playerCardsExposedLogTemplate", playerCards);
    },
    appendHandStrength : function(player,hand,cardStrings) {
        this.appendPokerEvent("playerHandStrengthLogTemplate", {player : player, hand : hand, cardStrings : cardStrings});
    },
    appendPotTransfer : function(player, potId, amount) {
        this.appendPokerEvent("potTransferLogTemplate", {player : player, potId : potId, amount : Poker.Utils.formatCurrency(amount) });
    },
    appendNewHand : function(handId) {
        this.appendPokerEvent("newHandLogTemplate", {handId : handId});
    },
    appendChatMessage : function(player, message) {
        if(this.chatEnabled == true ) {
            this.appendTemplate("chatMessageTemplate", { player : player, message : message});
        }
    },
    appendPokerEvent : function(template,data) {
        if(this.pokerEventsEnabled == true) {
            this.appendTemplate(template,data);
        }
    }
});