var Poker = Poker || {};

/**
 * @type Poker.Clock
 */
Poker.Clock = Class.extend({
    /**
     * @type Poker.PeriodicalUpdater
     */
    clockUpdater : null,
    currentTime : 0,
    timeElement : null,
    init : function(timeElement) {
        var self = this;
        this.timeElement = timeElement;
        this.clockUpdater = new Poker.PeriodicalUpdater(function(){
            self.increment();
        },1000);
    },
    increment : function() {
        if(this.currentTime==0){
            return;
        }
        this.currentTime--;
        var min = Math.floor(this.currentTime/60);
        var sec = this.currentTime%60;
        this.timeElement.html(min+":"+this.formatSeconds(sec));
    },
    formatSeconds : function(seconds) {
        if(seconds<10) {
            return "0"+seconds ;
        } else {
            return ""+seconds;
        }
    },
    /**
     * Synchronizes the clock if it differs more than one
     * sec from the time, it will start the clock if it's not running
     * @param {Number} time
     */
    sync : function(time) {
        if(Math.abs(this.currentTime-time)>1) {
            this.currentTime = time;
        }
        this.clockUpdater.start();

    },
    stop : function() {
        this.clockUpdater.stop();
    }
});