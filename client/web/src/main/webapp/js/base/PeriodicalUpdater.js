"use strict";
var Poker = Poker || {};
/**
*
 * @type {Poker.PeriodicalUpdater}
 */
Poker.PeriodicalUpdater = Class.extend({
    updateFunction : null,
    time : 5000,
    running : false,
    currentTimeout : null,
    init : function(updateFunction,time) {
        this.updateFunction = updateFunction;
        this.time = time
    },
    start : function() {
        if (this.running==true) {
            return;
        }
        this.running = true;
        this.startUpdating();
    },
    startUpdating : function() {
        var self = this;
        if (this.running == true) {
            this.currentTimeout = setTimeout(function(){
                self.updateFunction();
                self.startUpdating();
            }, this.time);
        }
    },
    stop : function() {
        this.running = false;
        if(this.currentTimeout!=null) {
            clearTimeout(this.currentTimeout);
        }
    },
    /**
     * If you don't want to wait for next timeout,
     * Clears the timeout and runs the update function
     * and then starts the periodical updating again
     */
    rushUpdate : function() {
        if(this.currentTimeout!=null) {
            clearTimeout(this.currentTimeout);
        }
        this.updateFunction();
        this.startUpdating();
    }
});