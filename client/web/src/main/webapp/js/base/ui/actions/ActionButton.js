"use strict";
var Poker = Poker || {};
/**
 * @type {Poker.ActionButton}
 */
Poker.ActionButton = Class.extend({
    el : null,
    actionType : null,
    callback : null,
    showAmount : false,
    minAmount : 0,
    maxAmount : 0,
    totalPot : 0,
    init : function(el,actionType,callback,showAmount) {
        this.el = el;
        if(!this.el) {
            console.log("Unable to find action button DOM element with id " + el);
        }
        this.showAmount = showAmount;
        if(this.showAmount==false){
            this.el.find(".amount").hide();
        }
        this.callback=callback;

        this.actionType = actionType;
        this.bindCallBack();
    },
    clear : function() {
        if(this.el) {
            this.el.unbind();
        }
    },
    bindCallBack : function() {

        var self = this;
        if(this.callback!=null && this.actionType!=null) {
            this.el.touchSafeClick(function(e) {
                self.callback(self.actionType,self.minAmount);
            });
        } else if(this.callback!=null) {
            this.el.touchSafeClick(function(e){
                self.callback();
            });
        }
    },
    setAmount : function(minAmount,maxAmount,mainPot){
        if(this.showAmount){
            this.el.find(".amount").html("").append(Poker.Utils.formatCurrency(minAmount)).show();
        }
        if(maxAmount) {
            this.maxAmount = maxAmount;
        }
        if(mainPot) {
            this.totalPot = mainPot;
        }
        this.minAmount = minAmount;
    },
    show : function(){
        this.el.show();
    },
    hide : function() {
        this.el.hide();
    },
    isVisible : function() {
        return this.el.is(":visible");
    }
});

/**
 *
 * @type {Poker.BetAmountButton}
 * @extends {Poker.ActionButton}
 */
Poker.BetAmountButton = Poker.ActionButton.extend({
    betAmountFunction : null,
    init : function(el,actionType,callback,showAmount,betAmountFunction){
        this._super(el,actionType,callback,showAmount);
        this.betAmountFunction = betAmountFunction;
    },
    bindCallBack : function() {
        var self = this;
        this.el.touchSafeClick(function(){
            self.callback(self.actionType, self.betAmountFunction());
        });
    },
    click : function() {
        this.callback(this.actionType, this.betAmountFunction());
    }

});

/**
 *
 * @type {Poker.BetSliderButton}
 * @extends {Poker.ActionButton}
 */
Poker.BetSliderButton = Poker.ActionButton.extend({
    init : function(el,actionType,callback,showAmount){
        this._super(el,actionType,callback,showAmount);
    },
    bindCallBack : function() {
        var self = this;
        this.el.touchSafeClick(function(){
            self.callback(self.minAmount,self.maxAmount,self.totalPot);
        });
    }
});