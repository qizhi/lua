///<reference path="DefaultStorage.ts"/>
var i18n;
(function (i18n) {
    function t(key, params) {
        console.log("[i18n] ", key, params === undefined ? "" : "," + params);
        return null;
    }
    i18n.t = t;

    var TextManager = (function () {
        function TextManager() {
        }
        TextManager.getInstance = function () {
            if (TextManager._instance == null) {
                TextManager._instance = new TextManager();
            }
            return TextManager._instance;
        };
        return TextManager;
    })();
})(i18n || (i18n = {}));

var data;
(function (data) {
    //------------------------------------------------------
    // Settings
    //------------------------------------------------------
    var Settings = (function () {
        function Settings() {
        }
        Settings.isEnabled = /**
        * Check whether a boolean property is set to true
        * @param param
        * @param [def]
        * @return {Boolean}
        */
        function (param, def) {
            if (def == null) {
                def = false;
            }
            return data.DefaultStorage.loadBoolean(param, def);
        };

        Settings.setProperty = /**
        * stores a property in the local storage
        * @param prop
        * @param value
        */
        function (prop, value) {
            data.DefaultStorage.store(prop, value);
        };
        Settings.SOUND_ENABLED = "sound.enabled";
        Settings.SWIPE_ENABLED = "settings.swipe";
        Settings.FREEZE_COMMUNICATION = "swttings.freeze";
        return Settings;
    })();
    data.Settings = Settings;

    //------------------------------------------------------
    // OperatorConfig
    //------------------------------------------------------
    var OperatorConfig = (function () {
        function OperatorConfig() {
        }
        OperatorConfig.prototype.isPopulated = function () {
            return this.populated;
        };

        OperatorConfig.prototype.populate = function (params) {
            for (var p in params) {
                this.configMap.put(p, params[p]);
            }
            this.populated = true;
        };

        OperatorConfig.prototype.getLogoutUrl = function () {
            return this.getValue("LOGOUT_PAGE_URL", "");
        };
        OperatorConfig.prototype.getClientHelpUrl = function () {
            return this.getValue("CLIENT_HELP_URL", "");
        };
        OperatorConfig.prototype.getProfilePageUrl = function () {
            return this.getValue("PROFILE_PAGE_URL", "http://localhost:8083/player-api/html/profile.html");
        };
        OperatorConfig.prototype.getBuyCreditsUrl = function () {
            return this.getValue("BUY_CREDITS_URL", "http://localhost:8083/player-api/html/buy-credits.html");
        };
        OperatorConfig.prototype.getAccountInfoUrl = function () {
            return this.getValue("ACCOUNT_INFO_URL", "http://localhost:8083/player-api/html/");
        };
        OperatorConfig.prototype.getShareUrl = function () {
            return this.getValue("SHARE_URL", null);
        };
        OperatorConfig.prototype.getValue = function (param, def) {
            var value = this.configMap.get(param);
            if (value == null) {
                console.log("Value for param " + param + " not available, returning default " + def);
                value = def;
            }
            return value;
        };

        OperatorConfig.getInstance = function () {
            if (OperatorConfig._instance == null) {
                OperatorConfig._instance = new OperatorConfig();
            }
            return OperatorConfig._instance;
        };
        return OperatorConfig;
    })();
    data.OperatorConfig = OperatorConfig;

    //------------------------------------------------------
    // Map
    //------------------------------------------------------
    //A simple map built on associative arrays, numeric keys are converted to strings
    var Map = (function () {
        function Map() {
            this.holder = {};
            this.length = 0;
        }
        Map.prototype.size = function () {
            return this.length;
        };

        Map.prototype.contains = function (key) {
            var key = this._key(key);
            return this.get(key) != null;
        };

        //Puts/replaces a value in the map with the specific key
        Map.prototype.put = function (key, val) {
            key = this._key(key);
            var existing = null;
            if (this.holder[key] !== undefined) {
                existing = this.holder[key];
            }
            this.holder[key] = val;

            if (existing == null) {
                this.length++;
            }

            return existing;
        };

        //Get a values by its key, null if no values are associated with the specified key
        Map.prototype.get = function (key) {
            key = this._key(key);
            if (this.holder[key] !== undefined) {
                return this.holder[key];
            } else {
                return null;
            }
        };

        Map.prototype.remove = function (key) {
            key = this._key(key);
            if (key !== undefined) {
                if (this.holder[key] !== undefined) {
                    var val = this.holder[key];
                    delete this.holder[key];
                    this.length--;
                    return val;
                }
            }
            return null;
        };

        Map.prototype.values = function () {
            var values = new Array();
            for (var v in this.holder) {
                values.push(this.holder[v]);
            }
            return values;
        };

        Map.prototype.keys = function () {
            var keys = new Array();
            for (var v in this.holder) {
                keys.push(v);
            }
            return keys;
        };

        //Gets a key value pair { key : k, value : v } array with the key-value pairs
        Map.prototype.keyValuePairs = function () {
            var valuePairs = new Array();
            for (var k in this.holder) {
                valuePairs.push({ key: k, value: this.holder[k] });
            }
            return valuePairs;
        };

        Map.prototype._key = function (key) {
            if (key === undefined) {
                throw "Key must not be undefined";
            } else if (typeof (key) == "number") {
                return "" + key;
            } else {
                return key;
            }
        };
        return Map;
    })();
    data.Map = Map;
})(data || (data = {}));
//@ sourceMappingURL=GameConfig.js.map
