<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML>
<html>
<head>
    <title></title>

    <meta name="viewport" content="width=device-width, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="default">

    <link rel="apple-touch-icon" href="${cp}/skins/${skin}/images/lobby/icon.png" />
    <link rel="stylesheet" type="text/css" href="${cp}/skins/default/css/gritter/css/jquery.gritter.css"/>

    <link id="defaultSkinCss" rel="stylesheet/less" type="text/css" href="${cp}/skins/default/less/base.less" />

    <!-- All less files are imported in this base.less-->
    <link id="skinCss" rel="stylesheet/less" type="text/css" href="${cp}/skins/${skin}/less/base.less" />

    <c:if test="${not empty cssOverride}">
        <link id="overrideSkinCss" rel="stylesheet/less" type="text/css" href="${cssOverride}" />
    </c:if>

    <script type="text/javascript" src="${cp}/skins/${skin}/skin-config.js"></script>
    <script type="text/javascript" src="${cp}/skins/${skin}/preload-images.js"></script>

    <script type="text/javascript"  src="${cp}/js/lib/less-1.3.0.min.js"></script>

    <script type="text/javascript" src="${cp}/js/lib/classjs.js"></script>
    <script type="text/javascript" src="${cp}/js/lib/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="${cp}/js/lib/jquery-ui-1.8.21.custom.min.js"></script>
    <script type="text/javascript" src="${cp}/js/lib/jquery.ui.touch-punch.js"></script>
    <script type="text/javascript" src="${cp}/js/base/jquery-plugins/touch-click.js"></script>
    <script type="text/javascript" src="${cp}/js/base/jquery-plugins/relative-offset.js"></script>
    <script type="text/javascript" src="${cp}/js/lib/moment.js"></script>

    <script type="text/javascript" src="${cp}/js/lib/handlebars.js"></script>
    <script type="text/javascript" src="${cp}/js/lib/json2.js"></script>

    <script type="text/javascript" src="${cp}/js/base/ui/CircularProgressBar.js"></script>

    <script src="${cp}/js/lib/cubeia/firebase-js-api-1.9.2-CE-javascript.js" type="text/javascript"></script>
    <script src="${cp}/js/lib/cubeia/firebase-protocol-1.9.2-CE-javascript.js" type="text/javascript"></script>
    <script src="${cp}/js/lib/poker-protocol-1.0-SNAPSHOT.js" type="text/javascript"></script>
    <script src="${cp}/js/lib/routing-service-protocol-1.0-SNAPSHOT.js" type="text/javascript"></script>
    <script src="${cp}/js/lib/quo.js" type="text/javascript"></script>
    <script src="${cp}/js/lib/i18next-1.6.0.js" type="text/javascript"></script>
    <script src="${cp}/js/lib/jquery.gritter.js" type="text/javascript"></script>


    <script type="text/javascript" src="${cp}/js/lib/PxLoader-0.1.js"></script>
    <script type="text/javascript" src="${cp}/js/lib/PxLoaderImage-0.1.js"></script>

    <script type="text/javascript" src="${cp}/js/lib/purl.js"></script>

    <script src="${cp}/js/base/Utils.js" type="text/javascript"></script>
    <script src="${cp}/js/base/ProtocolUtils.js" type="text/javascript"></script>

    <script type="text/javascript" src="${cp}/js/base/data/Map.js"></script>
    <script type="text/javascript" src="${cp}/js/base/PeriodicalUpdater.js"></script>
    <script type="text/javascript" src="${cp}/js/base/OperatorConfig.js"></script>
    <script type="text/javascript" src="${cp}/js/base/MyPlayer.js"></script>
    <script type="text/javascript" src="${cp}/js/base/PlayerTableStatus.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/NotificationsManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/AchievementManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/communication/achievement/AchievementPacketHandler.js"></script>


    <script src="${cp}/js/base/communication/poker-game/ActionUtils.js" type="text/javascript"></script>
    <script src="${cp}/js/base/communication/poker-game/PokerPacketHandler.js" type="text/javascript"></script>
    <script src="${cp}/js/base/communication/poker-game/PokerRequestHandler.js"  type="text/javascript"></script>
    <script src="${cp}/js/base/communication/poker-game/PokerSequence.js" type="text/javascript"></script>

    <script src="${cp}/js/base/communication/lobby/LobbyPacketHandler.js" type="text/javascript"></script>
    <script src="${cp}/js/base/communication/lobby/LobbyRequestHandler.js" type="text/javascript"></script>

    <script src="${cp}/js/base/communication/handhistory/HandHistoryRequestHandler.js" type="text/javascript"></script>
    <script src="${cp}/js/base/communication/handhistory/HandHistoryPacketHandler.js" type="text/javascript"></script>
    <script src="${cp}/js/base/ui/HandHistoryLayout.js" type="text/javascript"></script>
    <script src="${cp}/js/base/HandHistoryManager.js" type="text/javascript"></script>

    <script src="${cp}/js/base/communication/connection/ConnectionManager.js" type="text/javascript"></script>
    <script src="${cp}/js/base/communication/connection/ConnectionPacketHandler.js" type="text/javascript"></script>

    <script type="text/javascript" src="${cp}/js/base/communication/tournament/TournamentPacketHandler.js"></script>
    <script type="text/javascript" src="${cp}/js/base/communication/tournament/TournamentRequestHandler.js"></script>

    <script type="text/javascript" src="${cp}/js/base/communication/table/TableRequestHandler.js"></script>
    <script type="text/javascript" src="${cp}/js/base/communication/table/TablePacketHandler.js"></script>

    <script src="${cp}/js/base/communication/CommunicationManager.js" type="text/javascript"></script>

    <script type="text/javascript" src="${cp}/js/base/communication/player-api/PlayerApi.js"></script>

    <script type="text/javascript" src="${cp}/js/base/Settings.js"></script>

    <script type="text/javascript" src="${cp}/js/base/ui/BetSlider.js"></script>
    <script type="text/javascript" src="${cp}/js/base/Action.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/actions/CheckBoxAction.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/actions/BlindsActions.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/actions/ActionButton.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/actions/AbstractTableButtons.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/actions/ActionButtons.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/actions/TableButtons.js"></script>

    <script type="text/javascript" src="${cp}/js/base/ui/MyActionsManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/data/LobbyData.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/LobbyLayoutManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/LobbyManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/Player.js"></script>
    <script type="text/javascript" src="${cp}/js/base/Table.js"></script>
    <script type="text/javascript" src="${cp}/js/base/TableManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/Clock.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/PotTransferAnimator.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/Log.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/TableEventLog.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/ChatInput.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/TableLayoutManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/TemplateManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/Seat.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/MyPlayerSeat.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/cards/Card.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/cards/CommunityCard.js"></script>
    <script type="text/javascript" src="${cp}/js/base/Pot.js"></script>
    <script type="text/javascript" src="${cp}/js/base/Hand.js"></script>
    <script type="text/javascript" src="${cp}/js/base/jquery-plugins/describe.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/CSSUtils.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/animation/Transform.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/animation/Animation.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/animation/CSSClassAnimation.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/animation/TransformAnimation.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/animation/AnimationManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/DealerButton.js"></script>

    <script type="text/javascript" src="${cp}/js/base/Navigation.js"></script>
    <script type="text/javascript" src="${cp}/js/base/sound/SoundSource.js"></script>
    <script type="text/javascript" src="${cp}/js/base/sound/SoundPlayer.js"></script>
    <script type="text/javascript" src="${cp}/js/base/sound/SoundManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/sound/SoundRepository.js"></script>
    <script type="text/javascript" src="${cp}/js/base/sound/Sounds.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/FutureActionType.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/FutureActions.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/Dialog.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/DialogManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/DisconnectDialog.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/BuyInDialog.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/TournamentBuyInDialog.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/View.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/TabView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/LoginView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/TableView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/MultiTableView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/TournamentView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/SoundSettingsView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/DevSettingsView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/ViewManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/ExternalPageView.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/MainMenuManager.js"></script>

    <script type="text/javascript" src="${cp}/js/base/ui/views/AccountPageManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ApplicationContext.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/views/ViewSwiper.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/ContextMenu.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/Sharing.js"></script>
    <script type="text/javascript" src="${cp}/js/base/tournaments/Tournament.js"></script>
    <script type="text/javascript" src="${cp}/js/base/ui/tournaments/TournamentLayoutManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/tournaments/TournamentManager.js"></script>

    <script type="text/javascript" src="${cp}/js/base/ResourcePreLoader.js"></script>

    <script type="text/javascript" src="${cp}/js/base/communication/lobby/Unsubscribe.js"></script>

    <script type="text/javascript" src="${cp}/js/base/dev/MockEventManager.js"></script>
    <script type="text/javascript" src="${cp}/js/base/dev/PositionEditor.js"></script>
    <script type="text/javascript" src="${cp}/js/base/dev/DevTools.js"></script>

    <script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=${addThisPubId}"></script>



    <c:if test="${not empty operatorId}">
        <script type="text/javascript">
            Poker.OperatorConfig.operatorId = ${operatorId};
            Poker.SkinConfiguration.operatorId = ${operatorId};
        </script>
    </c:if>
    <c:if test="${not empty token}">
        <script type="text/javascript">
            Poker.MyPlayer.loginToken = "${token}";
            $(document).ready(function(){
                $(".login-container").hide();
            });
        </script>
    </c:if>

    <script type="text/javascript">

        var contextPath = "${cp}";

        $(document).ready(function(){
            //to clear the stored user add #clear to the url
            if(document.location.hash.indexOf("clear") != -1){
                Poker.Utils.removeStoredUser();
            }

            //less.watch(); //development only
            $(".describe").describe();

            $("title").html(Poker.SkinConfiguration.title);


            var onPreLoadComplete = function() {
                <c:choose>
                    <c:when test="${not empty firebaseHost}">
                        var requestHost = "${firebaseHost}";
                    </c:when>
                    <c:otherwise>
                        var requestHost = window.location.hostname;
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${not empty firebaseHttpPort}">
                        var webSocketPort = ${firebaseHttpPort};
                    </c:when>
                    <c:otherwise>
                        var webSocketPort = 9191;
                    </c:otherwise>
                </c:choose>
                
                var webSocketUrl = requestHost ? requestHost : "localhost";

                console.log("connecting to WS: " + webSocketUrl + ":" + webSocketPort);

                //handles the lobby UI
                Poker.AppCtx.wire({
                    webSocketUrl : webSocketUrl,
                    webSocketPort : webSocketPort,
                    tournamentLobbyUpdateInterval : 10000,
                    playerApiBaseUrl : "${playerApiBaseUrl}"
                });

            };
            Handlebars.registerHelper('t', function(i18n_key) {
                var result = i18n.t(i18n_key);
                return new Handlebars.SafeString(result);
            });
            Handlebars.registerHelper('fromNow',function(date){
                return moment(parseInt(date)).fromNow();
            });
            Handlebars.registerHelper('date', function(date) {
                return moment(parseInt(date)).format("lll");
            });

            i18n.init({ fallbackLng: 'en', postProcess: 'sprintf', resGetPath: '${cp}/i18n/__lng__.json' }, function(){
                $("body").i18n();
                new Poker.ResourcePreloader('${cp}',onPreLoadComplete,  Poker.SkinConfiguration.preLoadImages);
            });

        });

    </script>

