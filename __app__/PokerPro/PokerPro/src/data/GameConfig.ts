///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="../data/Player.ts"/>
///<reference path="../data/Table.ts"/>
///<reference path="DefaultStorage.ts"/>

module i18n {
    export function t(key: string, params?: any): string {
        console.log("[i18n] ", key, params=== undefined?"":","+params);
        return null;
    }

    class TextManager {
        private static _instance: TextManager;
        public static getInstance(): TextManager {
            if (TextManager._instance == null) {
                TextManager._instance = new TextManager();
            }
            return TextManager._instance;
        }
    }
}

module data {
    //------------------------------------------------------
    // Settings
    //------------------------------------------------------
    export class Settings {
        static SOUND_ENABLED: string = "sound.enabled";
        static SWIPE_ENABLED: string = "settings.swipe";
        static FREEZE_COMMUNICATION: string = "swttings.freeze";

        /**
         * Check whether a boolean property is set to true
         * @param param
         * @param [def]
         * @return {Boolean}
         */
        static isEnabled(param: string, def?: boolean): boolean {
            if (def == null) {
                def = false;
            }
            return DefaultStorage.loadBoolean(param, def);
        }
        /**
         * stores a property in the local storage
         * @param prop
         * @param value
         */
        static setProperty(prop: string, value: any): void {
            DefaultStorage.store(prop, value);
        }
    }
    //------------------------------------------------------
    // OperatorConfig
    //------------------------------------------------------
    export class OperatorConfig {
        operatorId: number;
        configMap: Map<any, any>;
        populated: boolean;

        public isPopulated(): boolean {
            return this.populated;
        }

        public populate(params: any): void {
            for (var p in params) {
                this.configMap.put(p, params[p]);
            }
            this.populated = true;
        }

        public getLogoutUrl(): any {
            return this.getValue("LOGOUT_PAGE_URL", "");
        }
        public getClientHelpUrl(): any {
            return this.getValue("CLIENT_HELP_URL", "");
        }
        public getProfilePageUrl(): any {
            return this.getValue("PROFILE_PAGE_URL", "http://localhost:8083/player-api/html/profile.html");
        }
        public getBuyCreditsUrl(): any {
            return this.getValue("BUY_CREDITS_URL", "http://localhost:8083/player-api/html/buy-credits.html");
        }
        public getAccountInfoUrl(): any {
            return this.getValue("ACCOUNT_INFO_URL", "http://localhost:8083/player-api/html/");
        }
        public getShareUrl(): any {
            return this.getValue("SHARE_URL", null);
        }
        public getValue(param: any, def: any): any {
            var value: any = this.configMap.get(param);
            if (value == null) {
                console.log("Value for param " + param + " not available, returning default " + def);
                value = def;
            }
            return value;
        }

        private static _instance: OperatorConfig;
        public static getInstance(): OperatorConfig {
            if (OperatorConfig._instance == null) {
                OperatorConfig._instance = new OperatorConfig();
            }
            return OperatorConfig._instance;
        }
    }

    //------------------------------------------------------
    // Map
    //------------------------------------------------------
    //A simple map built on associative arrays, numeric keys are converted to strings
    export class Map<T1, T2> {
        holder: any;
        length: number;
        constructor() {
            this.holder = {};
            this.length = 0;
        }

        public size(): number {
            return this.length;
        }

        public contains(key: T1): boolean {
            var key = this._key(key);
            return this.get(key) != null;
        }

        //Puts/replaces a value in the map with the specific key
        public put(key: T1, val: T2): void {
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
        }

        //Get a values by its key, null if no values are associated with the specified key
        public get(key: T1): T2 {
            key = this._key(key);
            if (this.holder[key] !== undefined) {
                return this.holder[key];
            } else {
                return null;
            }
        }

        public remove(key: T1): T2 {
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
        }

        public values(): Array<T2> {
            var values: Array<T2> = new Array<T2>();
            for (var v in this.holder) {
                values.push(this.holder[v]);
            }
            return values;
        }

        public keys(): Array<T1> {
            var keys: Array<T1> = new Array<T1>();
            for (var v in this.holder) {
                keys.push(v);
            }
            return keys;
        }
        //Gets a key value pair { key : k, value : v } array with the key-value pairs
        public keyValuePairs(): Array<any> {
            var valuePairs: Array<any> = new Array<any>();
            for (var k in this.holder) {
                valuePairs.push({ key: k, value: this.holder[k] });
            }
            return valuePairs;
        }

