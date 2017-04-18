/**
 * 流程设计界面
 */
EUI.WorkFlowView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    count: 0,
    instance: null,
    connectInfo: {},
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
                region: "north",
                border: false,
                height: 50,
                padding: 0,
                html: this.getTopHtml()
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
    },
    getTopHtml: function () {
        return "<div style='padding:5px 0;'><div class='flow-info'>" +
            "        <span class='flow-edit'></span>" +
            "        <span class='flow-info-text'>流程名称</span>" +
            "    </div>" +
            "    <div class='flow-info'>" +
            "        <span class='flow-edit'></span>" +
            "        <span class='flow-info-text'>代码</span>" +
            "    </div>" +
            "    <div class='flow-info'>" +
            "        <span class='flow-edit'></span>" +
            "        <span class='flow-info-text'>流程类型</span>" +
            "    </div>" +
            "    <div class='flow-tbar-right'>" +
            "        <div class='flow-tbar-btn deploy'>" + this.lang.deployText + "</div>" +
            "        <div class='flow-tbar-btn save'>" + this.lang.saveText + "</div>" +
            "        <div class='flow-tbar-btn clear'>" + this.lang.resetText + "</div>" +
            "    </div></div>";
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
                    + "'><div class='" + item.css + "'></div>"
                    + "<div class='node-title'>" + this.lang[item.name] + "</div>"
                    + "</div></div>";
            } else {
                html += "<div class='flow-task-box'>"
                    + "<div class='flow-task flow-node' type='" + item.type
                    + "'><div class='" + item.css + "'></div>"
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
                html += "<div class='flow-gateway-box flow-node last' type='"
                    + item.type + "'><div class='" + item.css + "'></div>"
                    + "<div class='node-title'>" + this.lang[item.name]
                    + "</div></div>";
            } else {
                html += "<div class='flow-gateway-box flow-node' type='"
                    + item.type + "'><div class='" + item.css + "'></div>"
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
        $(".flow-tbar-btn").bind("click", function () {
            if ($(this).hasClass(".deploy")) {
                g.save(true);
            } else if ($(this).hasClass(".save")) {
                g.save(false);
            }
            else if ($(this).hasClass(".clear")) {
                g.clear();
            }
        });
        this.addFlowEvents();
    },
    addFlowEvents: function () {
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
                dragDom.attr("id", type + "_" + g.count);
                g.count++;
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
                console.log(code);
                if (code == 46) {
                    g.instance.detachAllConnections($(this));
                    $(this).remove();
                    e.stopPropagation();
                }
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
                label: null,
                cssClass: "aLabel"
            }]],
            Container: "body"
        });

        this.instance.registerConnectionType("basic", {
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [40, 60],
                gap: 10,
                cornerRadius: 5,
                alwaysRespectStubs: true
            }]
        });
        // 双击删除连线
        this.instance.bind("dblclick", function (connection) {
            delete g.connectInfo[connection.sourceId + ","
            + connection.targetId];
            jsPlumb.detach(connection);
        });
        // 连接事件
        this.instance.bind("connection", function (connection, originalEvent) {
            g.connectInfo[connection.sourceId + "," + connection.targetId] = true;
        });
    }
    ,
    initNode: function (el) {
        this.instance.draggable(el);

        this.instance.makeSource(el, {
            filter: ".node-dot",
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [40, 60],
                gap: 10,
                cornerRadius: 5,
                alwaysRespectStubs: true
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
        var nodes = $(".node-choosed");
        var process = {
            id: "",
            name: "",
            isExecutable: true,
            nodes: {}
        };
        var parentPos = $(".flow-content").position();
        for (var i = 0; i < nodes.length; i++) {
            var item = $(nodes[i]);
            var id = item.attr("id");
            var node = {
                type: item.attr("type"),
                x: item.position().left - parentPos.left + 6,
                y: item.position().top - parentPos.top + 6,
                id: id,
                target: [],
                name: item.find(".node-title").text()
            };
            for (var key in this.connectInfo) {
                if (key.startsWith(id + ",")) {
                    node.target.push(key.split(",")[1]);
                }
            }
            process.nodes[id] = node;
        }
        return {process: process};
    }
    ,
    loadData: function (data) {
        var data = {
            "id": "",
            "name": "",
            "isExecutable": true,
            "nodes": {
                "node0": {
                    "type": "StartEvent",
                    "x": 2,
                    "y": 206,
                    "id": "node0",
                    "target": ["node4"],
                    "name": "开始"
                },
                "node1": {
                    "type": "EndEvent",
                    "x": 914,
                    "y": 187,
                    "id": "node1",
                    "target": [],
                    "name": "结束"
                },
                "node2": {
                    "type": "UserTask",
                    "x": 651,
                    "y": 92,
                    "id": "node2",
                    "target": ["node6", "node1"],
                    "name": "审批任务"
                },
                "node3": {
                    "type": "usertask",
                    "x": 663,
                    "y": 329,
                    "id": "node3",
                    "target": ["node6", "node1"],
                    "name": "审批任务"
                },
                "node4": {
                    "type": "UserTask",
                    "x": 181,
                    "y": 215,
                    "id": "node4",
                    "target": ["node5"],
                    "name": "审批任务"
                },
                "node5": {
                    "type": "ExclusiveGateway",
                    "x": 457,
                    "y": 204,
                    "id": "node5",
                    "target": ["node2", "node3"],
                    "name": "排他网关"
                }
            }
        };
        var html = "";
        for (var id in data.nodes) {
            var node = data.nodes[id];
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
        }
        for (var id in data.nodes) {
            var node = data.nodes[id];
            for (var index in node.target) {
                this.doConect(id, node.target[index]);
            }
        }
    }
    ,
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
            + "' class='flow-event-box flow-node node-choosed' type='"
            + node.type + "' style='cursor: pointer; left: "
            + node.x + "px; top: " + node.y + "px; opacity: 1;'>"
            + "<div class='" + node.type.toLowerCase() + "'></div>"
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
    }
})
;