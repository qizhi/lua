///<reference path="../../dtd/firebase.d.ts"/>
module net {
    export class TournamentPacketHandler {
        constructor(public tournamentId: number) {
        }

        handleTournamentTransport(packet: any): void {
        }
        handleTournamentTable(tournamentPacket: any): void {
        }
        handleTournamentRegistrationInfo(tournamentId: number, registrationInfo: any): void {
        }
        handleTournamentOut(packet: any): void {
        }
        handleRemovedFromTournamentTable(packet: any): void {
        }
        handleSeatedAtTournamentTable(seated: any): void {
        }
        handleRegistrationResponse(registrationResponse: any): void {
        }
        handleUnregistrationResponse(unregistrationResponse: any): void {
        }
        handleNotifyRegistered(packet: any): void {
        }
        handleTournamentBuyInInfo(tournamentId:number, packet:any):void {
        }
    }
}