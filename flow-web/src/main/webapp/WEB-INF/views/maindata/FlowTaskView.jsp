<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>流程任务页面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/styles/css/FlowTaskView.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/FlowTaskView.js"></script>
    <script type="text/javascript">
        var flowTask;
        EUI.onReady(function () {
            flowType = new EUI.FlowTaskView({
                renderTo: "container"
            });
        });
    </script>
</head>

<body style='min-width: 1260px;overflow: auto;background: white;'>

<div id="container"></div>

</body>
</html>
