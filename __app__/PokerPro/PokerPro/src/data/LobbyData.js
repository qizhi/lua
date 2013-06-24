var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>
var data;
(function (data) {
    var LobbyDataValidator = (function () {
        function LobbyDataValidator() {
        }
        LobbyDataValidator.prototype.validate = function (item) {
            return false;
        };
        LobbyDataValidator.prototype.shouldRemoveItem = function (item) {
            return item.showInLobby != null && item.showInLobby == 0;
        };
        return LobbyDataValidator;
    })();
    data.LobbyDataValidator = LobbyDataValidator;
    var TableLobbyDataValidator = (function (_super) {
        __extends(TableLobbyDataValidator, _super);
        function TableLobbyDataValidator() {
            _super.apply(this, arguments);
        }
        TableLobbyDataValidator.prototype.validate = function (item) {
            return item.name != null && item.capacity != null;
        };
        return TableLobbyDataValidator;
    })(LobbyDataValidator);
    data.TableLobbyDataValidator = TableLobbyDataValidator;
    var TournamentLobbyDataValidator = (function (_super) {
        __extends(TournamentLobbyDataValidator, _super);
        function TournamentLobbyDataValidator() {
            _super.apply(this, arguments);
        }
        TournamentLobbyDataValidator.prototype.validate = function (item) {
            return item.name != null && item.capacity != null && item.status != null && item.buyIn != null;
        };
        TournamentLobbyDataValidator.prototype.shouldRemoveItem = function (item) {
            return _super.prototype.shouldRemoveItem.call(this, item) || (item.status != null && item.status == "CLOSED");
        };
        return TournamentLobbyDataValidator;
    })(LobbyDataValidator);
    data.TournamentLobbyDataValidator = TournamentLobbyDataValidator;

    var LobbyData = (function () {
        function LobbyData(validator, onUpdate, onItemRemoved) {
            this.validator = validator;
            this.onUpdate = onUpdate;
            this.onItemRemoved = onItemRemoved;
            this.items = new data.Map();
            this.notifyUpdate = false;
        }
        LobbyData.prototype.addOrUpdateItems = function (items) {
            for (var i = 0; i < items.length; i++) {
                this.addOrUpdateItem(items[i]);
            }
            if (this.notifyUpdate == true) {
                this.notifyUpdate == false;
                this.onUpdate(this.getFilteredItems());
            }
        };
        LobbyData.prototype.remove = function (id) {
            this.items.remove(id);
            this.onItemRemoved(id);
        };
        LobbyData.prototype.clear = function () {
            this.items = new data.Map();
            this.notifyUpdate = false;
        };

        LobbyData.prototype.addOrUpdateItem = function (item) {
            if (typeof (item.id) == "undefined") {
                console.log("No id in item, don't know what to do");
                return;
            }
            if (this.validator.shouldRemoveItem(item)) {
                this.remove(item.id);
            } else {
                var current = this.items.get(item.id);
                if (current != null) {
                    current = this._update(current, item);
                    this.items.put(item.id, current);
                } else {
                    current = item;
                    this.items.put(item.id, current);
                }
                if (this.validator.validate(current)) {
                    this.notifyUpdate = true;
                }
            }
        };
        LobbyData.prototype._update = function (current, update) {
            for (var x in current) {
                if (typeof (update[x]) != "undefined" && update[x] != null) {
                    current[x] = update[x];
                }
            }

            return current;
        };

        /**
        * Returns items that passes the Poker.LobbyDataValidator
        * validation step
        * @return {Array}
        */
        LobbyData.prototype.getFilteredItems = function () {
            var items = this.items.values();
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                if (this.validator.validate(items[i]) == true) {
                    filtered.push(items[i]);
                }
            }
            return filtered;
        };

        LobbyData.prototype.getItem = function (id) {
            return this.items.get(id);
        };
        return LobbyData;
    })();
    data.LobbyData = LobbyData;
})(data || (data = {}));
//@ sourceMappingURL=LobbyData.js.map
