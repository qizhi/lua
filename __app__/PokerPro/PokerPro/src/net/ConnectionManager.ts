///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="TableHandler.ts"/>
///<reference path="TournamentHandler.ts"/>
///<reference path="PokerHandler.ts"/>
///<reference path="HandHistoryHandler.ts"/>
///<reference path="LobbyHandler.ts"/>

///<reference path="SocketManager.ts"/>

///<reference path="../data/DefaultStorage.ts"/>
///<reference path="../data/Player.ts"/>
///<reference path="../data/GameConfig.ts"/>
///<reference path="../data/TableManager.ts"/>
///<reference path="../data/TournamentManager.ts"/>
module net {
    export class ConnectionManager {
        public static MAX_RECONNECT_ATTEMPTS: number = 30;

        retryCount: number;
        //time out since last packet received to check for disconnect
        disconnectCheckTimeout: number;

        //version packet grace timeout that triggers the reconnecting
        startReconnectingGraceTimeout: number;

        //timeout for reconnect attempts
        reconnectRetryTimeout: number;

        connected: boolean;

        constructor() {
            this.retryCount = 0;
        }

        public onUserLoggedIn(playerId: number, name: string, credentials?: string): void {
            data.Player.getInstance().onLogin(playerId, name, credentials);

            new net.LobbyRequestHandler().subscribeToCashGames();

            data.TableManager.getInstance().onPlayerLoggedIn();
            data.TournamentManager.getInstance().onPlayerLoggedIn();
            data.DefaultStorage.storeUser(name, data.Player.getInstance().password);

            
            /*
            Poker.AppCtx.getNavigation().onLoginSuccess();
            Poker.AppCtx.getAccountPageManager().onLogin(playerId, name);
            $('#loginView').hide();
            $("#lobbyView").show();
            var viewManager = Poker.AppCtx.getViewManager();
            viewManager.onLogin();
            new Poker.LobbyRequestHandler().subscribeToCashGames();
            Poker.AppCtx.getTableManager().onPlayerLoggedIn();
            Poker.AppCtx.getTournamentManager().onPlayerLoggedIn();

            Poker.Utils.storeUser(name, Poker.MyPlayer.password);

            // check deposit return...
            var depositType = purl().fparam("deposit");
            if (depositType) {
                document.location.hash = "";
                Poker.Utils.depositReturn(depositType);
            }
            */
        }

        public onUserConnected(): void {
            this.connected = true;
            this.scheduleDisconnectCheck();
            this.retryCount = 0;
            
            this.showConnectStatus(i18n.t("login.connected"));

            if (data.Player.getInstance().loginToken) {
                this.handleTokenLogin();
            } else {
                var loggedIn = this.handleLoginOnReconnect();
                if (!loggedIn) {
                    this.handlePersistedLogin();
                }
            }
        }

        public onForcedLogout(): void {
            this.clearTimeouts();
            //Poker.AppCtx.getViewManager().onForceLogout();
        }

        public onUserDisconnected(): void {
            if (this.connected == true) {
                this.handleDisconnect();
                this.connected = false;
            }
        }

        public onUserConnecting(): void {
            console.log("CONNECTTING");
            this.showConnectStatus(i18n.t("login.connecting"));
        }

        public onUserReconnecting(): void {
            this.retryCount++;
            //this.disconnectDialog.show(this.retryCount);
            this.showConnectStatus(i18n.t("login.disconnected", { sprintf: [this.retryCount] }));
        }

        public onUserReconnected(): void {
            this.onUserConnected();
        }

        public onPacketReceived(): void {
            this.scheduleDisconnectCheck();
        }

        
        public showConnectStatus(text: string): void {
        }
        

        private scheduleDisconnectCheck(): void {
            this.clearTimeouts();
            var self: ConnectionManager = this;

            this.disconnectCheckTimeout = setTimeout(() => {
                self.sendVersionPacket();
                console.log("Starting reconnect grace timeout");
                self.startReconnectingGraceTimeout = setTimeout(() => {
                    console.log("version packet not received, handle disconnect");
                    self.handleDisconnect();
                }, 5000);
            }, 10000);

        }

        private clearTimeouts(): void {
            if (this.disconnectCheckTimeout) {
                clearTimeout(this.disconnectCheckTimeout);
            }
            if (this.startReconnectingGraceTimeout) {
                clearTimeout(this.startReconnectingGraceTimeout);
            }
            if (this.reconnectRetryTimeout) {
                clearTimeout(this.reconnectRetryTimeout);
            }
        }

