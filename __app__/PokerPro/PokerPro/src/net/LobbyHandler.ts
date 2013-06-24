module net {
    export class LobbyPacketHandler {
        public handleTableSnapshotList(snapshots: any): void {
            //this.lobbyManager.handleTableSnapshotList(snapshots);
        }
        public handleTableUpdateList(updates: any): void {
            //this.lobbyManager.handleTableUpdateList(updates);
        }
        public handleTableRemoved(tableId: number): void {
            //this.lobbyManager.handleTableRemoved(tableId);
        }
        public handleTournamentSnapshotList(snapshots: any): void {
            //this.lobbyManager.handleTournamentSnapshotList(snapshots);
        }
        public handleTournamentUpdates(updates: any): void {
            //this.lobbyManager.handleTournamentUpdates(updates);
        }
        public handleTournamentRemoved(tournamentId: number): void {
            //this.lobbyManager.handleTournamentRemoved(tournamentId);
        }
    }
}