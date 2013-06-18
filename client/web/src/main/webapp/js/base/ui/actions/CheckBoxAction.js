"use strict";
var Poker = Poker || {};

Poker.CheckboxAction = Class.extend({
    checkbox : null,
    container : null,
    enabled : false,
    onChangeFunction : null,

    init : function(view,container,checked) {
        this.enabled = checked;
        this.container = $(container,view);
        this.checkbox = this.container.find("input");
        this.setEnabled(checked);
    },
    _bind : function( ){
        var self = this;
        this.checkbox.on("change",function(){
            if($(this).is(":checked")) {
                self.enabled = true;
            } else {
                self.enabled = false;
            }
            if(self.onChangeFunction!=null) {
                console.log("calling change function");
                self.onChangeFunction(self.enabled);
            }
            $.ga._trackEvent("use_checkbox", self.checkbox["selector"], self.enabled);
            console.log("enabled changed to " + self.enabled);
        });
    },
    hide : function() {
        this.container.hide();
    },
    show : function() {
        this.container.show();
    },
    setEnabled : function(enabled)  {
        this.checkbox.off("change");
        this.checkbox.attr("checked",enabled);
        this.enabled = enabled;
        this._bind();
    },
    onChange : function(func) {
        this.onChangeFunction = func;
    },
    isEnabled : function() {
        return this.enabled;
    }

});