///<reference path="Player.ts"/>
///<reference path="GameConfig.ts"/>

module data {
    export interface ILobbyDataValidator {
        validate(item: any): boolean;
        shouldRemoveItem(item: any): boolean;
    }
    export class LobbyDataValidator implements ILobbyDataValidator {
        public validate(item: any): boolean {
            return false;
        }
        public shouldRemoveItem(item: any): boolean {
            return item.showInLobby != null && item.showInLobby == 0;
        }
    }
    export class TableLobbyDataValidator extends LobbyDataValidator implements ILobbyDataValidator{
        public validate(item: any): boolean {
            return item.name != null && item.capacity != null;
        }
    }
    export class TournamentLobbyDataValidator extends LobbyDataValidator implements ILobbyDataValidator{
        public validate(item: any): boolean {
            return item.name != null && item.capacity != null && item.status != null && item.buyIn != null;
        }
        public shouldRemoveItem(item: any): boolean {
            return super.shouldRemoveItem(item) || (item.status != null && item.status == "CLOSED");
        }
    }

    export class LobbyData {
        items: data.Map<number, any>;
        notifyUpdate: boolean;
        constructor(public validator: ILobbyDataValidator, public onUpdate: (items: any[]) => void , public onItemRemoved: (itemId:number) => void) {
            this.items = new data.Map<number, any>();
            this.notifyUpdate = false;
        }

        public addOrUpdateItems(items: any[]): void {
            for (var i = 0; i < items.length; i++) {
                this.addOrUpdateItem(items[i]);
            }
            if (this.notifyUpdate == true) {
                this.notifyUpdate == false;
                this.onUpdate(this.getFilteredItems());
            }
        }
        public remove(id: number): void {
            this.items.remove(id);
            this.onItemRemoved(id);
        }
        public clear(): void {
            this.items = new data.Map<number, any>();
            this.notifyUpdate = false;
        }

        public addOrUpdateItem(item: any): void {
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

        }
        public _update(current: any, update: any): void {
            for (var x in current) {
                if (typeof (update[x]) != "undefined" && update[x] != null) {
                    current[x] = update[x];
                }
            }

            return current;
        }

        /**
        * Returns items that passes the Poker.LobbyDataValidator
        * validation step
        * @return {Array}
        */
        public getFilteredItems(): any {
            var items = this.items.values();
            var filtered = [];
            for (var i = 0; i < items.length; i++) {
                if (this.validator.validate(items[i]) == true) {
                    filtered.push(items[i]);
                }
            }
            return filtered;
        }
        
        public getItem(id: number): any {
            return this.items.get(id);
        }
    }
}