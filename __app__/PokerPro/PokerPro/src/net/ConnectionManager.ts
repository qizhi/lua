module net {
    export class ConnectionManager {
        private static _instance: ConnectionManager;
        public static getInstance(): ConnectionManager {
            if (ConnectionManager._instance == null) {
                ConnectionManager._instance = new ConnectionManager();
            }
            return ConnectionManager._instance;
        }
    }
}