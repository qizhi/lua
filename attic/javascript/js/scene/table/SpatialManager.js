SpatialManager = function() {

};

SpatialManager.prototype.setVisualEntityTransform = function(entity, posX, posY) {
    entity.spatial.transform.pos.x = posX;
    entity.spatial.transform.pos.y = posY;
    this.positionVisualEntityAtSpatial(entity);
};


SpatialManager.prototype.positionVisualEntityAtSpatial = function(entity) {

    var anchorPointId = entity.spatial.transform.anchorId;
    var posX = entity.spatial.transform.pos.x;
    var posY = entity.spatial.transform.pos.y;

    /*
     * sets the div style directly to set the new positions
     *
     */

    document.getElementById(anchorPointId).style.left = posX+"%";
    document.getElementById(anchorPointId).style.top = posY+"%";

};