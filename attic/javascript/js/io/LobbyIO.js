var lobbyData = new Array();
var pid;
var screenname;

// global object, reference to open child windows and semaphore counter
var openWindows = new Object();
var openTables = new Array();
openWindows.semaphore = 1;

var seats = {};

function loginCallback(status, mypid, myscreenname) {
    if (status == "OK") {
        pid = mypid;
        screenname = myscreenname;
        $('#dialog1').fadeOut(1000);
        showLobby();
    }
}
;

function packetCallback(protocolObject) {
    handlePacket(protocolObject);
}
;


function lobbyCallback(protocolObject) {
    switch (protocolObject.classId) {
        // Table snapshot list
        case FB_PROTOCOL.TableSnapshotListPacket.CLASSID :
            handleTableSnapshotList(protocolObject.snapshots);
            break;
        case FB_PROTOCOL.TableUpdateListPacket.CLASSID :
            handleTableUpdateList(protocolObject.updates);
            break;
        case FB_PROTOCOL.TableRemovedPacket.CLASSID :
            handleTableRemoved(protocolObject.tableid);
            break;

    }
}
;

/**
 * Callback when network status changes
 * @param {com.cubeia.firebase.io.ConnectionStatus} status
 */
function statusCallback(status) {
    console.log("Status recevied: " + status);

    if (status === FIREBASE.ConnectionStatus.CONNECTED) {
        if (autoLogin) {
            connector.login(usr, pwd);
        } else {
            showLogin();
        }
    } else if (status === FIREBASE.ConnectionStatus.DISCONNECTED) {
        connector = new FIREBASE.Connector(packetCallback, lobbyCallback, loginCallback, statusCallback);
        connector.connect("FIREBASE.WebSocketAdapter", webSocketUrl, webSocketPort, "socket");
    }
}
;


function handleTableSnapshotList(tableSnapshotList) {
    for (var i = 0; i < tableSnapshotList.length; i ++) {
        handleTableSnapshot(tableSnapshotList[i]);
    }
    jQuery("#list4").trigger("reloadGrid");
}
;

function handleTableSnapshot(tableSnapshot) {
    if (findTable(tableSnapshot.tableid) === null) {
        var speedParam = readParam("SPEED", tableSnapshot.params);
        var i = lobbyData.push({id:tableSnapshot.tableid, name:tableSnapshot.name, speed:speedParam, capacity:tableSnapshot.capacity,seated:tableSnapshot.seated});
        console.debug("tableid: " + tableSnapshot.tableid);
        jQuery("#list4").jqGrid('addRowData', tableSnapshot.tableid, lobbyData[i - 1]);
    } else {
        console.debug("duplicate found - tableid: " + tableSnapshot.tableid);
    }
}
;

function handleTableUpdateList(tableUpdateList) {
    for (var i = 0; i < tableUpdateList.length; i ++) {
        handleTableUpdate(tableUpdateList[i]);
    }
    jQuery("#list4").trigger("reloadGrid");
    //reSort();
}
;

function reSort() {
    var lastsort = jQuery("#list4").jqGrid('getGridParam', 'lastsort');
    if (lastsort == 3) {
        jQuery("#list4").jqGrid('sortGrid', 'seated', true);
    }
}
;

function handleTableUpdate(tableUpdate) {
    var tableData = findTable(tableUpdate.tableid);
    if (tableData) {
        tableData.seated = tableUpdate.seated;
        jQuery("#list4").jqGrid('setRowData', tableUpdate.tableid, {seated:tableData.seated});
    }
}
;

function doLogin() {
    if ($('#user').val() != "username" && $('#pwd').val() != "Password") {
        usr = $('#user').val();
        pwd = $('#pwd').val();
        connector.login(usr, pwd, 0);
    }
}
;

function handleTableRemoved(tableid) {
    console.debug("removing table " + tableid);
    removeTable(tableid);
    jQuery("#list4").jqGrid('delRowData', tableid);
}
;

function readParam(key, params) {

    for (var i = 0; i < params.length; i ++) {


        var object = params[i];
        if (object.key == key) {
            var valueArray = FIREBASE.ByteArray.fromBase64String(object.value);
            return FIREBASE.Styx.readParam(valueArray);
        }
    }
}
;

