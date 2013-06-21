interface utf8_i {
    toByteArray(str: string): Array < number>;
    fromByteArray(bytes: Array < number>): string;
}
declare var utf8: utf8_i;

declare module FIREBASE {

    export class ByteArray {
        constructor(array?: Array<number>);

        remaining(): number;
        getBuffer(): Array<number>;
        checkBuffer(size: number): void;

        readBoolean(): boolean;

        readInt(): number;
        readShort(): number;
        readByte(): number;
        readUnsignedInt(): number;
        readUnsignedShort(): number;
        readUnsignedByte(): number;

        readString(): string;

        writeBoolean(value: boolean): void;

        writeString(str: string): void;
        writeInt(value: number): void;
        writeShort(value: number): void;
        writeByte(value: number): void;
        writeUnsignedInt(value: number): void;
        writeUnsignedShort(value: number): void;
        writeUnsignedByte(value: number): void;

        createServiceDataArray(classId: string): Array<number>;
        createDataArray(): Array<number>;
        createGameDataArray(classId: string): Array<number>;

        writeArray(byteArray:any): void;
        readArray(count?: number): Array<number>;

        static fromBase64String(input: string): Array<number>;
        static toBase64String(input: Array<number>): string;
    }

    export class CometdAdapter {
        constructor(hostname?: string, port?: number, endpoint?: number, secure?: boolean, cometdAccess?: () => void);
        reconnect(): void;
        //_dataCallback({data: org.cometd.JSON.toJSON(message.data)})
        connect(statusCallback: (status: number) => void , dataCallback: (data: any) => void ): void;
        //_instance.publish("/service/client", org.cometd.JSON.fromJSON(message))
        send(message:any): void;
    }
    
    export interface ConnectionStatus {
        //CONNECTING: 1, CONNECTED: 2, DISCONNECTED: 3, RECONNECTING: 4, RECONNECTED: 5, FAIL: 6, CANCELLED: 7, toString: function (status)
        CONNECTING: number;
        CONNECTED: number;
        DISCONNECTED: number;
        RECONNECTING: number;
        RECONNECTED: number;
        FAIL: number;
        CANCELLED: number;
        toString(status: number): string;
    }

    export class Connector {
        constructor(packetCallback: (protocolObject:any) => void ,
            lobbyCallback: (protocolObject: any) => void ,
            loginCallback: (status: number, pid: string, screenname: string, sessionToken: string) => void ,
            statusCallback: (status: number, reconnectAttempts: number, desc: string) => void );

        getIOAdapter():FIREBASE.WebSocketAdapter;
        cancel(): void;
        connect(ioAdapterName: string, hostname: string, port: number, endpoint: string, secure?: boolean, extraConfig?: any): void;
        send(packet: any): void;
        login(user: string, pwd: string, operatorid?: any, credentials?: any): void;
        logout(leaveTables: any): void;
        lobbySubscribe(gameId:string, address:string): void;
        watchTable(tableId:string): void;
        joinTable(tableId:string, seatId:string): void;
        leaveTable(tableId:string): void;

        sendStyxGameData(pid:string, tableid:string, protocolObject:any): void;
        sendStringGameData(pid:string, tableid:string, string:string): void;
        sendBinaryGameData(pid:string, tableId:string, bytearray:any): void;
        sendServiceTransportPacket(pid:string, gameId:string, classId:string, serviceContract:any, byteArray:any): void;
        sendProtocolObject(protocolObject:any): void;
    }

    //ErrorCodes = {INVALID_IO_ADAPTER: 1, BUFFER_UNDERRUN: 2, RECONNECT_FAILED: 3, IO_ADAPTER_ERROR: 4};
    export interface ErrorCodes {
        INVALID_IO_ADAPTER: number;
        BUFFER_UNDERRUN: number;
        RECONNECT_FAILED: number;
        IO_ADAPTER_ERROR: number;
    }

    export class FirebaseException {
        constructor(errorCode: number, errorMessage: string);
        Throw(errorCode: number, errorMessage: string): void;
    }

     //ReconnectStrategy = { MAX_ATTEMPTS: 0, RECONNECT_START_INTERVAL: 1000, INCREASE_THRESHOLD_COUNT: Infinity, INTERVAL_INCREMENT_STEP: 200 };
    export interface ReconnectStrategy {
        MAX_ATTEMPTS: number;
        RECONNECT_START_INTERVAL: number;
        INCREASE_THRESHOLD_COUNT: number;
        INTERVAL_INCREMENT_STEP: number;
    }

    export class Styx {
        wrapInGameTransportPacket(pid: string, tid: string, protocolObject: any);
        isByteArray(arr: Array<any>): boolean;
        cloneObject(protocolObject: any): any;
        writeParam(param: any, key: string, value: any): any;
        readParam(param: any): any;
        getParam(param: any): any;
        toJSON(protocolObject: any): string;
    }