</head>
<body>
<div class="view-port">
    <div id="toolbar" style="display:none;">
        <div class="main-menu-button">
        </div>
        <div class="tabs-container">
            <ul id="tabItems" class="tabs">
            </ul>
        </div>
        <div class="user-panel">
            <div class="user-panel-name username"></div>
            <div class="user-panel-avatar"></div>
        </div>
    </div>

    <div class="toolbar-background"></div>

    <div class="main-menu-container" style="">
        <ul id="mainMenuList">

        </ul>
    </div>
    <div class="menu-overlay slidable" style="display: none;">

    </div>

    <div id="soundSettingsView" class="config-view" style="display: none;">
        <h1>Sound Settings</h1>
        <h2>Configuration</h2>
        <div class="group">
            <div class="item">
                <fieldset class="toggle">
                    <input id="soundEnabled" type="checkbox">
                    <label onclick="" for="soundEnabled">Toggle Sounds</label>
                    <span class="toggle-button"></span>
                </fieldset>
            </div>
        </div>
    </div>

    <div id="devSettingsView" class="config-view" style="display: none;">
        <h1>Development config</h1>
        <h2>Communication</h2>
        <div class="group">
            <div class="item">
                <fieldset class="toggle">
                    <input id="freezeComEnabled" type="checkbox">
                    <label onclick="" for="freezeComEnabled">Freeze communication</label>
                    <span class="toggle-button"></span>
                </fieldset>
            </div>
        </div>
        <h2>Experimental features</h2>
        <div class="group">
            <div class="item">
                <fieldset class="toggle">
                    <input id="swipeEnabled" type="checkbox">
                    <label onclick="" for="swipeEnabled">Swipe to change tabs</label>
                    <span class="toggle-button"></span>
                </fieldset>
            </div>
            <div class="item">
                <span>Something else goes here</span>
            </div>
        </div>
    </div>
    <div class="view-container slidable">
        <div class="table-view-container" style="display:none;">
            <div class="multi-view-switch multi">
            </div>
        </div>

        <div id="loadingView" class="loading-view">
            <div class="login-dialog">
                <div class="logo-container"></div>
                <div class="loading-progressbar">
                    <div class="progress"></div>
                </div>
            </div>
        </div>
        <div id="loginView" class="login-view" style="display:none;">
            <div id="dialog1" class="login-dialog">
                <div class="logo-container"></div>
                <div class="login-container">
                    <div class="login-input-container">
                        <input name="user" class="describe" id="user" type="text" title="Username" value="" />
                        <input name="pwd" class="describe" id="pwd" type="password" title="Password" value=""/>
                    </div>
                    <div id="loginButton" class="login-button">
                        <span data-i18n="login.login"></span>
                    </div>
                </div>
                <div class="status-label"> <span data-i18n="login.status"></span> <span class="connect-status"></span></div>
            </div>
        </div>

        <div id="lobbyView" class="lobby-container"  style="display:none;">
            <div id="lobby" class="lobby-list">

                <div class="left-column">
                    <div class="logo-container">
                    </div>
                    <ul class="main-menu">
                            <li>
                                <a class="selected lobby-link" id="cashGameMenu" data-i18n="lobby.menu.cash-games">
                                    [Cash Games]
                                </a>
                            </li>
                            <li><a id="sitAndGoMenu" class="lobby-link" data-i18n="lobby.menu.sit-n-gos">[Sit &amp; Go's]</a></li>
                            <li><a  id="tournamentMenu" class="lobby-link" data-i18n="lobby.menu.tournaments">[Tournaments]</a></li>
                    </ul>
                </div>
                <div class="right-column">
                    <div class="top-panel" id="table-list">
                        <div class="show-filters">
                            <a data-i18n="lobby.filters.show-filters">Show filters</a>
                        </div>
                        <div class="table-filter hidden cash-game">
                            <div class="filter-group tables">
                                <div class="filter-label" data-i18n="lobby.filters.show-tables">Show tables:</div>
                                <div class="filter-button" id="fullTables" data-i18n="lobby.filters.full">Full</div>
                                <div class="filter-button" id="emptyTables" data-i18n="lobby.filters.empty">Empty</div>
                            </div>
                            <div class="filter-group limits">
                                <div class="filter-label" data-i18n="lobby.filters.show-limits">Show Limits:</div>
                                <div class="filter-button" id="noLimit" data-i18n="lobby.filters.no-limit">NL</div>
                                <div class="filter-button" id="potLimit" data-i18n="lobby.filters.pot-limit">PL</div>
                                <div class="filter-button" id="fixedLimit" data-i18n="lobby.filters.fixed-limit">FL</div>
                            </div>
                            <div class="filter-group stakes">
                                <div class="filter-label" data-i18n="lobby.filters.stakes">Stakes:</div>
                                <div class="filter-button" id="lowStakes" data-i18n="lobby.filters.low">Low</div>
                                <div class="filter-button" id="mediumStakes" data-i18n="lobby.filters.mid">Mid</div>
                                <div class="filter-button" id="highStakes" data-i18n="lobby.filters.high">High</div>
                            </div>
                        </div>
                    </div>
                        <div class="lobby-tab"  id="tableListAnchor">
                        <div id="tableListContainer">

                        </div>
                    </div>
                </div>
            </div>

        </div>
        <div class="user-overlay-container" style="display: none;">
            <h1>Account</h1>
            <div class="account-button logout-link">
                <span data-i18n="user.log-out"></span>
            </div>
            <iframe id="accountIframe" class="account-iframe" scrolling="no"></iframe>
            <div class="account-buttons">
                <div class="account-button" id="editProfileButton">
                    Edit Profile
                </div>
                <div class="account-button" id="buyCreditsButton">
                    Buy Credits
                </div>
            </div>
        </div>

        <div class="profile-view" id="editProfileView" style="display: none;">
            <iframe class="external-view-iframe"></iframe>
            <a class="close-button">Close</a>
        </div>
        <div class="buy-credits-view" id="buyCreditsView"  style="display: none;">
            <iframe class="external-view-iframe"></iframe>
            <a class="close-button">Close</a>
        </div>
    </div>

</div>
<div id="emptySeatTemplate" style="display: none;">
    <div class="avatar-base">
        <div class="open-seat">{{t "table.open"}}</div>
    </div>
</div>

<div id="seatTemplate" style="display: none;">

    <div class="avatar-base">
        <div class="progress-bar">

        </div>
    </div>
    <div class="player-name">
        {{name}}
    </div>
    <div class="avatar">

    </div>
    <div class="cards-container cards-container-player">

    </div>

    <div class="player-status">

    </div>

    <div class="seat-balance balance">

    </div>
    <div class="action-text">

    </div>
    <div class="action-amount balance">
        <span></span>
    </div>
    <div class="hand-strength">

    </div>
</div>

<script type="text/mustache" id="playerCardTemplate" style="display: none;">
    <div id="playerCard-{{domId}}" class="player-card-container">
        <div class="card-image" id="playerCardImage-{{domId}}" style="background-image:{{backgroundImage}}"></div>
    </div>
</script>
<script type="text/mustache" id="communityCardTemplate" style="display: none;">
    <div id="communityCard-{{domId}}" class="community-card-container">
        <div class="card-image" id="communityCardImage-{{domId}}" style="background-image:{{backgroundImage}}"></div>
    </div>
</script>
<div id="mainPotTemplate" style="display: none;">
        <div class="balance pot-container-{{potId}}"><span class="pot-value pot-{{potId}}">{{amount}}</span></div>
</div>
<div id="myPlayerSeatTemplate" style="display:none;">
    <div class="player-name">
        {{name}}
    </div>

    <div class="seat-balance balance">

    </div>
    <div class="avatar-base">

    </div>
    <div class="avatar">

    </div>
    <div class="action-amount balance">

    </div>
    <div class="cards-container">

    </div>
    <div class="player-status"></div>
    <div class="action-text">

    </div>
    <div class="hand-strength">

    </div>
</div>


<script type="text/mustache" id="sitAndGoLobbyListTemplate">
    <div class="table-item-header sit-and-go">
        <div class="table-name">{{t "lobby.list.name"}}</div>
        <div class="buy-in">{{t "lobby.list.buy-in"}}</div>
        <div class="seated">{{t "lobby.list.players"}}</div>
        <div class="status">{{t "lobby.list.status"}}</div>
    </div>

    <div class="table-list-item-container">

    </div>
</script>

<script type="text/mustache" id="tournamentLobbyListTemplate">
    <div class="table-item-header tournament">
        <div class="table-name">{{t "lobby.list.name"}}</div>
        <div class="buy-in">{{t "lobby.list.buy-in"}}</div>
        <div class="registered">{{t "lobby.list.registered"}}</div>
        <div class="group">{{t "lobby.list.starting"}}</div>
    </div>

    <div class="table-list-item-container">

    </div>
</script>

<script type="text/mustache" id="tableLobbyListTemplate">
    <div class="table-item-header">
        <div class="table-name">{{t "lobby.list.name"}}</div>
        <div class="seated">{{t "lobby.list.seated"}}</div>
        <div class="blinds">{{t "lobby.list.blinds"}}</div>
        <div class="type">{{t "lobby.list.type"}}</div>
        <div class="play"></div>
    </div>

    <div class="table-list-item-container">

    </div>
</script>

<div id="tableListItemTemplate" style="display: none;">
    <div class="table-item  {{tableStatus}}" id="tableItem{{id}}">
        <div class="table-name">{{name}}</div>
        <div class="seated">{{seated}}/{{capacity}}</div>
        <div class="blinds">{{blinds}} {{currencyCode}}</div>
        <div class="type">{{type}}</div>
        <div class="play-text">&raquo;</div>
        <div class="full-text">Full</div>
    </div>
</div>
<div id="sitAndGoListItemTemplate" style="display: none;">
    <div class="table-item sit-and-go  {{tableStatus}}" id="sitAndGoItem{{id}}">
        <div class="table-name">{{name}}</div>
        <div class="buy-in">{{currency buyIn}}+{{currency fee}} {{buyInCurrencyCode}}</div>
        <div class="seated">{{registered}}/{{capacity}}</div>
        <div class="type">{{type}}</div>
        <div class="status {{status}}">{{status}}</div>
        <div class="play-text">&raquo;</div>
    </div>
</div>
<div id="tournamentListItemTemplate" style="display: none;">
    <div class="table-item tournament {{tableStatus}}" id="tournamentItem{{id}}">
        <div class="table-name">{{name}}</div>
        <div class="buy-in">{{currency buyIn}}+{{currency fee}} {{buyInCurrencyCode}}</div>
        <div class="registered">{{registered}}/{{capacity}}</div>
        <div class="group">
            <div class="start-time">{{date startTime}}</div>
            <div class="status {{status}}">{{status}}</div>
        </div>
        <div class="play-text">&raquo;</div>
    </div>
</div>
<div id="potTransferTemplate" style="display: none;">
        <div id="{{ptId}}" class="pot-transfer" style="visibility: hidden;">
        <div class="balance">{{amount}}</div>
    </div>
</div>

<script type="text/mustache" id="tableViewTemplate" style="display:none;">
    <div id="tableView-{{tableId}}" class="table-container">

        <div class="table-logo"></div>
        <div id="seatContainer-{{tableId}}" class="default-table table-{{capacity}}">
            <div class="seat" id="seat0-{{tableId}}">

            </div>
            <div class="seat" id="seat1-{{tableId}}">

            </div>
            <div class="seat" id="seat2-{{tableId}}">

            </div>
            <div class="seat" id="seat3-{{tableId}}">

            </div>
            <div class="seat" id="seat4-{{tableId}}">

            </div>
            <div class="seat" id="seat5-{{tableId}}">

            </div>
            <div class="seat" id="seat6-{{tableId}}">

            </div>
            <div class="seat" id="seat7-{{tableId}}">

            </div>
            <div class="seat" id="seat8-{{tableId}}">

            </div>
            <div class="seat" id="seat9-{{tableId}}">

            </div>
            <div class="action-button action-leave" style="display: none;">
                <span>{{t "table.buttons.leave"}}</span>
            </div>
            <a class="share-button">+Share</a>
            <div class="my-player-seat" id="myPlayerSeat-{{tableId}}">

            </div>
                <div class="click-area-0">

                </div>
            <div class="table-info" style="display:none;">
                <div class="blinds">
                    {{t "table.blinds" }} <span class="table-blinds-value value">10/20</span>
                </div>
                <div class="tournament-info">
                    <div class="time-to-next-level">
                        {{t "table.level"}} <span class="time-to-next-level-value time">10:00</span>
                    </div>
                </div>
            </div>
            <div class="community-cards">

            </div>
            <div class="total-pot">
                {{t "table.pot" }} <span><span class="amount"></span></span>
            </div>
            <div class="main-pot">

            </div>
            <div class="dealer-button" style="display:none;">
                <img src="${cp}/skins/${skin}/images/table/dealer-button.svg"/>
            </div>
        </div>
        <div class="hand-history" style="display:none;">
            {{t "table.hand-history" }}
        </div>
        <div class="bottom-bar">
            <div class="table-log-container">
                <div class="table-event-log-settings"></div>
                <div class="table-event-log">
                </div>
                <input type="text" class="chat-input describe" title="{{t 'table.log.chat-input-desc'}}"/>

            </div>
            <div class="own-player" id="myPlayerSeat-{{tableId}}Info" style="display:none;">
                <div class="name" id="myPlayerName-{{tableId}}"></div>
                <div class="balance" id="myPlayerBalance-{{tableId}}"></div>
                <div class="no-more-blinds">
                    <input class="checkbox" type="checkbox" id="noMoreBlinds-{{tableId}}"/>
                    <label class="checkbox-icon-label" for="noMoreBlinds-{{tableId}}">
                        {{t "table.buttons.no-more-blinds" }}
                    </label>
                </div>
                    <div class="sit-out-next-hand">
                        <input class="checkbox" type="checkbox" id="sitOutNextHand-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="sitOutNextHand-{{tableId}}">
                            {{t "table.buttons.sit-out-next"}}
                        </label>
                    </div>
            </div>


            <div id="userActActions-{{tableId}}" class="user-actions">
                <div class="action-button action-fold"  style="display: none;">
                    <span>{{t "table.buttons.fold"}}</span>
                </div>
                <div class="action-button action-call" style="display: none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.call"}}</span>
                </div>
                <div class="action-button action-check"  style="display: none;">
                    <span>{{t "table.buttons.check"}}</span>
                </div>
                <div class="action-button action-raise" style="display: none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.raise"}}</span>
                </div>
                <div class="action-button action-bet"  style="display: none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.bet"}}</span>
                </div>
                <div class="action-button action-big-blind"  style="display: none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.big-blind"}}</span>
                </div>
                <div class="action-button action-small-blind"  style="display: none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.small-blind"}}</span>
                </div>
                <div class="action-button action-cancel-bet" style="display:none;">
                    <span>{{t "table.buttons.cancel"}}</span>
                </div>
                <div class="action-button do-action-bet" style="display:none;">
                    <span class="slider-value amount"></span>
                    <span>{{t "table.buttons.bet"}}</span>
                </div>
                <div class="action-button do-action-raise" style="display:none;">
                    <span class="slider-value amount"></span>
                    <span>{{t "table.buttons.raise-to"}}</span>
                </div>
                <div class="action-button fixed-action-bet" style="display:none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.bet"}}</span>
                </div>
                <div class="action-button fixed-action-raise" style="display:none;">
                    <span class="amount"></span>
                    <span>{{t "table.buttons.raise-to"}}</span>
                </div>
                <div class="action-button action-join"style="display: none;">
                    <span>{{t "table.buttons.join"}}</span>
                </div>

                <div class="action-button action-sit-in" style="display: none;">
                    <span>{{t "table.buttons.sit-in"}}</span>
                </div>
                <div class="action-button action-rebuy" style="display: none;">
                    <span>{{t "table.buttons.rebuy"}}</span>
                </div>
                <div class="action-button action-decline-rebuy" style="display: none;">
                    <span>{{t "table.buttons.decline"}}</span>
                </div>
                <div class="action-button action-add-on" style="display: none;">
                    <span>{{t "table.buttons.add-on"}}</span>
                </div>
            </div>
            <div id="futureActions-{{tableId}}" class="future-actions" style="display:none;">
                    <div class="future-action check" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-check-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-check-{{tableId}}">{{t "table.future.check"}}</label>
                    </div>

                    <div class="future-action check-or-fold" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-check-or-fold-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-check-or-fold-{{tableId}}">{{t "table.future.check-fold"}}</label>
                    </div>

                    <div class="future-action call-current-bet" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-call-current-bet-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-call-current-bet-{{tableId}}">{{t "table.future.call"}} <span class="amount"></span></label>
                    </div>

                    <div class="future-action check-or-call-any" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-check-or-call-any-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-check-or-call-any-{{tableId}}">{{t "table.future.check-call-any"}}</label>
                    </div>
                    <div class="future-action call-any" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-call-any-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-call-any-{{tableId}}">{{t "table.future.call-any"}}</label>
                    </div>

                    <div class="future-action fold" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-fold-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-fold-{{tableId}}">{{t "table.future.fold"}}</label>
                    </div>

                    <div class="future-action raise" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-raise-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-raise-{{tableId}}">{{t "table.future.raise-to"}} <span class="amount"></span></label>
                    </div>

                    <div class="future-action raise-any" style="display:none;">
                        <input class="checkbox" type="checkbox" id="future-raise-any-{{tableId}}"/>
                        <label class="checkbox-icon-label" for="future-raise-any-{{tableId}}">{{t "table.future.raise-any"}}</label>
                    </div>

            </div>
            <div id="waitForBigBlind-{{tableId}}" class="wait-for-big-blind" style="display:none;">
                <input class="checkbox" type="checkbox" id="wait-for-big-blind-cb-{{tableId}}" checked="checked"/>
                <label class="checkbox-icon-label" for="wait-for-big-blind-cb-{{tableId}}">{{t "table.wait-for-big-blind"}}</label>
                <div>{{t "table.wait-for-big-blind-description"}}</div>
            </div>
        <div id="myPlayerSeat-{{tableId}}Progressbar" class="circular-progress-bar">

        </div>

    </div>
