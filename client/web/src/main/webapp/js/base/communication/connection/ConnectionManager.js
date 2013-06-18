var Poker = Poker || {};


Poker.ConnectionManager = Class.extend({

    MAX_RECONNECT_ATTEMPTS : 30,

    retryCount : 0,

    /**
     * time out since last packet received to check for disconnect
     */
    disconnectCheckTimeout : null,

    /**
     * version packet grace timeout that triggers the reconnecting
     */
    startReconnectingGraceTimeout : null,

    /**
     * timeout for reconnect attempts
     */
    reconnectRetryTimeout : null,

    connected : false,

    /**
     * @type {Poker.DisconnectDialog}
     */
    disconnectDialog : null,
    init : function() {
        this.disconnectDialog = new Poker.DisconnectDialog();
    },
    onUserLoggedIn : function(playerId, name, credentials) {
        Poker.MyPlayer.onLogin(playerId,name, credentials);
        Poker.AppCtx.getNavigation().onLoginSuccess();
        Poker.AppCtx.getAccountPageManager().onLogin(playerId,name);
        $('#loginView').hide();
        $("#lobbyView").show();
        var viewManager = Poker.AppCtx.getViewManager();
        viewManager.onLogin();
        new Poker.LobbyRequestHandler().subscribeToCashGames();
        Poker.AppCtx.getTableManager().onPlayerLoggedIn();
        Poker.AppCtx.getTournamentManager().onPlayerLoggedIn();

        Poker.Utils.storeUser(name,Poker.MyPlayer.password);
        
        // check deposit return...
        var depositType = purl().fparam("deposit");
        if(depositType) {
            document.location.hash = "";
            Poker.Utils.depositReturn(depositType);
        } 
    },
    onUserConnected : function() {
        this.connected = true;
        this.scheduleDisconnectCheck();
        this.retryCount = 0;
        this.disconnectDialog.close();
        this.showConnectStatus(i18n.t("login.connected"));

        if(Poker.MyPlayer.loginToken!=null) {
            this.handleTokenLogin();
        } else {
            var loggedIn = this.handleLoginOnReconnect();
            if(!loggedIn) {
                this.handlePersistedLogin();
            }
        }


    },
    handleTokenLogin : function() {
        var token = Poker.MyPlayer.loginToken;
        Poker.AppCtx.getCommunicationManager().doLogin(token, token);
    },
    /**
     * Tries to login with credentials stored in local storage
     */
    handlePersistedLogin : function() {
        var username = Poker.Utils.load("username");
        if(username!=null) {
            var password = Poker.Utils.load("password");
            Poker.AppCtx.getCommunicationManager().doLogin(username, password);
        }
    },

    handleLoginOnReconnect : function() {
        if(Poker.MyPlayer.password!=null) {
            Poker.AppCtx.getCommunicationManager().doLogin(Poker.MyPlayer.name, Poker.MyPlayer.password);
            return true;
        } else {
            return false;
        }

    },
    onForcedLogout : function() {
        this.clearTimeouts();
        Poker.AppCtx.getViewManager().onForceLogout();
    },
    onUserDisconnected : function() {
        if(this.connected==true) {
            this.handleDisconnect();
            this.connected = false;
        }
    },
    handleDisconnect : function() {
        console.log("DISCONNECTED");
        this.showConnectStatus(i18n.t("login.disconnected", {sprintf : [this.retryCount]}));
        this.clearTimeouts();
        this.reconnect();
    },
    onUserConnecting : function() {
        this.showConnectStatus(i18n.t("login.connecting"));
    },
    showConnectStatus : function(text) {
        $(".connect-status").html(text);
    },
    onUserReconnecting : function() {
        this.retryCount++;
        this.disconnectDialog.show(this.retryCount);
        this.showConnectStatus(i18n.t("login.disconnected", {sprintf : [this.retryCount]}));
    },
    onUserReconnected : function() {
        this.onUserConnected();
    },
    onPacketReceived : function() {
        this.scheduleDisconnectCheck();
    },
    scheduleDisconnectCheck : function() {
        this.clearTimeouts();
        var self = this;
        this.disconnectCheckTimeout = setTimeout(function(){
            self.sendVersionPacket();
            console.log("Starting reconnect grace timeout");
            self.startReconnectingGraceTimeout = setTimeout(function(){
                console.log("version packet not received, handle disconnect");
                self.handleDisconnect();
            },5000);
        },10000);
    },
    clearTimeouts : function() {
        if(this.disconnectCheckTimeout!=null) {
            clearTimeout(this.disconnectCheckTimeout);
        }
        if(this.startReconnectingGraceTimeout!=null) {
            clearTimeout(this.startReconnectingGraceTimeout);
        }
        if(this.reconnectRetryTimeout!=null) {
            clearTimeout(this.reconnectRetryTimeout);
        }
    },
    reconnect : function() {

        if(this.retryCount < this.MAX_RECONNECT_ATTEMPTS) {
            console.log("Reconnecting");
            this.onUserReconnecting();
            Poker.AppCtx.getCommunicationManager().connect();
            this.scheduleReconnect();
        } else {
            this.disconnectDialog.stoppedReconnecting();
        }
    },
    scheduleReconnect : function() {
        if(this.reconnectRetryTimeout) {
            clearTimeout(this.reconnectRetryTimeout);
        }
        var self = this;
        this.reconnectRetryTimeout = setTimeout(function(){
            self.reconnect();
        },2000);
    },
    sendVersionPacket : function() {
        console.log("Sending version packet");
        var versionPacket = new FB_PROTOCOL.VersionPacket();
        versionPacket.game = 1;
        versionPacket.operatorid = 0;
        versionPacket.protocol = 8559;
        Poker.AppCtx.getCommunicationManager().getConnector().sendProtocolObject(versionPacket);
    }
});

