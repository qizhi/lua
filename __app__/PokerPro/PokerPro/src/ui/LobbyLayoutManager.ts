module ui {
    export class LobbyLayoutManager {
        public createTableList(items: any[]): void {
            console.log(JSON.stringify(items));
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