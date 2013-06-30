module net {
    export class PeriodicalUpdater {
        running: boolean;
        currentTimeout: number;
        constructor(public updateFunction: () => void , public time: number = 5000) {
            this.running = false;
        }

        start(): void {
            if (this.running == true) {
                return;
            }
            this.running = true;
            this.startUpdating();
        }
        startUpdating(): void {
            var self:PeriodicalUpdater = this;
            if (this.running == true) {
                this.currentTimeout = setTimeout(()=> {
                    self.updateFunction();
                    self.startUpdating();
                }, this.time);
            }
        }
        stop(): void {
            this.running = false;
            if (this.currentTimeout) {
                clearTimeout(this.currentTimeout);
            }
        }
        /**
         * If you don't want to wait for next timeout,
         * Clears the timeout and runs the update function
         * and then starts the periodical updating again
         */
        rushUpdate(): void {
            if (this.currentTimeout) {
                clearTimeout(this.currentTimeout);
            }
            this.updateFunction();
            this.startUpdating();
        }
    }
}