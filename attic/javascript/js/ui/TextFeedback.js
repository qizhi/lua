TextFeedback = function() {
    this.entityId = "text_log_id"
    this.textRows = ["", "", "", "", "", "", "", ""];
    this.textEvents = 0;
};

TextFeedback.prototype.initTextFeedback = function() {

    var textLogEntity = entityHandler.addEntity(this.entityId)
    textLogEntity.watchingEntities = [];
    entityHandler.addUiComponent(textLogEntity, "", "text_log", null)

    var posX = 1;
    var posY = 68;

    entityHandler.addSpatial(view.containerId, textLogEntity, posX, posY);
    uiElementHandler.setDivElementParent(textLogEntity.ui.divId, textLogEntity.spatial.transform.anchorId)


    view.spatialManager.positionVisualEntityAtSpatial(textLogEntity)

    textLogEntity.ui.textFieldDivId = textLogEntity.ui.divId+"_box"
    uiElementHandler.createDivElement(textLogEntity.ui.divId, textLogEntity.ui.textFieldDivId, this.textRows, "text_log_rows", null);

    document.getElementById(textLogEntity.ui.textFieldDivId).style.height = "60";
    document.getElementById(textLogEntity.ui.textFieldDivId).style.width = "220px";

    this.addLogText("Text log...")
};

TextFeedback.prototype.addLogText = function(text) {
    var entity = entityHandler.getEntityById(this.entityId);
    var textBoxDivId = entity.ui.textFieldDivId

    this.textRows.push(text);
    var textstring = "";
    for (var i = 0; i < this.textRows.length; i++) {
        textstring = ""+textstring+""+this.textRows[i]+"</br>";
        if (i >= 8) {
            this.textRows.shift();
        }
    }
    document.getElementById(textBoxDivId).innerHTML = textstring;
};

TextFeedback.prototype.showSeatSpaceTextFeedback = function(playerId, action, value, actionType) {
    var betFieldDivId = "";
    var betTextDivId = "";
    if(pid == playerId) {
        var tableEntity = entityHandler.getEntityById(view.table.entityId);
        betFieldDivId = tableEntity.ui.ownBetTextDivId;
        betTextDivId = tableEntity.ui.ownActionTextDivId;
    } else {

        var playerEntityId = playerHandler.getPlayerEntityIdByPid(playerId);
        if(!playerEntityId) {
            return;
        }
        var playerEntity = entityHandler.getEntityById(playerEntityId);
        var seatEntity = entityHandler.getEntityById(view.seatHandler.getSeatEntityIdBySeatNumber(playerEntity.state.seatId));
        if (!seatEntity) return;

        betFieldDivId = seatEntity.ui.betFieldDivId;
        betTextDivId = seatEntity.ui.betTextDivId;
    }

    this.setSeatBetText(betFieldDivId,betTextDivId,action,actionType,value);

};
TextFeedback.prototype.setSeatBetText = function(betFieldDivId, betTextDivId, action, actionType, value) {
    var valueString = "";

    if (value) {
        if(actionType==ACTIONS.RAISE || actionType==ACTIONS.BET) {
            this.clearAllActionText();
            $(".user-action").addClass("action-inactive");
        }
        valueString = "&euro;<span style='color:#FFF;'>" + value + '</span><div class="user-action '+actionType+'"></div>'
    }
    var betText =  $("#"+betTextDivId);
    betText.html(action).show();
    $("#"+betFieldDivId).html(valueString).show();
};


TextFeedback.prototype.addSeatEventText = function(pid, textString) {

    var divId = "";

    if(pid==playerHandler.myPlayerPid) {
        var tableEntity = entityHandler.getEntityById(view.table.entityId);
        divId = tableEntity.ui.ownHandStrengthDivId;
    } else {
        var playerEntityId = playerHandler.getPlayerEntityIdByPid(pid);
        var playerEntity = entityHandler.getEntityById(playerEntityId);
        var seatEntity = entityHandler.getEntityById(view.seatHandler.getSeatEntityIdBySeatNumber(playerEntity.state.seatId));
        if (!seatEntity) return;
        var tableEntity = entityHandler.getEntityById(view.table.entityId);
        var key = "seat_event_key"+this.textEvents+"";
        console.debug("hand strength == " + textString);
        divId = seatEntity.ui.handStrengthDiv;
    }


    view.seatHandler.showHandStrength(divId,textString);

};

TextFeedback.prototype.triggerTextAnimation = function(key) {


    document.getElementById(key).style.opacity = 0.1;
    document.getElementById(key).style.left = document.getElementById(key).style.left + (Math.random()*4) - 2 +"%";


    var t = setTimeout(function() {
        uiElementHandler.removeElements(0.95, key)
    }, 800);

}

TextFeedback.prototype.clearAllSeatSpaceTextFeedback = function() {
    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    for (index in tableEntity.seats) {
        var seatEntity = entityHandler.getEntityById(view.seatHandler.getSeatEntityIdBySeatNumber(index));
        var betFieldDivId = seatEntity.ui.betFieldDivId;
        $("#"+seatEntity.ui.betFieldDivId).html("");
        $("#"+seatEntity.ui.betTextDivId).html("").hide();
        view.seatHandler.hideHandStrength(seatEntity.ui.handStrengthDiv);
    }
    var tableEntity = entityHandler.getEntityById(view.table.entityId);

    $("#"+tableEntity.ui.ownBetTextDivId).html("").hide();
    $("#"+tableEntity.ui.ownActionTextDivId).html("").hide();

    document.getElementById(tableEntity.ui.ownHandStrengthDivId).style.visibility ="hidden";
    document.getElementById(tableEntity.ui.ownHandStrengthDivId).innerHTML = "";


}
TextFeedback.prototype.clearAllActionText = function() {
    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    for (index in tableEntity.seats) {
        var seatEntity = entityHandler.getEntityById(view.seatHandler.getSeatEntityIdBySeatNumber(index));
        var text = $("#"+seatEntity.ui.betTextDivId);
        text.hide();
        text.html("");
    }
    $("#"+tableEntity.ui.ownActionTextDivId).html("").hide();


}

TextFeedback.prototype.tick = function(currentTime) {

};