</div>
</script>
<script type="text/mustache" id="notificationTemplate" style="display: none;">
    {{text}}
    <div class="notification-actions">
    </div>
</script>
<div id="disconnectDialog" style="display: none;">
    <h1 data-i18n="disconnect-dialog.title"></h1>
    <p class="message disconnect-reconnecting">
        <span data-i18n="disconnect-dialog.message"></span> (<span data-i18n="disconnect-dialog.attempt"></span> <span class="reconnectAttempt"></span>)
        <br/>
        <br/>
    </p>
    <p class="stopped-reconnecting" style="display: none;" data-i18n="disconnect-dialog.unable-to-reconnect">
    </p>
    <p class="dialog-buttons stopped-reconnecting" style="display: none;">
        <a class="dialog-ok-button" data-i18n="disconnect-dialog.reload">
        </a>
    </p>
</div>
<div id="buyInDialog" style="display: none;">
</div>
<div id="genericDialog" style="display: none;">
    <h1>Header</h1>
    <p class="message">Message</p>
    <p class="dialog-buttons">
            <a class="dialog-cancel-button" style="display:none;" data-i18n="generic-dialog.cancel">
               Cancel
            </a>
            <a class="dialog-ok-button" data-i18n="generic-dialog.continue">
                Continue
            </a>
    </p>

    </div>
    <script type="text/mustache" id="tournamentBuyInContent">
        <h1>{{t "buy-in.buy-in-at"}} {{name}}</h1>
        <div class="buy-in-row">
            <span class="desc">{{t "buy-in.your-balance" }}</span>  <span class="balance buyin-balance">{{currency balance}} {{currencyCode}}</span>
        </div>
        <div class="buy-in-row">
            <span class="desc">{{t "buy-in.buy-in" }}</span>  <span class="balance buyin-max-amount">{{currency buyIn}}+{{currency fee}} {{currencyCode}}</span>
        </div>
        <div class="buy-in-row">
            <span class="buyin-error" style="display: none;"></span>
        </div>
        <p class="dialog-buttons">
            <a class="dialog-cancel-button">
                {{t "buy-in.cancel" }}
        </a>
            <a class="dialog-ok-button">
                {{t "buy-in.ok-button" }}
        </a>
    </p>
