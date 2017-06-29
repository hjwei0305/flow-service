/**
 * Created by fly on 2017/4/18.
 */
EUI.LookFlowNodeSettingView = EUI.extend(EUI.CustomUI, {
    title: null,
    data: null,
    nodeType: null,
    afterConfirm: null,
    businessModelId: null,
    flowTypeId: null,

    initComponent: function () {
        var g = this;
        this.window = EUI.Window({
            width: 550,
            height: 420,
            padding: 15,
            afterRender: function () {
                this.dom.find(".ux-window-content").css("border-radius", "6px");
            },
            buttons: [{
                title: "确定",
                iconCss:"ecmp-common-ok",
                 selected: true,
                handler: function () {
                    g.window.close();
                }
            }],
            items: [{
                xtype: "TabPanel",
                isOverFlow: false,
                defaultConfig: {
                    iframe: false,
                    closable: false
                },
                items: [this.getNormalTab(), this.getExcutorTab(), this.getEventTab(),
                    this.getNotifyTab()]
            }]
        });
        this.initNotify();
        if (this.data && !Object.isEmpty(this.data)) {
            this.loadData();
        }
        this.addEvent();
    },
    addEvent: function () {
        var g = this;
        $(".condetail-delete").live("click", function () {
            var data = EUI.getCmp("userType").getValue();
            var userType = data.userType;
            var id = $(this).attr("id");
            var grid;
            if (userType == "Position") {
                grid = EUI.getCmp("positionGrid");
            } else if (userType == "PositionType") {
                grid = EUI.getCmp("positionTypeGrid");
            } else if (userType == "SelfDefinition") {
                grid = EUI.getCmp("selfDefGrid");
            }
            grid.deleteRow(id);
        });

        $(".west-navbar").live("click", function () {
            if ($(this).hasClass("select-navbar")) {
                return;
            }
            $(this).addClass("select-navbar").siblings().removeClass("select-navbar");
            var index = $(this).index();
            $(".notify-center").hide();
            var selecter = ".notify-center:eq(" + index + ")";
            $(selecter).show();
            if (index == 0) {
                g.nowNotifyTab = EUI.getCmp("notify-before");
            } else {
                g.nowNotifyTab = EUI.getCmp("notify-after");
            }
        });

        $(".notify-user-item").live("click", function () {
            if ($(this).hasClass("select")) {
                return;
            }
            $(this).addClass("select").siblings().removeClass("select");
            EUI.getCmp(g.nowNotifyTab.items[0]).hide();
            EUI.getCmp(g.nowNotifyTab.items[1]).hide();
            EUI.getCmp(g.nowNotifyTab.items[2]).hide();
            var index = $(this).index();
            switch (index) {
                case 0:
                    EUI.getCmp(g.nowNotifyTab.items[0]).show();
                    break;
                case 1:
                    EUI.getCmp(g.nowNotifyTab.items[1]).show();
                    break;
                case 2:
                    EUI.getCmp(g.nowNotifyTab.items[2]).show();
                    break;
                default:
                    break;
            }
        });
    },
    getNormalTab: function () {
        return {
            title: "常规",
            xtype: "FormPanel",
            id: "normal",
            padding: 10,
            defaultConfig: {
                width: 300,
                xtype: "TextField",
                labelWidth: 150,
                colon: false,
                readonly: true
            },
            style: {
                padding: "10px 30px"
            },
            items: [{
                title: "节点名称",
                labelWidth: 100,
                name: "name",
                value: this.title
            }, {
                xtype: "NumberField",
                title: "额定工时",
                allowNegative: false,
                name: "executeTime",
                width: 262,
                labelWidth: 100,
                unit: "分钟"
            }, {
                title: "工作界面",
                labelWidth: 100,
                name: "workPageName"
            }, {
                xtype: "CheckBox",
                title: "允许流程发起人终止",
                name: "allowTerminate"
            }, {
                xtype: "CheckBox",
                title: "允许上步撤回",
                name: "allowPreUndo"
            }, {
                xtype: "CheckBox",
                title: "允许驳回",
                name: "allowReject"
            }]
        };
    },
    getExcutorTab: function () {
        var g = this;
        return {
            xtype: "FormPanel",
            title: "执行人",
            height: 395,
            width: 535,
            id: "excutor",
            itemspace: 0,
            items: [{
                xtype: "Container",
                height: 65,
                width: 532,
                padding: 0,
                border: false,
                items: [this.initUserTypeGroup()]
            }, {
                xtype: "Container",
                width: 532,
                height: 310,
                padding: 0,
                id: "gridBox",
                hidden: true,
                defaultConfig: {
                    border: true,
                    height: 300,
                    width: 520
                },
                items: [this.getPositionGrid(), this.getPositionTypeGrid(), this.getSelfDefGrid()]
            }]
        };
    },
    initUserTypeGroup: function () {
        var g = this;
        return {
            xtype: "RadioBoxGroup",
            title: "执行人类型",
            labelWidth: 100,
            name: "userType",
            id: "userType",
            readonly: true,
            defaultConfig: {
                labelWidth: 100
            },
            items: [{
                title: "流程发起人",
                name: "StartUser",
                checked: true
            }, {
                title: "指定岗位",
                name: "Position"
            }, {
                title: "指定岗位类别",
                name: "PositionType"
            }, {
                title: "自定义执行人",
                name: "SelfDefinition"
            }, {
                title: "任意执行人",
                name: "AnyOne"
            }]
        };
    },
    showChooseUserGrid: function (userType, rowdata) {
        if (userType == "StartUser") {
            var grid = EUI.getCmp("gridBox");
            grid && grid.hide();
        }
        else if (userType == "Position") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").show();
            EUI.getCmp("positionTypeGrid").hide();
            EUI.getCmp("selfDefGrid").hide();
            if (rowdata) {
                EUI.getCmp("positionGrid").setDataInGrid(rowdata);
            }
        }
        else if (userType == "PositionType") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").hide();
            EUI.getCmp("positionTypeGrid").show();
            EUI.getCmp("selfDefGrid").hide();
            if (rowdata) {
                EUI.getCmp("positionTypeGrid").setDataInGrid(rowdata);
            }
        } else if (userType == "SelfDefinition") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").hide();
            EUI.getCmp("positionTypeGrid").hide();
            EUI.getCmp("selfDefGrid").show();
            if (rowdata) {
                EUI.getCmp("selfDefGrid").setDataInGrid(rowdata);
            }
        } else if (userType == "AnyOne") {
            EUI.getCmp("gridBox").hide();
        }
    },
    getEventTab: function () {
        return {
            xtype: "FormPanel",
            title: "事件",
            id: "event",
            padding: 20,
            defaultConfig: {
                readonly: true,
                xtype: "TextField"
            },
            items: [{
                name: "beforeExcuteService",
                title: "任务执行前",
                colon: false,
                labelWidth: 100,
                width: 220
            }, {
                name: "afterExcuteService",
                field: ["afterExcuteServiceId"],
                title: "任务执行后",
                colon: false,
                labelWidth: 100,
                width: 220
            }]
        };
    }
    ,
    getNotifyTab: function () {
        return {
            title: "通知",
            padding: 10,
            defaultConfig: {
                width: 300,
                xtype: "TextField",
                colon: false
            },
            html: '<div class="notify-west">' +
            '<div class="west-navbar select-navbar">任务达到时</div>' +
            '<div class="west-navbar">任务执行后</div>' +
            '</div>' +
            '<div class="notify-center">' +
            '<div class="notify-user">' +
            '<div class="notify-user-item select">通知执行人</div>' +
            '<div class="notify-user-item">通知发起人</div>' +
            '<div class="notify-user-item">通知岗位</div>' +
            '</div>' +
            '<div id="notify-before"></div>' +
            '</div>' +
            '<div class="notify-center" style="display: none;">' +
            '<div class="notify-user">' +
            '<div class="notify-user-item select">通知执行人</div>' +
            '<div class="notify-user-item">通知发起人</div>' +
            '<div class="notify-user-item">通知岗位</div>' +
            '</div>' +
            '<div id="notify-after"></div>' +
            '</div>'
        };
    },
    initNotify: function () {
        this.nowNotifyTab = EUI.Container({
            width: 445,
            height: 330,
            padding: 12,
            renderTo: "notify-before",
            itemspace:0,
            defaultConfig: {
                iframe: false,
                xtype: "FormPanel",
                width: 425,
                height: 310,
                padding: 0,
                itemspace: 10
            },
            items: [{
                items: this.getNotifyItem()
            }, {
                hidden: true,
                items: this.getNotifyItem()
            }, {
                hidden: true,
                items: this.getNotifyItem()
            }]
        });
        var nextTab = EUI.Container({
            width: 445,
            height: 330,
            padding: 12,
            itemspace:0,
            renderTo: "notify-after",
            defaultConfig: {
                iframe: false,
                xtype: "FormPanel",
                width: 425,
                height: 310,
                padding: 0,
                itemspace: 10
            },
            items: [{
                items: this.getNotifyItem()
            }, {
                hidden: true,
                items: this.getNotifyItem()
            }, {
                hidden: true,
                items: this.getNotifyItem()
            }]
        });
    },
    getNotifyItem: function () {
        return [{
            xtype: "CheckBoxGroup",
            title: "通知方式",
            labelWidth: 80,
            name: "type",
            readonly:true,
            defaultConfig: {
                labelWidth: 60
            },
            items: [{
                title: "邮件",
                name: "EMAIL"
            }, {
                title: "短信",
                name: "SMS"
            }, {
                title: "APP",
                name: "APP"
            }]
        }, {
            xtype: "TextArea",
            width: 320,
            height: 220,
            readonly:true,
            labelWidth: 80,
            title: "通知备注",
            name: "content"
        }];
    },
    getPositionGrid: function () {
        return {
            xtype: "GridPanel",
            id: "positionGrid",
            gridCfg: {
                loadonce: true,
                hasPager: false,
                // url: _ctxPath + "",
                colModel: this.positionGridColModel()
            }
        };
    },
    positionGridColModel: function () {
        return [{
            name: "id",
            index: "id",
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
            width: 200
        }];
    },
    positionTypeGridColModel: function () {
        return [{
            name: "id",
            index: "id",
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
            width: 200
        }];
    },
    getPositionTypeGrid: function () {
        return {
            xtype: "GridPanel",
            hidden: true,
            id: "positionTypeGrid",
            gridCfg: {
                loadonce: true,
                hasPager: false,
                // url: _ctxPath + "",
                colModel: this.positionTypeGridColModel()
            }
        };
    },
    getSelfDefGridColModel: function () {
        return [{
            name: "id",
            index: "id",
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
            width: 200
        }];
    },
    getSelfDefGrid: function () {
        return {
            xtype: "GridPanel",
            id: "selfDefGrid",
            hidden: true,
            gridCfg: {
                loadonce: true,
                hasPager: false,
                // url: _ctxPath + "",
                colModel: this.getSelfDefGridColModel()
            }
        };
    },
    loadData: function () {
        var normalForm = EUI.getCmp("normal");
        var executorForm = EUI.getCmp("excutor");
        var eventForm = EUI.getCmp("event");
        var notifyForm = EUI.getCmp("notify");
        var nodeConfig = this.data.nodeConfig;
        if (!nodeConfig) {
            return;
        }
        //加载常规配置
        normalForm.loadData(nodeConfig.normal);

        //加载执行人配置
        if (nodeConfig.executor) {
            var userType = nodeConfig.executor.userType;
            var userTypeCmp = EUI.getCmp("userType");
            userTypeCmp.setValue(userType);
            this.showChooseUserGrid(userType, nodeConfig.executor.rowdata);
        }
        //加载事件配置
        eventForm.loadData(nodeConfig.event);
    }
})
;