UserInput = function() {
    this.entityId = "userInputEntityId";
    this.playerProgressBar = null;
    this.tempHiddenButtons = [];

};

UserInput.prototype.setCheckAvailable = function(playerAction) {
    this.showCheck(playerAction);
};

UserInput.prototype.setCallAvailable = function(playerAction) {
    this.showCall(playerAction);
};

UserInput.prototype.setBetAvailable = function(playerAction) {
    this.showPlaceBet(playerAction);
};

UserInput.prototype.setRaiseAvailable = function(playerAction) {

    this.showRaiseBet(playerAction);
};

UserInput.prototype.setFoldAvailable = function() {
    this.showFold();
};

UserInput.prototype.betOK = function() {
    console.log("Bet OK!");
    userInput.hideActionButtons();
    var amount = $("#sliderValue").text().replace('$', '');
    console.log("Bet amount: " + amount);
    playerActions.bet(amount * 100);
    userInput.endUserTurn();
    console.log("User Action Button:   Bet ");
    userInput.hideSlider();
    this.tempHiddenButtons = [];
};

UserInput.prototype.raiseOK = function() {
    console.log("Raise OK!");
    userInput.hideActionButtons();
    var amount = $("#sliderValue").text().replace('$', '');
    console.log("Raise amount: " + amount);
    playerActions.raise(amount * 100);
    userInput.endUserTurn();
    console.log("User Action Button:   Raise ");
    userInput.hideSlider();
    this.tempHiddenButtons = [];
};

UserInput.prototype.setupUserInput = function() {
    var entity = entityHandler.addEntity(this.entityId);
    entityHandler.addUiComponent(entity, "", "user_input_frame", null);

    this.initPlayerGameActionUi(entity);
};

UserInput.prototype.initPlayerGameActionUi = function(parentEntity) {
    var entity = entityHandler.addEntity(this.entityId+"_player_actions")
    var userInputEntity = entityHandler.getEntityById(this.entityId);

    entityHandler.addUiComponent(entity, "", "player_actions_frame", null, userInputEntity.ui.divId);

    var placeBet = function() {
        userInput.clickBetButton();
    };

    var raise = function() {
        userInput.clickRaiseButton();
    };

    var call = function() {
        userInput.clickCallButton();
    };

    var check = function() {
        userInput.clickCheckButton();
    };

    var fold = function() {
        userInput.clickFoldButton();
    };

    var sliderOK = function() {
        userInput.hideActionButtons();
    }

    var sliderCancel = function() {
        console.log("CANCEL!");
        userInput.hideSlider();
        userInput.showTempHiddenButtons();
    }

    var buttonSide = 100;
    this.inputButtons = {
        foldButton: {label: "Fold", posX: 0, posY: 0, height: buttonSide, width: buttonSide, hasValue:false, clickFunction:fold},
        checkButton: {label: "Check", posX: 20, posY: 0, height: buttonSide, width: buttonSide, hasValue:false, clickFunction:check},
        callButton: {label: "Call", posX: 20, posY: 0, height: buttonSide, width: buttonSide, hasValue:true, clickFunction:call},
        betButton: {label: "Bet", posX: 40, posY: 0, height: buttonSide, width: buttonSide, hasValue:false, clickFunction:placeBet},
        raiseButton: {label: "Raise", posX: 40, posY: 0, height: buttonSide, width: buttonSide, hasValue:false, clickFunction:raise},
        sliderCancelButton: {label: "Cancel", posX: 20, posY: 0, height: buttonSide, width: buttonSide, hasValue:false, clickFunction:sliderCancel},
        sliderOKButton: {label: "OK", posX: 40, posY: 0, height: buttonSide, width: buttonSide, hasValue:false, clickFunction:sliderOK}
    };

    uiUtils.createActionButton(this.inputButtons.betButton, entity.ui.divId);
    uiUtils.createActionButton(this.inputButtons.raiseButton, entity.ui.divId);
    uiUtils.createActionButton(this.inputButtons.callButton, entity.ui.divId);
    uiUtils.createActionButton(this.inputButtons.checkButton, entity.ui.divId);
    uiUtils.createActionButton(this.inputButtons.foldButton, entity.ui.divId);
    uiUtils.createActionButton(this.inputButtons.sliderCancelButton, entity.ui.divId);
    uiUtils.createActionButton(this.inputButtons.sliderOKButton, entity.ui.divId);

    this.hideActionButtons();


};


UserInput.prototype.clearUserEntityTimeToAct = function() {
    var userEntity = entityHandler.getEntityById(playerHandler.getPlayerEntityIdByPid(playerHandler.myPlayerPid));
    console.log(userEntity);
    userEntity.state.timeToAct = 1;
    var seatEntity = entityHandler.getEntityById(view.seatHandler.getSeatEntityIdBySeatNumber(userEntity.state.seatId));
    view.seatHandler.setSeatEntityToPassive(seatEntity);

};

UserInput.prototype.endUserTurn = function() {
    this.hideActionButtons();
    this.clearUserEntityTimeToAct();
    this.tempHiddenButtons = [];
};

UserInput.prototype.hideActionButtons = function() {
    for (index in this.inputButtons) {
        console.log(this.inputButtons[index]);
        console.log("#### HIDING " + this.inputButtons[index].divId);
        document.getElementById(this.inputButtons[index].divId).style.visibility = "hidden";
    }
};

UserInput.prototype.tempHideActionButtons = function() {
    for (var index in this.inputButtons) {
        var button = document.getElementById(this.inputButtons[index].divId);
        if (button.style.visibility == "visible") {
            console.log("#### TEMP HIDING " + this.inputButtons[index].divId);
            console.log(this.inputButtons[index]);
            button.style.visibility = "hidden";
            this.tempHiddenButtons.push(button);
        }
    }
};

