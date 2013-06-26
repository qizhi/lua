module ui {
    export class LobbyLayoutManager {
        public createTableList(item: any): void {
        }
        public tableRemoved(itemId: number): void {
        }

        public createTournamentList(item: any): void {
        }
        public tournamentRemoved(itemId: number): void {
        }
        
        private static _instance: LobbyLayoutManager;
        public static getInstance(): LobbyLayoutManager {
            if (LobbyLayoutManager._instance == null) {
                LobbyLayoutManager._instance = new LobbyLayoutManager();
            }
            return LobbyLayoutManager._instance;
        }
    }


}