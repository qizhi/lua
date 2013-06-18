EntityState = function() {

};

EntityState.prototype.disengageEntityFromCurrentState = function(entity) {
    var currentState = entity.state.active;

    switch (currentState) {
        case "watching":
            view.watchingPlayers.releaseEntity(entity);
        break;
        case "playing":
            // If the table needs to disengage for some reason do so here.
            break;
        case "waiting":
            // If the table needs to disengage for some reason do so here.
        break;
        case null:
            return;
        break;
    }

}

EntityState.prototype.setEntityToWatchingState = function(entity) {
    this.disengageEntityFromCurrentState(entity)
    entity.state.active = "watching";

    view.watchingPlayers.addWatchingEntity(entity);
};

EntityState.prototype.setEntityToSeatedAtSeatIdState = function(entity, seatId) {
    this.disengageEntityFromCurrentState(entity)
    entity.state.active = "seated";
    entity.state.seatId = seatId;

    var seat = view.table.getSeatBySeatNumber(seatId)
    view.seatHandler.addPlayerToSeat(entity, seat);
};

EntityState.prototype.setTableEntityToWaitingState = function() {
    var entity = entityHandler.getEntityById(view.table.entityId)
    this.disengageEntityFromCurrentState(entity)
    entity.state.active = "waiting";

    view.table.showCurrentState()

};

EntityState.prototype.setTableEntityToPlayingState = function() {
    var entity = entityHandler.getEntityById(view.table.entityId)
    this.disengageEntityFromCurrentState(entity)
    entity.state.active = "playing";
    view.table.showCurrentState()

};