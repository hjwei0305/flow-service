<%--
  Created by IntelliJ IDEA.
  User: fly
  Date: 2017/4/13
  Time: 13:27
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"
         contentType="text/html; charset=UTF-8" %>
<%@ include file="../base/base.jsp" %>
<html>
<head>
    <title>流程设计界面</title>
    <link rel="stylesheet" type="text/css" href="<%=basePath%>/static/styles/css/WorkFlowView.css">
    <script type="text/javascript"
            src="<%=basePath%>/static/data/flow_node.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/ext/jsPlumb/jsplumb-2.2.10.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/scripts/design/WorkFlowView.js"></script>
    <script type="text/javascript"
            src="<%=basePath%>/static/lang/lang.js"></script>
    <script type="text/javascript">
        var flowView;
        EUI.onReady(function () {
            flowView = new EUI.WorkFlowView({
                renderTo: "content"
            });
        });
    </script>
</head>
<body style="background: white">
<div id="content"></div>
</body>
</html>
