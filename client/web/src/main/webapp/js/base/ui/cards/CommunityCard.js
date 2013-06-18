"use strict";
var Poker = Poker || {};

Poker.CommunityCard = Poker.Card.extend({
    cardString : null,
    templateManager : null,
    init : function(id,tableId,cardString,templateManager) {
        this._super(id,tableId, cardString,templateManager);
    },
    getTemplate : function() {
        return "communityCardTemplate";
    },
    getCardDivId : function() {
        return "communityCard-"+this.id+"-"+this.tableId;
    }
});