function removeTable(tableid) {
    for (var i = 0; i < lobbyData.length; i ++) {
        var object = lobbyData[i];
        if (object.id == tableid) {
            lobbyData.splice(i, 1);
            return;
        }
    }
}
;

function findTable(tableid) {
    for (var i = 0; i < lobbyData.length; i ++) {
        var object = lobbyData[i];
        if (object.id == tableid) {
            return object;
        }
    }
    return null;
}
;

function createGrid() {
    jQuery("#list4").jqGrid({
                                datatype: "local",
                                data: lobbyData,
                                height: 504,
                                colNames:['Name', 'Speed', 'Capacity', 'Seated', ''],
                                colModel:[
                                    {name:'name',index:'name', width:250, sorttype:"string"},
                                    {name:'speed',index:'speed', width:150, sorttype:"string"},
                                    {name:'capacity',index:'capacity', width:110, sorttype:"int"},
                                    {name:'seated',index:'seated', width:110, sorttype:"int"},
                                    {name:'act',index:'act', width:100}
                                ],
                                caption: "Lobby",
                                scroll: true,
                                multiselect: false,
                                gridComplete: function() {
                                    var ids = jQuery("#list4").jqGrid('getDataIDs');
                                    for (var i = 0; i < ids.length; i++) {
                                        var cl = ids[i];
                                        playButton = "<input class='ui-button' type='button' value='Open' onclick='openTable(" + cl + ");'/>";
                                        jQuery("#list4").jqGrid('setRowData', ids[i], {act:playButton});
                                    }

                                },
                                cellSelect: function() {
                                }

                            });
    console.debug("grid created");
}
;

function openTable(tableid) {
    var tableData = findTable(tableid);
    if (tableData != null) {
        createTable(tableid, tableData.capacity);
    }
}
;

openWindows.broadcastPacket = function(protocolObject) {
    for (var i = 0; i < openTables.length; i ++) {
        try {
            openTables[i].handlePacket(protocolObject);
        } catch (error) {
            console.debug(error);
        }
    }
};

/**
 * Check if a window is closed
 * @param windowName
 * @return true if the window doesn't exist or is closed
 */
openWindows.isWindowClosed = function(windowName) {
    return openWindows[windowName] == null || openWindows[windowName].closed;
};

/**
 * Add a window to the collection
 * @param windowName
 * @param windowObject object returned from a window.open call
 */
openWindows.addWindow = function(windowName, windowObject) {
    openWindows[windowName] = windowObject;
    openTables.push(windowObject);
};

/**
 * Set focus to a window if it exists
 * @param windowName
 */
openWindows.setFocus = function(windowName) {
    if (!openWindows.isWindowClosed(windowName)) {
        openWindows[windowName].focus();
    }
};

/**
 * Open a new window or set focus to it if it already exists
 * Uses a semaphore to make sure only one window will be open
 * @param url
 * @param name
 */
function openGame(url, name) {
    // check that there is no other window.open operations pending
    openWindows.semaphore --;
    if (openWindows.semaphore >= 0) {
        if (openWindows.isWindowClosed(name)) {
            var options = "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=1024,height=768";
            //var options="toolbar=yes,location=yes,directories=no,status=yes,menubar=yes,scrollbars=yes,resizable=yes,width=1018,height=800";


            // This almost works. Does however not find the "lobbyWindow" as currently defined in table_view.html.
            /*

             var ifrm = document.createElement("iframe");
             ifrm.setAttribute("src", url);
             ifrm.setAttribute("id", "Game Window");
             ifrm.setAttribute("name", name);
             ifrm.style.width = 1024+"px";
             ifrm.style.height = 768+"px";
             ifrm.style.position = "absolute";
             ifrm.style.zIndex = 1000;
             ifrm.style.top = 0+"px";
             ifrm.style.left = 0+"px";
             document.body.appendChild(ifrm);


             */
            document.getElementById("lobby").style.visibility = "hidden";
            openWindows.addWindow(name, window.open(url, name, options, true));

        } else {
            openWindows.setFocus(name);
        }
    }
    openWindows.semaphore ++;
}
;


function createCookie(name, value, days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    }
    document.cookie = name + "=" + value + expires + "; path=/";
}
;

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}
;

function eraseCookie(name) {
    createCookie(name, "", -1);
}
;