        private _key(key?: T1): T1 {
            /*if (key === undefined) {
                throw "Key must not be undefined";
            } else if (typeof (key) == "number") {
                return "" + key;
            } else {
                return key;
            }*/
            return key;
        }
    }

}

module util {
    export class Utils {
        private static currencySymbol: string = "";

        public static formatCurrency(amount: any): string {
            return Utils._baseFormat(amount);
        }

        private static _baseFormat(amount: any): string {
            var fractionalDigits:number = 10;
            var amount:string = "" + parseFloat(amount).toFixed(10);

            var result:string = "";
            var split:string[] = amount.split(".");

            //remove trailing zeros
            var decimals:string = split[1];
            for (var i:number = decimals.length - 1; i >= 0; i--) {
                if (decimals.charAt(i) != '0') {
                    result = Utils.formatWholePart(split[0]) + "." + decimals.substr(0, i + 1);
                    break;
                }
                if (i == 0) {
                    result = Utils.formatWholePart(split[0]);
                }
            }

            return result;
        }

        private static formatWholePart(amount: any): string {
            return amount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        }

        public static formatCurrencyString(amount: any): string {
            return Utils.currencySymbol + Utils.formatCurrency(amount);
        }

        public static getCardString(gamecard:any): string {
            var ranks:string = "23456789tjqka ";
            var suits:string = "cdhs ";
            return ranks.charAt(gamecard.rank) + suits.charAt(gamecard.suit);
        }

        public static depositReturn(type: string): void {
            /*var dialogManager = Poker.AppCtx.getDialogManager();
            if (type == "success" || type == "cancel") {
                dialogManager.displayGenericDialog({
                    container: Poker.AppCtx.getViewManager().getActiveView().getViewElement(),
                    translationKey: "deposit-" + type,
                    displayCancelButton: false
                });
            }
            */
        }
    }

    export class ActionUtils {
        /**
        * @param {com.cubeia.games.poker.io.protocol.PlayerAction} act
        */
        static getAction(act: any): data.Action {
            var type:number = this.getActionType(act.type);
            return new data.Action(type, parseFloat(act.minAmount), parseFloat(act.maxAmount));
        }
        static getPokerActions(allowedActions: any): data.Action[] {
            var actions: data.Action[] = [];
            for (var a in allowedActions) {
                var ac = ActionUtils.getAction(allowedActions[a]);
                if (ac != null) {
                    actions.push(ac);
                }
            }
            return actions;
        }
        /**
         * @return {com.cubeia.games.poker.io.protocol.PerformAction}
         */
        static getPlayerAction(tableId: number, seq: number, actionType: number, betAmount: number, raiseAmount: number): any {
            var performAction = new com.cubeia.games.poker.io.protocol.PerformAction();
            performAction.player = data.Player.getInstance().id;
            performAction.action = new com.cubeia.games.poker.io.protocol.PlayerAction();
            performAction.action.type = actionType;
            performAction.action.minAmount = "0";
            performAction.action.maxAmount = "0";
            performAction.betAmount = "" + betAmount;
            performAction.raiseAmount = "" + (raiseAmount || 0);
            performAction.timeOut = 0;
            performAction.seq = seq;
            performAction.cardsToDiscard = [];
            return performAction;
        }
        static getActionType(actType: number): string {
            switch (actType) {
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK:
                    return data.ActionType.CHECK;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL:
                    return data.ActionType.CALL;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET:
                    return data.ActionType.BET;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE:
                    return data.ActionType.RAISE;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD:
                    return data.ActionType.FOLD;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE:
                    return data.ActionType.ANTE;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND:
                    return data.ActionType.SMALL_BLIND;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND:
                    return data.ActionType.BIG_BLIND;                   
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET:
                    return data.ActionType.DECLINE_ENTRY_BET;                    
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND:
                    return data.ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND:
                    return data.ActionType.DEAD_SMALL_BLIND;
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.ENTRY_BET:
                    return data.ActionType.ENTRY_BET;
                case com.cubeia.games.poker.io.protocol.ActionTypeEnum.WAIT_FOR_BIG_BLIND:
                    return data.ActionType.WAIT_FOR_BIG_BLIND;
                default:
                    console.log("Unhandled ActionTypeEnum " + actType);
                    break;
            }
            return null;
        }
        static getActionEnumType(actionType: string): any {
            switch (actionType) {
                case data.ActionType.ANTE:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ANTE;
                case data.ActionType.SMALL_BLIND:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.SMALL_BLIND;
                case data.ActionType.BIG_BLIND:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND;
                case data.ActionType.CALL:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL;
                case data.ActionType.CHECK:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK;
                case data.ActionType.BET:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET;
                case data.ActionType.RAISE:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE;
                case data.ActionType.FOLD:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD;
                case data.ActionType.DECLINE_ENTRY_BET:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DECLINE_ENTRY_BET;
                case data.ActionType.DEAD_SMALL_BLIND:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.DEAD_SMALL_BLIND;
                case data.ActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.BIG_BLIND_PLUS_DEAD_SMALL_BLIND;
                case data.ActionType.ENTRY_BET:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.ENTRY_BET;
                case data.ActionType.WAIT_FOR_BIG_BLIND:
                    return com.cubeia.games.poker.io.protocol.ActionTypeEnum.WAIT_FOR_BIG_BLIND;
                default:
                    console.log("Unhandled action " + actionType.text);
                    return null;

            }
        }

    }

