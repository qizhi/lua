PlayerActions = function() {

};

PlayerActions.prototype.getActionTextString = function(pid, action, value) {
    var playerEntityId = playerHandler.getPlayerEntityIdByPid(pid);
    var playerEntity = entityHandler.getEntityById(playerEntityId);
    var textString = ""+playerEntity.name+" "+action+"s";
    if (value) textString = textString+" "+value;
    textString = textString+".";
    return textString;
}

PlayerActions.prototype.handlePlayerActionFeedback = function(pid, action, value,actionType) {

    view.textFeedback.showSeatSpaceTextFeedback(pid, action, value,actionType);
    var textString = this.getActionTextString(pid, action, value);
    view.textFeedback.addLogText(textString);
};

PlayerActions.prototype.leaveTable = function() {
    console.log("Player pressed Leave Table");
    leaveTable();
};
var ACTIONS = {
   BET : "bet-action",
   CHECK : "check-action",
   FOLD : "fold-action",
   RAISE : "raise-action",
   CALL : 'call-action'
};

PlayerActions.prototype.bet = function(betValue) {

    view.textFeedback.addLogText("You Bet "+currencyFormatted(betValue)+".");
	sendAction(view.table.lastActionRequest.seq, com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET, betValue, 0);
};

PlayerActions.prototype.check = function() {
    //view.textFeedback.showSeatSpaceTextFeedback(playerHandler.myPlayerPid, "Check", null,ACTIONS.CHECK);
    view.textFeedback.addLogText("You Check.");
	sendAction(view.table.lastActionRequest.seq, com.cubeia.games.poker.io.protocol.ActionTypeEnum.CHECK, 0, 0);
};

PlayerActions.prototype.fold = function() {
    //view.textFeedback.showSeatSpaceTextFeedback(playerHandler.myPlayerPid, "Fold", null, ACTIONS.FOLD);
    view.textFeedback.addLogText("You Check.");
	sendAction(view.table.lastActionRequest.seq, com.cubeia.games.poker.io.protocol.ActionTypeEnum.FOLD, 0, 0);
};

PlayerActions.prototype.raise = function(betValue) {
    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    //view.textFeedback.setSeatBetText(tableEntity.ui.ownBetTextDivId,"Raise",ACTIONS.RAISE,currencyFormatted(betValue));
    view.textFeedback.addLogText("You Raise "+currencyFormatted(betValue)+".");

	sendAction(view.table.lastActionRequest.seq, com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE, betValue, 0);
};

PlayerActions.prototype.call = function(betValue) {
    //view.textFeedback.showSeatSpaceTextFeedback(playerHandler.myPlayerPid, "Call", null, ACTIONS.CALL);
    view.textFeedback.addLogText("You Call.");
	sendAction(view.table.lastActionRequest.seq, com.cubeia.games.poker.io.protocol.ActionTypeEnum.CALL, betValue, 0);
};

