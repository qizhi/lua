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
var net;
(function (net) {
    var ConnectionManager = (function () {
        function ConnectionManager() {
            this.retryCount = 0;
        }
        ConnectionManager.prototype.onUserLoggedIn = function (playerId, name, credentials) {
            data.Player.getInstance().onLogin(playerId, name, credentials);

            new net.LobbyRequestHandler().subscribeToCashGames();

            data.TableManager.getInstance().onPlayerLoggedIn();
            data.TournamentManager.getInstance().onPlayerLoggedIn();
            data.DefaultStorage.storeUser(name, data.Player.getInstance().password);
        };

        ConnectionManager.prototype.onUserConnected = function () {
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
        };

        ConnectionManager.prototype.onForcedLogout = function () {
            this.clearTimeouts();
        };

        ConnectionManager.prototype.onUserDisconnected = function () {
            if (this.connected == true) {
                this.handleDisconnect();
                this.connected = false;
            }
        };

        ConnectionManager.prototype.onUserConnecting = function () {
            console.log("CONNECTTING");
            this.showConnectStatus(i18n.t("login.connecting"));
        };

        ConnectionManager.prototype.onUserReconnecting = function () {
            this.retryCount++;

            //this.disconnectDialog.show(this.retryCount);
            this.showConnectStatus(i18n.t("login.disconnected", { sprintf: [this.retryCount] }));
        };

        ConnectionManager.prototype.onUserReconnected = function () {
            this.onUserConnected();
        };

        ConnectionManager.prototype.onPacketReceived = function () {
            this.scheduleDisconnectCheck();
        };

        ConnectionManager.prototype.showConnectStatus = function (text) {
        };

        ConnectionManager.prototype.scheduleDisconnectCheck = function () {
            this.clearTimeouts();
            var self = this;

            this.disconnectCheckTimeout = setTimeout(function () {
                self.sendVersionPacket();
                console.log("Starting reconnect grace timeout");
                self.startReconnectingGraceTimeout = setTimeout(function () {
                    console.log("version packet not received, handle disconnect");
                    self.handleDisconnect();
                }, 5000);
            }, 10000);
        };

        ConnectionManager.prototype.clearTimeouts = function () {
            if (this.disconnectCheckTimeout) {
                clearTimeout(this.disconnectCheckTimeout);
            }
            if (this.startReconnectingGraceTimeout) {
                clearTimeout(this.startReconnectingGraceTimeout);
            }
            if (this.reconnectRetryTimeout) {
                clearTimeout(this.reconnectRetryTimeout);
            }
        };

        ConnectionManager.prototype.reconnect = function () {
            if (this.retryCount < ConnectionManager.MAX_RECONNECT_ATTEMPTS) {
                console.log("Reconnecting");
                this.onUserReconnecting();
                net.SocketManager.getInstance().connect();

                this.scheduleReconnect();
            } else {
                console.log("stoppedReconnecting");
            }
        };

        ConnectionManager.prototype.scheduleReconnect = function () {
            if (this.reconnectRetryTimeout) {
                clearTimeout(this.reconnectRetryTimeout);
            }
            var self = this;
            this.reconnectRetryTimeout = setTimeout(function () {
                self.reconnect();
            }, 2000);
        };

        ConnectionManager.prototype.sendVersionPacket = function () {
            console.log("Sending version packet");
            var versionPacket = new FB_PROTOCOL.VersionPacket();
            versionPacket.game = 1;
            versionPacket.operatorid = 0;
            versionPacket.protocol = 8559;
            net.SocketManager.getInstance().getConnector().sendProtocolObject(versionPacket);
        };

        ConnectionManager.prototype.handleDisconnect = function () {
            console.log("DISCONNECTED");
            this.showConnectStatus(i18n.t("login.disconnected", { sprintf: [this.retryCount] }));
            this.clearTimeouts();
            this.reconnect();
        };
        ConnectionManager.prototype.handleTokenLogin = function () {
            var token = data.Player.getInstance().loginToken;
            net.SocketManager.getInstance().doLogin(token, token);
        };

        //Tries to login with credentials stored in local storage
        ConnectionManager.prototype.handlePersistedLogin = function () {
            var username = "Hunter";
            if (username != null) {
                var password = "1234545";
                net.SocketManager.getInstance().doLogin(username, password);
            }
        };

        ConnectionManager.prototype.handleLoginOnReconnect = function () {
            if (data.Player.getInstance().password) {
                net.SocketManager.getInstance().doLogin(data.Player.getInstance().name, data.Player.getInstance().password);
                return true;
            } else {
                return false;
            }
        };

        ConnectionManager.getInstance = function () {
            if (ConnectionManager._instance == null) {
                ConnectionManager._instance = new ConnectionManager();
            }
            return ConnectionManager._instance;
        };
        ConnectionManager.MAX_RECONNECT_ATTEMPTS = 30;
        return ConnectionManager;
    })();
    net.ConnectionManager = ConnectionManager;

    var ConnectionPacketHandler = (function () {
        function ConnectionPacketHandler() {
            this.connectionManager = ConnectionManager.getInstance();
        }
        ConnectionPacketHandler.prototype.handleLogin = function (status, playerId, name, credentials) {
            if (status == FB_PROTOCOL.ResponseStatusEnum.OK) {
                this.connectionManager.onUserLoggedIn(playerId, name, credentials);
            } else {
                this.connectionManager.showConnectStatus(i18n.t("login.login-failed", { sprintf: [status] }));
            }
        };

        ConnectionPacketHandler.prototype.handleForceLogout = function (code, message) {
            console.log(message);

            //logged in somewhere else
            this.connectionManager.onForcedLogout();
        };

        ConnectionPacketHandler.prototype.handleStatus = function (status) {
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
        };

        ConnectionPacketHandler.prototype.initOperatorConfig = function () {
            if (!data.OperatorConfig.getInstance().isPopulated()) {
                var packet = new FB_PROTOCOL.LocalServiceTransportPacket();
                packet.seq = 0;
                packet.servicedata = utf8.toByteArray("" + data.OperatorConfig.getInstance().operatorId);
                net.SocketManager.getInstance().getConnector().sendProtocolObject(packet);
            }
        };
        return ConnectionPacketHandler;
    })();
    net.ConnectionPacketHandler = ConnectionPacketHandler;
})(net || (net = {}));
//@ sourceMappingURL=ConnectionManager.js.map
