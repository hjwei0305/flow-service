/**
 * 流程设计界面
 */
EUI.WorkFlowView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    count: 0,
    id: null,
    versionCode: null,
    orgId: null,
    instance: null,
    connectInfo: {},
    uelInfo: {},
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
                width: 240,
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
            width: 760,
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
                        index: "code"
                    }, {
                        label: this.lang.nameText,
                        name: "name",
                        index: "name"
                    }]
                },
                labelWidth: 85,
                allowBlank: false,
                afterSelect: function (data) {
                    g.businessModelId = data.data["businessModel.id"];
                },
                reader: {
                    name: "name",
                    field: ["id"]
                }
            }, {
                xtype: "TextField",
                name: "id",
                width: 90,
                labelWidth: 85,
                allowBlank: false,
                displayText: "请输入流程代码"
            }, {
                xtype: "TextField",
                displayText: "请输入流程名称",
                labelWidth: 85,
                width: 200,
                allowBlank: false,
                name: "name"
            }, {
                xtype: "NumberField",
                displayText: "请输入优先级",
                labelWidth: 85,
                width: 90,
                name: "priority"
            }]
        }, {
            xtype: "Button",
            title: "启动条件",
            id: "setStartUel",
            handler: function () {
                if (!g.businessModelId) {
                    EUI.ProcessStatus({
                        success: false,
                        msg: "请先选择流程类型"
                    });
                    return;
                }
                var scope = this;
                new EUI.UELSettingView({
                    title: "流程启动条件",
                    data: this.startUEL,
                    showName: false,
                    businessModelId: g.businessModelId,
                    afterConfirm: function (data) {
                        scope.startUEL = data;
                    }
                });
            }
        }, "->", {
            xtype: "Button",
            selected: true,
            title: this.lang.deployText,
            handler: function () {
                g.save(true);
            }
        }, {
            xtype: "Button",
            title: this.lang.saveText,
            handler: function () {
                g.save(false);
            }
        }, {
            xtype: "Button",
            title: this.lang.resetText,
            handler: function () {
                var msgBox = EUI.MessageBox({
                    title: "提示",
                    msg: "清空设计将不能恢复，确定要继续吗？",
                    buttons: [{
                        title: "确定",
                        selected: true,
                        handler: function () {
                            g.clear();
                        }
                    }, {
                        title: "取消",
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
                        if (type != "EndEvent") {
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
                if (type == "StartEvent" || type == "EndEvent") {
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
                    g.showSimpleNodeConfig(input, input.text());
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
                        g.instance.detach(connection);
                    }
                }
            }]],
            Container: "body"
        });

        this.instance.registerConnectionType("basic", {
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [5, 5],
                // gap: 10,
                cornerRadius: 5,
                // alwaysRespectStubs: false
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
            new EUI.UELSettingView({
                title: "表达式配置",
                data: g.uelInfo[connection.sourceId + "," + connection.targetId],
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
            }
        });
    }
    ,
    initNode: function (el) {
        this.instance.draggable(el);

        this.instance.makeSource(el, {
            filter: ".node-dot",
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [5, 5],
                // gap: 10,
                cornerRadius: 5,
                // alwaysRespectStubs: true
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
            connectionType: "basic"
        });

        this.instance.makeTarget(el, {
            anchor: "Continuous",
            allowLoopback: false,
            beforeDrop: function (params) {
                if (params.sourceId == params.targetId) {
                    return false;
                    /* 不能链接自己 */
                }
                return true;
            }
        });

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
            var flag = false;
            for (var key in this.connectInfo) {
                if (key.indexOf(id) != -1) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                var name = item.find(".node-title").text();
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
        var baseInfo = EUI.getCmp("formPanel").getFormValue();
        var nodes = $(".node-choosed");
        var baseDoms = $(".flow-info-text");
        var process = {
            name: baseInfo.name,
            id: baseInfo.id,
            isExecutable: true,
            startUEL: EUI.getCmp("setStartUel").startUEL,
            nodes: {}
        };
        var parentPos = $(".flow-content").position();
        for (var i = 0; i < nodes.length; i++) {
            var item = $(nodes[i]);
            var id = item.attr("id");
            var nodeConfig = item.data();
            delete nodeConfig.rowdata;
            var node = {
                type: item.attr("type"),
                x: item.position().left - parentPos.left + 6,
                y: item.position().top - parentPos.top + 6,
                id: id,
                nodeType: item.attr("nodeType"),
                target: [],
                name: item.find(".node-title").text(),
                nodeConfig: nodeConfig
            };
            if (node.type.endsWith("Gateway")) {
                node.busType = item.attr("bustype");
            }
            for (var key in this.connectInfo) {
                if (key.startsWith(id + ",")) {
                    var item = {
                        targetId: key.split(",")[1],
                        uel: this.uelInfo[key] || ""
                    };
                    node.target.push(item);
                }
            }
            process.nodes[id] = node;
        }
        return {
            flowTypeId: baseInfo.flowTypeId,
            flowTypeName: baseInfo.flowTypeName,
            orgId: this.orgId,
            id: this.id,
            priority: baseInfo.priority,
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
                if (status.success) {
                    var data = JSON.parse(status.data.defJson);
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
            item.data(data.process.nodes[id]);
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
            baseInfo: data.baseInfo
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
            + "<div class='flow-event-iconbox'><div class='flow-event-start'></div></div>"
            + "<div class='node-title'>" + this.lang.endEventText + "</div>	</div>";
    }
    ,
    showTaskNode: function (id, node) {
        return "<div tabindex=0 id='" + id
            + "' class='flow-task flow-node node-choosed' type='"
            + node.type + "' style='cursor: pointer; left: "
            + node.x + "px; top: " + node.y + "px; opacity: 1;'>"
            + "<div class='" + node.type.toLowerCase() + "'></div>"
            + "<div class='node-title'>" + node.name + "</div>"
            + "<div class='node-dot' action='begin'></div></div>";
    }
    ,
    showGatewayNode: function (id, node) {
        return "<div tabindex=0 id='" + id
            + "' class='flow-event-box flow-node node-choosed' bustype='" + node.busType + "' type='"
            + node.type + "' style='cursor: pointer; left: "
            + node.x + "px; top: " + node.y + "px; opacity: 1;'>"
            + "<div class='flow-gateway-iconbox'>"
            + "<div class='" + node.type.toLowerCase() + "'></div></div>"
            + "<div class='node-title'>" + node.name + "</div>"
            + "<div class='node-dot' action='begin'></div></div>";
    }
    ,
    save: function (deploy) {
        var data = this.getFlowData();
        if (!data) {
            return;
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
        this.instance.deleteEveryEndpoint();
        $(".node-choosed").remove();
    },
    showSimpleNodeConfig: function (input, title) {
        var win = EUI.Window({
            height: 30,
            padding: 30,
            items: [{
                xtype: "TextField",
                title: "节点名称",
                labelWidth: 100,
                width: 220,
                id: "nodeName",
                name: "name",
                value: title
            }],
            buttons: [{
                title: "保存配置",
                selected: true,
                handler: function () {
                    var name = EUI.getCmp("nodeName").getValue();
                    input.text(name);
                    win.close();
                }
            }, {
                title: "取消",
                handler: function () {
                    win.close();
                }
            }]
        });
    }

})
;