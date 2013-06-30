///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/HandHistoryManager.ts"/>
///<reference path="../data/Player.ts"/>
///<reference path="SocketManager.ts"/>
module net {
    export class HandHistoryPacketHandler {
        private handHistoryManager: data.HandHistoryManager;
        constructor() {
            this.handHistoryManager = data.HandHistoryManager.getInstance();
        }
        public handleHandIds(tableId:number, handIds:any):void {
        }
        public handleHandSummaries(tableId: number, handSummaries: any): void {
            var jsonData = JSON.parse(handSummaries);
            this.handHistoryManager.showHandSummaries(tableId, jsonData);
        }
        
        handleHands(tableId: number, hands: any): void {
        }

        handleHand(hand: any): void {
            var jsonData = JSON.parse(hand);
            this.handHistoryManager.showHand(jsonData[0]);
        }
    }

    export class HandHistoryRequestHandler {
        constructor(public tableId: number) {
        }

        requestHandIds(count: number): void {
            console.log("Requesting hands for table " + this.tableId);
            var handIdRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHandIds();
            handIdRequest.tableId = this.tableId;
            handIdRequest.count = count;
            handIdRequest.time = "" + new Date().getTime();
            this.sendPacket(handIdRequest);
        }
        requestHandSummaries(count: number): void {
            console.log("Requesting hands for table " + this.tableId);
            var handIdRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHandSummaries();
            handIdRequest.tableId = this.tableId;
            handIdRequest.count = count;
            handIdRequest.time = "" + new Date().getTime();
            this.sendPacket(handIdRequest);
        }
        requestHands(count: number): void {
            var handsRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHands();
            handsRequest.tableId = this.tableId;
            handsRequest.count = count;
            handsRequest.time = "" + new Date().getTime();
            this.sendPacket(handsRequest);
        }
        requestHand(handId: number): void {
            var handRequest = new com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderRequestHand();
            handRequest.handId = handId;
            this.sendPacket(handRequest);
        
        }
        sendPacket(historyRequest: any): void {
            var packet = new FB_PROTOCOL.ServiceTransportPacket();
            packet.pid = data.Player.getInstance().id;
            packet.seq = 0;
            packet.idtype = 0; // namespace
            packet.service = "ns://www.cubeia.com/poker/handhistory/provider-service";
            packet.servicedata = FIREBASE.ByteArray.toBase64String(historyRequest.save().createGameDataArray(historyRequest.classId()));
            
            SocketManager.getInstance().getConnector().sendProtocolObject(packet);
        }
    }
}