/**
 * Created by fly on 2017/4/18.
 */
EUI.FlowNodeSettingView = EUI.extend(EUI.CustomUI, {
    title: null,
    data: null,
    nodeType: null,
    afterConfirm: null,
    businessModelId: null,

    initComponent: function () {
        day = 1;
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
        if (this.data && !Object.isEmpty(this.data)) {
            this.loadData();
        }
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
                    notify: null
                });
                g.window.close();
            }
        }, {
            title: "取消",
            handler: function () {
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
                title: "额定工时",
                name: "executeTime",
                width: 262,
                labelWidth: 100,
                unit: "分钟"
            }, {
                xtype: "ComboBox",
                title: "工作界面",
                labelWidth: 100,
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
            height: 375,
            id: "excutor",
            itemspace: 0,
            items: [{
                xtype: "Container",
                height: 65,
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
                    height: 240
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
    showChooseUserGrid: function (userType) {
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
        }
        else if (userType == "PositionType") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").hide();
            EUI.getCmp("positionTypeGrid").show();
            EUI.getCmp("selfDefGrid").hide();
            EUI.getCmp("chooseBtn").setTitle("选择岗位类别");
        } else if (userType == "SelfDefinition") {
            EUI.getCmp("gridBox").show();
            EUI.getCmp("positionGrid").hide();
            EUI.getCmp("positionTypeGrid").hide();
            EUI.getCmp("selfDefGrid").show();
            EUI.getCmp("chooseBtn").setTitle("选择自定义执行人");
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
                    url: _ctxPath + "/flowServiceUrl/listServiceUrl",
                    params: {
                        "Q_EQ_businessModel.id": this.businessModelId
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
                    url: _ctxPath + "/flowServiceUrl/listServiceUrl",
                    params: {
                        "Q_EQ_businessModel.id": this.businessModelId
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
            xtype: "FormPanel",
            id: "notify",
            padding: 10,
            defaultConfig: {
                width: 300,
                xtype: "TextField",
                colon: false
            },
            items: []
        };
    }
    ,
    getPositionGrid: function () {
        var colModel = [{
            label: this.lang.operateText,
            name: "operate",
            index: "operate",
            width: 150,
            align: "center",
            formatter: function (cellvalue, options, rowObject) {
                return "<div class='condetail-operate'>" +
                    "<div class='condetail-delete' title='删除'></div></div>";
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
            name: "operate",
            index: "operate",
            width: 150,
            align: "center",
            formatter: function (cellvalue, options, rowObject) {
                return "<div class='condetail-operate'>" +
                    "<div class='condetail-delete' title='删除'></div></div>";
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
            width: 150,
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
                    loadonce: true,
                    multiselect: true,
                    // url: _ctxPath + "",
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
                    loadonce: true,
                    multiselect: true,
                    // url: _ctxPath + "",
                    colModel: this.positionTypeGridColModel()
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
    },
    getExcutorData: function () {
        var data = EUI.getCmp("userType").getValue();
        var userType = data.userType;
        if (userType == "Position") {
            rowdata = EUI.getCmp("positionGrid").getGridData();
            data.ids = this.getSelectIds(rowdata);
        } else if (userType == "PositionType") {
            rowdata = EUI.getCmp("positionTypeGrid").getGridData();
            data.ids = this.getSelectIds(rowdata);
        } else if (userType == "SelfDefinition") {
            rowdata = EUI.getCmp("selfDefGrid").getGridData();
            data.ids = this.getSelectIds(rowdata);
        }
        return data;
    },
    getSelectIds: function (data) {
        var ids = "";
        for (var i = 0; i < data.length; i++) {
            ids += data[i].id;
        }
        return ids;
    },
    loadData: function () {
        var normalForm = EUI.getCmp("normal");
        var executorForm = EUI.getCmp("excutor");
        var eventForm = EUI.getCmp("event");
        var notifyForm = EUI.getCmp("notify");
        normalForm.loadData(this.data.normal);
        var userType = this.data.executor.userType;
        var userTypeCmp = EUI.getCmp("userType");
        userTypeCmp.setValue(userType);
        this.showChooseUserGrid(userType);
    }
})
;