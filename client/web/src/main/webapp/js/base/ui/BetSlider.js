"use strict";
var Poker = Poker || {};

/**
 * BetSlider used to select the bet/raise amount
 *
 * You have to set minBet and maxBet. As default start value of the slider is 0
 * you can however not select a value less then minBet, it starts at 0 to give the
 * user an idea how big the min bet is
 *
 * You can add new Markers to the slider by calling Poker.BetSlider.addMarker.
 * When adding a marker the slider will check if there's a value close
 * to the new value you're adding if there is it will not be added to prevent
 * overlapping labels. How close they can be can be set by changing
 * Poker.BetSlider.delta (in percent 0-100)
 *
 * @type {Poker.BetSlider}
 */
Poker.BetSlider = Class.extend({
    minBet : 0,
    maxBet : 0,
    bigBlind : 0,
    delta : 5,
    markers : null,
    slider : null,
    valueOutputs : null,
    containerId : null,
    tableId : 0,
    betInput : null,
    currentBetAmount : 0,
    triggerChange : true,
    betCallback : null,
    step : 1,
    init : function(tableId,containerId,betCallback,step) {
       this.markers = [];
       this.valueOutputs =  $(".slider-value");
       this.containerId = containerId + "-" + this.tableId;
       this.tableId = tableId;
       this.betCallback = betCallback;
       this.step = step;
    },
    /**
     * Draws the bet slider in the container with the id
     * supplied in the constructor, it will remove any existing element with that id
     * and create a new one
     */
    draw : function() {
        var self = this;
        this.currentBetAmount = this.minBet;
        var container = $("#"+this.containerId);
        container.remove();
        container = $("<div/>").attr("id",this.containerId).addClass("poker-slider");
        var betInputId = "betInput"+this.tableId;
        $("#"+betInputId).off().remove();

        var betInput = $("<input/>").attr("id",betInputId).attr("type","text").addClass("bet-input");

        $("#tableView-"+this.tableId).append(container).append(betInput);
        this.betInput = $("#"+betInputId);
        this.betInput.on("keyup",function(e){
            var val = parseFloat($.trim($(this).val()));
            if(self.betAmountInRange(val)) {
                self.handleChangeValue(self.betInput,val);
                if(e.keyCode == 13) {
                    self.betCallback();
                }
            }
        });
        this.betInput.on("blur",function(){
            self.betInput.on("click",function(){
                $(this).off("click");
                $(this).select();
            });
        });
        this.betInput.blur();



        var sliderMouseDown = function (e) { // disable clicks on track
            var sliderHandle =  self.slider.find('.ui-slider-handle');
            if (e.target != sliderHandle[0]) {
                e.stopImmediatePropagation();
                e.stopPropagation();
                e.preventDefault();
                var val = self.slider.slider("value");
                if(e.target == self.slider.find(".ui-slider-range")[0]) {
                    val = val - self.bigBlind;
                } else {
                    val = val + self.bigBlind;
                }
                self.handleChangeValue(null,val);

            }
        };

        container.on('mousedown', sliderMouseDown).on('touchstart', sliderMouseDown);

        this.slider = container.slider({
                animate: true,
                range: "min",
                orientation: "vertical",
                value: self.minBet,
                max: self.maxBet,
                min: 0,
                step: self.step,

                //this gets a live reading of the value and prints it on the page
                slide : function(event,ui) {
                    if(self.triggerChange == true) {
                        self.handleChangeValue(self.slider,ui.value);
                    }
                },

                //this updates the hidden form field so we can submit the data using a form
                change: function(event, ui) {
                    if(self.triggerChange==true) {
                        self.handleChangeValue(self.slider,ui.value);
                    } else {
                        self.triggerChange = true;
                    }
                }

            });

        $.each(this.markers,function(i,m){
            var value = m.value;
            var marker = m.name;
            var percent = Math.round(100*(value/self.maxBet));


            var div = $("<div/>").append(marker).addClass("marker").css("bottom", percent+"%");
            container.append(div);
            div.touchSafeClick(function(e){
                self.slider.slider("value",value);
            });
        });
        this.handleChangeValue(null,this.minBet);
    },
    betAmountInRange : function(value) {
        return value>=this.minBet && value<=this.maxBet;
    },
    handleChangeValue : function(source,value) {
        if(value<this.minBet) {
            value = this.minBet;
        } else if(value>this.maxBet) {
            value = this.maxBet;
        }
        this.currentBetAmount = value;
        this.updateBetAmountDisplays(source,value);

    },
    updateBetAmountDisplays : function(source,value) {
        var formattedValue = Poker.Utils.formatCurrency(value);

        if(source!=this.betInput) {
            this.betInput.val(formattedValue.replace(",",""));
        } else {
            this.triggerChange = false;
        }
        if(source!=this.slider) {
            this.slider.slider("value",value);
        }
        this.valueOutputs.html("").append(formattedValue);
    },
    /**
     * Set min bet value of the slider
     * @param minBet
     */
    setMinBet : function(minBet) {
        this.minBet = parseFloat(minBet);
    },
    /**
     * Set max bet value of the slider, used as the sliders max value
     * @param maxBet
     */
    setMaxBet : function(maxBet){
        this.maxBet = parseFloat(maxBet);
    },
    getValue : function() {
        return this.currentBetAmount;
    },
    setBigBlind : function(bigBlind) {
        this.bigBlind = bigBlind;
    },
    /**
     * Clears the markers of the slider
     */
    clear : function() {
        this.markers = [];
    },
    /**
     * Removes the slider element from the dom
     */
    remove : function() {
      if(this.slider) {
          this.slider.slider("destroy");
          $("#"+this.containerId).remove();
      }
      if(this.betInput) {
          this.betInput.remove();
          this.betInput = null;
      }
    },
    hide : function() {
        if(this.slider) {
            this.slider.hide();
        }
        if(this.betInput) {
            this.betInput.hide();
        }
    },
    closeValueExist : function(val) {
      for(var x in this.markers) {
          var mv =  this.markers[x].value / this.maxBet;
          var valPercent = 100 * val / this.maxBet;
          if(mv<(valPercent+this.delta) && mv>(valPercent-this.delta)){
              return true;
          }
      }
      return false;
    },
    /**
     * Adds a marker to the slider, if there is a marker too close
     * tho this marker value it will be ignored
     *
     * @param name  - marker label
     * @param value - the value of the marker
     */
    addMarker : function(name, value) {
        value = parseFloat(value);
        if(value<=this.maxBet && value>=this.minBet && !this.closeValueExist(value)) {
            this.markers.push({name : name, value  : value})
        }
    }
});