UserInput.prototype.showTempHiddenButtons = function() {
    console.log("Showing temp hidden buttons");
    for (var index = 0; index < this.tempHiddenButtons.length; index++) {
        var button = this.tempHiddenButtons[index];
        console.log(button);
        button.style.visibility = "visible";
    }
    this.tempHiddenButtons = [];
};


UserInput.prototype.showPlaceBet = function(playerAction) {
    this.setBetValue(playerAction.minAmount);
    document.getElementById(this.inputButtons.betButton.divId).style.visibility = "visible";
};

UserInput.prototype.showRaiseBet = function(playerAction) {
    this.setRaiseValue(playerAction.minAmount);
    document.getElementById(this.inputButtons.raiseButton.divId).style.visibility = "visible";
};

UserInput.prototype.showCall = function(playerAction) {
    this.setCallValue(playerAction.minAmount);
    document.getElementById(this.inputButtons.callButton.divId).style.visibility = "visible";
};

UserInput.prototype.showCheck = function(playerAction) {
    document.getElementById(this.inputButtons.checkButton.divId).style.visibility = "visible";
};

UserInput.prototype.showFold = function() {
    document.getElementById(this.inputButtons.foldButton.divId).style.visibility = "visible";
};

UserInput.prototype.showSliderOKCancel = function() {
    document.getElementById(this.inputButtons.sliderCancelButton.divId).style.visibility = "visible";
    document.getElementById(this.inputButtons.sliderOKButton.divId).style.visibility = "visible";
};

UserInput.prototype.hideSlider = function() {
    $('#slider').remove();
    document.getElementById(this.inputButtons.sliderCancelButton.divId).style.visibility = "hidden";
    document.getElementById(this.inputButtons.sliderOKButton.divId).style.visibility = "hidden";
};

UserInput.prototype.setBetValueMinMax = function(min, valueMax) {

};

UserInput.prototype.setCallValue = function(value) {
    document.getElementById(this.inputButtons.callButton.valueDivId).innerHTML ="€"+ currencyFormatted(value);
};

UserInput.prototype.setRaiseValue = function(value) {
//    document.getElementById(this.inputButtons.raiseButton.valueDivId).innerHTML = currencyFormatted(value);
};

UserInput.prototype.setBetValue = function(value) {
//    document.getElementById(this.inputButtons.betButton.valueDivId).innerHTML = currencyFormatted(value);
};

UserInput.prototype.clickBetButton = function() {
    $('#' + this.inputButtons.sliderOKButton.divId + '_button').click(this.betOK);
    $('#' + this.inputButtons.sliderOKButton.divId + '_button_label').text("Bet");
    jQuery('<span/>', {
        id: 'sliderValue'
    }).prependTo('#' + this.inputButtons.sliderOKButton.divId + '_button_label');

    var min = view.table.validActions[com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET].minAmount;
    var max = view.table.validActions[com.cubeia.games.poker.io.protocol.ActionTypeEnum.BET].maxAmount;
    this.showBetSlider(min, max);
};

UserInput.prototype.showBetSlider = function(minBet, maxBet) {
    console.log("Showing bet slider, min = " + minBet + " max = " + maxBet);
    jQuery('<div/>', {
        id: 'slider',
        class: 'cubeia'
    }).appendTo('#body');

    var slider = new BetSlider();
    slider.setMinBet(minBet);
    slider.setMaxBet(maxBet);

    slider.addMarker("Min", minBet);
    slider.addMarker("Half in", maxBet / 2);
    slider.addMarker("All in", maxBet);

    slider.draw($("#slider"));

//    $(".bettingButton").hide();
//    $(".sliderButton").show();
    this.tempHideActionButtons();
    this.showSliderOKCancel();
}

UserInput.prototype.clickRaiseButton = function() {
    $('#' + this.inputButtons.sliderOKButton.divId + '_button').click(this.raiseOK);
    $('#' + this.inputButtons.sliderOKButton.divId + '_button_label').text("Raise");
    jQuery('<span/>', {
        id: 'sliderValue'
    }).prependTo('#' + this.inputButtons.sliderOKButton.divId + '_button_label');

    var min = view.table.validActions[com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE].minAmount;
    var max = view.table.validActions[com.cubeia.games.poker.io.protocol.ActionTypeEnum.RAISE].maxAmount;
    this.showBetSlider(min, max);
};

UserInput.prototype.clickCallButton = function() {
    playerActions.call();
    this.endUserTurn();
    console.log("User Action Button:   Call ");
};

UserInput.prototype.clickCheckButton = function() {
    playerActions.check();
    this.endUserTurn();
    console.log("User Action Button:   Check ");
};

UserInput.prototype.clickFoldButton = function() {
    playerActions.fold();
    this.endUserTurn();
    console.log("User Action Button:   Fold ");
};



UserInput.prototype.setUserActionProgressBar = function(currentTime) {
	if(view.seatHandler.myTurn) {
		var playerEntity =  entityHandler.getEntityById(playerHandler.getPlayerEntityIdByPid(playerHandler.myPlayerPid));
		
		var percentRemaining = playerHandler.getPlayerEntityActionTimePercentRemaining(playerEntity, currentTime);
		this.playerProgressBar.reset();
		this.playerProgressBar.render(percentRemaining);
	}
};

UserInput.prototype.tick = function(currentTime) {
    this.setUserActionProgressBar(currentTime);
};