</script>
<script type="text/mustache" id="cashGamesBuyInContent">
    <h1>Buy-in at table <span class="buyin-table-name">{{title}}</span></h1>
    <div class="buy-in-row">
        <span class="desc">{{t "buy-in.your-balance" }}</span>  <span class="balance buyin-balance">{{currency balance}} {{currencyCode}}</span>
    </div>
    <div class="buy-in-row max-amount-container">
        <span class="desc">{{t "buy-in.max-amount" }}</span>  <span class="balance buyin-max-amount">{{currency maxAmount}} {{currencyCode}}</span>
    </div>
    <div class="buy-in-row">
        <span class="desc">{{t "buy-in.min-amount" }}</span>  <span class="balance buyin-min-amount">{{currency minAmount}} {{currencyCode}}</span>
    </div>

    <div class="buy-in-row input-container">
        <span class="desc">{{t "buy-in.buy-in-amount" }}</span>
        <input type="text" class="buyin-amount dialog-input" value="" />
    </div>
    <div class="buy-in-row buy-in-amount-errors">
        <span class="insufficient-funds" style="display: none;">
            {{t "buy-in.insufficient-funds"}}
        </span>
        <span class="too-much-funds" style="display: none;">
            {{t "buy-in.too-much-funds"}}
        </span>
    </div>
    <div class="buy-in-row">
        <span class="buyin-error" style="display: none;"></span>
    </div>
    <p class="dialog-buttons">
        <a class="dialog-cancel-button">
            {{t "buy-in.cancel" }}
        </a>
        <a  class="dialog-ok-button">
            {{t "buy-in.ok-button" }}
        </a>
    </p>
