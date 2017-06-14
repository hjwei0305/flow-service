/**
 * 流程设计界面
 */
EUI.LookWorkFlowView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    count: 0,
    id: null,
    versionCode: null,
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
            itemspace: 5,
            defaultConfig: {
                labelWidth: 88,
                readonly: true,
                xtype: "TextField"
            },
            items: [{
                name: "flowTypeName",
                title: "流程类型"
            }, {
                name: "id",
                width: 90,
                labelWidth: 85,
                title: "流程代码"
            }, {
                xtype: "TextField",
                title: "流程名称",
                labelWidth: 85,
                width: 200,
                name: "name"
            }]
        }, {
            xtype: "Button",
            title: "启动条件",
            id: "setStartUel",
            handler: function () {
                if(!g.startUEL){
                    EUI.ProcessStatus({
                        success:false,
                        msg:"该流程未配置启动条件"
                    });
                    return;
                }
                var scope = this;
                new EUI.UELSettingView({
                    title: "流程启动条件",
                    data: g.startUEL,
                    showName: false,
                    readOnly: true,
                    businessModelId: g.businessModelId,
                    afterConfirm: function (data) {
                        scope.startUEL = data;
                    }
                });
            }
        }];
    },
    getCenterHtml: function () {
        return "<div class='flow-content'></div>";
    },
    addEvents: function () {
        var g = this;
        $(".node-choosed").live({
            "dblclick": function () {
                var dom = $(this);
                var type = dom.attr("type");
                if (type == "StartEvent" || type == "EndEvent") {
                    return;
                }
                var input = dom.find(".node-title");
                if (type.endsWith("Gateway")) {
                    g.showSimpleNodeConfig(input.text());
                } else {
                    new EUI.LookFlowNodeSettingView({
                        title: input.text(),
                        businessModelId: g.businessModelId,
                        data: dom.data(),
                        nodeType: dom.attr("nodeType")
                    });
                }
            }
        });
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
            }]],
            Container: "body"
        });

        this.instance.registerConnectionType("basic", {
            anchor: "Continuous",
            connector: ["Flowchart", {
                stub: [5, 5],
                cornerRadius: 5
            }]
        });
        // 双击连线弹出UEL配置界面
        this.instance.bind("dblclick", function (connection) {
            new EUI.UELSettingView({
                title: "表达式配置",
                readOnly:true,
                data: g.uelInfo[connection.sourceId + "," + connection.targetId],
                businessModelId: g.businessModelId
            });
        });
    }
    ,
    initNode: function (el) {
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
            flowTypeName: data.flowTypeName
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
            + "</div>";
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
            + "</div>";
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
            + "</div>";
    },
    showSimpleNodeConfig: function (title) {
        var win = EUI.Window({
            height: 30,
            padding: 30,
            title: "节点配置",
            items: [{
                xtype: "TextField",
                title: "节点名称",
                labelWidth: 100,
                readonly: true,
                width: 220,
                id: "nodeName",
                name: "name",
                value: title
            }]
        });
    }
})
;