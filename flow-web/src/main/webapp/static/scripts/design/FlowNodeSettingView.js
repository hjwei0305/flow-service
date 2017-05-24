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
        this.addEvent();
    },
    addEvent: function () {
        $(".condetail-delete").live("click", function () {
            var data = EUI.getCmp("userType").getValue();
            var userType = data.userType;
            var id = $(this).attr("id");
            var grid;
            if (userType == "Position") {
                grid = EUI.getCmp("positionGrid").getGridData();
            } else if (userType == "PositionType") {
                grid = EUI.getCmp("positionTypeGrid").getGridData();
            } else if (userType == "SelfDefinition") {
                grid = EUI.getCmp("selfDefGrid").getGridData();
            }
            grid.deleteRow(id);
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
                    loadonce: true,
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
                    loadonce: true,
                    multiselect: true,
                    hasPager: false,
                    // url: _ctxPath + "/design/listPosType",
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
                    }],
                    data: [{
                        "id": "07F93AFF-3B91-11E7-B2C2-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495087975000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495087975000,
                        "code": "10009",
                        "name": "技术岗",
                        "tenantCode": "10011"
                    }, {
                        "id": "0D953B30-3B91-11E7-B2C2-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495087984000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495087984000,
                        "code": "10010",
                        "name": "管理岗",
                        "tenantCode": "10011"
                    }, {
                        "id": "13087EB1-3B91-11E7-B2C2-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495087993000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495087993000,
                        "code": "10011",
                        "name": "总裁/总经理/CEO",
                        "tenantCode": "10011"
                    }, {
                        "id": "2B63799B-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": null,
                        "createdDate": null,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495090432000,
                        "code": "10013",
                        "name": "市场助理",
                        "tenantCode": "10011"
                    }, {
                        "id": "32F99D1D-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495088906000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495088906000,
                        "code": "10014",
                        "name": "虹信系统管理员",
                        "tenantCode": "10011"
                    }, {
                        "id": "337F4A27-3B97-11E7-BF86-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495090625000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495090625000,
                        "code": "10023",
                        "name": "程序员/软件工程师",
                        "tenantCode": "10011"
                    }, {
                        "id": "340FB1F2-3B91-11E7-B2C2-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495088049000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495088049000,
                        "code": "10012",
                        "name": "财务复核",
                        "tenantCode": "10011"
                    }, {
                        "id": "3E6EF69E-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495088925000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495088925000,
                        "code": "10015",
                        "name": "虹信财务扫描",
                        "tenantCode": "10011"
                    }, {
                        "id": "47645B00-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495088940000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495088940000,
                        "code": "10016",
                        "name": "虹微高级管理岗",
                        "tenantCode": "10011"
                    }, {
                        "id": "4D402451-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495088950000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495088950000,
                        "code": "10017",
                        "name": "华润雪花系统管理员",
                        "tenantCode": "10011"
                    }, {
                        "id": "5466E432-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495088962000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495088962000,
                        "code": "10018",
                        "name": "电商-总经理",
                        "tenantCode": "10011"
                    }, {
                        "id": "6DDBD258-3B98-11E7-BF86-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495091152000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495091152000,
                        "code": "10024",
                        "name": "系统分析员",
                        "tenantCode": "10011"
                    }, {
                        "id": "76028F44-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495089018000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495089018000,
                        "code": "10019",
                        "name": "BRM-董事长-长智光电",
                        "tenantCode": "10011"
                    }, {
                        "id": "7E36195E-3B95-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495089891000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495089891000,
                        "code": "10022",
                        "name": "网络工程师",
                        "tenantCode": "10011"
                    }, {
                        "id": "7E36A076-3B93-11E7-A16A-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495089032000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495089032000,
                        "code": "10020",
                        "name": "虹信系统管理员",
                        "tenantCode": "10011"
                    }, {
                        "id": "c0a80171-5bcd-1066-815b-cd85c58f0004",
                        "createdBy": null,
                        "createdDate": null,
                        "lastModifiedBy": "anonymous",
                        "lastModifiedDate": 1493960842000,
                        "code": "10005",
                        "name": "有限公司部门领导(正职)",
                        "tenantCode": "10011"
                    }, {
                        "id": "c0a80171-5bcd-1066-815b-cd85ea570005",
                        "createdBy": "anonymous",
                        "createdDate": 1493801757000,
                        "lastModifiedBy": "anonymous",
                        "lastModifiedDate": 1493801757000,
                        "code": "10006",
                        "name": "有限公司部门领导(副职)",
                        "tenantCode": "10011"
                    }, {
                        "id": "FF8E896E-3B90-11E7-B2C2-9681B6E77C6A",
                        "createdBy": "10011,anonymous[anonymous]",
                        "createdDate": 1495087961000,
                        "lastModifiedBy": "10011,anonymous[anonymous]",
                        "lastModifiedDate": 1495087961000,
                        "code": "10008",
                        "name": "SPM-HX-中心副总监",
                        "tenantCode": "10011"
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
        normalForm.loadData(this.data.normal);
        var userType = this.data.executor.userType;
        var userTypeCmp = EUI.getCmp("userType");
        userTypeCmp.setValue(userType);
        this.showChooseUserGrid(userType);
    }
})
;