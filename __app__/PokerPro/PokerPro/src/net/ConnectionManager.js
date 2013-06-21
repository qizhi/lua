var net;
(function (net) {
    var ConnectionManager = (function () {
        function ConnectionManager() {
        }
        ConnectionManager.getInstance = function () {
            if (ConnectionManager._instance == null) {
                ConnectionManager._instance = new ConnectionManager();
            }
            return ConnectionManager._instance;
        };
        return ConnectionManager;
    })();
    net.ConnectionManager = ConnectionManager;
})(net || (net = {}));
//@ sourceMappingURL=ConnectionManager.js.map
