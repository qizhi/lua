///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="TableHandler.ts"/>
///<reference path="TournamentHandler.ts"/>
///<reference path="PokerHandler.ts"/>
///<reference path="HandHistoryHandler.ts"/>
///<reference path="LobbyHandler.ts"/>

///<reference path="ConnectionManager.ts"/>


///<reference path="../data/Player.ts"/>
///<reference path="../data/GameConfig.ts"/>

module net {

    export class SocketManager {
        public webSocketUrl: string;
        public webSocketPort: number;

        public retryCount: number;
        public connector: FIREBASE.Connector;

        private tableManager: data.TableManager;

        constructor() {
            this.retryCount = 0;
            this.tableManager = data.TableManager.getInstance();
        }

        public initialize(socketURL: string, socketPort: number):void {
            this.webSocketUrl = socketURL;
            this.webSocketPort = socketPort;

            this.connect();
        }

        public getConnector(): FIREBASE.Connector {
            return this.connector;
        }

        public forceLogout  (packet:any):void {
            console.log("Force log out", packet.code, packet.message);
            new net.ConnectionPacketHandler().handleForceLogout(packet.code, packet.message);
            this.unregisterHandlers();
        }

        private unregisterHandlers(): void {
            if (this.connector) {
                console.log("unregisterHandlers");
                this.connector.getIOAdapter().unregisterHandlers();
            }
        }

        public doLogin(username:string, password:string):void {
            data.Player.getInstance().password = password;
            var operatorId: number = 0;
            this.connector.login(username, password, operatorId);
        }

        public connect() {
            console.log("Connecting");
            this.unregisterHandlers();
            FIREBASE.ReconnectStrategy.MAX_ATTEMPTS = 0;

            this.connector = new FIREBASE.Connector(
                (po) => { this.handlePacket(po); },
                (po) => { this.lobbyCallback(po); },
                (status, playerId, name, token) => { this.loginCallback(status, playerId, name, token); },
                (status, attempts, desc) => { this.statusCallback(status); }
                );
  
            console.log("Connector connect: ", this.webSocketUrl, this.webSocketPort);
            this.connector.connect("FIREBASE.WebSocketAdapter", this.webSocketUrl, this.webSocketPort, "socket", false, null);
        }

        private loginCallback(status: number, playerId: number, name: string, credentials?: string): void {
            console.log("Login Callback credentials: ", credentials);
            new net.ConnectionPacketHandler().handleLogin(status, playerId, name, credentials);
        }

        private statusCallback(status: number): void {
            new net.ConnectionPacketHandler().handleStatus(status);
        }


