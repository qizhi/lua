"use strict";

var Poker = Poker || {};



Poker.PositionEditor = Class.extend({
    elements : null,
    elementIdSeq : 0,
    selectedElement : null,
    elementsSelector : null,
    init : function(elementsSelector) {
        var self = this;
        this.elementsSelector = elementsSelector;
        this.elements = new Poker.Map();
        setTimeout(function(){
            self.initHighlight();
        },2000);

    },
    clearStyle : function(){
        if(this.selectedElement!=null) {
            this.selectedElement.get(0).style.cssText="";
        }
    },
    initHighlight : function() {

        var self = this;
        if($("#divSelector").length==0) {
            $("body").append('<div class="dev-div-selector" id="divSelector"><ul></ul></div>');
            $("body").append('<div class="dev-div-selector" id="devElementStyles" style="display:none;"><a id="devClearDiv">clear style</a><div class="style-container"></div></div>');
        }

        $("#devClearDiv").click(function(e){
            self.clearStyle();
        });

        $(document).bind('keydown',function(e){


            $("#devElementStyles").show();
            var moveX = 0;
            var moveY = 0;
            if(e.keyCode == 38) {
                //upp
                moveY= -0.5;

            } else if(e.keyCode == 39) {
                //right
                moveX = 0.5;
            }
            else if(e.keyCode == 40) {
                //down
                moveY = 0.5;
            }
            else if(e.keyCode == 37) {
                //left
                moveX = -0.5;
            } else if(e.keyCode == 27) {    //esc

                $("#divSelector").hide();
                if(self.selectedElement!=null) {
                    self.selectedElement.removeClass("dev-style-selected");
                    self.selectedElement = null;
                }
                return;
            }

            if(self.selectedElement==null) {
                return null;
            }

            var left = self.selectedElement.css("left").replace("%","");
            var top = self.selectedElement.css("top").replace("%","");
            var right = self.selectedElement.css("right").replace("%","");
            var bottom = self.selectedElement.css("bottom").replace("%","");
            $("#devElementStyles .style-container").empty();
            if(moveX!=0) {
                if(typeof(left)!="undefined" && left!="auto") {
                    left=parseFloat(left);
                    self.selectedElement.css("left",(left+moveX) + "%");

                } else {
                    right=parseFloat(right);
                    self.selectedElement.css("right",(right-moveX) + "%");

                }
            }
            if(moveY!=0) {
                if(typeof(top)!="undefined" && top!="auto") {
                    var newTop = top=parseFloat(top) + moveY;
                    self.selectedElement.css("top",newTop + "%");

                } else {
                    right=parseFloat(bottom);
                    self.selectedElement.css("bottom",(bottom-moveY) + "%");

                }
            }

            self.addAttr("left");
            self.addAttr("right");
            self.addAttr("top");
            self.addAttr("bottom");


        });

        $(this.elementsSelector).click(function(e){
            console.log(e);
            var x = e.pageX;
            var y = e.pageY;
            $("#divSelector").show();
            self.elements = new Poker.Map();
            var children = $(self.elementsSelector).children();

            self.storeChildren(children,x,y);

            var selectedElements = self.elements.values();
            var selector =  $("#divSelector ul");
            selector.empty();
            $.each(selectedElements,function(i,element){
                var li = $("<li>").append(element.attr("id")+ ".["+element.attr("class")+"]");
                selector.append(li);

                li.click(function(){

                    if(self.selectedElement!=null) {
                        self.selectedElement.removeClass("dev-style-selected");
                    }
                    self.selectedElement = element;
                    element.addClass("dev-style-selected");
                    $("#divSelector").hide();
                });
            });
            $("#divSelector").css({top:y,left:x});


        });


    },
    storeChildren : function(elements,x,y){
        var self = this;
        $.each(elements,function(i,el){
            el = $(el);

            if(self.isElementAt(el,x,y)){
                var id = el.attr("id");
                if(typeof(id)=="undefined") {
                    id = "devel-"+self.elementIdSeq++;
                    el.attr("id",id);
                }
                self.elements.put(id,el);
            }

            var children = el.children();

            if(children.length>0) {
                self.storeChildren(children,x,y);
            }
        });
    },
    isElementAt : function(el,x,y){
        el = $(el);
        var offset = el.offset();
        var position = el.css("position");
        if(position=="absolute"){
            if(x>=offset.left && x<=(offset.left+el.outerWidth())){
                if(y>=offset.top && y<=(offset.top+el.outerHeight())) {
                    return true;
                }
            }
        }
        return false;

    },
    addAttr : function(attr){
        if(this.selectedElement.css(attr)!="auto") {
            $("#devElementStyles .style-container").append($("<span/>").append(attr+":"+this.selectedElement.css(attr)+";"));
        }

    }
});