    export class WebSocketAdapter {
        constructor(hostname: string, port: number, endpoint: string, secure?: boolean, config?: any);
        getSocket(): WebSocket;

        //FIREBASE.ConnectionStatus
        connect(statusCallback: (status: number) => void , dataCallback: (msg: any) => void ): void;
        reconnect(): void;
        send(message: any): void;

        unregisterHandlers(): void;
    }

} 

declare module FB_PROTOCOL {
    export class FB_PROTOCOL_FAKE { }
    export interface TableChatPacket {
        CLASSID: number;
    }
    export interface NotifyJoinPacket {
        CLASSID: number;
    }
    export interface NotifyLeavePacket {
        CLASSID: number;
    }
    export interface SeatInfoPacket {
        CLASSID: number;
    }
    export interface JoinResponsePacket {
        CLASSID: number;
    }
    export interface GameTransportPacket {
        CLASSID: number;
    }
    export interface UnwatchResponsePacket {
        CLASSID: number;
    }
    export interface LeaveResponsePacket {
        CLASSID: number;
    }
    export interface WatchResponsePacket {
        CLASSID: number;
    }
    export interface NotifySeatedPacket {
        CLASSID: number;
    }
    export interface MttSeatedPacket {
        CLASSID: number;
    }
    export interface MttRegisterResponsePacket {
        CLASSID: number;
    }
    export interface MttUnregisterResponsePacket {
        CLASSID: number;
    }
    export interface MttTransportPacket {
        CLASSID: number;
    }
    export interface MttPickedUpPacket {
        CLASSID: number;
    }
    export interface NotifyRegisteredPacket {
        CLASSID: number;
    }
    export interface PingPacket {
        CLASSID: number;
    }
    export interface ForcedLogoutPacket {
        CLASSID: number;
    }
    export interface ServiceTransportPacket {
        CLASSID: number;
    }
    export interface LocalServiceTransportPacket {
        CLASSID: number;
    }
}
//com.cubeia.games.poker.io.protocol.ProtocolObjectFactory
declare var com: any;
/*
declare module com {
    module cubeia {
        module games {
            module poker {
                module io {
                    module protocol {
                        export class protocol_fake { }

                        export interface ProtocolObjectFactory {
                            create(classId: any, gameData: any): any;
                        }
                        
                        export interface GameState { CLASSID: number; }
                        export interface BestHand { CLASSID: number; }
                        export interface BuyInInfoRequest { CLASSID: number; }
                        export interface BuyInInfoResponse { CLASSID: number; }
                        export interface BuyInResponse { CLASSID: number; }
                        export interface CardToDeal { CLASSID: number; }
                        export interface DealerButton { CLASSID: number; }
                        export interface DealPrivateCards { CLASSID: number; }
                        export interface DealPublicCards { CLASSID: number; }
                        export interface DeckInfo { CLASSID: number; }
                        export interface ErrorPacket { CLASSID: number; }
                        export interface ExposePrivateCards { CLASSID: number; }
                        export interface ExternalSessionInfoPacket { CLASSID: number; }
                        export interface FuturePlayerAction { CLASSID: number; }
                        export interface GameCard { CLASSID: number; }
                        export interface HandCanceled { CLASSID: number; }
                        export interface HandEnd { CLASSID: number; }
                        export interface InformFutureAllowedActions { CLASSID: number; }
                        export interface PerformAction { CLASSID: number; }
                        export interface PingPacket { CLASSID: number; }
                        export interface PlayerAction { CLASSID: number; }
                        export interface PlayerBalance { CLASSID: number; }
                        export interface PlayerDisconnectedPacket { CLASSID: number; }
                        export interface PlayerHandStartStatus { CLASSID: number; }
                        export interface PlayerPokerStatus { CLASSID: number; }
                        export interface PlayerReconnectedPacket { CLASSID: number; }
                        export interface PlayerState { CLASSID: number; }
                        export interface PongPacket { CLASSID: number; }
                        export interface PotTransfers { CLASSID: number; }
                        export interface RakeInfo { CLASSID: number; }
                        export interface RequestAction { CLASSID: number; }
                        export interface RebuyOffer { CLASSID: number; }
                        export interface AddOnOffer { CLASSID: number; }
                        export interface AddOnPeriodClosed { CLASSID: number; }
                        export interface PlayerPerformedRebuy { CLASSID: number; }
                        export interface PlayerPerformedAddOn { CLASSID: number; }
                        export interface StartHandHistory { CLASSID: number; }
                        export interface HandStartInfo { CLASSID: number; }
                        export interface StopHandHistory { CLASSID: number; }
                        export interface TakeBackUncalledBet { CLASSID: number; }
                        export interface BlindsAreUpdated { CLASSID: number; }
                        export interface TournamentDestroyed { CLASSID: number; }
                        export interface WaitingToStartBreak { CLASSID: number; }
                    
                    }

                }
            }
        }
    }
}
*/