</script>
<script type="text/mustache" id="menuItemTemplate">
    <li class="{{cssClass}}">
        <div class="icon">
        </div>
        <div class="text">
            {{title}}
            <span class="description">{{description}}</span>
        </div>
    </li>

</script>
<script type="text/mustache" id="tabTemplate">
    <li>
        <div class="tab-content">
            <div class="tab-index"></div>
            <div class="mini-cards"></div>
            <span class="name">{{name}}</span>
        </div>
    </li>
</script>
<script type="text/mustache" id="miniCardTemplate" style="display: none;">
    <div id="miniCard-{{domId}}" class="mini-card-container">
         <img src="${cp}/skins/${skin}/images/cards/{{cardString}}.svg"/>
    </div>
</script>
<script type="text/mustache" id="tournamentTemplate" style="display:none;">
    <div id="tournamentView{{tournamentId}}" class="tournament-view">
        <div class="top-row">
            <h3 class="tournament-name">
                <div  style="display:inline-block;" class="tournament-name-title">{{name}}</div>
                <span class="tournament-start-date"></span>
            </h3>
            <a class="share-button">+Share</a>

            <a class="register-button leave-action">{{t "tournament-lobby.close" }}</a>
            <a class="register-button register-action">{{t "tournament-lobby.register" }}</a>
            <a class="register-button unregister-action">{{t "tournament-lobby.unregister" }}</a>
            <a class="register-button take-seat-action">{{t "tournament-lobby.go-to-table" }}</a>
            <a class="register-button loading-action">{{t "tournament-lobby.please-wait" }}</a>
        </div>
        <div class="lobby-data-container">
            <div class="column column-3">
                <div class="tournament-info-container">
                    <div class="info-section tournament-info"></div>
                    <div class="info-section tournament-stats"></div>
                    <div class="info-section payout-structure"></div>
                    <div class="info-section blinds-structure"></div>

                </div>
            </div>
            <div class="column column-3-2">
                <div class="tournament-info-container">
                    <div class="info-section registered-players">
                        <h4>{{t "tournament-lobby.players.players" }}</h4>
                        <table class="player-list">
                            <thead>
                            <tr>
                                <th colspan="2">{{t "tournament-lobby.players.player" }}</th>
                                <th>{{t "tournament-lobby.players.stack" }}</th>
                                <th>{{t "tournament-lobby.players.winnings" }}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td colspan="4">{{t "tournament-lobby.players.loading" }}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
