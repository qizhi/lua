module ui {
    export class HandHistoryLayout {
        constructor(public tableId: number, public closeFunction: (tableId:number)=>void) {
        }

        public prepareUI(tableId: number): void {
        }

        public activate(): void {
        }
        public close(): void {
        }

        public showHandSummaries(summaries: any[]): void {
        }

        public showHand(hand: any): void {
        }
    }
}