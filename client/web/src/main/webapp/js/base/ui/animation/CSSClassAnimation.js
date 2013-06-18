"use strict";
var Poker = Poker || {};

/**
 * A simple Poker.Animation based on css classes
 * @type {Poker.CSSClassAnimation}
 */
Poker.CSSClassAnimation = Poker.Animation.extend({
    classNames : null,
    init : function(element) {
        this._super(element);
        this.classNames = [];
    },
    addClass : function(className) {
        this.classNames.push(className);
        return this;
    },
    animate : function() {
        var el =  $(this.element);
        for(var i = 0; i<this.classNames.length; i++) {
            el.addClass(this.classNames[i]);
        }
    },
    next : function(el) {
        this.nextAnimation = new Poker.CSSClassAnimation(el || this.element);
        return this.nextAnimation;
    }
});