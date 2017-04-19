<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>工作界面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/styles/css/WorkPageUrlView.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/WorkPageUrlView.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/lang/lang.js"></script>
    <script type="text/javascript">
        var workPageUrl;
        EUI.onReady(function () {
            workPageUrl = new EUI.WorkPageUrlView({
                renderTo: "container"
            });
        });
    </script>
</head>
<body style="min-width: 1260px;overflow: auto;background: white;">

<div id="container"></div>

</body>
</html>