        private handlePacket(packet: any): void {
            net.ConnectionManager.getInstance().onPacketReceived();
            var tournamentId:number = -1;
            if (packet.mttid) {
                tournamentId = packet.mttid;
            }
            
            var tableId:number = -1;
            if (packet.tableid) {
                tableId = packet.tableid;
            }

            var classId: number = packet.classId;
            var tournamentPacketHandler: net.TournamentPacketHandler = new net.TournamentPacketHandler();
            var tablePacketHandler:net.TablePacketHandler = new net.TablePacketHandler(tableId);
            
            switch (classId) {
                case FB_PROTOCOL.TableChatPacket.CLASSID:
                    tablePacketHandler.handleChatMessage(packet);
                    break;
                case FB_PROTOCOL.NotifyJoinPacket.CLASSID:
                    tablePacketHandler.handleNotifyJoin(packet);
                    break;
                case FB_PROTOCOL.NotifyLeavePacket.CLASSID:
                    tablePacketHandler.handleNotifyLeave(packet);
                    break;
                case FB_PROTOCOL.SeatInfoPacket.CLASSID:
                    tablePacketHandler.handleSeatInfo(packet);
                    break;
                case FB_PROTOCOL.JoinResponsePacket.CLASSID:
                    tablePacketHandler.handleJoinResponse(packet);
                    break;
                case FB_PROTOCOL.GameTransportPacket.CLASSID:
                    this.handleGameDataPacket(packet);
                    break;
                case FB_PROTOCOL.UnwatchResponsePacket.CLASSID:
                    tablePacketHandler.handleUnwatchResponse(packet);
                    break;
                case FB_PROTOCOL.LeaveResponsePacket.CLASSID:
                    tablePacketHandler.handleLeaveResponse(packet);
                    break;
                case FB_PROTOCOL.WatchResponsePacket.CLASSID:
                    tablePacketHandler.handleWatchResponse(packet);
                    break;
                case FB_PROTOCOL.NotifySeatedPacket.CLASSID:
                    if (packet.mttid == -1) {
                        tablePacketHandler.handleSeatedAtTable(packet);
                    } else {
                        tournamentPacketHandler.handleSeatedAtTournamentTable(packet);
                    }
                    break;
                case FB_PROTOCOL.MttSeatedPacket.CLASSID:
                    tournamentPacketHandler.handleSeatedAtTournamentTable(packet);
                    break;
                case FB_PROTOCOL.MttRegisterResponsePacket.CLASSID:
                    tournamentPacketHandler.handleRegistrationResponse(packet);
                    break;
                case FB_PROTOCOL.MttUnregisterResponsePacket.CLASSID:
                    tournamentPacketHandler.handleUnregistrationResponse(packet);
                    break;
                case FB_PROTOCOL.MttTransportPacket.CLASSID:
                    tournamentPacketHandler.handleTournamentTransport(packet);
                    break;
                case FB_PROTOCOL.MttPickedUpPacket.CLASSID:
                    tournamentPacketHandler.handleRemovedFromTournamentTable(packet);
                    break;
                case FB_PROTOCOL.LocalServiceTransportPacket.CLASSID:
                    this.handleLocalServiceTransport(packet);
                    break;
                case FB_PROTOCOL.NotifyRegisteredPacket.CLASSID:
                    tournamentPacketHandler.handleNotifyRegistered(packet);
                    break;
                case FB_PROTOCOL.PingPacket.CLASSID:
                    console.log("PING PACKET RECEIVED");
                    break;
                case FB_PROTOCOL.ForcedLogoutPacket.CLASSID:
                    this.forceLogout(packet);
                    break;
                case FB_PROTOCOL.ServiceTransportPacket.CLASSID:
                    this.handleServicePacket(packet);
                    break;
                default:
                    console.log("[dev] handler not found", packet);
                    break;
            }
        }

        private handleLocalServiceTransport(packet: any): void {
            var byteArray:Array<number> = FIREBASE.ByteArray.fromBase64String(packet.servicedata);
            var message:string = utf8.fromByteArray(byteArray);
            var config: any = JSON.parse(message);
            data.OperatorConfig.getInstance().populate(config);
            console.log(config);
        }

        private handleServicePacket(servicePacket: any): void {
            var valueArray:Array<number> = FIREBASE.ByteArray.fromBase64String(servicePacket.servicedata);
            var serviceData: FIREBASE.ByteArray = new FIREBASE.ByteArray(valueArray);
            var length:number = serviceData.readInt();
            var classId: number = serviceData.readUnsignedByte();
            var protocolObject = com.cubeia.games.poker.routing.service.io.protocol.ProtocolObjectFactory.create(classId, serviceData);
            var handler: net.HandHistoryPacketHandler = new net.HandHistoryPacketHandler();
            switch (protocolObject.classId()) {
                case com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHandIds.CLASSID:
                    handler.handleHandIds(protocolObject.tableId, protocolObject.handIds);
                    break;
                case com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHandSummaries.CLASSID:
                    handler.handleHandSummaries(protocolObject.tableId, protocolObject.handSummaries);
                    break;
                case com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHands.CLASSID:
                    handler.handleHands(protocolObject.tableId, protocolObject.hands);
                    break;
                case com.cubeia.games.poker.routing.service.io.protocol.HandHistoryProviderResponseHand.CLASSID:
                    handler.handleHand(protocolObject.hand);
                    break;
            }
        }

