"use strict";
var Poker = Poker || {};

Poker.MultiTableView = Poker.TabView.extend({
    /**
     * @type {Poker.TableView[]}
     */
    tableViews : null,
    columns : 1,
    tableViewContainer : null,
    init : function() {
        this._super(null,"","M");
        this.tableViews = [];
        this.fixedSizeView = true;
        this.tableViewContainer = $(".table-view-container");
    },
    /**
     * Adds multiple table views to this multi table view
     * @param {Poker.TableView[]} tableViews
     */
    addTableViews : function(tableViews) {
        for(var i = 0; i<tableViews.length; i++) {
            tableViews[i].hideTab();
            this.tableViews.push(tableViews[i]);
        }
        this.updateTableCount();
    },
    updateTableCount : function() {
        this.updateName("Tables (" + this.tableViews.length + ")");
    },
    addTableView : function(tableView) {
        this.tableViews.push(tableView);
        tableView.hideTab();
        this.updateTableCount();
    },
    removeTableView : function(tableId) {
        for(var i = 0; i<this.tableViews.length; i++) {
            if(this.tableViews[i].getTableId() === tableId) {
                this.tableViews[i].close();
                this.tableViews.splice(i,1);
            }
        }
        this.updateTableCount();
    },
    isEmpty : function() {
        return this.tableViews.length == 0;
    },
    calculateSize : function(maxWidth, maxHeight, aspectRatio) {
        if(!this.isActive()) {
            return;
        }
        var views = this.tableViews;

        var count = this.tableViews.length;
        var columns = this.getColumns(count);
        this.columns = columns;
        var rows = Math.ceil(count/columns);
        var vc = $(".view-container");

        var dim = Poker.Utils.calculateDimensions(maxWidth/columns, (maxHeight/rows),aspectRatio);

        this.tableViewContainer.width(dim.width*columns).height(dim.height)
            .css({marginLeft : (maxWidth-this.tableViewContainer.width())/2});

        for(var i = 0; i<views.length; i++) {
            var ve = views[i].getViewElement();
            ve.css({ width : dim.width, height : dim.height, fontSize : 100/columns + "%", marginLeft: 0}).show();
            views[i].layoutManager.positionDealerButton();
        }
    },
    getColumns : function(nr) {
        var vc = $(".view-container");
        var h = vc.height()*4/3.2;
        var w = vc.width();
        if(h > w) {
            if(nr < 4) return 1;
            else if(nr < 7) return 2;
            else return 3;
        } else {
            if(nr<2) return 1;
            else if(nr<5) return 2;
            else return 3;
        }
    },
    getViewElement : function() {
        var viewElements = null;
        for(var i = 0; i<this.tableViews.length; i++) {
            if(viewElements == null) {
                viewElements = $(this.tableViews[i].getViewElement());
            } else {
                viewElements.add(this.tableViews[i].getViewElement());
            }
        }
        return viewElements;
    },
    activate : function() {
        this.activateTab();
        this.active = true;
        this.tableViewContainer.addClass("multi-table");
        var views = this.tableViews;
        for(var i = 0; i<views.length; i++) {
            var ve = views[i].activate();
        }
    },
    deactivate : function() {
        this.deactivateTab();
        this.active = false;
        var views = this.tableViews;
        for(var i = 0; i<views.length; i++) {
            var ve = views[i].deactivate();
        }
    },
    close : function() {
        this.tableViewContainer.css({width:"",height:"",marginLeft:""});
        this.tableViewContainer.removeClass("multi-table");
        this.tabElement.remove();
        this.active = false;
    },
    getTableViews : function() {
        return this.tableViews;
    },
    calculateFontSize : function() {
        var targetFontSize =  Math.round(90* this.getViewElement().width()/this.baseWidth) ;
        if(targetFontSize>130) {
            targetFontSize=130;
        }
        var views = this.tableViews;
        for(var i = 0; i<views.length; i++) {
            var ve = views[i].getViewElement().css({fontSize : targetFontSize+"%"});
        }
    }

});