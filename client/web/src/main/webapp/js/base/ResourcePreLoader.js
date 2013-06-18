"use strict";
var Poker = Poker || {};
Poker.ResourcePreloader = Class.extend({
    completionListener : null,
    init : function(contextPath, completionListener, images) {
        this.completionListener = completionListener;
        var loader = new PxLoader();
        if(images!=null && images.length > 0) {
            for(var i = 0; i<images.length; i++) {
                if(images[i]!="") {
                    loader.addImage(contextPath + "/skins" + images[i]);
                }
            }
        } else {
            this.onComplete();
        }
        loader.addCompletionListener(function(){
            self.onComplete();
        });
        var self = this;
        loader.addProgressListener(function(e) {
            self.onProgress(e.completedCount, e.totalCount);
        });
        loader.start();
    },
    onComplete : function() {
        $("#loadingView").hide();
        this.completionListener();
        $(".loading-progressbar .progress").width("100%");
    },
    onProgress : function(completedCount, totalCount) {
        $(".loading-progressbar .progress").width((100*completedCount/totalCount) + "%");
    }
});