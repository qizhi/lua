CommunityCards = function() {
    this.entityId = "communityCardsEntityId";
};

CommunityCards.prototype.initCommunityCardArea = function() {
    var tableEntity = entityHandler.getEntityById(view.table.entityId);
    var uiEntity = entityHandler.addEntity(this.entityId);
    uiEntity.cards = {};

    entityHandler.addUiComponent(uiEntity, "", "card_area", null);

    var posX = 50;
    var posY = 38;

    tableEntity.ui.commmunityCardsDivId = uiEntity.ui.divId
    entityHandler.addSpatial("body", uiEntity, posX, posY);
    uiElementHandler.setDivElementParent(uiEntity.ui.divId, uiEntity.spatial.transform.anchorId);
    uiElementHandler.setDivElementParent(uiEntity.spatial.transform.anchorId, tableEntity.ui.divId);

    document.getElementById(uiEntity.ui.divId).style.width = 490;
    document.getElementById(uiEntity.ui.divId).style.left = -240;
    document.getElementById(uiEntity.ui.divId).style.top = -50;

    view.spatialManager.positionVisualEntityAtSpatial(uiEntity);

};

CommunityCards.prototype.setClientCardAsCommunityCard = function(card) {

    var communityCardEntity = entityHandler.getEntityById(view.communityCards.entityId);
    var parentDiv = communityCardEntity.ui.divId;

    uiElementHandler.setDivElementParent(card.divId, parentDiv);
    document.getElementById(card.divId).className = "community_card";
}

CommunityCards.prototype.clearCommunityCards = function() {
    var communityCardEntity = entityHandler.getEntityById(view.communityCards.entityId);
    communityCardEntity.cards = {}
    uiElementHandler.removeElementChildren(0, communityCardEntity.ui.divId);

};
