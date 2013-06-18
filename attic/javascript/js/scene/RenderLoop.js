RenderLoop = function() {

};

RenderLoop.prototype.initRenderLoop = function(frameTime) {
    var renderLoop = setInterval(function() {
        var currentTime = new Date().getTime();
        view.renderLoop.render(currentTime)
    }, frameTime)

};

RenderLoop.prototype.render = function(currentTime) {
    view.seatHandler.tick(currentTime);
    userInput.tick(currentTime);
    animator.tick(currentTime);
};