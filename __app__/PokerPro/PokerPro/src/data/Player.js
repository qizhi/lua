var data;
(function (data) {
    var PlayerTableStatus = (function () {
        function PlayerTableStatus() {
        }
        PlayerTableStatus.SITTING_OUT = 1;
        PlayerTableStatus.SITTING_IN = 2;
        PlayerTableStatus.TOURNAMENT_OUT = 3;
        return PlayerTableStatus;
    })();
    data.PlayerTableStatus = PlayerTableStatus;

    var UserInfo = (function () {
        function UserInfo(id, name) {
            this.id = id;
            this.name = name;
            this.away = false;
            this.sitOutNextHand = false;
            this.balance = "0";
            this.tableStatus = PlayerTableStatus.SITTING_IN;
        }
        return UserInfo;
    })();
    data.UserInfo = UserInfo;

    var Player = (function () {
        function Player() {
            this.name = "";
        }
        Player.prototype.onLogin = function (id, name, credentials) {
            if (!credentials)
                credentials = null;

            Player.getInstance().betAmount = 0;
            Player.getInstance().sessionToken = credentials;

            Player.getInstance().id = id;
            Player.getInstance().name = name;
        };

        Player.prototype.clear = function () {
            Player.getInstance().id = -1;
            Player.getInstance().name = "";
        };

        Player.getInstance = function () {
            if (Player._instance == null) {
                Player._instance = new Player();
            }
            return Player._instance;
        };
        return Player;
    })();
    data.Player = Player;

    var PlayerApi = (function () {
        function PlayerApi(baseUrl) {
            this.baseUrl = baseUrl;
        }
        PlayerApi.prototype.requestPlayerProfile = function (playerId, sessionToken, callback, errorCallback) {
            var self = this;
            var url = this.baseUrl + "/public/player/" + playerId + "/profile?session=" + sessionToken;
        };
        return PlayerApi;
    })();
    data.PlayerApi = PlayerApi;
})(data || (data = {}));
//@ sourceMappingURL=Player.js.map
