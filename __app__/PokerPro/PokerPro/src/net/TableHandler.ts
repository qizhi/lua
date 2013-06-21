module net {
    export class TablePacketHandler {
        constructor(public tableId: number) {
        }

        handleSeatInfo(seatInfoPacket: any): void {
        }
        handleNotifyLeave(notifyLeavePacket: any) {
        }
        handleSeatedAtTable(packet: any) {
        }
        handleNotifyJoin(notifyJoinPacket: any) {
        }
        handleJoinResponse(joinResponsePacket: any) {
        }
        handleUnwatchResponse(unwatchResponse: any) {
        }
        handleLeaveResponse(leaveResponse: any) {
        }
        handleWatchResponse(watchResponse: any) {
        }
        handleChatMessage(chatPacket: any) {
        }
    }

    export class TableRequestHandler {
    }
}