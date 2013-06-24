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
        configMap: Map;
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
    export class Map {
        holder: any;
        length: number;
        constructor() {
            this.holder = {};
            this.length = 0;
        }

        public size(): number {
            return this.length;
        }

        public contains(key: any): boolean {
            var key = this._key(key);
            return this.get(key) != null;
        }

        //Puts/replaces a value in the map with the specific key
        public put(key: any, val: any): void {
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
        public get(key: any): any {
            key = this._key(key);
            if (this.holder[key] !== undefined) {
                return this.holder[key];
            } else {
                return null;
            }
        }

        public remove(key: any): any {
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

        public values(): Array<any> {
            var values: Array<any> = new Array<any>();
            for (var v in this.holder) {
                values.push(this.holder[v]);
            }
            return values;
        }

        public keys(): Array<any> {
            var keys: Array<any> = new Array<any>();
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

        private _key(key: any): any {
            if (key === undefined) {
                throw "Key must not be undefined";
            } else if (typeof (key) == "number") {
                return "" + key;
            } else {
                return key;
            }
        }
    }

}
