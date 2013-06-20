module net {

    export class SocketManager {
        public webSocketUrl: string;
        public webSocketPort: number;

       // public connector: FIREBASE;

        constructor() {
        }

        public initialize(socketURL: string, socketPort: number) {
            this.webSocketUrl = socketURL;
            this.webSocketPort = socketPort;
        }

        public connect() {
        }

        private static _instance: SocketManager;
        public static getInstance(): SocketManager {
            if (SocketManager._instance == null) {
                SocketManager._instance = new SocketManager();
            }
            return SocketManager._instance;
        }
    }

}