var net;
(function (net) {
    var PeriodicalUpdater = (function () {
        function PeriodicalUpdater(updateFunction, time) {
            if (typeof time === "undefined") { time = 5000; }
            this.updateFunction = updateFunction;
            this.time = time;
            this.running = false;
        }
        PeriodicalUpdater.prototype.start = function () {
            if (this.running == true) {
                return;
            }
            this.running = true;
            this.startUpdating();
        };
        PeriodicalUpdater.prototype.startUpdating = function () {
            var self = this;
            if (this.running == true) {
                this.currentTimeout = setTimeout(function () {
                    self.updateFunction();
                    self.startUpdating();
                }, this.time);
            }
        };
        PeriodicalUpdater.prototype.stop = function () {
            this.running = false;
            if (this.currentTimeout) {
                clearTimeout(this.currentTimeout);
            }
        };

        /**
        * If you don't want to wait for next timeout,
        * Clears the timeout and runs the update function
        * and then starts the periodical updating again
        */
        PeriodicalUpdater.prototype.rushUpdate = function () {
            if (this.currentTimeout) {
                clearTimeout(this.currentTimeout);
            }
            this.updateFunction();
            this.startUpdating();
        };
        return PeriodicalUpdater;
    })();
    net.PeriodicalUpdater = PeriodicalUpdater;
})(net || (net = {}));
//@ sourceMappingURL=PeriodicalUpdater.js.map
