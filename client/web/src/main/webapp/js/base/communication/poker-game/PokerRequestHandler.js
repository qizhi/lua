var Poker = Poker || {};

Poker.PokerRequestHandler = Class.extend({
    tableId : null,

    /**
     * @type Poker.TableManager
     */
    tableManager : null,

    /**
     * @constructor
     * @param {Number} tableId
     */
    init : function(tableId){
        this.tableId = tableId;
        this.tableManager = Poker.AppCtx.getTableManager();
    },
    onMyPlayerAction : function (actionType, amount) {
        console.log("ON my player action");
        console.log(actionType);
        console.log(amount);
        var tableRequestHandler = new Poker.TableRequestHandler(this.tableId);
        if (actionType.id == Poker.ActionType.JOIN.id) {
            tableRequestHandler.joinTable();
        } else if (actionType.id == Poker.ActionType.LEAVE.id) {
            var table = this.tableManager.getTable(this.tableId);
            if (table.tournamentClosed) {
                console.log("Tournament is closed, will close the table without telling the server.");
                this.tableManager.leaveTable(this.tableId);
            } else if (this.tableManager.isSeated(this.tableId)) {
                tableRequestHandler.leaveTable();
                this.tableManager.leaveTable(this.tableId);
            } else {
                tableRequestHandler.unwatchTable();
                this.tableManager.leaveTable(this.tableId);
            }
        } else if (actionType.id == Poker.ActionType.SIT_IN.id) {
            this.sitIn();
        } else if (actionType.id == Poker.ActionType.SIT_OUT.id) {
            this.sitOut();
        } else if (actionType.id == Poker.ActionType.REBUY.id) {
            this.sendRebuyResponse(true);
            console.log("Hiding rebuy buttons for player " + Poker.MyPlayer.id + " table " + this.tableId);
            this.tableManager.hideRebuyButtons(this.tableId, Poker.MyPlayer.id);
        } else if (actionType.id == Poker.ActionType.DECLINE_REBUY.id) {
            this.sendRebuyResponse(false);
            console.log("Hiding rebuy buttons for player " + Poker.MyPlayer.id + " table " + this.tableId);
            this.tableManager.hideRebuyButtons(this.tableId, Poker.MyPlayer.id);
        } else if (actionType.id == Poker.ActionType.ADD_ON.id) {
            this.sendAddOnRequest();
            this.tableManager.hideAddOnButton(this.tableId, Poker.MyPlayer.id);
        } else {
            this.sendAction(Poker.ActionUtils.getActionEnumType(actionType), amount, 0);
        }
    },
    sendAction : function(actionType, betAmount, raiseAmount) {
        var action = Poker.ActionUtils.getPlayerAction(this.tableId,Poker.PokerSequence.getSequence(this.tableId),
            actionType, betAmount, raiseAmount);
        this.sendGameTransportPacket(action);
    },
    sendGameTransportPacket : function(gamedata) {
        var connector = Poker.AppCtx.getConnector();
        connector.sendStyxGameData(0, this.tableId, gamedata);
        console.log("package sent to table " + this.tableId);
        console.log(gamedata);
    },
    buyIn : function(amount) {
        var buyInRequest = new com.cubeia.games.poker.io.protocol.BuyInRequest();
        buyInRequest.amount = amount;
        buyInRequest.sitInIfSuccessful = true;
        this.sendGameTransportPacket(buyInRequest);
    },
    requestBuyInInfo : function() {
        console.log("REQUEST BUY-IN INFO!");
        var buyInInfoRequest = new com.cubeia.games.poker.io.protocol.BuyInInfoRequest();
        this.sendGameTransportPacket(buyInInfoRequest);

    },
    sitOut : function () {
        var sitOut = new com.cubeia.games.poker.io.protocol.PlayerSitoutRequest();
        sitOut.player = Poker.MyPlayer.id;
        this.sendGameTransportPacket(sitOut);
    },
    sitIn : function () {
        var sitIn = new com.cubeia.games.poker.io.protocol.PlayerSitinRequest();
        sitIn.player = Poker.MyPlayer.id;
        this.sendGameTransportPacket(sitIn);
    },
    sendRebuyResponse : function(answer) {
        var rebuy = new com.cubeia.games.poker.io.protocol.RebuyResponse();
        rebuy.answer = answer;
        this.sendGameTransportPacket(rebuy);
    },
    sendAddOnRequest : function() {
        this.sendGameTransportPacket(new com.cubeia.games.poker.io.protocol.PerformAddOn());
    }
});