"use strict";
var Poker = Poker || {};

/**
 * Handles a poker card in the UI
 * @type {Poker.Card}
 */
Poker.Card = Class.extend({
    cardString:null,
    id:-1,
    tableId : -1,
    templateManager:null,
    cardImage : null,
    cardElement : null,

    init:function (id, tableId, cardString, templateManager) {
        this.templateManager = templateManager;
        this.id = id;
        this.tableId = tableId;
        if (cardString == "  ") {
            cardString = "back";
        }
        this.cardString = cardString;
    },
    /**
     * Creates and returns the html for the card, does not add anything
     * to the DOM
     * @return {*}
     */
    render : function () {
        var t = this.getTemplate();
        var backfaceImageUrl = "url(" +contextPath+ "/skins/" + Poker.SkinConfiguration.name +"/images/cards/"+this.cardString+".svg)";
        var output = this.templateManager.render(t, {domId:this.id + "-" + this.tableId, backgroundImage:backfaceImageUrl});
        return output;
    },
    /**
     * Exposes a card, from displaying the back to the actual card
     * updates DOM
     * @param cardString
     */
    exposeCard : function(cardString, callback) {
        this.cardString = cardString;
        this.setCardImage("url(" +contextPath+ "/skins/" + Poker.SkinConfiguration.name +"/images/cards/"+this.cardString+".svg)");
        callback();
    },

    /**
     * Sets the backgroundImage attribute on card image div.
     * @param imageUrl
     */

    setCardImage : function(imageUrl) {
        if (!this.cardImage) this.getJQElement();
        // The img element is not suitable for animating elements. Replaced with backgroundImage.
        this.cardImage.style.backgroundImage = imageUrl;
    },


    /**
     * Returns the JQuery card element
     * @return {*}
     */
    getJQElement:function () {
        if(this.cardImage==null) {
            this.cardElement =  $("#" + this.getCardDivId());
            this.cardImage = document.getElementById(this.getCardDivId()).children[0];
        }
        return $("#" + this.getCardDivId());
    },
    getDOMElement : function() {
      return this.getJQElement().get(0);
    },
    getTemplate:function () {
        return "playerCardTemplate";
    },
    getCardDivId:function () {
        return "playerCard-" + this.id + "-" + this.tableId;
    }
});