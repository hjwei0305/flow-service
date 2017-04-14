<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>实体模型页面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/css/contactview.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/AppModuleView.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/lang/lang.js"></script>
    <script type="text/javascript">
        var appModule;
        EUI.onReady(function () {
            appModule = new EUI.AppModuleView({
                renderTo: "appModule"
            });
        });
    </script>
</head>

<body>

<div id="appModule"></div>

</body>
</html>
