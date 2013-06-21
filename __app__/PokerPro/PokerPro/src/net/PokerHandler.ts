module net {
    export class PokerPacketHandler {
        constructor(public tableId: number) {
        }
        handleRequestAction (requestAction:any):void {
        }

        handleRebuyOffer (rebuyOffer:any, playerId:number):void {
        }
        handleAddOnOffer (addOnOffer:any, playerId:number):void {
        }
        handleAddOnPeriodClosed (playerId:number):void {
        }
        handleRebuyPerformed (playerId:number):void {
        }
        handleAddOnPerformed (playerId:number):void {
        }
        handlePlayerBalance (packet:any):void {
        }
        handlePlayerHandStartStatus (packet:any):void {
        }
        handleBuyIn (protocolObject:any):void {
        }
        handlePerformAction (performAction:any):void {
        }
        handleDealPublicCards (packet:any):void {
        }
        handleDealPrivateCards (protocolObject:any):void {
        }
        handleExposePrivateCards(packet: any): void {
        }
        handlePlayerPokerStatus(packet: any): void {
        }
        handlePotTransfers(packet: any): void {
        }
        handleFuturePlayerAction(packet: any): void {
        }
    }
}