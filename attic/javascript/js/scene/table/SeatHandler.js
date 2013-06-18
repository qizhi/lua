SeatHandler = function() {
    this.activeSeatEntity = null;
    this.myTurn = false;
    this.playerProgressBar = null;

};



SeatHandler.prototype.removePlayerFromSeat = function(seatEntity) {
    document.getElementById(seatEntity.spatial.transform.anchorId).style.opacity = 0.4;
    seatEntity.occupant = null;
};

SeatHandler.prototype.getSeatEntityIdBySeatNumber = function(seatNr) {
    return "seat_nr_"+seatNr;
};

SeatHandler.prototype.setSeatEntityToPassive = function(seatEntity) {
    if (!seatEntity) return;
    this.setSeatTimerPercentRemaining(seatEntity, 0);
    console.log($("#"+seatEntity.ui.divId));
    $("#"+seatEntity.ui.divId).removeClass("poker_seat_box_active");
  
};

SeatHandler.prototype.setCurrentActingSeatEntity = function(seatEntity) {
    if (this.activeSeatEntity) {
        this.setSeatEntityToPassive(this.activeSeatEntity);
    };

    if (seatEntity === undefined) return;

    this.activeSeatEntity = seatEntity;
    $("#"+this.activeSeatEntity.ui.divId).addClass("poker_seat_box_active");
};

SeatHandler.prototype.setCurrentPlayerActionTimeout = function(pid, timeToAct) {
	if(pid == playerHandler.myPlayerPid) {
		this.myTurn = true;
	} else {
		this.myTurn = false;
	}
	
    var playerEntity = entityHandler.getEntityById(playerHandler.getPlayerEntityIdByPid(pid));
    

    var seatNr = playerEntity.state.seatId;
    

    playerEntity.state.actionStartTime = new Date().getTime();
    playerEntity.state.timeToAct = timeToAct;
    
    var seatEntity = entityHandler.getEntityById(this.getSeatEntityIdBySeatNumber(seatNr));
  
    this.setCurrentActingSeatEntity(seatEntity);
    console.log(seatEntity);
}

SeatHandler.prototype.getPidByOccupiedSeatNumber = function(occupiedSeatNr) {
    console.log(occupiedSeatNr);
    var seatEntity = entityHandler.getEntityById("seat_nr_"+occupiedSeatNr);
    console.log(seatEntity);
    var playerEntityId = seatEntity.occupant.id;
    var playerEntity = entityHandler.getEntityById(playerEntityId);
    return playerEntity.pid;
};

SeatHandler.prototype.createSeatNumberOnTableEntityAtXY = function(seatNr, tableEntity, x, y) {
    var seatEntity = entityHandler.addEntity(this.getSeatEntityIdBySeatNumber(seatNr));

    entityHandler.addUiComponent(seatEntity, "", "poker_seat_box", null);

    console.log(seatEntity);

    var seatBoxDivId = seatEntity.ui.divId;

    entityHandler.addSpatial(tableEntity.ui.divId, seatEntity, x, y);

    var playerPoint = {posX: 50, posY:0};
    var cardsPoint = {posX:50, posY:50};
    var dealerButtonPoint = {posX: 75, posY:-2};
    var actionPoint = {posX: 0, posY: 100};
    var balancePoint = {posX:0, posY:100};
    var playerTimer = {posX: 15, posY:50};
    var playerHandStrength = {posX:0,posY:155};

    var seatAttachmentPoints = {
        player:{transform:playerPoint},
        cards:{transform:cardsPoint},
        dealerButton:{transform:dealerButtonPoint},
        actionLabel:{transform:actionPoint},
        balance:{transform:balancePoint},
        playerTimer:{transform:playerTimer},
        playerHandStrength : {transform : playerHandStrength}
    };
    seatEntity.spatial.attachmentPoints = seatAttachmentPoints;
    uiElementHandler.setDivElementParent(seatEntity.ui.divId, seatEntity.spatial.transform.anchorId);
    this.addBalanceFieldToSeat(seatEntity);
    

    
    this.addPlayerTimerProgressBar(seatEntity);
    this.addPlayerActionIndicator(seatEntity);
    this.removePlayerFromSeat(seatEntity);
    this.addPlayerAvatar(seatEntity);
    this.addCardFieldToSeat(seatEntity);
    this.addPlacedBetFieldToSeat(seatEntity);
    this.addDealerButtonFieldToSeat(seatEntity);
    this.addHandStrength(seatEntity);
    
    return seatEntity;

};

