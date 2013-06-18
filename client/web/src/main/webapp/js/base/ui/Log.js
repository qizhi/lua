var Poker = Poker || {};

Poker.Log = Class.extend({

    logContainer : null,
    templateManager : null,
    messageCount : 0,
    maxMessageCount: 100,
    init : function(logContainer) {
        this.logContainer = logContainer;
        this.templateManager = Poker.AppCtx.getTemplateManager();
    },

    append : function(message){

        var scrollDown = false;
        if(Math.abs(this.logContainer[0].scrollHeight - this.logContainer.scrollTop() - this.logContainer.outerHeight())<10) {
            scrollDown = true;
        }

        this.logContainer.append(message);

        if(scrollDown==true) {
            this.scrollDown();
        }
        this.messageCount++;
        if(this.messageCount>this.maxMessageCount) {
            $(this.logContainer.children().get(0)).remove();
            this.messageCount--;
        }
    },
    scrollDown : function() {
        this.logContainer.scrollTop(this.logContainer[0].scrollHeight);
    },
    appendTemplate : function(templateId,data) {
        var html = this.templateManager.render(templateId,data);
        this.append(html);
    }
});