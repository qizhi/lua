///<reference path="../../dtd/firebase.d.ts"/>

///<reference path="SocketManager.ts"/>
///<reference path="../data/LobbyManager.ts"/>

module net {
    export class LobbyPacketHandler {
        private lobbyManager: data.LobbyManager;
        constructor() {
            this.lobbyManager = data.LobbyManager.getInstance();
        }
        public handleTableSnapshotList(snapshots: any): void {
            this.lobbyManager.handleTableSnapshotList(snapshots);
        }
        public handleTableUpdateList(updates: any): void {
            this.lobbyManager.handleTableUpdateList(updates);
        }
        public handleTableRemoved(tableId: number): void {
            this.lobbyManager.handleTableRemoved(tableId);
        }
        public handleTournamentSnapshotList(snapshots: any): void {
            this.lobbyManager.handleTournamentSnapshotList(snapshots);
        }
        public handleTournamentUpdates(updates: any): void {
            this.lobbyManager.handleTournamentUpdates(updates);
        }
        public handleTournamentRemoved(tournamentId: number): void {
            this.lobbyManager.handleTournamentRemoved(tournamentId);
        }
    }

    export class LobbyRequestHandler {
        private connector: FIREBASE.Connector;
        constructor() {
            this.connector = net.SocketManager.getInstance().getConnector();
        }

        public subscribeToCashGames(): void {
            this.unsubscribe();

            this.connector.lobbySubscribe(1, "/texas");

            data.LobbyManager.getInstance().clearLobby();
            Unsubscribe.unsubscribe = () => {
                console.log("Unsubscribing from cash games.");
                var unsubscribeRequest = new FB_PROTOCOL.LobbyUnsubscribePacket();
                unsubscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.REGULAR;
                unsubscribeRequest.gameid = 1;
                unsubscribeRequest.address = "/texas";
                net.SocketManager.getInstance().getConnector().sendProtocolObject(unsubscribeRequest);
            }
        }

        public subscribeToSitAndGos():void {
            data.LobbyManager.getInstance().clearLobby();
            this.subscribeToTournamentsWithPath("/sitandgo")
        }

        public subscribeToTournaments ():void {
            data.LobbyManager.getInstance().clearLobby();
            this.subscribeToTournamentsWithPath("/scheduled");
        }

        private subscribeToTournamentsWithPath(path: string): void {
            this.unsubscribe();

            var subscribeRequest = new FB_PROTOCOL.LobbySubscribePacket();
            subscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.MTT;
            subscribeRequest.gameid = 1;
            subscribeRequest.address = path;
            this.connector.sendProtocolObject(subscribeRequest);

            console.log("Subscribing to tournaments with path " + path, subscribeRequest);

            Unsubscribe.unsubscribe = ()=>{
                console.log("Unsubscribing from tournaments, path  = " + path);
                var unsubscribeRequest = new FB_PROTOCOL.LobbyUnsubscribePacket();
                unsubscribeRequest.type = FB_PROTOCOL.LobbyTypeEnum.MTT;
                unsubscribeRequest.gameid = 1;
                unsubscribeRequest.address = path;
                net.SocketManager.getInstance().getConnector().sendProtocolObject(unsubscribeRequest);
            }
        }
        
        private unsubscribe(): void {
            if (Unsubscribe.unsubscribe != null) {
                data.LobbyManager.getInstance().clearLobby();
                Unsubscribe.unsubscribe();
            } else {
                console.log("No unsubscribe function defined.");
            }
        }

    }

    class Unsubscribe {
        static unsubscribe:()=>void = null;
    }
}