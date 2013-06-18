WatchingPlayers = function() {
    this.entityId = "watchBox";
};

WatchingPlayers.prototype.getWatchingPlayersEntity = function() {
    return entityHandler.getEntityById(this.entityId)
}

WatchingPlayers.prototype.releaseEntity = function(entity) {
    var watchBoxEntity = this.getWatchingPlayersEntity();
    var occupants = watchBoxEntity.watchingEntities
    occupants.slice(occupants.indexOf(entity.id))
    entity.state.active = null;
}

WatchingPlayers.prototype.setupWatchingPlayersBox = function() {

    var watchBoxEntity = entityHandler.addEntity(this.entityId)
    watchBoxEntity.watchingEntities = [];
    entityHandler.addUiComponent(watchBoxEntity, "", "watch_box", null)

    var posX = 2;
    var posY = 2;

    entityHandler.addSpatial(view.containerId, watchBoxEntity, posX, posY);
    uiElementHandler.setDivElementParent(watchBoxEntity.ui.divId, watchBoxEntity.spatial.transform.anchorId)

    view.spatialManager.positionVisualEntityAtSpatial(watchBoxEntity)

};

WatchingPlayers.prototype.addWatchingEntity = function(entity) {

    var watchBoxEntity = this.getWatchingPlayersEntity();
    watchBoxEntity.watchingEntities.push = entity.id;

    this.setEntityToWatching(entity)

};

WatchingPlayers.prototype.setEntityToWatching = function(entity) {

    var watchBoxEntity = this.getWatchingPlayersEntity();
    uiElementHandler.setDivElementParent(entity.spatial.transform.anchorId, watchBoxEntity.ui.divId)
    this.setEntityPositionInbox(entity, watchBoxEntity);

};


WatchingPlayers.prototype.setEntityPositionInbox = function(entity, watchBoxEntity) {
    var nrEntities = watchBoxEntity.watchingEntities.length;

    var posXY = this.getRandomPositionWithinBoxInPercent();

    entity.spatial.transform.pos.x = posXY[0];
    entity.spatial.transform.pos.y = posXY[1];

    view.spatialManager.positionVisualEntityAtSpatial(entity)
}

WatchingPlayers.prototype.getRandomPositionWithinBoxInPercent = function() {

    var posX = Math.round(Math.random() * 100);
    var posY = Math.round(Math.random() * 100);

    return [posX, posY]

}