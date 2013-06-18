BetSlider = function() {
    this.minBet = 0;
    this.maxBet = 0;
    this.markers = [];
}

BetSlider.prototype.draw = function(parent) {
    parent.slider({
        animate: true,
        range: "min",
        orientation: "vertical",
        value: this.minBet / 100,
        max: this.maxBet / 100,
        min: this.minBet / 100,
        step: 0.5,

        //this gets a live reading of the value and prints it on the page
        slide: function( event, ui ) {
            console.log("Slide: " + ui.value);
             $("#sliderValue").html("$" + ui.value + "<br>");
        },

        //this updates the hidden form field so we can submit the data using a form
        change: function(event, ui) {
            console.log("Change: "+ui.value);
            $("#sliderValue").html("$" + ui.value + "<br>");
        }

    });

    for(var marker in this.markers) {
        var value = this.markers[marker];
        var percent = Math.round((100 * value) / this.maxBet);
        console.log("VALUE: " + value + " PERCENT: " + percent + " MAX: " + this.maxBet);
        position = 100 - percent;
        console.log("Add marker: " + marker + " -> "+value+" @ "+position+"%");

        var div = jQuery("<div>"+marker+"</div>")
        div.addClass("marker");
        div.css("top", position+"%");

        div.appendTo(parent);

        // parent.append("<div class='marker'>"+marker+"</div>");
    }
    $("#sliderValue").html("$" + (this.minBet / 100) + "<br>");
}

BetSlider.prototype.setMinBet = function(minBet) {
    console.log("Set min bet: "+minBet);
    this.minBet = minBet;
}


BetSlider.prototype.setMaxBet = function(maxBet) {
    console.log("Set max bet: "+maxBet);
    this.maxBet = maxBet;
}

BetSlider.prototype.addMarker = function(name, value) {
    this.markers[name] = value;
}