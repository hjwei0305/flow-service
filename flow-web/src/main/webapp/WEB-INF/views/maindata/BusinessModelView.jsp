<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<%

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>业务模型页面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/styles/css/workflowview.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/maindata/BusinessModelView.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/lang/lang.js"></script>
    <script type="text/javascript">
        var businessModel;
        EUI.onReady(function () {
            businessModel = new EUI.BusinessModelView({
                renderTo: "businessModel"
            });
        });
    </script>
</head>

<body style='min-width: 1260px;overflow: auto;background: white;'>

<div id="businessModel"></div>

</body>
</html>
