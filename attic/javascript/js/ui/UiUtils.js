UiUtils = function() {

};

UiUtils.prototype.getPercentDoneForMinMaxCurrent = function(min, max, current) {
    var total = max - min;
    if (total == 0) return 0;
    var completed = current - min
    var percentDone = Math.max(0, Math.min(100, ((completed) / total) * 100));

    return percentDone;
};


UiUtils.prototype.createActionButton = function(buttonData, parentDivId) {
    var buttonFrameDivId = "userActionButton_"+buttonData.label;

    uiElementHandler.createDivElement(parentDivId, buttonFrameDivId, "<br>"+buttonData.label, "pressed_frame", null);
    document.getElementById(buttonFrameDivId).style.top = buttonData.posY+"%";
    document.getElementById(buttonFrameDivId).style.left = buttonData.posX+"%";
    document.getElementById(buttonFrameDivId).style.width = buttonData.width+"px";
    document.getElementById(buttonFrameDivId).style.height = buttonData.height+"px";
//  document.getElementById(buttonFrameDivId).style.visibility = "hidden";

    var buttonDivId = buttonFrameDivId+"_button";

    buttonData.buttonDivId = buttonDivId;
    buttonData.divId = buttonFrameDivId;

    uiElementHandler.createDivElement(buttonFrameDivId, buttonData.buttonDivId, "", "poker_game_action_button", null);

    var labelDivId = buttonDivId+"_label"
    uiElementHandler.createDivElement(buttonData.buttonDivId, labelDivId, buttonData.label, "poker_button_label", null);

    if (buttonData.hasValue == true) {
        var valueLabelDiv = labelDivId+"_value";
        uiElementHandler.createDivElement(labelDivId, valueLabelDiv, "", "poker_button_value", null);
        buttonData.valueDivId = valueLabelDiv;
    }

    document.getElementById(buttonDivId).onclick = function(e) {
        buttonData.clickFunction();
    };

    return [buttonFrameDivId, buttonDivId, labelDivId];

};