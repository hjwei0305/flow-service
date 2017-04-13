<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>页面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/css/contactview.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/WaitDealtView.js"></script>
    <script type="text/javascript">
        var waitDealt;
        EUI.onReady(function () {
            waitDealt = new EUI.WaitDealtView({
                renderTo: "waitDealt"
            });
        });
    </script>
</head>

<body style='min-width: 1260px;overflow: auto;background: white;'>

<div id="waitDealt"></div>

</body>
</html>