SeatHandler.prototype.addPlayerAvatar = function(seatEntity) {
    var uiEntity = entityHandler.addEntity(seatEntity.id+"_avatarUI");
    entityHandler.addUiComponent(uiEntity, "", "player_avatar", null);

    var posX = seatEntity.spatial.attachmentPoints.playerTimer.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.playerTimer.transform.posY;

    seatEntity.ui.avatarDiv = uiEntity.ui.divId;

    entityHandler.addSpatial(seatEntity.ui.divId, uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);

    seatEntity.ui.avatarUI = uiEntity.ui.divId+"Avatar";
    var randomAvatarId = Math.floor(Math.random() * 9);
    console.log("Setting avatar " + randomAvatarId + " on avatar " + uiEntity.ui.divId);
    $('#' + uiEntity.ui.divId).css('background-image','url(images/players/player' + randomAvatarId + '.png)')

    //uiElementHandler.createDivElement(seatEntity.ui.divId, seatEntity.ui.avatarUI, "", "class", null);
};
SeatHandler.prototype.addPlayerActionIndicator = function(seatEntity) {
    var uiEntity = entityHandler.addEntity(seatEntity.id+"_seatActionUi");
    entityHandler.addUiComponent(uiEntity, "", "seat_element_frame", null);
    seatEntity.ui.seatActionFrameDivId = uiEntity.ui.divId;
    seatEntity.ui.seatActionSlotDivId = uiEntity.ui.divId+"_slot";


};
SeatHandler.prototype.showHandStrength = function(divId,text) {
    document.getElementById(divId).style.visibility="";
    document.getElementById(divId).innerHTML = text;
};
SeatHandler.prototype.hideHandStrength = function(divId) {
    document.getElementById(divId).style.visibility="hidden";
};
SeatHandler.prototype.addHandStrength = function(seatEntity) {
    var uiEntity = entityHandler.addEntity(seatEntity.id+"_handStrengthUI");
    entityHandler.addUiComponent(uiEntity, "", "player_hand_strength", null);

    var posX = seatEntity.spatial.attachmentPoints.playerHandStrength.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.playerHandStrength.transform.posY;

    seatEntity.ui.handStrengthDiv = uiEntity.ui.divId;

    entityHandler.addSpatial(seatEntity.ui.divId, uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);
};

SeatHandler.prototype.addPlayerTimerProgressBar = function(seatEntity) {
    var uiEntity = entityHandler.addEntity(seatEntity.id+"_timeProgressUi");
    entityHandler.addUiComponent(uiEntity, "", "seat_timer_frame", null);


    var posX = seatEntity.spatial.attachmentPoints.playerTimer.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.playerTimer.transform.posY;

    seatEntity.ui.timeProgressBarFrameDivId = uiEntity.ui.divId;

    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);

    seatEntity.ui.timeProgressBarDivId = uiEntity.ui.divId+"TimeProgress";
    uiElementHandler.createDivElement(seatEntity.ui.divId, seatEntity.ui.timeProgressBarDivId, "", "standing_progressbar", null);

};

SeatHandler.prototype.addDealerButtonFieldToSeat = function(seatEntity) {

    var uiEntity = entityHandler.addEntity(seatEntity.id+"_dealerButtonUi");
    entityHandler.addUiComponent(uiEntity, "", "anchor", null);

    var posX = seatEntity.spatial.attachmentPoints.dealerButton.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.dealerButton.transform.posY;

	seatEntity.ui.dealerButtonDivId = uiEntity.ui.divId;

    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);

    seatEntity.ui.dealerButtonSlotDivId = uiEntity.ui.divId+"_slot";
    uiElementHandler.createDivElement(uiEntity.ui.divId, seatEntity.ui.dealerButtonSlotDivId, "", "anchor", null);
};