        private reconnect(): void {
            if (this.retryCount < ConnectionManager.MAX_RECONNECT_ATTEMPTS) {
                console.log("Reconnecting");
                this.onUserReconnecting();
                net.SocketManager.getInstance().connect();
                
                this.scheduleReconnect();
            } else {
                console.log("stoppedReconnecting");
                //this.disconnectDialog.stoppedReconnecting();
            }
        }

        private scheduleReconnect(): void {
            if (this.reconnectRetryTimeout) {
                clearTimeout(this.reconnectRetryTimeout);
            }
            var self:ConnectionManager = this;
            this.reconnectRetryTimeout = setTimeout(() => {
                self.reconnect();
            }, 2000);
        }

        private sendVersionPacket(): void {
            console.log("Sending version packet");
            var versionPacket = new FB_PROTOCOL.VersionPacket();
            versionPacket.game = 1;
            versionPacket.operatorid = 0;
            versionPacket.protocol = 8559;
            net.SocketManager.getInstance().getConnector().sendProtocolObject(versionPacket);
        }

        private handleDisconnect(): void {
            console.log("DISCONNECTED");
            this.showConnectStatus(i18n.t("login.disconnected", { sprintf: [this.retryCount] }));
            this.clearTimeouts();
            this.reconnect();
        }
        private handleTokenLogin(): void {
            var token = data.Player.getInstance().loginToken;
            net.SocketManager.getInstance().doLogin(token, token);
        }

        //Tries to login with credentials stored in local storage
        private handlePersistedLogin(): void {
            var username:string = "Hunter";//data.DefaultStorage.load("username");
            if (username != null) {
                var password:string = "1234545";//data.DefaultStorage.load("password");
                net.SocketManager.getInstance().doLogin(username, password);
            }
        }

        private handleLoginOnReconnect(): boolean {
            if (data.Player.getInstance().password) {
                net.SocketManager.getInstance().doLogin(data.Player.getInstance().name, data.Player.getInstance().password);
                return true;
            } else {
                return false;
            }
        }

        private static _instance: ConnectionManager;
        public static getInstance(): ConnectionManager {
            if (ConnectionManager._instance == null) {
                ConnectionManager._instance = new ConnectionManager();
            }
            return ConnectionManager._instance;
        }
    }

    export class ConnectionPacketHandler {
        private connectionManager: ConnectionManager;
        constructor() {
            this.connectionManager = ConnectionManager.getInstance();
        }

        public handleLogin(status: number, playerId: number, name: string, credentials?: string): void {
            if (status == FB_PROTOCOL.ResponseStatusEnum.OK) {
                this.connectionManager.onUserLoggedIn(playerId, name, credentials);
            } else {
                this.connectionManager.showConnectStatus(i18n.t("login.login-failed", { sprintf: [status] }));
            }
        }

        public handleForceLogout(code:number, message:string): void {
            console.log(message);
            //logged in somewhere else
            this.connectionManager.onForcedLogout();
        }

        public handleStatus(status: number): void {
            //CONNECTING:1,CONNECTED:2,DISCONNECTED:3,RECONNECTING:4,RECONNECTED:5,FAIL:6,CANCELLED:7
            if (status === FIREBASE.ConnectionStatus.CONNECTED) {
                this.connectionManager.onUserConnected();
                this.initOperatorConfig();
            } else if (status === FIREBASE.ConnectionStatus.DISCONNECTED) {
                this.connectionManager.onUserDisconnected();
            } else if (status === FIREBASE.ConnectionStatus.CONNECTING) {
                this.connectionManager.onUserConnecting();
            } else if (status == FIREBASE.ConnectionStatus.RECONNECTING) {
                this.connectionManager.onUserReconnecting();
            } else if (status == FIREBASE.ConnectionStatus.RECONNECTED) {
                this.connectionManager.onUserReconnected();
            } else {
                console.log("Unhandled status " + status);
            }
        }

        private initOperatorConfig(): void {
            if (!data.OperatorConfig.getInstance().isPopulated()) {
                var packet = new FB_PROTOCOL.LocalServiceTransportPacket();
                packet.seq = 0;
                packet.servicedata = utf8.toByteArray("" + data.OperatorConfig.getInstance().operatorId);
                net.SocketManager.getInstance().getConnector().sendProtocolObject(packet);
            }
        }
    }
}