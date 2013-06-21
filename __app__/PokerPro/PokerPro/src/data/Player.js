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
            this.away = false;
            this.sitOutNextHand = false;
            this.balance = 0;
            this.tableStatus = PlayerTableStatus.SITTING_IN;
        }
        return UserInfo;
    })();
    data.UserInfo = UserInfo;

    var Player = (function () {
        function Player(id, name, credentials) {
            if (!credentials)
                credentials = null;

            Player.getInstance().betAmount = 0;
            Player.getInstance().sessionToken = credentials;

            Player.getInstance().id = id;
            Player.getInstance().name = name;
        }
        Player.prototype.clear = function () {
            Player.getInstance().id = -1;
            Player.getInstance().name = "";
        };

        Player.getInstance = function () {
            if (Player._instance == null) {
                Player._instance = new Player(-1, "");
            }
            return Player._instance;
        };
        return Player;
    })();
    data.Player = Player;
})(data || (data = {}));
//@ sourceMappingURL=Player.js.map
