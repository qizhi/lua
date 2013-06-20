var net;
(function (net) {
    var SocketManager = (function () {
        // public connector: FIREBASE;
        function SocketManager() {
        }
        SocketManager.prototype.initialize = function (socketURL, socketPort) {
            this.webSocketUrl = socketURL;
            this.webSocketPort = socketPort;
        };

        SocketManager.prototype.connect = function () {
        };

        SocketManager.getInstance = function () {
            if (SocketManager._instance == null) {
                SocketManager._instance = new SocketManager();
            }
            return SocketManager._instance;
        };
        return SocketManager;
    })();
    net.SocketManager = SocketManager;
})(net || (net = {}));
//@ sourceMappingURL=SocketManager.js.map
