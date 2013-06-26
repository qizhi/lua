var Poker = Poker || {};
Poker.NotificationsManager = Class.extend({
    notifications : null,
    /**
     * @type {Poker.TemplateManager}
     */
    templateManager : null,
    init : function() {
        this.templateManager = Poker.AppCtx.getTemplateManager();
        this.notifications = [];
    },
    notify : function(notification,opts) {
        opts = $.extend({
            time: 5000,
            class_name : 'gritter-dark'
        },opts||{});
        this.notifications.push(notification);
        this.displayNotification(notification,opts);

    },
    displayNotification : function(notification,opts) {
        var t = this.templateManager.getRenderTemplate("notificationTemplate");

        var notificationHTML = t.render({ text : notification.text });
        var nid = $.gritter.add({
            title: notification.title,
            text: notificationHTML,
            position:'top-right',
            time : opts.time,
            class_name: opts.class_name
        });

        var container = $("#gritter-item-"+nid).find(".notification-actions");

        $.each(notification.actions,function(i,a){
            var act = $("<a/>").addClass("notification-action").append(a.text).click(function(){
                a.callback();
                $.gritter.removeAll();
            });
            container.append(act);
        });
        console.log("NIIIID = " + nid);

        console.log(notification);
    }


});

Poker.Notification = Class.extend({
    created : null,
    title : null,
    text : null,
    actions : null,
    init : function(title,text) {
        this.created = new Date();
        this.title = title;
        this.text = text;
        this.actions = [];
    },
    addAction : function(text,callback) {
        this.actions.push(new Poker.NotificationAction(text,callback));
    }
});
Poker.NotificationAction = Class.extend({
    text : null,
    callback : null,
    init : function(text,callback) {
        this.text = text;
        this.callback = callback;
    }
});
