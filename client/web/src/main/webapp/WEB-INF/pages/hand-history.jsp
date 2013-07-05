<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>Hand History</title>
    <link id="skinCss" rel="stylesheet/less" type="text/css" href="${cp}/skins/${skin}/less/base.less" />
    <script type="text/javascript"  src="${cp}/js/lib/less-1.3.0.min.js"></script>

    <script type="text/javascript" src="${cp}/js/lib/jquery-1.7.2.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function(){
            var hm = window.opener.Poker.AppCtx.getHandHistoryManager();
            hm.ready(${tableId},$(".hand-history-container"));
        });
    </script>
</head>
<body>
    <div class="hand-history-container">
        <div class="hand-ids-header">
            <div class="table-name">Table</div>
            <div class="hand-id">Hand id</div>
            <div class="start-time">Start time</div>
        </div>
        <div class="hand-ids">

        </div>
        <div class="hand-log">

        </div>
    </div>

</body>
</html>