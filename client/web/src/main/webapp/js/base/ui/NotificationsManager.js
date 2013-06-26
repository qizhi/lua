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
        var notification = notification.toGritterNotification();
        var baseNotification = {
            position:'top-right',
            time : opts.time,
            class_name: opts.class_name
        };

        notification = $.extend(baseNotification,notification);
        var nid = $.gritter.add(notification);

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
    actions : null,
    init : function() {
        this.created = new Date();
        this.actions = [];
    },
    addAction : function(text,callback) {
        this.actions.push(new Poker.NotificationAction(text,callback));
    },
    toGritterNotification : function() {

    }
});
Poker.TextNotifcation = Poker.Notification.extend({
    title : null,
    text : null,
    imageUrl : null,
    init : function(title,text,imageUrl) {
        this._super();
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
    },
    toGritterNotification : function() {
        return { text : this.text, title : this.title, image : this.imageUrl };
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