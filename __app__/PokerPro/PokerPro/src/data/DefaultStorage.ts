module data {
    interface ILocalStorage {
        setItem(name: string, value: any): void;
        removeItem(name: string): void;
        getItem(name: string): any;
    }

    export class DefaultStorage {
        public static store(name: string, value: any): void {
            var store: ILocalStorage = DefaultStorage.getStorage();
            if (store != null) {
                store.removeItem(name);
                store.setItem(name, value);
            }
        }

        public static remove(name: string): void {
            var store: ILocalStorage = DefaultStorage.getStorage();
            if (store != null) {
                store.removeItem(name);
            }
        }

        public static load(name:string, defaultValue?:any): any {
            var store: ILocalStorage = DefaultStorage.getStorage();
            if (store != null) {
                return store.getItem(name);
            } else if (typeof (defaultValue) !== "undefined") {
                return defaultValue;
            } else {
                return null;
            }
        }

        public static loadBoolean(name: string, defaultValue?:boolean): boolean {
            var val:any = DefaultStorage.load(name, defaultValue);
            if (val != null) {
                return val == "true";
            } else if (typeof (defaultValue) !== "undefined") {
                return defaultValue;
            }
            return false;
        }

        public static storeUser(username: string, password: string): void {
            DefaultStorage.store("username", username);
            DefaultStorage.store("password", password);
        }
        public static removeStoredUser() {
            DefaultStorage.remove("username");
            DefaultStorage.remove("password");
        }

        private static getStorage(): ILocalStorage {
            return null;
        }
    }
}