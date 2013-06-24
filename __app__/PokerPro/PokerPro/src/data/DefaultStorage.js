var data;
(function (data) {
    var DefaultStorage = (function () {
        function DefaultStorage() {
        }
        DefaultStorage.store = function (name, value) {
            var store = DefaultStorage.getStorage();
            if (store != null) {
                store.removeItem(name);
                store.setItem(name, value);
            }
        };

        DefaultStorage.remove = function (name) {
            var store = DefaultStorage.getStorage();
            if (store != null) {
                store.removeItem(name);
            }
        };

        DefaultStorage.load = function (name, defaultValue) {
            var store = DefaultStorage.getStorage();
            if (store != null) {
                return store.getItem(name);
            } else if (typeof (defaultValue) !== "undefined") {
                return defaultValue;
            } else {
                return null;
            }
        };

        DefaultStorage.loadBoolean = function (name, defaultValue) {
            var val = DefaultStorage.load(name, defaultValue);
            if (val != null) {
                return val == "true";
            } else if (typeof (defaultValue) !== "undefined") {
                return defaultValue;
            }
            return false;
        };

        DefaultStorage.storeUser = function (username, password) {
            DefaultStorage.store("username", username);
            DefaultStorage.store("password", password);
        };
        DefaultStorage.removeStoredUser = function () {
            DefaultStorage.remove("username");
            DefaultStorage.remove("password");
        };

        DefaultStorage.getStorage = function () {
            return null;
        };
        return DefaultStorage;
    })();
    data.DefaultStorage = DefaultStorage;
})(data || (data = {}));
//@ sourceMappingURL=DefaultStorage.js.map
