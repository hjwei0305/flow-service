/**
 * 流程设计界面
 */
EUI.WorkFlowView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    count: 0,
    id: null,
    versionCode: null,
    flowDefVersionId: null,
    orgId: null,
    orgCode: null,
    instance: null,
    connectInfo: {},
    uelInfo: {},
    isCopy: false,
    businessModelId: null,//业务实体ID

    initComponent: function () {
        var g = this;
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            defaultConfig: {
                border: true,
                borderCss: "flow-border"
            },
            items: [{
                xtype: "ToolBar",
                region: "north",
                border: false,
                isOverFlow: false,
                height: 40,
                padding: 3,
                items: this.getTopItems()
            }, {
                region: "west",
                width: 160,
                html: this.getLeftHtml()
            }, {
                region: "center",
                id: "center",
                html: this.getCenterHtml()
            }]
        });
        //设置面板背景表格
        EUI.getCmp("center").dom.addClass("flow-grid");
        this.addEvents();
        this.initJSPlumb();
        if (this.id) {
            this.loadData();
        }
    },
    getTopItems: function () {
        var g = this;
        return [{
            xtype: "FormPanel",
            width: 660,
            isOverFlow: false,
            height: 40,
            padding: 0,
            layout: "auto",
            id: "formPanel",
            border: false,
            defaultConfig: {
                labelWidth: 88,
                allowBlank: false
            },
            items: [{
                xtype: "ComboGrid",
                name: "flowTypeName",
                field: ["flowTypeId"],
                displayText: "请选择流程类型",
                listWidth: 400,
                gridCfg: {
                    url: _ctxPath + "/flowType/listFlowType",
                    colModel: [{
                        name: "id",
                        index: "id",
                        hidden: true
                    }, {
                        name: "businessModel.id",
                        index: "businessModel.id",
                        hidden: true
                    }, {
                        label: this.lang.codeText,
                        name: "code",
                        index: "code",
                        width: 100
                    }, {
                        label: this.lang.nameText,
                        name: "name",
                        index: "name",
                        width: 150
                    }]
                },
                labelWidth: 85,
                readonly: !isCopy && this.id ? true : false,
                allowBlank: false,
                beforeSelect: function (data) {
                    var scope = this;
                    var busModelId = data.data["businessModel.id"];
                    if (g.businessModelId && g.businessModelId != busModelId) {
                        var msgBox = EUI.MessageBox({
                            title: "操作提示",
                            msg: "切换流程类型将清空所有流程设计，请确定是否继续?",
                            buttons: [{
                                title: "确定",
                                iconCss: "ecmp-common-ok",
                                handler: function () {
                                    g.businessModelId = busModelId;
                                    scope.loadData({
                                        flowTypeName: data.data.name,
                                        flowTypeId: busModelId
                                    });
                                    g.clear();
                                    msgBox.remove();
                                }
                            }, {
                                title: "取消",
                                iconCss: "ecmp-common-delete",
                                handler: function () {
                                    msgBox.remove();
                                }
                            }]
                        });
                        return false;
                    }
                },
                afterSelect: function (data) {
                    var busModelId = data.data["businessModel.id"];
                    g.businessModelId = busModelId;
                },
                reader: {
                    name: "name",
                    field: ["id"]
                }
            }, {
                xtype: "TextField",
                name: "id",
                width: 100,
                readonly: !isCopy && this.id ? true : false,
                labelWidth: 85,
                allowBlank: false,
                displayText: "请输入流程代码"
            }, {
                xtype: "TextField",
                displayText: "请输入流程名称",
                labelWidth: 85,
                width: 220,
                allowBlank: false,
                name: "name"
            }, {
                xtype: "NumberField",
                displayText: "请输入优先级",
                labelWidth: 85,
                width: 90,
                allowNegative: false,
                name: "priority"
            }]
        }, {
            xtype: "Button",
            title: "启动条件",
            iconCss: "ecmp-common-configuration",
            id: "setStartUel",
            handler: function () {
                if (!g.businessModelId) {
                    EUI.ProcessStatus({
                        success: false,
                        msg: "请先选择流程类型"
                    });
                    return;
                }
                new EUI.UELSettingView({
                    title: "流程启动条件",
                    data: g.startUEL,
                    showName: false,
                    businessModelId: g.businessModelId,
                    afterConfirm: function (data) {
                        g.startUEL = data;
                    }
                });
            }
        }, "->", {
            xtype: "Button",
            selected: true,
            title: this.lang.deployText,
            iconCss: "ecmp-common-upload",
            handler: function () {
                g.save(true);
            }
        }, {
            xtype: "Button",
            title: this.lang.saveText,
            iconCss: "ecmp-common-save",
            handler: function () {
                g.save(false);
            }
        }, {
            xtype: "Button",
            title: this.lang.resetText,
            iconCss: "ecmp-common-clear",
            handler: function () {
                var msgBox = EUI.MessageBox({
                    title: "提示",
                    msg: "清空设计将不能恢复，确定要继续吗？",
                    buttons: [{
                        title: "确定",
                        iconCss: "ecmp-common-ok",
                        selected: true,
                        handler: function () {
                            g.clear();
                            msgBox.remove();
                        }
                    }, {
                        title: "取消",
                        iconCss: "ecmp-common-delete",
                        handler: function () {
                            msgBox.remove();
                        }
                    }]
                });
            }
        }];
    },
    getLeftHtml: function () {
        var html = "";
        // 初始化事件
        html += this.initEventNode(_flownode.event);
        // 初始化任务
        html += this.initTaskNode(_flownode.task);
        // 初始化网关
        html += this.initGatewayNode(_flownode.gateway);
        return html;
    },
    initEventNode: function (events) {
        var html = "<div class='flow-item-box'>"
            + "<div class='flow-item-title select'>" + this.lang.eventTitleText + "</div>"
            + "<div class='flow-item-space'></div>"
            + "<div class='flow-item-content'>";
        // 初始化事件
        for (var i = 0; i < events.length; i++) {
            var item = events[i];
            if (i == events.length - 1) {
                html += "<div class='flow-event-box flow-node last' type='"
                    + item.type
                    + "'><div class='flow-event-iconbox'><div class='"
                    + item.css + "'></div></div>"
                    + "<div class='node-title'>" + this.lang[item.name]
                    + "</div></div>";
            } else {
                html += "<div class='flow-event-box flow-node' type='"
                    + item.type
                    + "'><div class='flow-event-iconbox'><div class='"
                    + item.css + "'></div></div>"
                    + "<div class='node-title'>" + this.lang[item.name]
                    + "</div></div>";
            }
        }
        html += "</div><div class='flow-item-space'></div></div>";
        return html;
    },
    initTaskNode: function (tasks) {
        var html = "<div class='flow-item-box'>"
            + "<div class='flow-item-title'>" + this.lang.taskTitleText + "</div>"
            + "<div class='flow-item-space'></div>"
            + "<div class='flow-item-content' style='display:none;'>";
        // 初始化事件
        for (var i = 0; i < tasks.length; i++) {
            var item = tasks[i];
            if (i == tasks.length - 1) {
                html += "<div class='flow-task-box last'>"
                    + "<div class='flow-task flow-node' type='" + item.type
                    + "' nodeType='" + item.nodeType + "'><div class='" + item.css + "'></div>"
                    + "<div class='node-title'>" + this.lang[item.name] + "</div>"
                    + "</div></div>";
            } else {
                html += "<div class='flow-task-box'>"
                    + "<div class='flow-task flow-node' type='" + item.type
                    + "' nodeType='" + item.nodeType + "'><div class='" + item.css + "'></div>"
                    + "<div class='node-title'>" + this.lang[item.name] + "</div>"
                    + "</div></div>";
            }
        }
        html += "</div><div class='flow-item-space' style='display:none;'></div></div>";
        return html;
    },
    initGatewayNode: function (gateways) {
        var html = "<div class='flow-item-box'>"
            + "<div class='flow-item-title'>" + this.lang.gatewayTitleText + "</div>"
            + "<div class='flow-item-space'></div>"
            + "<div class='flow-item-content' style='display:none;'>";
        // 初始化事件
        for (var i = 0; i < gateways.length; i++) {
            var item = gateways[i];
            if (i == gateways.length - 1) {
                html += "<div class='flow-gateway-box flow-node last' bustype='" + item.busType + "' type='"
                    + item.type + "'><div class='flow-gateway-iconbox'><div class='" + item.css + "'></div></div>"
                    + "<div class='node-title'>" + this.lang[item.name]
                    + "</div></div>";
            } else {
                html += "<div class='flow-gateway-box flow-node' bustype='" + item.busType + "' type='"
                    + item.type + "'><div class='flow-gateway-iconbox'><div class='" + item.css + "'></div></div>"
                    + "<div class='node-title'>" + this.lang[item.name]
                    + "</div></div>";
            }
        }
        html += "</div><div class='flow-item-space' style='display:none;'></div></div>";
        return html;
    },
    getCenterHtml: function () {
        return "<div class='flow-content'></div>";
    },
    addEvents: function () {
        var g = this;
        $(".flow-item-title").bind("click", function () {
            if ($(this).hasClass("select")) {
                return;
            }
            var preDom = $(".select.flow-item-title");
            preDom.removeClass("select");
            preDom.siblings(".flow-item-content").slideUp("normal");
            $(this).addClass("select");
            $(this).siblings(".flow-item-content").slideDown("normal");
            $(this).parent().find(".flow-item-space:last").show();
            $(this).parent().siblings().find(".flow-item-space:last")
                .hide();
        });
        var dragging = false;
        var dragDom, preNode;
        $(".flow-node", ".flow-item-content").bind({
            mousedown: function (event) {
                $(this).css("cursor", "move");
                preNode = $(this);
                dragDom = $(this).clone().appendTo($("body"));
                var type = $(this).attr("type");
                g.count++;
                dragDom.attr("id", type + "_" + g.count);
                dragDom.addClass("node-choosed").attr("tabindex", 0);
                dragging = true;
            }
        });
        $(document).bind({
            "mousemove": function (event) {
                if (dragging) {
                    var e = event || window.event;
                    var oX = e.clientX - 20;
                    var oY = e.clientY - 35;
                    var css = {
                        "left": oX + "px",
                        "top": oY + "px"
                    };
                    if (g.isInCenter(dragDom)) {
                        css.cursor = "alias";
                    } else {
                        css.cursor = "no-drop";
                    }
                    dragDom.css(css);
                }
            },
            "mouseup": function (e) {
                if (dragging) {
                    preNode.css("cursor", "auto");
                    if (!g.isInCenter(dragDom)) {
                        dragDom.remove();
                    } else {
                        var offset = $("#center").offset();
                        var doffset = dragDom.offset();
                        dragDom.css({
                            cursor: "pointer",
                            opacity: 1,
                            left: doffset.left - offset.left,
                            top: doffset.top - offset.top - 12
                        });
                        var type = dragDom.attr("type");
                        if (type.indexOf("EndEvent") == -1) {
                            dragDom
                                .append("<div class='node-dot' action='begin'></div>");
                        }
                        $(".flow-content").append(dragDom);
                        dragDom.focus();
                        g.initNode(dragDom[0]);
                    }
                    dragging = false;
                }
            }
        });
        $(".node-choosed").live({
            click: function () {
                $(this).focus();
            },
            "keyup": function (e) {
                var code = e.keyCode || e.charCode;
                if (code == 46) {
                    g.instance.detachAllConnections($(this));
                    var sourceId = $(this).attr("id");
                    for (var key in g.connectInfo) {
                        if (key.indexOf(sourceId) != -1) {
                            delete g.connectInfo[key];
                        }
                    }
                    $(this).remove();
                    e.stopPropagation();
                }
            },
            "dblclick": function () {
                var dom = $(this);
                var type = dom.attr("type");
                if (type == "StartEvent" || type.indexOf("EndEvent") != -1) {
                    return;
                }
                if (!g.businessModelId) {
                    EUI.ProcessStatus({
                        success: false,
                        msg: "请先选择流程类型"
                    });
                    return;
                }
                var input = dom.find(".node-title");
                if (type.endsWith("Gateway")) {
                    g.showSimpleNodeConfig(input.text(), function (value) {
                        input.text(value);
                    });
                } else {
                    new EUI.FlowNodeSettingView({
                        title: input.text(),
                        businessModelId: g.businessModelId,
                        data: dom.data(),
                        nodeType: dom.attr("nodeType"),
                        afterConfirm: function (data) {
                            input.text(data.normal.name);
                            dom.data(data);
                        }
                    });
                }
            }
        });
        $(".jtk-connector").live("keyup", function (e) {
            var code = e.keyCode || e.charCode;
            if (code == 46) {
                g.instance.detachAllConnections($(this));
                $(this).remove();
                e.stopPropagation();
            }
        });
    }
    ,
    isInCenter: function (dom) {
        var offset = EUI.getCmp("center").getDom().offset();
        var domOffset = dom.offset();
        if (offset.left < domOffset.left && offset.top < domOffset.top) {
            return true;
        }
    }
    ,
    initJSPlumb: function () {
        var g = this;
        this.instance = jsPlumb.getInstance({
            Endpoint: "Blank",
            ConnectionOverlays: [["Arrow", {
                location: 1,
                visible: true,
                length: 14,
                id: "ARROW"
            }], ["Label", {
                location: 0.2,
                id: "label",
                visible: false,
                label: null,
                cssClass: "flow-line-note"
            }], ["Label", {
                location: 0.7,
                id: "delete",
                label: "&times",
                visible: false,
                cssClass: "node-delete",
                events: {
                    click: function (overlay, originalEvent) {
                        var connection = overlay.component;
                        delete g.uelInfo[connection.sourceId + "," + connection.targetId];
                        delete g.connectInfo[connection.sourceId + "," + connection.targetId];
                        g.instance.detach(connection);
                    }
                }
            }]],
            Container: "body"
        });

        this.instance.registerConnectionType("basic", {
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [0, 0],
                cornerRadius: 5
            }]
        });
        // 双击连线弹出UEL配置界面
        this.instance.bind("dblclick", function (connection) {
            if (!g.businessModelId) {
                EUI.ProcessStatus({
                    success: false,
                    msg: "请先选择流程类型"
                });
                return;
            }
            var ueldata = g.uelInfo[connection.sourceId + "," + connection.targetId];
            var type = $("#" + connection.sourceId).attr("bustype");
            if (type == "ManualExclusiveGateway") {
                var name = ueldata ? ueldata.name : "";
                g.showSimpleNodeConfig(name, function (value) {
                    g.uelInfo[connection.sourceId + "," + connection.targetId] = {
                        name: value,
                        groovyUel: "",
                        logicUel: ""
                    };
                    var overlay = connection.getOverlay("label");
                    overlay.setLabel(value);
                    overlay.show();
                });
                return;
            }
            var nodeType = $("#" + connection.sourceId).attr("nodetype");
            if (nodeType == "Approve" || nodeType == "CounterSign") {
                return;
            }
            new EUI.UELSettingView({
                title: "表达式配置",
                data: ueldata,
                businessModelId: g.businessModelId,
                afterConfirm: function (data) {
                    g.uelInfo[connection.sourceId + "," + connection.targetId] = data;
                    var overlay = connection.getOverlay("label");
                    overlay.setLabel(data.name);
                    overlay.show();
                }
            });
        });
        //delete删除连线
        this.instance.bind("mouseover", function (connection) {
            connection.getOverlay("delete").show();
        });
        this.instance.bind("mouseout", function (connection) {
            connection.hideOverlay("delete");
        });
        // 连接事件
        this.instance.bind("connection", function (connection, originalEvent) {
            g.connectInfo[connection.sourceId + "," + connection.targetId] = true;
            var uel = g.uelInfo[connection.sourceId + "," + connection.targetId];
            if (uel) {
                var overlay = connection.connection.getOverlay("label");
                overlay.setLabel(uel.name);
                overlay.show();
            } else {
                var busType = $("#" + connection.sourceId).attr("bustype");
                if (busType == "ExclusiveGateway" || busType == "InclusiveGateway") {
                    var overlay = connection.connection.getOverlay("label");
                    overlay.setLabel("默认");
                    overlay.show();
                    g.uelInfo[connection.sourceId + "," + connection.targetId] = {
                        name: "默认",
                        isDefault: true,
                        logicUel: "",
                        groovyUel: ""
                    };
                } else {
                    var nodeType = $("#" + connection.sourceId).attr("nodetype");
                    if (nodeType == "Approve") {
                        var result = g.getApproveLineInfo(connection.sourceId);
                        var name = "同意", agree = true;
                        if (result == 0) {
                            name = "不同意";
                            agree = false;
                        }
                        var overlay = connection.connection.getOverlay("label");
                        overlay.setLabel(name);
                        overlay.show();
                        g.uelInfo[connection.sourceId + "," + connection.targetId] = {
                            name: name,
                            agree: agree,
                            groovyUel: "${approveResult == " + agree + "}",
                            logicUel: ""
                        };
                    } else if (nodeType == "CounterSign") {
                        var result = g.getApproveLineInfo(connection.sourceId);
                        var name = "通过", agree = true;
                        if (result == 0) {
                            name = "未通过";
                            agree = false;
                        }
                        var overlay = connection.connection.getOverlay("label");
                        overlay.setLabel(name);
                        overlay.show();
                        g.uelInfo[connection.sourceId + "," + connection.targetId] = {
                            name: name,
                            agree: agree,
                            groovyUel: "${approveResult == " + agree + "}",
                            logicUel: ""
                        };
                    }
                }
            }
        });
    }
    ,
    getApproveLineInfo: function (id) {
        for (var key in this.uelInfo) {
            if (key.startsWith(id + ",")) {
                var uel = this.uelInfo[key];
                if (uel.agree) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
    },
    initNode: function (el) {
        this.instance.draggable(el);
        var maxConnections = -1;
        var nodeType = $(el).attr("nodetype");
        if (nodeType == "Approve" || nodeType == "CounterSign") {
            maxConnections = 2;
        } else if (nodeType) {
            maxConnections = 1;
        } else if ($(el).attr("type") == "StartEvent") {
            maxConnections = 1;
        }
        this.instance.makeSource(el, {
            filter: ".node-dot",
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [0, 0],
                cornerRadius: 5
            }],
            connectorStyle: {
                stroke: "#61B7CF",
                strokeWidth: 2,
                joinstyle: "round",
                outlineStroke: "white",
                outlineWidth: 2
            },
            connectorHoverStyle: {
                strokeWidth: 3,
                stroke: "#216477",
                outlineWidth: 5,
                outlineStroke: "white"
            },
            connectionType: "basic",
            maxConnections: maxConnections
        });

        if ($(el).attr("type") != "StartEvent") {
            this.instance.makeTarget(el, {
                anchor: "Continuous",
                allowLoopback: false,
                beforeDrop: function (params) {
                    if (params.sourceId == params.targetId) {
                        return false;
                    }
                    return true;
                }
            });
        }
    }
    ,
    doConect: function (sourceId, targetId) {
        this.instance.connect({
            source: sourceId,
            target: targetId,
            type: "basic"
        });
    }
    ,
    checkValid: function () {
        var nodes = $(".node-choosed");
        for (var i = 0; i < nodes.length; i++) {
            var item = $(nodes[i]);
            var id = item.attr("id");
            var type = item.attr("type");
            var name = item.find(".node-title").text();
            var nodeConfig = item.data();
            if (type.indexOf("Task") != -1 && Object.isEmpty(nodeConfig)) {
                EUI.ProcessStatus({
                    success: false,
                    msg: "请将节点：" + name + "，配置完整"
                });
                return;
            }

            var flag = false;
            for (var key in this.connectInfo) {
                if (key.indexOf(id) != -1) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                EUI.ProcessStatus({
                    success: false,
                    msg: String.format(this.lang.noConnectLineText, name)
                });
                return false;
            }
        }
        return true;
    }
    ,
    getFlowData: function () {
        if (!this.checkValid()) {
            return;
        }
        var headForm = EUI.getCmp("formPanel");
        if (!headForm.isValid()) {
            EUI.ProcessStatus({
                success: false,
                msg: "请将流程信息填写完整"
            });
            return;
        }
        var baseInfo = headForm.getFormValue();
        var nodes = $(".node-choosed");
        var baseDoms = $(".flow-info-text");
        var process = {
            name: baseInfo.name,
            id: baseInfo.id,
            flowDefVersionId: this.flowDefVersionId || "",
            isExecutable: true,
            startUEL: this.startUEL,
            nodes: {}
        };
        var parentPos = $(".flow-content").position();
        for (var i = 0; i < nodes.length; i++) {
            var item = $(nodes[i]);
            var id = item.attr("id");
            var type = item.attr("type");
            var name = item.find(".node-title").text();
            var nodeConfig = item.data();
            var node = {
                    type: type,
                    x: item.position().left - parentPos.left + 6,
                    y: item.position().top - parentPos.top + 6,
                    id: id,
                    nodeType: item.attr("nodeType"),
                    target: [],
                    name: name,
                    nodeConfig: nodeConfig
                }
            ;
            if (node.type.endsWith("Gateway")) {
                node.busType = item.attr("bustype");
            }
            var defaultCount = 0;
            for (var key in this.connectInfo) {
                if (key.startsWith(id + ",")) {
                    var item = {
                        targetId: key.split(",")[1],
                        uel: this.uelInfo[key] || ""
                    };
                    if ((node.busType == "ExclusiveGateway" || node.busType == "InclusiveGateway")
                        && item.uel && item.uel.isDefault) {
                        defaultCount++;
                    }
                    node.target.push(item);
                }
            }
            if (defaultCount > 1) {
                EUI.ProcessStatus({
                    success: false,
                    msg: node.name + "：最多只能有1个默认路径，请修改配置"
                });
                return;
            }
            process.nodes[id] = node;
        }
        return {
            flowTypeId: baseInfo.flowTypeId,
            flowTypeName: baseInfo.flowTypeName,
            orgId: this.orgId,
            orgCode: this.orgCode,
            id: this.id,
            versionCode: this.versionCode,
            priority: baseInfo.priority,
            businessModelId: this.businessModelId,
            process: process
        };
    }
    ,
    loadData: function (data) {
        var g = this;
        var mask = EUI.LoadMask({
            msg: "正在获取数据，请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/design/getEntity",
            params: {
                id: this.id,
                versionCode: this.versionCode
            },
            success: function (status) {
                mask.hide();
                if (status.success && status.data) {
                    g.flowDefVersionId = status.data.id;
                    var data = JSON.parse(status.data.defJson);
                    if (g.isCopy) {
                        data.process.name = data.process.name + "_COPY";
                        data.process.id = data.process.id + "_COPY";
                    }
                    g.showDesign(data);
                } else {
                    EUI.ProcessStatus(status);
                }
            },
            failure: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
            }
        });
    }
    ,
    showDesign: function (data) {
        this.loadHead(data);
        var html = "";
        for (var id in data.process.nodes) {
            var node = data.process.nodes[id];
            var type = node.type;
            if (type == "StartEvent") {
                html += this.showStartNode(id, node);
            } else if (type == "EndEvent") {
                html += this.showEndNode(id, node);
            } else if (type.indexOf("Task") != -1) {
                html += this.showTaskNode(id, node);
            } else if (type.indexOf("Gateway") != -1) {
                html += this.showGatewayNode(id, node);
            }
            var tmps = id.split("_");
            var count = parseInt(tmps[1]);
            this.count = this.count > count ? this.count : count;
        }
        $(".flow-content").append(html);
        var doms = $(".node-choosed");
        for (var i = 0; i < doms.length; i++) {
            this.initNode(doms[i]);
            var item = $(doms[i]);
            var id = item.attr("id");
            item.data(data.process.nodes[id].nodeConfig);
        }
        for (var id in data.process.nodes) {
            var node = data.process.nodes[id];
            for (var index in node.target) {
                var target = node.target[index];
                if (target.uel) {
                    this.uelInfo[id + "," + target.targetId] = target.uel;
                }
                this.doConect(id, target.targetId);
            }
        }
    },
    loadHead: function (data) {
        var headData = {
            name: data.process.name,
            id: data.process.id,
            flowTypeId: data.flowTypeId,
            flowTypeName: data.flowTypeName,
            baseInfo: data.baseInfo,
            priority: data.priority
        };
        this.startUEL = data.process.startUEL;
        EUI.getCmp("formPanel").loadData(headData);
    },
    showStartNode: function (id, node) {
        return "<div tabindex=0 type='StartEvent' id='"
            + id
            + "' class='flow-event-box flow-node node-choosed'  style='cursor: pointer; left: "
            + node.x
            + "px; top: "
            + node.y
            + "px; opacity: 1;'>"
            + "<div class='flow-event-iconbox'><div class='flow-event-start'></div></div>"
            + "<div class='node-title'>" + this.lang.startEventText + "</div>"
            + "<div class='node-dot' action='begin'></div></div>";
    }
    ,
    showEndNode: function (id, node) {
        return "<div tabindex=0 type='EndEvent' id='"
            + id
            + "' class='flow-event-box flow-node node-choosed' style='cursor: pointer; left: "
            + node.x
            + "px; top: "
            + node.y
            + "px; opacity: 1;'>"
            + "<div class='flow-event-iconbox'><div class='flow-event-end'></div></div>"
            + "<div class='node-title'>" + this.lang.endEventText + "</div>	</div>";
    }
    ,
    showTaskNode: function (id, node) {
        var css = node.css;
        if (!css) {
            if (node.nodeType == "Normal") {
                css = "usertask";
            } else if (node.nodeType == "SingleSign") {
                css = "singletask";
            } else {
                css = "countertask";
            }
        }
        return "<div tabindex=0 id='" + id
            + "' class='flow-task flow-node node-choosed' type='"
            + node.type + "' nodeType='" + node.nodeType + "' style='cursor: pointer; left: "
            + node.x + "px; top: " + node.y + "px; opacity: 1;'>"
            + "<div class='" + css + "'></div>"
            + "<div class='node-title'>" + node.name + "</div>"
            + "<div class='node-dot' action='begin'></div></div>";
    }
    ,
    showGatewayNode: function (id, node) {
        var css = node.type.toLowerCase();
        if (node.busType == "ManualExclusiveGateway") {
            css = "manualExclusivegateway";
        }
        return "<div tabindex=0 id='" + id
            + "' class='flow-event-box flow-node node-choosed' bustype='" + node.busType + "' type='"
            + node.type + "' style='cursor: pointer; left: "
            + node.x + "px; top: " + node.y + "px; opacity: 1;'>"
            + "<div class='flow-gateway-iconbox'>"
            + "<div class='" + css + "'></div></div>"
            + "<div class='node-title'>" + node.name + "</div>"
            + "<div class='node-dot' action='begin'></div></div>";
    }
    ,
    save: function (deploy) {
        var g = this;
        var data = this.getFlowData();
        if (!data) {
            return;
        }
        if (!deploy && g.isCopy) {
            delete data.id;
        }
        var mask = EUI.LoadMask({
            msg: this.lang.nowSaveMsgText
        })
        EUI.Store({
            url: _ctxPath + "/design/save",
            params: {
                def: JSON.stringify(data),
                deploy: deploy
            },
            success: function (result) {
                mask.hide();
                EUI.ProcessStatus(result);
                if (result.success) {
                    g.flowDefVersionId = result.data.id;
                }
            },
            failure: function (result) {
                mask.hide();
                EUI.ProcessStatus(result);
            }
        })
    },
    clear: function () {
        this.count = 0;
        this.connectInfo = {};
        this.uelInfo = {};
        this.startUEL = null,
            this.instance.deleteEveryEndpoint();
        $(".node-choosed").remove();
    },
    showSimpleNodeConfig: function (title, callback) {
        var win = EUI.Window({
            height: 30,
            padding: 30,
            items: [{
                xtype: "TextField",
                title: "节点名称",
                labelWidth: 80,
                width: 220,
                id: "nodeName",
                name: "name",
                value: title
            }],
            buttons: [{
                title: "保存配置",
                iconCss: "ecmp-common-save",
                selected: true,
                handler: function () {
                    var name = EUI.getCmp("nodeName").getValue();
                    callback && callback.call(this, name);
                    win.close();
                }
            }, {
                title: "取消",
                iconCss: "ecmp-common-delete",
                handler: function () {
                    win.close();
                }
            }]
        });
    }
})
;