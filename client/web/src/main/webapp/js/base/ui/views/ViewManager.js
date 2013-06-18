var Poker = Poker || {};
/**
 * In charge for the main views.
 * Handles the following:
 *  * Showing/hiding views
 *  * Setting view sizes for fixed sized views (full window views, such as the tale view)
 *  * Swiping between views
 *  * Tabs
 * @type {Poker.ViewManager}
 */
Poker.ViewManager = Class.extend({
    currentId : 0,
    views : null,
    activeView : null,
    tabsContainer: null,
    lobbyView : null,
    loginView : null,
    cssAnimator : null,
    swiper : null,
    toolbar : null,
    mobileDevice : false,
    portrait : false,
    multiTableView : null,
    tabsElement : null,
    userPanel : null,
    userOverlay : null,
    userPanel : null,
    init : function(tabsContainerId) {
        var self = this;
        this.tabsContainer = $("#"+tabsContainerId);
        this.tabsElement = $(".tabs-container");
        this.userPanel = $(".user-panel");
        this.views = [];
        this.loginView = this.addView(new Poker.LoginView("#loginView","Login"));
        this.loginView.baseWidth=440;
        this.lobbyView = this.addView(new Poker.TabView("#lobbyView",i18n.t("tabs.lobby"),"L"));
        this.cssAnimator = new Poker.CSSUtils();
        this.toolbar = $("#toolbar");
        this.activateView(this.loginView);

        $(window).resize(function(){
            self.setViewDimensions();
        });
        $(document).ready(function(){
           self.setViewDimensions();
        });

        this.checkMobileDevice();
        var self = this;
        $(".multi-view-switch").off().on("click",function(){
            self.toggleMultiTableView();
            if(self.multiTableView == null) {
                $(this).addClass("multi");
            } else {
                $(this).removeClass("multi");
            }
        });
    },

    toggleMultiTableView : function() {

        if(this.multiTableView == null) {
            var tableViews = this.getTableViews();
            this.removeTableViews();
            this.multiTableView = new Poker.MultiTableView();
            this.multiTableView.addTableViews(tableViews);

            this.activeView = null;
            this.addView(this.multiTableView);
            this.activateView(this.multiTableView);


        } else {
            var views = this.multiTableView.getTableViews();
            this.closeMultiTableView();
            for(var i = 0; i<views.length; i++) {
                this.addView(views[i]);
                views[i].showTab();
                views[i].deactivate();
            }
            if(views.length>0) {
                this.activateView(views[0]);
            }

        }

        this.setViewDimensions();

    },
    closeMultiTableView : function() {
        this.removeView(this.multiTableView);
        this.multiTableView.close();
        this.multiTableView = null;
        $(".multi-view-switch").removeClass("multi");
    },
    removeTableViews : function() {

        for(var i = this.views.length-1; i>=0; i--) {
            var v = this.views[i];
            if(v instanceof Poker.TableView) {
                this.views.splice(i,1);
            }
        }
    },
    getTableViews : function() {
        var tableViews = [];
        for(var i = 0; i<this.views.length; i++) {
            var v = this.views[i];
            if(v instanceof Poker.TableView) {
                tableViews.push(v);
            }
        }
        return tableViews;
    },
    checkMobileDevice : function() {
        if(window.matchMedia) {
            var mq1 = window.matchMedia("(max-width:700px)");
            var mq2 = window.matchMedia("(max-height: 400px)");
            if(mq1.matches || mq2.matches) {
                this.mobileDevice = true;
            } else {
                this.mobileDevice = false;
            }
        } else {
            this.mobileDevice = false;
        }
        this.portrait = $(window).height() < $(window).width();
    },
    /**
     * Gets the next view null if there are no more views
     * @return {Poker.View}
     */
    getNextView : function() {
        for(var i = 0; i<this.views.length; i++) {
            if(this.views[i].id == this.activeView.id) {
                if(i==(this.views.length-1)){
                    return null;
                } else {
                    return this.views[i+1];
                }
            }
        }
        return null;
    },
    /**
     * Activates the next view
     */
    nextView : function() {
        this.activateView(this.getNextView());
    },
    onForceLogout : function() {
        $(".view-container").hide();
        $("#toolbar").hide();
        Poker.AppCtx.getDialogManager().displayGenericDialog(
            {   translationKey : "force-logged-out",
                okButtonText:"Reload" }, function(){
                document.location.reload();
            });
    },
    /**
     * Gets the previous view, null if there are no previous view
     * @return {Poker.View}
     */
    getPreviousView : function() {
        for(var i = 0; i<this.views.length; i++) {
            if(this.views[i].id == this.activeView.id) {
                if(i==0) {
                    return null;
                } else {
                    return this.views[i-1];
                }
            }
        }
        return null;
    },
    /**
     * Activates the previous view
     */
    previousView : function() {
        this.activateView(this.getPreviousView());
    },
    /**
     * Return a unique view id
     * @return {Number}
     */
    nextId : function() {
        return this.currentId++;
    },
    /**
     * Called when user log in.
     * Shows the appropriate views and menus
     */
    onLogin : function(){
        var self = this;
        this.toolbar.show();
        if(!this.loginView.isClosed()) {
            this.activateView(this.lobbyView);
            this.loginView.close();
            this.views.splice(0,1);
        }


    },
    /**
     * Will change a views tab to get the users attention
     * @param tableId - the id for the table who's view to request focus for
     */
    requestTableFocus : function(tableId) {
       var v = this.findViewByTableId(tableId);
       if(v!=null) {
           v.requestFocus();
       }
    },
    updateTableInfo : function(tableId,data)  {
        var v = this.findViewByTableId(tableId);
        if(v!=null) {
            v.updateInfo(data);
        }
    },
    /**
     * Removes a table view and activates the previous view
     * @param tableId - the id for the table who's view to close
     */
    removeTableView : function(tableId) {
        if(this.multiTableView!=null) {
            this.multiTableView.removeTableView(tableId);
            if(this.multiTableView.isEmpty()) {
                var pv = this.getPreviousView();
                this.closeMultiTableView();
                $(".multi-view-switch").addClass("multi");
                $(".table-view-container").hide();
                this.activeView = null;
                this.safeActivateView(pv);
            }
        } else {
            for(var i = 0; i<this.views.length; i++) {
                var v = this.views[i];
                if(typeof(v.getTableId)!="undefined" && v.getTableId()==tableId) {
                    var pv = this.getPreviousView();
                    v.close();
                    $(".table-view-container").hide();
                    this.views.splice(i,1);
                    this.activeView = null;
                    this.safeActivateView(pv);

                }
            }
        }
        this.updateTableTabIndexes();
        this.setViewDimensions();
    },
    removeView : function(view) {
        for(var i = 0; i<this.views.length; i++) {
            var v = this.views[i];
            if(v.id === view.id) {
                var pv = this.getPreviousView();
                v.close();
                this.views.splice(i,1);
                this.activeView = null;
                this.safeActivateView(pv);
            }
        }
    },
    /**
     * Activates the previous view, if it is null it activates the lobby
     * @param pv
     */
    safeActivateView : function(pv) {
        if(pv!=null) {
            this.activateView(pv);
        } else {
            this.activateView(this.lobbyView);
        }
    },
    removeTournamentView : function(tournamentId) {
        for(var i = 0; i<this.views.length; i++) {
            var v = this.views[i];
            if(v instanceof Poker.TournamentView && v.getTournamentId()==tournamentId) {
                var pv = this.getPreviousView();
                v.close();
                this.views.splice(i,1);
                this.activeView = null;
                this.safeActivateView(pv);
            }
        }
    },
    /**
     * Activates a Poker.TableView by it's table id
     * @param tableId
     */
    activateViewByTableId : function(tableId) {
        if(this.multiTableView!=null) {
            this.activateView(this.multiTableView);
        } else {
            var v = this.findViewByTableId(tableId);
            if(v!=null) {
                this.activateView(v);
            }
        }

    },
    activateViewByTournamentId : function(tournamentId) {
        var v = this.findViewByTournamentId(tournamentId);
        if(v!=null) {
            this.activateView(v);
        }
    },
    findViewByTournamentId : function(tournamentId) {
        for(var i = 0; i<this.views.length; i++) {
            var v = this.views[i];
            if(v instanceof Poker.TournamentView && v.getTournamentId()==tournamentId) {
                return v;
            }
        }
    },
    /**
     * Find a TableView by it's table id
     * @param tableId
     * @return {Poker.TableView}
     */
    findViewByTableId : function(tableId) {
        for(var i = 0; i<this.views.length; i++) {
            var v = this.views[i];
            if(typeof(v.getTableId)!="undefined" && v.getTableId()==tableId) {
                return v;
            }
        }
        return null;
    },
    /**
     * Adds a Poker.TableView and activates it
     * @param tableLayoutManager - the layout manager to handle the UI for this view
     * @param name - name of the view, to be displayed on the tab
     */
    addTableView : function(tableLayoutManager,name) {
        if(this.multiTableView == null) {
            var view = this.addView(new Poker.TableView(tableLayoutManager,name));
            view.fixedSizeView = true;
            this.activateView(view);
            this.updateTableTabIndexes();
        }  else {
            var view = this.prepareView(new Poker.TableView(tableLayoutManager,name));
            this.multiTableView.addTableView(view);
            this.activateView(this.multiTableView);
        }
        var self = this;
        this.setViewDimensions();
    },
    updateTableTabIndexes : function() {
        var tableViews = this.getTableViews();
        for(var i = 0; i<tableViews.length; i++) {
            tableViews[i].updateTabIndex(i+1);
        }
    },
    addTournamentView : function(viewElementId,name,layoutManager) {
        var view = this.addView(new Poker.TournamentView(viewElementId, name, layoutManager));
        this.activateView(view);
    },
    /**
     * Sets the dimensions of all views that are set to fixedSizedViews
     * and updates the body's font-size. Usually called on resize window event
     */
    setViewDimensions : function(){

        this.checkMobileDevice();

        var w = $(window);

        //iphone - statusbar = 8/5
        var maxAspectRatio = 4/3.2;
        if(this.mobileDevice) {
            maxAspectRatio = 4/3;
            this.tabsElement.width("auto");
            this.tabsElement.find("li").css("width","");
        } else {
            var count = this.getVisibleTabCount();
            var userPanelWidth = this.userPanel.outerWidth();
            this.tabsElement.width(( $(window).width() - 55 - userPanelWidth ) + "px");
            this.tabsElement.find("li").width((100/count) + "%");
        }
        var views = this.views;
        var topMargin = 40;
        var leftMargin = 0;

        if(this.mobileDevice==true && this.portrait == true) {
            leftMargin = 40;
            topMargin = 0;
            $("body").addClass("portrait");
        } else {
            $("body").removeClass("portrait");
        }

        for(var i = 0; i<views.length; i++) {
            views[i].calculateSize(w.width()-leftMargin, w.height()-topMargin, maxAspectRatio);
            views[i].calculateFontSize();
        }

        this.calculateSettingsFontSize();
    },
    calculateSettingsFontSize : function() {

        var targetFontSize =  Math.round(90* $(window).width()/1024);
        if(targetFontSize>125) {
            targetFontSize=125;
        }
        $(".config-view").css({fontSize : targetFontSize+"%"});
        $(".main-menu-container").css({fontSize : targetFontSize+"%"});
    },
    /**
     * Activate a view.
     * Will:
     *  * Highlight the views tab.
     *  * Hide the previously active view
     *  * Set up the swipe-to-change-tab elements
     * @param view
     */
    activateView : function(view) {
        if(this.activeView!=null) {
            this.activeView.deactivate();
        }
        this.activeView = view;
        view.activate();
        view.showTab();

        if(view.fixedSizeView==true){
            $(".view-port").scrollTop(0).css("overflow-y","hidden");
        } else {
            $(".view-port").css("overflow-y","");
        }
        this.setViewDimensions();
        if(this.swiper!=null) {
            this.swiper.setElements(
                this.getPreviousView(),
                this.getActiveView(),
                this.getNextView()
            );
        }

    },
    /**
     * Retrieves the current active view
     * @return {Poker.View}
     */
    getActiveView : function() {
        return this.activeView;
    },
    prepareView : function(view) {
        view.id = this.nextId();
        this.tabsContainer.append(view.tabElement);
        var self = this;
        view.tabElement.off().touchSafeClick(function(e){
            self.activateView(view);
        });
        view.showTab();
        return view;
    },
    /**
     * Adds and activates a view
     * @param view
     * @return {Poker.View}
     */
    addView : function(view) {
        if(view.id==null) {
            this.prepareView(view);
        }
        this.views.push(view);
        return view;
    },
    /**
     * Get the the nr of views that has a visible tab
     * @return {Number}
     */
    getVisibleTabCount : function() {
        var count = 0;
        for(var i = 0; i<this.views.length;i++) {
            if(this.views[i].selectable == true) {
                count++;
            }
        }
        return count;
    }
});
