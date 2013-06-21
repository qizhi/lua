///<reference path="../../dtd/firebase.d.ts"/>
///<reference path="TableHandler.ts"/>
///<reference path="TournamentHandler.ts"/>
///<reference path="PokerHandler.ts"/>
///<reference path="../data/Player.ts"/>
var net;
(function (net) {
    var SocketManager = (function () {
        function SocketManager() {
            this.retryCount = 0;
        }
        SocketManager.prototype.initialize = function (socketURL, socketPort) {
            this.webSocketUrl = socketURL;
            this.webSocketPort = socketPort;

            this.connect();
        };

        SocketManager.prototype.getConnector = function () {
            return this.connector;
        };

        SocketManager.prototype.forceLogout = function (packet) {
            console.log("Force log out", packet.code, packet.message);

            //new Poker.ConnectionPacketHandler().handleForceLogout(packet.code, packet.message);
            this.unregisterHandlers();
        };

        SocketManager.prototype.unregisterHandlers = function () {
            if (this.connector) {
                console.log("unregisterHandlers");
                this.connector.getIOAdapter().unregisterHandlers();
            }
        };

        SocketManager.prototype.doLogin = function (username, password) {
            data.Player.getInstance().password = password;
            var operatorId = 0;
            this.connector.login(username, password, operatorId);
        };

        SocketManager.prototype.connect = function () {
            var _this = this;
            console.log("Connecting");
            this.unregisterHandlers();
            FIREBASE.ReconnectStrategy.MAX_ATTEMPTS = 0;

            this.connector = new FIREBASE.Connector(function (po) {
                _this.handlePacket(po);
            }, function (po) {
                _this.lobbyCallback(po);
            }, function (status, playerId, name, token) {
                _this.loginCallback(status, playerId, name, token);
            }, function (status, attempts, desc) {
                _this.statusCallback(status);
            });

            /*
            this.connector = new FIREBASE.Connector(
            function (po) {
            Poker.AppCtx.getConnectionManager().onPacketReceived();
            self.handlePacket(po);
            },
            function (po) {
            Poker.AppCtx.getConnectionManager().onPacketReceived();
            self.lobbyCallback(po);
            },
            function (status, playerId, name, credentials) {
            self.loginCallback(status, playerId, name, credentials);
            },
            function (status) {
            self.statusCallback(status);
            });
            */
            console.log("Connector connect: ", this.webSocketUrl, this.webSocketPort);
            this.connector.connect("FIREBASE.WebSocketAdapter", this.webSocketUrl, this.webSocketPort, "socket", false, null);
        };

        SocketManager.prototype.handlePacket = function (packet) {
            var tournamentId = -1;
            if (packet.mttid) {
                tournamentId = packet.mttid;
            }

            var tableId = -1;
            if (packet.tableid) {
                tableId = packet.tableid;
            }

            var classId = packet.classId;
            var tournamentPacketHandler = new net.TournamentPacketHandler(tournamentId);
            var tablePacketHandler = new net.TablePacketHandler(tableId);

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
                    break;
                default:
                    console.log("handler not found", packet);
                    break;
            }
        };

        SocketManager.prototype.handleGameDataPacket = function (gameTransportPacket) {
            /*if (Poker.Settings.isEnabled(Poker.Settings.Param.FREEZE_COMMUNICATION, null) == true) {
            return;
            }
            if (!this.tableManager.tableExist(gameTransportPacket.tableid)) {
            console.log("Received packet for table (" + gameTransportPacket.tableid + ") you're not viewing");
            return;
            }*/
            var tableId = gameTransportPacket.tableid;
            var playerId = gameTransportPacket.pid;
            var valueArray = FIREBASE.ByteArray.fromBase64String(gameTransportPacket.gamedata);
            var gameData = new FIREBASE.ByteArray(valueArray);
            var length = gameData.readInt();
            var classId = gameData.readUnsignedByte();

            var protocolObject = com.cubeia.games.poker.io.protocol.ProtocolObjectFactory.create(classId, gameData);
            var pokerPacketHandler = new net.PokerPacketHandler(tableId);
            switch (protocolObject.classId()) {
                case com.cubeia.games.poker.io.protocol.GameState.CLASSID:
                    break;
                case com.cubeia.games.poker.io.protocol.BestHand.CLASSID:
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

                    break;
                case com.cubeia.games.poker.io.protocol.CardToDeal.CLASSID:
                    console.log("UNHANDLED PO CardToDeal");
                    console.log(protocolObject);
                    break;
                case com.cubeia.games.poker.io.protocol.DealerButton.CLASSID:
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
                    break;
                case com.cubeia.games.poker.io.protocol.BlindsAreUpdated.CLASSID:
                    break;
                case com.cubeia.games.poker.io.protocol.TournamentDestroyed.CLASSID:
                    break;
                default:
                    console.log("Ignoring packet");
                    console.log(protocolObject);
                    break;
            }
        };

        SocketManager.prototype.lobbyCallback = function (po) {
        };

        SocketManager.prototype.loginCallback = function (status, playerId, name, credentials) {
        };

        SocketManager.prototype.statusCallback = function (status) {
        };

        SocketManager.getInstance = function () {
            if (SocketManager._instance == null) {
                SocketManager._instance = new SocketManager();
            }
            return SocketManager._instance;
        };
        return SocketManager;
    })();
    net.SocketManager = SocketManager;
})(net || (net = {}));
//@ sourceMappingURL=SocketManager.js.map
