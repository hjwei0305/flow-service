/**
 * Created by fly on 2017/4/18.
 */
EUI.FlowNodeSettingView = EUI.extend(EUI.CustomUI, {
    title: null,
    data: null,
    nodeType: null,
    afterConfirm: null,
    businessModelId: null,
    flowTypeId: null,

    initComponent: function () {
        this.window = EUI.Window({
            width: 550,
            height: 420,
            padding: 15,
            buttons: this.getButtons(),
            afterRender: function () {
                this.dom.find(".ux-window-content").css("border-radius", "6px");
            },
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

        $(".west-navbar").live("click",function () {
            if($(this).hasClass("select-navbar")){
                return;
            }
            $(this).addClass("select-navbar").siblings().removeClass("select-navbar");

        });

        $(".notify-user-item").live("click",function () {
            if($(this).hasClass("select")){
                return;
            }
            $(this).addClass("select").siblings().removeClass("select");
            EUI.getCmp("notifyExcutor").hide();
            EUI.getCmp("notifyStarter").hide();
            EUI.getCmp("notifyPosition").hide();
            var index = $(this).index();
            switch (index){
                case 0:
                    EUI.getCmp("notifyExcutor").show();
                    break;
                case 1:
                    EUI.getCmp("notifyStarter").show();
                    break;
                case 2:
                    EUI.getCmp("notifyPosition").show();
                    break;
                default:
                    break;
            }
        });
    },
    getButtons: function () {
        var g = this;
        return [{
            title: "保存配置",
            selected: true,
            handler: function () {
                var normalForm = EUI.getCmp("normal");
                if (!normalForm.isValid()) {
                    EUI.ProcessStatus({
                        success: false,
                        msg: "请将常规项配置完整"
                    });
                    return;
                }
                if (!g.checkExcutor()) {
                    EUI.ProcessStatus({
                        success: false,
                        msg: "请将执行人项配置完整"
                    });
                    return;
                }
                var executorForm = EUI.getCmp("excutor");
                var eventForm = EUI.getCmp("event");
                var notifyForm = EUI.getCmp("notify");
                var normalData = normalForm.getFormValue();
                var eventData = eventForm.getFormValue();
                g.afterConfirm && g.afterConfirm.call(this, {
                    normal: normalData,
                    executor: g.getExcutorData(),
                    event: eventData,
                    notify: ""
                });
                EUI.getCmp("notify-tab").remove();
                g.window.close();
            }
        }, {
            title: "取消",
            handler: function () {
                EUI.getCmp("notify-tab").remove();
                g.window.close();
            }
        }];
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
                colon: false
            },
            style: {
                padding: "10px 30px"
            },
            items: [{
                title: "节点名称",
                labelWidth: 100,
                allowBlank: false,
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
                xtype: "ComboBox",
                title: "工作界面",
                labelWidth: 100,
                allowBlank: false,
                name: "workPageName",
                field: ["workPageUrl"],
                async: false,
                store: {
                    url: _ctxPath + "/design/listAllWorkPage",
                    params: {
                        businessModelId: this.businessModelId
                    }
                },
                reader: {
                    name: "name",
                    field: ["url"]
                }
            }, {
                xtype: "CheckBox",
                title: "允许流程发起人终止",
                name: "allowTerminate"
            }, {
                xtype: "CheckBox",
                title: "允许撤回",
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
            height: 375,
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
                height: 290,
                padding: 0,
                id: "gridBox",
                hidden: true,
                defaultConfig: {
                    border: true,
                    height: 240,
                    width: 520,
                },
                items: [{
                    xtype: "ToolBar",
                    region: "north",
                    height: 40,
                    padding: 0,
                    border: false,
                    items: [{
                        xtype: "Button",
                        title: "选择岗位",
                        id: "chooseBtn",
                        handler: function () {
                            var userType = EUI.getCmp("userType").getValue().userType;
                            if (userType == "Position") {
                                g.showSelectPositionWindow();
                            } else if (userType == "PositionType") {
                                g.showSelectPositionTypeWindow();
                            } else if (userType == "SelfDefinition") {
                                g.showSelectSelfUserWindow();
                            }
                        }
                    }]
                }, this.getPositionGrid(), this.getPositionTypeGrid(), this.getSelfDefGrid()]
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
            defaultConfig: {
                labelWidth: 100
            },
            items: [{
                title: "流程发起人",
                name: "StartUser",
                checked: true,
                onChecked: function (value) {
                    g.showChooseUserGrid(this.name);
                }
            }, {
                title: "指定岗位",
                name: "Position",
                onChecked: function (value) {
                    g.showChooseUserGrid(this.name);
                }
            }, {
                title: "指定岗位类别",
                name: "PositionType",
                onChecked: function (value) {
                    g.showChooseUserGrid(this.name);
                }
            }, {
                title: "自定义执行人",
                name: "SelfDefinition",
                onChecked: function (value) {
                    g.showChooseUserGrid(this.name);
                }
            }, {
                title: "任意执行人",
                name: "AnyOne",
                onChecked: function (value) {
                    g.showChooseUserGrid(this.name);
                }
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
            EUI.getCmp("chooseBtn").setTitle("选择岗位");
            if (rowdata) {
                EUI.getCmp("positionGrid").setDataInGrid(rowdata);
            }
        }
        else if (userType == "PositionType") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").hide();
            EUI.getCmp("positionTypeGrid").show();
            EUI.getCmp("selfDefGrid").hide();
            EUI.getCmp("chooseBtn").setTitle("选择岗位类别");
            if (rowdata) {
                EUI.getCmp("positionTypeGrid").setDataInGrid(rowdata);
            }
        } else if (userType == "SelfDefinition") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").hide();
            EUI.getCmp("positionTypeGrid").hide();
            EUI.getCmp("selfDefGrid").show();
            EUI.getCmp("chooseBtn").setTitle("选择自定义执行人");
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
            items: [{
                xtype: "ComboBox",
                name: "beforeExcuteService",
                field: ["beforeExcuteServiceId"],
                title: "任务执行前",
                colon: false,
                labelWidth: 100,
                width: 220,
                store: {
                    url: _ctxPath + "/design/listAllServiceUrl",
                    params: {
                        "busModelId": this.businessModelId
                    }
                },
                reader: {
                    name: "name",
                    field: ["id"]
                }
            }, {
                xtype: "ComboBox",
                name: "afterExcuteService",
                field: ["afterExcuteServiceId"],
                title: "任务执行后",
                colon: false,
                labelWidth: 100,
                width: 220,
                store: {
                    url: _ctxPath + "/design/listAllServiceUrl",
                    params: {
                        "busModelId": this.businessModelId
                    }
                },
                reader: {
                    name: "name",
                    field: ["id"]
                }
            }]
        };
    }
    ,
    getNotifyTab: function () {
        return {
            title: "通知",
            id: "notify",
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
            '<div id="notify-tab"></div>' +
            '</div>'
        };
    }
    ,
    initNotify: function () {
        EUI.FormPanel({
            width: 445,
            height: 365,
            renderTo: "notify-tab",
            defaultConfig: {
                iframe: false,
                xtype: "Container",
                width: 425,
                height: 345,
                itemspace:10
            },
            items: [{
                id: "notifyExcutor",
                items: this.getNotifyItem()
            }, {
                id: "notifyStarter",
                hidden: true,
                items: this.getNotifyItem()
            }, {
                id: "notifyPosition",
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
            labelWidth: 80,
            title: "通知备注",
            name: ""
        }];
    },
    getPositionGrid: function () {
        var colModel = [{
            label: this.lang.operateText,
            name: "id",
            index: "id",
            width: 60,
            align: "center",
            formatter: function (cellvalue, options, rowObject) {
                return "<div class='condetail-operate'>" +
                    "<div class='condetail-delete' title='删除' id='" + cellvalue + "'></div></div>";
            }
        }];
        colModel = colModel.concat(this.positionGridColModel());
        return {
            xtype: "GridPanel",
            id: "positionGrid",
            gridCfg: {
                loadonce: true,
                hasPager: false,
                // url: _ctxPath + "",
                colModel: colModel
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
            index: "code"
        }, {
            label: this.lang.nameText,
            name: "name",
            index: "name"
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
            index: "code"
        }, {
            label: this.lang.nameText,
            name: "name",
            index: "name"
        }];
    },
    getPositionTypeGrid: function () {
        var colModel = [{
            label: this.lang.operateText,
            name: "id",
            index: "id",
            width: 60,
            align: "center",
            formatter: function (cellvalue, options, rowObject) {
                return "<div class='condetail-operate'>" +
                    "<div class='condetail-delete' title='删除' id='" + cellvalue + "'></div></div>";
            }
        }];
        colModel = colModel.concat(this.positionTypeGridColModel());
        return {
            xtype: "GridPanel",
            hidden: true,
            id: "positionTypeGrid",
            gridCfg: {
                loadonce: true,
                hasPager: false,
                // url: _ctxPath + "",
                colModel: colModel
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
            index: "code"
        }, {
            label: this.lang.nameText,
            name: "name",
            index: "name"
        }];
    },
    getSelfDefGrid: function () {
        var colModel = [{
            label: this.lang.operateText,
            name: "operate",
            index: "operate",
            width: 60,
            align: "center",
            formatter: function (cellvalue, options, rowObject) {
                return "<div class='condetail-operate'>" +
                    "<div class='condetail-delete' title='删除'></div></div>";
            }
        }];
        colModel = colModel.concat(this.getSelfDefGridColModel());
        return {
            xtype: "GridPanel",
            id: "selfDefGrid",
            hidden: true,
            gridCfg: {
                loadonce: true,
                hasPager: false,
                // url: _ctxPath + "",
                colModel: colModel
            }
        };
    },
    showSelectPositionWindow: function () {
        var win = EUI.Window({
            title: "选择岗位",
            padding: 0,
            width: 420,
            height: 350,
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    var data = EUI.getCmp("selPositionGrid").getSelectRow();
                    EUI.getCmp("positionGrid").addRowData(data);
                    win.close();
                }
            }, {
                title: "取消",
                handler: function () {
                    win.close();
                }
            }],
            items: [{
                xtype: "GridPanel",
                id: "selPositionGrid",
                gridCfg: {
                    hasPager: false,
                    multiselect: true,
                    url: _ctxPath + "/design/listPos",
                    colModel: this.positionGridColModel()
                }
            }]
        })
    },
    showSelectPositionTypeWindow: function () {
        var win = EUI.Window({
            title: "选择岗位类别",
            padding: 0,
            width: 420,
            height: 350,
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    var data = EUI.getCmp("selPositionTypeGrid").getSelectRow();
                    EUI.getCmp("positionTypeGrid").addRowData(data);
                    win.close();
                }
            }, {
                title: "取消",
                handler: function () {
                    win.close();
                }

            }],
            items: [{
                xtype: "GridPanel",
                id: "selPositionTypeGrid",
                gridCfg: {
                    multiselect: true,
                    hasPager: false,
                    url: _ctxPath + "/design/listPosType",
                    colModel: [{
                        name: "id",
                        index: "id",
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
                }
            }]
        })
    },
    showSelectSelfUserWindow: function () {
        var win = EUI.Window({
            title: "选择自定义执行人",
            padding: 0,
            width: 420,
            height: 350,
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    var data = EUI.getCmp("selfUserGrid").getSelectRow();
                    EUI.getCmp("selfDefGrid").addRowData(data);
                    win.close();
                }
            }, {
                title: "取消",
                handler: function () {
                    win.close();
                }
            }],
            items: [{
                xtype: "GridPanel",
                id: "selfUserGrid",
                gridCfg: {
                    loadonce: true,
                    multiselect: true,
                    // url: _ctxPath + "",
                    colModel: this.getSelfDefGridColModel()
                }
            }]
        })
    }
    , checkExcutor: function () {
        var userType = EUI.getCmp("userType").getValue().userType;
        var data;
        if (userType == "Position") {
            data = EUI.getCmp("positionGrid").getGridData();
        } else if (userType == "PositionType") {
            data = EUI.getCmp("positionTypeGrid").getGridData();
        } else if (userType == "SelfDefinition") {
            data = EUI.getCmp("selfDefGrid").getGridData();
        } else {
            return true;
        }
        if (!data || data.length == 0) {
            return false;
        }
        return true;
    },
    getExcutorData: function () {
        var data = EUI.getCmp("userType").getValue();
        var userType = data.userType;
        if (userType == "Position") {
            rowdata = EUI.getCmp("positionGrid").getGridData();
            data.ids = this.getSelectIds(rowdata);
            data.rowdata = rowdata;
        } else if (userType == "PositionType") {
            rowdata = EUI.getCmp("positionTypeGrid").getGridData();
            data.ids = this.getSelectIds(rowdata);
            data.rowdata = rowdata;
        } else if (userType == "SelfDefinition") {
            rowdata = EUI.getCmp("selfDefGrid").getGridData();
            data.ids = this.getSelectIds(rowdata);
            data.rowdata = rowdata;
        }
        return data;
    },
    getSelectIds: function (data) {
        var ids = "";
        for (var i = 0; i < data.length; i++) {
            if (i > 0) {
                ids += ",";
            }
            ids += data[i].id;
        }
        return ids;
    },
    loadData: function () {
        var normalForm = EUI.getCmp("normal");
        var executorForm = EUI.getCmp("excutor");
        var eventForm = EUI.getCmp("event");
        var notifyForm = EUI.getCmp("notify");
        if (!this.data) {
            return;
        }
        //加载常规配置
        normalForm.loadData(this.data.normal);

        //加载执行人配置
        var userType = this.data.executor.userType;
        var userTypeCmp = EUI.getCmp("userType");
        userTypeCmp.setValue(userType);
        this.showChooseUserGrid(userType, this.data.executor.rowdata);

        //加载事件配置
        eventForm.loadData(this.data.event);
    }
})
;