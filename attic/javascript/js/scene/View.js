View = function(containerId) {
    this.containerId = containerId;

    this.table = new Table();
    this.communityCards = new CommunityCards();
    this.seatHandler = new SeatHandler();
    this.spatialManager = new SpatialManager();
    this.watchingPlayers = new WatchingPlayers();
    this.renderLoop = new RenderLoop();
    this.textFeedback = new TextFeedback();
};

View.prototype.clear = function() {
    $("#"+this.containerId).remove();
    this.table.clear();
    $(".anchor").remove();
    $(".seat_element_frame").remove();
    $("#userInputEntityId_divId").remove();
    $("#dealer_button_entity_divId").remove();
    $("#myBalanceEntity_divId").remove();
    $("#playerTimerEntityId_divId").remove();
};
View.prototype.initTableView = function(numberOfSeats) {
    var id = uiElementHandler.createDivElement("body", this.containerId, "View Container", "screenframe", null);

    var parent = document.getElementsByTagName("head")[0];

    var index = parent.getElementsByTagName("*");
    var newdiv = document.createElement('meta', [index]);
    newdiv.setAttribute('name', "apple-mobile-web-app-status-bar-style");
    newdiv.setAttribute('content', "black-translucent");

    parent.appendChild(newdiv);



    this.setupTableStructure(numberOfSeats);


    this.watchingPlayers.setupWatchingPlayersBox();
    this.communityCards.initCommunityCardArea();
    var renderFrameTime = 24; // in milliseconds
    this.renderLoop.initRenderLoop(renderFrameTime);
    this.textFeedback.initTextFeedback();
    pokerCards.initOwnCardArea();
    this.seatHandler.initOwnHandStrengthArea();
    this.seatHandler.initOwnBetTextArea();
    this.seatHandler.initOwnActionTextArea();
}

View.prototype.setupTableStructure = function(numberOfSeats) {
    this.table.createTableOfSize(numberOfSeats, this.containerId)

}



