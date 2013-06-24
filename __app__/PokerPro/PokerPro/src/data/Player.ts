module data {
    export class PlayerTableStatus {
        static SITTING_OUT: number = 1;
        static SITTING_IN: number = 2;
        static TOURNAMENT_OUT: number = 3;

    }

    export interface IPlayer {
        id: number;
        name: string;
    }

    export class UserInfo implements IPlayer {
        balance: number;
        tableStatus: number;
        away: boolean;
        sitOutNextHand: boolean;
        lastActionType: any;

        constructor(public id: number, public name: string) {
            this.away = false;
            this.sitOutNextHand = false;
            this.balance = 0;
            this.tableStatus = PlayerTableStatus.SITTING_IN;
        }

    }

    export class Player implements IPlayer{
        id: number;
        name: string;
        password: string;
        sessionToken: string;
        betAmount: number;
        loginToken: string;

        constructor() {
            this.name = "";
        }

        public onLogin(id: number, name: string, credentials?: string) {
            if (!credentials) credentials = null;

            Player.getInstance().betAmount = 0;
            Player.getInstance().sessionToken = credentials;

            Player.getInstance().id = id;
            Player.getInstance().name = name;
            // $.ga._trackEvent("client_initiation", "login_success");
        }

        public clear() {
            Player.getInstance().id = -1;
            Player.getInstance().name = "";
        }

        private static _instance: Player;
        public static getInstance(): Player {
            if (Player._instance == null) {
                Player._instance = new Player();
            }
            return Player._instance;
        }
    }
}