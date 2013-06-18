var CircularProgressBar = function(containerId,animationManager) {
	this._initialize(containerId,animationManager);
};
CircularProgressBar.prototype = {
	containerId : null,
	secondPartCreated : false,
    slice : null,
    fill : null,
    pie : null,
    pieElement : null,
    animationManager : null,
    time : 0,
	_initialize : function(containerId,animationManager) {
        this.i = Math.random();
		if (containerId == null) {
			throw "CircularProgressBar: containerId must be set";
		}
		if (containerId.indexOf("#") != 0) {
			containerId = "#" + containerId;
		}
		var c = $(containerId);
		this.containerId = containerId;
		this._addContent();
        this.animationManager = animationManager;
	},
	show : function() {
		$(this.containerId).show();
	},

	hide : function() {
        this.running=false;
		$(this.containerId).hide();
        $(this.containerId).empty();
        if(this.pieElement!=null) {
            new Poker.CSSUtils().removeTransitionCallback(this.pieElement);
        }
        this._addContent();
	},
	_addContent : function() {
		var progressBarHTML = ''
            + '<div class="cpb-timer cpb-animated">'
                + '<div class="cpb-slice">'
                    + '<div class="cpb-pie"></div>'
				    + '<div class="cpb-pie cpb-fill" style="display:none;"></div>'
				+ '</div>'
            + '</div>';

		var backgroundHTML = '<div class="cpb-timer cpb-background">'
				    + '<div class="cpb-slice cpb-gt50">'
				        + '<div class="cpb-pie"></div>'
				        + '<div class="cpb-pie cpb-fill"></div>'
                    + '</div>'
                + '</div>';

        $(this.containerId).append(backgroundHTML).append(progressBarHTML);
        this.slice = $(".cpb-animated .cpb-slice", this.containerId);
        this.fill = $(".cpb-animated .cpb-fill", this.containerId);
        this.pie = $('.cpb-animated .cpb-pie', this.containerId);
        this.pieElement = this.pie.get(0);
	},
    animation : null,
    setTime : function(time) {
        this.time = time;
    },
	render : function() {
        var self = this;
        var time = this.time;
        this.animation = new Poker.TransformAnimation(this.pieElement);
        this.animation.addTransition("transform",(time/2000),"linear")
            .addStartRotate(0)
            .addRotate(180).addCallback(function(){
                self.slice.addClass("cpb-gt50");
                self.fill.show();
            }).next().addTransition("transform",(time/2000),"linear")
                .setTimed(true)
                .addStartRotate(180)
                .addRotate(360);
        this.animation.setTimed(true);
        this.animation.start(this.animationManager);



    },
    detach : function() {
        if(this.animation!=null) {
            this.animationManager.removeAnimation(this.animation);
        }
        $(this.containerId).empty();
    }
};