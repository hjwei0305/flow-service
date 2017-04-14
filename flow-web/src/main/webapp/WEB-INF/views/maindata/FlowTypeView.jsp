<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>流程类型页面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/styles/css/workflowview.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/FlowTypeView.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/lang/lang.js"></script>
    <script type="text/javascript">
        var flowType;
        EUI.onReady(function () {
            flowType = new EUI.FlowTypeView({
                renderTo: "flowType"
            });
        });
    </script>
</head>

<body style='min-width: 1260px;overflow: auto;background: white;'>

<div id="flowType"></div>

</body>
</html>