        private handleGameDataPacket(gameTransportPacket: any): void {
            if (data.Settings.isEnabled(data.Settings.FREEZE_COMMUNICATION, false))
            {
                console.log("freeze communication in handleGameDataPacket")
                return;
            }

            if (!this.tableManager.tableExist(gameTransportPacket.tableid)) {
                console.log("Received packet for table (" + gameTransportPacket.tableid + ") you're not viewing");
                return;
            }
            var tableId = gameTransportPacket.tableid;
            var playerId = gameTransportPacket.pid;
            var valueArray = FIREBASE.ByteArray.fromBase64String(gameTransportPacket.gamedata);
            var gameData = new FIREBASE.ByteArray(valueArray);
            var length = gameData.readInt();
            var classId = gameData.readUnsignedByte();

            var protocolObject = com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create(classId, gameData);
            var pokerPacketHandler: net.PokerPacketHandler = new net.PokerPacketHandler(tableId);
            switch (protocolObject.classId()) {
                case com.cubeia.games.poker.io.protocol.GameState.CLASSID:
                    this.tableManager.notifyGameStateUpdate(tableId, protocolObject.currentLevel, protocolObject.secondsToNextLevel, protocolObject.betStrategy, protocolObject.currency);
                    break;
                case com.cubeia.games.poker.io.protocol.BestHand.CLASSID:
                    this.tableManager.updateHandStrength(tableId, protocolObject, false);
                    break;
                case com.cubeia.games.poker.io.protocol.BuyInInfoRequest.CLASSID:
                    console.log("UNHANDLED PO BuyInInfoRequest");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.BuyInInfoResponse.CLASSID:
                    pokerPacketHandler.handleBuyIn(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.BuyInResponse.CLASSID:
                    console.log("BUY-IN RESPONSE ");
                    console.log(protocolObject);
                    this.tableManager.handleBuyInResponse(tableId, protocolObject.resultCode);
                    break;
                case com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID:
                    console.log("UNHANDLED PO CardToDeal");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.DealerButton.CLASSID:
                    this.tableManager.setDealerButton(tableId, protocolObject.seat);
                    break;
                case com.cubeia.games.poker.io.protocol.DealPrivateCards.CLASSID:
                    pokerPacketHandler.handleDealPrivateCards(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.DealPublicCards.CLASSID:
                    pokerPacketHandler.handleDealPublicCards(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.DeckInfo.CLASSID:
                    console.log("UNHANDLED PO DeckInfo");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.ErrorPacket.CLASSID:
                    console.log("UNHANDLED PO ErrorPacket");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.ExposePrivateCards.CLASSID:
                    pokerPacketHandler.handleExposePrivateCards(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.ExternalSessionInfoPacket.CLASSID:
                    console.log("UNHANDLED PO ExternalSessionInfoPacket");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.FuturePlayerAction.CLASSID:
                    console.log("UNHANDLED PO FuturePlayerAction");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.GameCard.CLASSID:
                    console.log("UNHANDLED PO GameCard");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.HandCanceled.CLASSID:
                    console.log("UNHANDLED PO HandCanceled");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.HandEnd.CLASSID:
                    this.tableManager.endHand(tableId, protocolObject.hands, protocolObject.potTransfers);
                    break;
                case com.cubeia.games.poker.io.protocol.InformFutureAllowedActions.CLASSID:
                    pokerPacketHandler.handleFuturePlayerAction(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PerformAction.CLASSID:
                    pokerPacketHandler.handlePerformAction(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PingPacket.CLASSID:
                    console.log("UNHANDLED PO PingPacket");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerAction.CLASSID:
                    console.log("UNHANDLED PO PlayerAction");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerBalance.CLASSID:
                    pokerPacketHandler.handlePlayerBalance(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerDisconnectedPacket.CLASSID:
                    console.log("UNHANDLED PO PlayerDisconnectedPacket");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerHandStartStatus.CLASSID:
                    pokerPacketHandler.handlePlayerHandStartStatus(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerPokerStatus.CLASSID:
                    pokerPacketHandler.handlePlayerPokerStatus(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerReconnectedPacket.CLASSID:
                    console.log("UNHANDLED PO PlayerReconnectedPacket");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerState.CLASSID:
                    console.log("UNHANDLED PO PlayerState");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PongPacket.CLASSID:
                    console.log("UNHANDLED PO PongPacket");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.PotTransfers.CLASSID:
                    pokerPacketHandler.handlePotTransfers(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.RakeInfo.CLASSID:
                    console.log("UNHANDLED PO RakeInfo");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.RequestAction.CLASSID:
                    pokerPacketHandler.handleRequestAction(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.RebuyOffer.CLASSID:
                    console.log("Received rebuy offer!");
                    pokerPacketHandler.handleRebuyOffer(protocolObject, playerId);
                    break;
                case com.cubeia.games.poker.io.protocol.AddOnOffer.CLASSID:
                    console.log("Add-ons offered.");
                    console.log(protocolObject);
                    pokerPacketHandler.handleAddOnOffer(protocolObject, data.Player.getInstance().id);
                    break;
                case com.cubeia.games.poker.io.protocol.AddOnPeriodClosed.CLASSID:
                    console.log("Add-on period closed.");
                    pokerPacketHandler.handleAddOnPeriodClosed(data.Player.getInstance().id);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerPerformedRebuy.CLASSID:
                    pokerPacketHandler.handleRebuyPerformed(playerId);
                    break;
                case com.cubeia.games.poker.io.protocol.PlayerPerformedAddOn.CLASSID:
                    pokerPacketHandler.handleAddOnPerformed(playerId);
                    break;
                case com.cubeia.games.poker.io.protocol.StartHandHistory.CLASSID:
                    console.log("UNHANDLED PO StartHandHistory");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.HandStartInfo.CLASSID:
                    this.tableManager.startNewHand(tableId, protocolObject.handId);
                    break;
                case com.cubeia.games.poker.io.protocol.StopHandHistory.CLASSID:
                    console.log("UNHANDLED PO StopHandHistory");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.TakeBackUncalledBet.CLASSID:
                    console.log("UNHANDLED PO TakeBackUncalledBet");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.WaitingToStartBreak:
                    this.tableManager.notifyWaitingToStartBreak(tableId);
                    break;
                case com.cubeia.games.poker.io.protocol.BlindsAreUpdated.CLASSID:
                    //TODO: fix bug
                    this.tableManager.notifyBlindsUpdated(tableId, protocolObject.level, protocolObject.secondsToNextLevel);
                    break;
                case com.cubeia.games.poker.io.protocol.TournamentDestroyed.CLASSID:
                    this.tableManager.notifyTournamentDestroyed(tableId);
                    break;
                case com.cubeia.games.poker.io.protocol.AchievementNotificationPacket.CLASSID:
                    //new Poker.AchievementPacketHandler(tableId).handleAchievementNotification(protocolObject.playerId, protocolObject.message);
                    break;
                default:
                    console.log("Ignoring packet");
                    console.log(protocolObject);
                    break;
            }
        }

        private lobbyCallback(protocolObject: any): void {
            net.ConnectionManager.getInstance().onPacketReceived();

            var lobbyPacketHandler: net.LobbyPacketHandler = new net.LobbyPacketHandler();
            switch (protocolObject.classId) {
                case FB_PROTOCOL.TableSnapshotListPacket.CLASSID:
                    lobbyPacketHandler.handleTableSnapshotList(protocolObject.snapshots);
                    break;
                case FB_PROTOCOL.TableUpdateListPacket.CLASSID:
                    lobbyPacketHandler.handleTableUpdateList(protocolObject.updates);
                    break;
                case FB_PROTOCOL.TableRemovedPacket.CLASSID:
                    lobbyPacketHandler.handleTableRemoved(protocolObject.tableid);
                    break;
                case FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID:
                    lobbyPacketHandler.handleTournamentSnapshotList(protocolObject.snapshots);
                    break;
                case FB_PROTOCOL.TournamentUpdateListPacket.CLASSID:
                    lobbyPacketHandler.handleTournamentUpdates(protocolObject.updates);
                    break;
                case FB_PROTOCOL.TournamentRemovedPacket.CLASSID:
                    lobbyPacketHandler.handleTournamentRemoved(protocolObject.mttid);
                    break;
            }
        }

        private static _instance: SocketManager;
        public static getInstance(): SocketManager {
            if (SocketManager._instance == null) {
                SocketManager._instance = new SocketManager();
            }
            return SocketManager._instance;
        }
    }

}