"use strict";
var Poker = Poker || {};
Poker.HandHistoryLayout = Class.extend({
    handIdsTemplate : null,
    handLogTemplate : null,
    handHistoryViewTemplate : null,
    container : null,
    tableId : null,
    view : null,
    closeFunction : null,
    logContainer : null,
    pagingContainer : null,
    init : function(viewContainer,tableId,closeFunction) {
        var templateManager = Poker.AppCtx.getTemplateManager();
        this.handHistoryViewTemplate = templateManager.getRenderTemplate("handHistoryViewTemplate");
        this.handIdsTemplate = templateManager.getRenderTemplate("handHistoryIdsTemplate");
        this.handLogTemplate = templateManager.getRenderTemplate("handHistoryLogTemplate");
        this.tableId = tableId;
        this.prepareUI(tableId);
        this.closeFunction = closeFunction;
    },
    prepareUI : function(tableId) {
        var self = this;

        var containerId = "#handHistoryView"+tableId;
        var html = this.handHistoryViewTemplate.render({id:tableId});

        $(".view-container").append(html);
        this.container = $(containerId);

        this.container.find(".next").click(function(e){
            var next = self.container.find(".hand-ids ul .active").next();
            if(next.length>0) {
                next.click();
            }
        });

        this.container.find(".previous").click(function(e){
            var prev = self.container.find(".hand-ids ul .active").prev();
            if(prev.length>0) {
                prev.click();
            }
        });

        this.pagingContainer = this.container.find(".paging-container");

        this.container.find(".close-button").click(function(e){
            self.close();
        });
        this.logContainer = this.container.find(".hand-log");
        this.view = new Poker.TabView(containerId,"Hand History", "H");
        Poker.AppCtx.getViewManager().addView(this.view);
        Poker.AppCtx.getViewManager().activateView(this.view);
    },
    activate : function() {
        Poker.AppCtx.getViewManager().activateView(this.view);
    },
    close : function() {
        Poker.AppCtx.getViewManager().removeView(this.view);
        this.closeFunction(this.tableId);
    },
    showHandSummaries : function(summaries) {
        var self = this;
        var container = this.container;
        var html = this.handIdsTemplate.render({summaries : summaries});
        var handIdsContainer = container.find(".hand-ids");
        handIdsContainer.html(html);
        if(summaries.length>0) {
            container.find(".hand-log").show();

            $.each(summaries,function(i,e){
                var item = $("#hand-"+ e.id);
                item.click(function(el){
                    if(self.pagingContainer.offset().top > $(window).height()) {
                        $("body").scrollTop(self.pagingContainer.offset().top);
                    }
                    new Poker.HandHistoryRequestHandler(self.tableId).requestHand(e.id);
                });

            });
            new Poker.HandHistoryRequestHandler(this.tableId).requestHand(summaries[0].id);
            container.find(".no-hands").hide();
        } else {
            container.find(".no-hands").show();
            container.find(".hand-log").empty().hide();
        }

    },
    showHand : function(hand) {
        var log = this.container.find(".hand-log");
        this.container.find(".hand-ids .active").removeClass("active");
        this.container.find("#hand-"+hand.id).addClass("active");
        log.empty();
        log.append(this.handLogTemplate.render(hand));

    }

});