SeatHandler.prototype.addPlacedBetFieldToSeat = function(seatEntity) {

    var betTextEntity = entityHandler.addEntity(seatEntity.id+"_betText");
    var uiEntity = entityHandler.addEntity(seatEntity.id+"_betFieldUi");
    entityHandler.addUiComponent(uiEntity, "", "player_action", null);
    entityHandler.addUiComponent(betTextEntity,"","player_action_text",null);

    var posX = seatEntity.spatial.attachmentPoints.actionLabel.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.actionLabel.transform.posY;
	seatEntity.ui.betFieldDivId = uiEntity.ui.divId;
    seatEntity.ui.betTextDivId = betTextEntity.ui.divId;
	
    entityHandler.addSpatial("body", uiEntity, posX, posY);
    entityHandler.addSpatial("body",betTextEntity,posX,posY);

    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);

    uiElementHandler.setDivElementParent(betTextEntity.ui.divId, betTextEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(betTextEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(betTextEntity);
    $("#"+seatEntity.ui.betTextDivId).hide();

};
SeatHandler.prototype.initOwnBetTextArea = function() {

    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    var uiEntity = entityHandler.addEntity("ownBetTextAreaEntityId");
    entityHandler.addUiComponent(uiEntity, "", "own_player_action", null);

    var posX = 40;
    var posY = 60;

    tableEntity.ui.ownBetTextDivId = uiEntity.ui.divId;
    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, tableEntity.ui.divId);

    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);
};
SeatHandler.prototype.initOwnActionTextArea = function() {

    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    var uiEntity = entityHandler.addEntity("ownActionTextAreaEntityId");

    entityHandler.addUiComponent(uiEntity, "", "own_action_text", null);

    var posX = 43.5;
    var posY = 72;

    tableEntity.ui.ownActionTextDivId = uiEntity.ui.divId;
    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, tableEntity.ui.divId);

    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);
};
SeatHandler.prototype.initOwnHandStrengthArea = function() {

    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    var uiEntity = entityHandler.addEntity("ownHandStrengthAreaEntityId");

    entityHandler.addUiComponent(uiEntity, "", "own_hand_strength", null);

    var posX = 40;
    var posY = 60;

    tableEntity.ui.ownHandStrengthDivId = uiEntity.ui.divId;
    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, tableEntity.ui.divId);

    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);
};
SeatHandler.prototype.addBalanceFieldToSeat = function(seatEntity) {
    var uiEntity = entityHandler.addEntity(seatEntity.id+"_balanceUi");
    entityHandler.addUiComponent(uiEntity, "", "player_balance", null);

    var posX = seatEntity.spatial.attachmentPoints.balance.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.balance.transform.posY;
	seatEntity.ui.balanceDivId = uiEntity.ui.divId;

    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);
};

SeatHandler.prototype.getCardFieldIdBySeatId = function(seatEntityId) {
    return seatEntityId+"_cardFieldUi";
};

SeatHandler.prototype.addCardFieldToSeat = function(seatEntity) {
    var cardBoxId = this.getCardFieldIdBySeatId(seatEntity.id);
    var uiEntity = entityHandler.addEntity(cardBoxId);
    entityHandler.addUiComponent(uiEntity, "", "card_area", null);

    var posX = seatEntity.spatial.attachmentPoints.cards.transform.posX;
    var posY = seatEntity.spatial.attachmentPoints.cards.transform.posY;

    seatEntity.ui.cardFieldDivId = uiEntity.ui.divId;
    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);
};

SeatHandler.prototype.addPlayerToSeat = function(playerEntity, seatEntity) {
    if (playerEntity.pid == playerHandler.myPlayerPid) {
    	
    } else {
        document.getElementById(seatEntity.spatial.transform.anchorId).style.opacity = 1;
        uiElementHandler.setDivElementParent(playerEntity.spatial.transform.anchorId, seatEntity.ui.divId);
    }
};

SeatHandler.prototype.setSeatTimerPercentRemaining = function(seatEntity, percent) {
    document.getElementById(seatEntity.ui.timeProgressBarDivId).style.height = percent+"%";

};

SeatHandler.prototype.updateActiveSeatTimer = function(currentTime) {
	if (this.myTurn || !this.activeSeatEntity) {
		return;
	} else {
    	var playerEntity = this.activeSeatEntity.occupant;
    	
    	var percentRemaining = playerHandler.getPlayerEntityActionTimePercentRemaining(playerEntity, currentTime);

    	this.setSeatTimerPercentRemaining(this.activeSeatEntity, 100-percentRemaining);
    	
    	if (percentRemaining < 0) {
    		this.clearActiveSeatEntity();
    	};
    }
};
SeatHandler.prototype.clearActiveSeatEntity = function() {
	this.setSeatEntityToPassive(this.activeSeatEntity);
	this.activeSeatEntity = null;
};



SeatHandler.prototype.tick = function(currentTime) {
    this.updateActiveSeatTimer(currentTime);
};

