function getUrlParam(name) {
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}
		
function handleSeatInfo(seatInfoPacket) {
	console.log("seatInfo pid[" + seatInfoPacket.player.pid + "]  seat[" +  seatInfoPacket.seat +"]");

	seatPlayer(seatInfoPacket.player.pid, seatInfoPacket.seat, seatInfoPacket.player.nick);
}

function handleNotifyLeave(notifyLeavePacket) {
	playerHandler.unseatPlayer(notifyLeavePacket.pid);
}

function handleNotifyJoin(notifyJoinPacket) {
    console.log("NOTIFY JOIN!!");
	seatPlayer(notifyJoinPacket.pid, notifyJoinPacket.seat, notifyJoinPacket.nick);
}

function handleJoinResponse(joinResponsePacket) {
	seatPlayer(pid, joinResponsePacket.seat, screenname);
}

function seatPlayer(pid, seat, nick) {
    console.log("Seating player " + nick);
	playerHandler.addWatchingPlayer(pid, nick);
    playerHandler.seatPlayerIdAtTable(pid, seat);
};


function leaveTable() {
	connector.leaveTable(tableid);
};

function handlePacket(protocolObject) {
	switch (protocolObject.classId) {
		case FB_PROTOCOL.NotifyJoinPacket.CLASSID :
			handleNotifyJoin(protocolObject);
			break;
		case FB_PROTOCOL.NotifyLeavePacket.CLASSID :
			handleNotifyLeave(protocolObject);
			break;
		case FB_PROTOCOL.SeatInfoPacket.CLASSID :
			handleSeatInfo(protocolObject);
			break;
		case FB_PROTOCOL.JoinResponsePacket.CLASSID :
			view.table.hideJoinButton();
            view.table.hideLeaveButton();
            view.table.showSitOutButton();
			handleJoinResponse(protocolObject);
			break;
		case FB_PROTOCOL.GameTransportPacket.CLASSID :
			handleGameDataPacket(protocolObject);
			break;
		case FB_PROTOCOL.WatchResponsePacket.CLASSID :
			view.table.enableJoinTable();
            view.table.showLeaveButton();
			break;
		case FB_PROTOCOL.LeaveResponsePacket.CLASSID :
            showLobby();
			break;
	}
};

function handleGameDataPacket(packet) {
	pokerProtocolHandler.handleGameTransportPacket(packet);
};


function currencyFormatted(amount) {
	return parseFloat(amount/100).toFixed(2);
};

function joinGame() {
	connector.joinTable(tableid, -1);
//    fakeAction();
}

function rotate() {
    view.table.rotate(6);
}

function fakeAction() {
//    fakeActionRequest();
    fakeSomeCards();
};

function fakeSomeCards() {
    var cardId = 0;
    for (var i = 0; i < 9; i+=2 ) {
        console.log("Seating player " + (89+i));
        seatPlayer(89+i, i, "Player" + i);
        pokerDealer.dealCardIdToPid({player: 89+i, card: {cardId: cardId++, rank:13, suit:4}});
        pokerDealer.dealCardIdToPid({player: 89+i, card: {cardId: cardId++, rank:13, suit:4}});
        playerHandler.updateSeatBalance(89+i, "6.25");
    }
    playerActions.handlePlayerActionFeedback(89+4, "93", null);
    playerActions.handlePlayerActionFeedback(89+6, "95", null);
    playerActions.handlePlayerActionFeedback(89, "89", null);
    playerActions.handlePlayerActionFeedback(89, "89", null);

    pokerDealer.dealCommunityCard({cardId: cardId++, rank:8, suit:3});
    pokerDealer.dealCommunityCard({cardId: cardId++, rank:2, suit:1});
    pokerDealer.dealCommunityCard({cardId: cardId++, rank:11, suit:0});
    pokerDealer.dealCommunityCard({cardId: cardId++, rank:12, suit:3});
    pokerDealer.dealCommunityCard({cardId: cardId++, rank:10, suit:2});
    view.table.potUpdated(1025);
    pokerDealer.moveDealerButton(4);
}

function fakeActionRequest() {
    pid = 88;
    seatPlayer(1, 1, "player" + 1);
    seatPlayer(88, 2, "player" + 88);

    var action = new com.cubeia.games.poker.io.protocol.RequestAction();
    var allowedAction = new com.cubeia.games.poker.io.protocol.PlayerAction();
    allowedAction.type = 6;
    allowedAction.minAmount = 0;
    allowedAction.maxAmount = 0;

    var allowedAction2 = new com.cubeia.games.poker.io.protocol.PlayerAction();
    allowedAction2.type = 3;
    allowedAction2.minAmount = 100;
    allowedAction2.maxAmount = 1000;

    var allowedAction3 = new com.cubeia.games.poker.io.protocol.PlayerAction();
    allowedAction3.type = 4;
    allowedAction3.minAmount = 100;
    allowedAction3.maxAmount = 500;

    action.allowedActions = [allowedAction, allowedAction2, allowedAction3];
    action.timeToAct = 15000;
    action.currentPotSize = 0;
    action.player = 88;
//    view.table.validActions = action.allowedActions;
    view.table.handleRequestAction(action);
}


function sendAction(seq, actionType, betAmount, raiseAmount) {
	var performAction = new com.cubeia.games.poker.io.protocol.PerformAction();
	performAction.player = pid;
	performAction.action = new com.cubeia.games.poker.io.protocol.PlayerAction();
	console.log("sending action type=" + actionType);
	performAction.action.type = actionType;
	performAction.action.minAmount = 0;
	performAction.action.maxAmount = 0;
	performAction.betAmount = betAmount;
	performAction.raiseAmount = raiseAmount || 0;
	performAction.timeOut = 0;
	performAction.seq = seq;
	
	sendGameTransportPacket(performAction);
};
function sitOutAction() {
    var sitOut = new com.cubeia.games.poker.io.protocol.PlayerSitoutRequest();
    sitOut.player = pid;
    //sendGameTransportPacket(sitOut);
    console.log("SITTING OUT");
    connector.sendStyxGameData(0, tableid, sitOut); //sendProtocolObject(0, tableid, sitOut.classId(), []);
};
function sitInAction() {
    var sitIn = new com.cubeia.games.poker.io.protocol.PlayerSitinRequest();
    sitIn.player = pid;
    //sendGameTransportPacket(sitOut);
    console.log("SITTING IN");
    connector.sendStyxGameData(0, tableid, sitIn); //sendProtocolObject(0, tableid, sitOut.classId(), []);
};
function sendGameTransportPacket(gamedata) {
//	var byteArray = gamedata.save();
    connector.sendStyxGameData(0, tableid, gamedata);
//	connector.sendGameTransportPacket(0, tableid, gamedata.classId(), byteArray);
};


function suppressBackspace(evt) {
    evt = evt || window.event;
    var target = evt.target || evt.srcElement;

    if (evt.keyCode == 8 && !/input|textarea/i.test(target.nodeName)) {
        return false;
    }
};