<script type="text/mustache" id="tournamentInfoTemplate">
    <h4>{{t "tournament-lobby.info.title" }}</h4>
    <div class="stats-item"><span>{{gameType}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.info.title" }} <span>{{currency buyIn}}+{{currency fee}} {{buyInCurrencyCode}}</span></div>
    <div class="stats-item">
        {{t "tournament-lobby.info.status" }}
            <span class="status-container">
                <span class="status-0-{{tournamentStatus}}">{{t "tournament-lobby.info.announced" }}</span>
                <span class="status-1-{{tournamentStatus}}">{{t "tournament-lobby.info.registering" }}</span>
                <span class="status-2-{{tournamentStatus}}">{{t "tournament-lobby.info.running" }}</span>
                <span class="status-3-{{tournamentStatus}}">{{t "tournament-lobby.info.break" }}</span>
                <span class="status-4-{{tournamentStatus}}">{{t "tournament-lobby.info.break" }}</span>
                <span class="status-5-{{tournamentStatus}}">{{t "tournament-lobby.info.finished" }}</span>
                <span class="status-6-{{tournamentStatus}}">{{t "tournament-lobby.info.cancelled" }}</span>
                <span class="status-7-{{tournamentStatus}}">{{t "tournament-lobby.info.closed" }}</span>
            </span>
    </div>
    {{#sitAndGo}}
    <div class="stats-item">{{t "tournament-lobby.info.players" }} <span>{{minPlayers}}</span></div>
    {{/sitAndGo}}
    {{^sitAndGo}}
    <div class="stats-item">{{t "tournament-lobby.info.max-players" }} <span>{{maxPlayers}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.info.min-players" }} <span>{{minPlayers}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.info.registration-starts" }} <span><br/>{{date registrationStartTime}}</span></div>
    {{/sitAndGo}}


</script>
<script type="text/mustache" id="tournamentStatsTemplate">
    <h4>{{t "tournament-lobby.stats.title" }}</h4>
    <div class="stats-item">{{t "tournament-lobby.stats.max-stack" }} <span>{{chipStatistics.maxStack}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.stats.min-stack" }} <span>{{chipStatistics.minStack}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.stats.average-stack" }} <span>{{chipStatistics.averageStack}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.stats.current-level" }} <span>{{levelInfo.currentLevel}}</span></div>
    <div class="stats-item">{{t "tournament-lobby.stats.players-left" }} <span>{{playersLeft.remainingPlayers}}/{{playersLeft.registeredPlayers}}</span></div>
</script>
<script type="text/mustache" id="handHistoryViewTemplate">

    <div id="handHistoryView{{id}}" class="hand-history-container" style="display:none;">
        <h1>{{t "hand-history.title"}}<a class="close-button">{{t "hand-history.close"}}</a></h1>

        <div class="hand-ids-container">
            <div class="hand-ids-header">
                <div class="start-time">{{t "hand-history.start-time"}}</div>
                <div class="table-name">{{t "hand-history.table-name"}}</div>
            </div>
            <div class="hand-ids">
            </div>
        </div>
        <div class="paging-container">
            <div class="previous">{{t "hand-history.previous"}}</div>
            <div class="next">{{t "hand-history.next"}}</div>
        </div>
        <div class="hand-log">

        </div>
    </div>

</script>
<script type="text/mustache" id="tournamentPayoutStructureTemplate" style="display:none;">
    <h4>{{t "tournament-lobby.payouts.title" }}</h4>
    <div class="prize-pool">{{t "tournament-lobby.payouts.prize-pool" }} <span>{{currency prizePool}}</span></div>
    <div class="payouts">
        <div class="payout info-list-item header">
            {{t "tournament-lobby.payouts.position" }} <span>{{t "tournament-lobby.payouts.amount" }}</span>
        </div>
        <div class="info-list">
            {{#payouts}}
            <div class="payout info-list-item">
                {{position}} <span>{{currency payoutAmount}}</span>
            </div>
            {{/payouts}}
        </div>
    </div>
</script>
<script type="text/mustache" id="handHistoryIdsTemplate" style="display:none;">
   <p class="no-hands" style="display:none;">
       {{t "hand-history.no-history" }}
   </p>
    <ul>
       {{#summaries}}
        <li id="hand-{{id}}">
            <div class="start-time">{{startTime}}</div>
            <div class="table-name">{{table.tableName}}</div>

        </li>
       {{/summaries}}
   </ul>
</script>
<script type="text/mustache" id="handHistoryLogTemplate" style="display:none;">
    <h2>{{t "hand-history.hand-info" }}</h2>
    <p>
        {{t "hand-history.hand-id" }} <span>{{id}}</span><br/>
        {{t "hand-history.table-name"}} <span>{{table.tableName}}</span><br/>
        {{t "hand-history.table-id" }} <span>{{table.tableId}}</span><br/>
        {{t "hand-history.start-time" }} <span>{{startTime}}</span>
    </p>
    <h2>Seats</h2>
    {{#seats}}
        <div class="seat-group">
            <div>{{t "hand-history.player-name" }} <span>{{name}}</span></div>
            <div>{{t "hand-history.position" }} <span>{{seatId}}</span></div>
            <div>{{t "hand-history.initial-balance" }} <span>{{initialBalance}}</span></div>
        </div>
    {{/seats}}

    <h2>{{t "hand-history.events" }}</h2>
    <div class="events">
        {{#events}}
        <p class="event">
           {{#player}}
                {{name}} {{action}} {{amount.amount}}
                {{#playerCardsDealt}}
                {{t "hand-history.was-dealt" }}
                {{/playerCardsDealt}}
            {{/player}}
            {{#tableCards}}
                {{t "hand-history.community-cards" }}
            {{/tableCards}}
            {{#playerCardsExposed}}
                {{t "hand-history.shows" }}
            {{/playerCardsExposed}}
            {{#playerHand}}
                {{name}} {{t "hand-history.has" }} {{handDescription}}:
            {{/playerHand}}
            {{#bestHandCards}}
                {{text}}
            {{/bestHandCards}}

            {{#cards}}
            {{text}}
            {{/cards}}
        </p>
        {{/events}}
    </div>
    <h2>Results</h2>
   {{#results}}
        {{#res}}
        <p class="results">
            <div>{{t "hand-history.player-name" }} <span>{{name}}</span></div>
            <div>{{t "hand-history.total-bet" }} <span>{{totalBet}}</span></div>
            <div>{{t "hand-history.total-win" }} <span>{{totalWin}}</span></div>
        </p>
       {{/res}}
    {{/results}}

</script>
<script type="text/mustache" id="tournamentBlindsStructureTemplate" style="display:none;">
    <h4>{{t "tournament-lobby.blinds-structure.title" }}</h4>
    <div class="blinds-level info-list-item header">
        {{t "tournament-lobby.blinds-structure.blinds" }}
        <span>{{t "tournament-lobby.blinds-structure.duration" }}</span>
    </div>
    <div class="info-list">
        {{#blindsLevels}}
        <div class="blinds-level info-list-item">
            {{#isBreak}}
            {{t "tournament-lobby.blinds-structure.break" }}
            {{/isBreak}}
            {{^isBreak}}
            {{currency smallBlind}}/{{currency bigBlind}}
            {{/isBreak}}
            <span>{{durationInMinutes}}</span>
        </div>
        {{/blindsLevels}}
    </div>
</script>

<script type="text/mustache" id="tournamentPlayerListItem" style="display:none;">
    <tr>
        <td>{{position}}</td>
        <td>{{name}}</td>
        <td>{{currency stackSize}}</td>
        <td>{{currency winnings}}</td>
    </tr>
</script>

<script type="text/mustache" id="playerActionLogTemplate" style="display:none;">
   <div>{{name}} {{action}} {{#showAmount}} {{currency amount}} {{/showAmount}}</div>
</script>
<script type="text/mustache" id="communityCardsLogTemplate" style="display:none;">
    <div>{{t "table-log.community-cards"}} {{#cards}}&nbsp;{{cardString}}{{/cards}}</div>
</script>
<script type="text/mustache" id="playerCardsExposedLogTemplate" style="display:none;">
    <div>{{player.name}} {{t "table-log.shows"}} {{#cards}}&nbsp;{{cardString}}{{/cards}}</div>
</script>
<script type="text/mustache" id="playerHandStrengthLogTemplate" style="display:none;">
    <div>{{player.name}} {{t "table-log.has"}} {{#hand}}&nbsp;{{text}}{{/hand}} ({{#cardStrings}}&nbsp;{{.}}{{/cardStrings}}&nbsp;)</div>
</script>
<script type="text/mustache" id="potTransferLogTemplate" style="display:none;">
    <div>{{player.name}} {{t "table-log.wins"}} {{amount}}</div>
</script>
<script type="text/mustache" id="newHandLogTemplate" style="display:none;">
    <div class="hand-started">{{t "table-log.hand-started"}}{{handId}} </div>
</script>
<script type="text/mustache" id="chatMessageTemplate" style="display:none;">
    <div class="chat-message"><span class="chat-player-name">{{player.name}}:</span> {{message}} </div>
</script>
<script type="text/mustache" id="overLayDialogTemplate" style="display:none;">
    <div class="dialog-overlay" id="{{dialogId}}">
        <div class="dialog-content">
        </div>
    </div>
</script>

<!-- UserVoice JavaScript SDK (only needed once on a page) -->
<script>
    (function(){

    var id = "${userVoiceId}";
    if (!id) {
        console.log("No UserVoiceID, skipping user voice");
        return;
    } else {
        var uv=document.createElement('script');
        uv.type='text/javascript';
        uv.async=true;uv.src='//widget.uservoice.com/'+id+'.js';
        var s=document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(uv,s)
    }
})();

// A tab to launch the Classic Widget -->

    UserVoice = window.UserVoice || [];
    UserVoice.push(['showTab', 'classic_widget', {
        mode: 'feedback',
        primary_color: '#a01800',
        link_color: '#670f00',
        forum_id: 200038,
        tab_label: 'Feedback',
        tab_color: '#a01800',
        tab_position: 'middle-right',
        tab_inverted: false
    }]);
</script>



<!-- Google Analytics -->
<script type="text/javascript">

    var id = "${googleAnalyticsId}";

    var _gaq = _gaq || [];

    if (!id) {
        console.log("No Analytics id, skipping analytics");
    } else {
        _gaq.push(['_setAccount', id]);
        _gaq.push(['_trackPageview']);
        (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();

    }

    $.ga = {
        _trackEvent:function(event, action, label, value) {
            if (label == undefined) {
                if (Poker.MyPlayer.id) {
                    label = ""+Poker.MyPlayer.id;
                } else {
                    label = "";
                }
            } else {
                label = ""+label;
            }

            if (value == undefined) {
                if (Poker.OperatorConfig.operatorId) {
                    value = Poker.OperatorConfig.operatorId;
                } else {
                    value = 0;
                }
            }
            _gaq.push(['_trackEvent', event, action, label, value ]);
        }
    };

</script>


</body>
</html>
