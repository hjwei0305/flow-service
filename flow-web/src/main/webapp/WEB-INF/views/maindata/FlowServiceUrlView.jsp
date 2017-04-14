<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>服务地址管理页面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/styles/css/workflowview.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/FlowServiceUrlView.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/lang/lang.js"></script>
    <script type="text/javascript">
        var flowServiceUrl;
        EUI.onReady(function () {
            flowServiceUrl = new EUI.FlowServiceUrlView({
                renderTo: "flowServiceUrl"
            });
        });
    </script>
</head>

<body style='min-width: 1260px;overflow: auto;background: white;'>

<div id="flowServiceUrl"></div>

</body>
</html>
