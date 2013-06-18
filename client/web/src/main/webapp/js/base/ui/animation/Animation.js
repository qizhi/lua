"use strict";
var Poker = Poker || {};

/**
 * Base class for animations handle by tha Poker.AnimationManager
 *
 * @Type {Poker.Animation}
 */
Poker.Animation = Class.extend({
    id : null,
    element : null,
    callback : null,
    nextAnimation : null,
    timed : false,
    startTime : null,
    transitionTime : 0,
    init : function(element) {

        if(typeof(element)=="undefined") {
            throw "Poker.Animation requires an element";
        }
        if(typeof(element.length)!="undefined" && element.length==0) {
            throw "Poker.Animation requires an element";
        } else if(typeof(element.length)!="undefined") {
            element = element.get(0);
        }
        this.element = element;
        this.startTime = this.getNow();
    },
    getNow : function() {
        return new Date().getTime();
    },
    getRemainingTime : function() {
        if(this.timed === true) {
            var now = this.getNow();

            var timeLeft = (this.transitionTime - (now-this.startTime)/1000);

            if(timeLeft<0){
                timeLeft=0;
            }
            return timeLeft;
        } else {
            return this.transitionTime;
        }

    },
    cancel : function () {
        var cssUtils = new Poker.CSSUtils();
        cssUtils.clear(this.element);
    },
    setTimed : function(timed) {
        this.timed = timed;
        return this;
    },
    addCallback : function(callback) {
        this.callback = callback;
        return this;
    },
    prepare : function() {

    },
    animate : function() {

    },
    next : function(el) {

    },
    build : function() {

    },
    prepareElement : function() {

    },
    start : function(animationManager) {
        animationManager.animate(this);

    }
});