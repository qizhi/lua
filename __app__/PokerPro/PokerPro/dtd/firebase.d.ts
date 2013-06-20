/*
interface ICometdAdapter {
}

declare var FIREBASE;
*/

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

        fromBase64String(input: string): Array<number>;
        toBase64String(input: Array<number>): string;
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
            loginCallback: (status: number, pid: number, screenname: string, sessionToken: string) => void ,
            statusCallback: (status: number, reconnectAttempts: number, desc: string) => void );

        getIOAdapter(): any;
        cancel(): void;
        connect(ioAdapterName: string, hostname: string, port: number, endpoint: string, secure: boolean, extraConfig?: any): void;
        send(packet: any): void;
        login(user: string, pwd: any, operatorid?: any, credentials?: any): void;
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
   
} 