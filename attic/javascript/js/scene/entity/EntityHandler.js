EntityHandler = function() {
    this.entities = {};
};

EntityHandler.prototype = new EntityState();

EntityHandler.prototype.addEntity = function(id) {
    var entity = {};
    entity.id = id;
    entity.state = {};
    this.entities[id] = entity;
    return entity;
};

EntityHandler.prototype.addSpatial = function(parentDivId, entity, posX, posY) {
    // Spatial use case expects to always be a div

    var transform = {
        anchorId:entity.id + "_anchor",
        pos: {
            x:posX,
            y:posY
        }
    };

    uiElementHandler.createDivElement(parentDivId, transform.anchorId, "", "anchor", null);

    var spatial = {};
    spatial.transform = transform;
    entity.spatial = spatial;

};

EntityHandler.prototype.addUiComponent = function(entity, html ,styleClass, clickFunction, parentDiv) {

    entity.ui = {};
    entity.ui.divId = entity.id+"_divId";

    if (!parentDiv) parentDiv = "body";

    uiElementHandler.createDivElement(parentDiv, entity.ui.divId, html, styleClass, clickFunction);


};

EntityHandler.prototype.getEntityById = function(id) {
    var entity = this.entities[id];
    return entity;
};

EntityHandler.prototype.updateEntities = function() {
    for (index in this.entities) {
        var entity = this.entities[index];
        if (entity.animation) {
             client.animationHandler.animateEntity(entity);
        }
    }
};