    export class ProtocolUtils {
        public static readParam(key: any, params: any): any {
            for (var i = 0; i < params.length; i++) {
                var object = params[i];

                if (object.key == key) {
                    var p = null;
                    var valueArray = FIREBASE.ByteArray.fromBase64String(object.value);
                    var byteArray = new FIREBASE.ByteArray(valueArray);
                    if (object.type == 1) {
                        p = byteArray.readInt();
                    } else {
                        p = byteArray.readString();
                    }
                    return p;
                }
            }
            return null;
        }

        public static extractTournamentData(snapshot: any): any {
            var params: any = snapshot.params;
            var param = function (name) {
                var val = ProtocolUtils.readParam(name, params);
                if (val == null) {
                    val = null;
                }
                return val;
            };

            var data = {
                id: snapshot.mttid,
                name: param("NAME"),
                speed: param("SPEED"),
                capacity: param("CAPACITY"),
                registered: param("REGISTERED"),
                biggestStack: param("BIGGEST_STACK"),
                smallestStack: param("SMALLEST_STACK"),
                averageStack: param("AVERAGE_STACK"),
                playersLeft: param("PLAYERS_LEFT"),
                buyIn: param("BUY_IN"),
                fee: param("FEE"),
                status: param("STATUS"),
                registered: param("REGISTERED"),
                startTime: param("START_TIME"),
                identifier: param("IDENTIFIER"),
                operatorIds: param("OPERATOR_IDS"),
                buyInCurrencyCode: param("BUY_IN_CURRENCY_CODE")
            };

            return data;
        }

        public static extractTableData(snapshot: any): any {
            var params = snapshot.params;
            var param = function (name) {
                var val = ProtocolUtils.readParam(name, params);
                if (typeof (val) == "undefined" || val == null) {
                    val = null;
                }
                return val;
            };
            var val = function (val) {
                return typeof (val) != "undefined" ? val : null;
            }

        var data = {
                id: val(snapshot.tableid),
                name: val(snapshot.name),
                speed: param("SPEED"),
                capacity: val(snapshot.capacity),
                seated: val(snapshot.seated),
                blinds: this.getBlinds(param),
                type: this.getBettingModel(param("BETTING_GAME_BETTING_MODEL")),
                tableStatus: this.getTableStatus(snapshot.seated, snapshot.capacity),
                smallBlind: param("SMALL_BLIND"),
                showInLobby: param("VISIBLE_IN_LOBBY"),
                currencyCode: param("CURRENCY_CODE")
            };

            return data;
        }

        public static getBlinds(param: any): string {
            var sb = param("SMALL_BLIND");
            if (sb != null)
                return (Utils.formatCurrency(sb) + "/" + Utils.formatCurrency(param("BIG_BLIND")));
            return null;
        }
        public static getTableName(data: any): string {
            return data.name + " " + data.blinds + " " + data.type + " " + data.capacity;
        }
        public static getTableStatus(seated?: number, capacity?: number): string {
            if (typeof (seated) == "undefined" || typeof (capacity) == "undefined") {
                return null;
            }
            if (seated == capacity) {
                return "full";
            }
            return "open";
        }
        public static getBettingModel(model: string): string {
            if (model == "NO_LIMIT") {
                return "NL"
            } else if (model == "POT_LIMIT") {
                return "PL";
            } else if (model == "FIXED_LIMIT") {
                return "FL";
            }
            return null;
        }
    }
}