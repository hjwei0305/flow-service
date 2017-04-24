/**
 * 显示页面
 */
EUI.BusinessModelView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModule: "",
    initComponent: function () {
        var appModuleName = EUI.util.getUrlParam("appModuleName");
        var appModule = EUI.util.getUrlParam("appModule");
        this.appModuleName = appModuleName;
        this.appModule = appModule;
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initTbar(), this.initGrid()]
        });
        this.addEvents();
    },
    initTbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 50,
            padding: 10,
            style: {
                overflow: "hidden"
            },
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "应用模块",
                labelWidth: 70,
                id: "coboId",
                async: false,
                colon: false,
                name: "appModule.name",
                // submitValue:{
                //     "appModule.id":"appModule.id"
                // },
                store: {
                    url: "http://localhost:8081/flow/maindata/businessModel/findAllAppModuleName",
                },
                field: ["appModule.id"],
                reader: {
                    name: "name",
                    field: ["id"]
                },
                afterLoad: function (data) {
                    if (!data) {
                        return;
                    }
                    var cobo = EUI.getCmp("coboId");
                    cobo.setValue(data[0].name);
                    g.appModule = data[0].id;
                    g.appModuleName = data[0].name;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: "http://localhost:8081/flow/maindata/businessModel/find",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            "Q_EQ_appModule.id": data[0].id
                        }
                    }, true)
                },
                afterSelect: function (data) {
                    if (!data) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请选择应用模块"
                        });
                        return;
                    }
                    // console.log(data);
                    g.appModule = data.data.id;
                    g.appModuleName = data.data.name;
                    EUI.getCmp("gridPanel").setPostParams({
                            "Q_EQ_appModule.id": data.data.id
                        }
                    ).trigger("reloadGrid");
                },
            }, {
                xtype: "Button",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addBusinessModel();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: this.lang.searchNameText,
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("gridPanel").setPostParams({
                                Q_LK_name: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_name: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        };
    },
    initGrid: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "gridPanel",
            style: {
                "border": "1px solid #aaa",
                "border-radius": "3px"
            },
            gridCfg: {
                loadonce: true,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: 100,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_operate'>"
                            + "<div class='condetail_update'></div>"
                            + "<div class='condetail_delete'></div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name",
                    title: false
                }, {
                    label: this.lang.classPathText,
                    name: "className",
                    index: "className",
                    title: false
                }, {
                    label: this.lang.conditonBeanText,
                    name: "conditonBean",
                    index: "conditonBean",
                    title: false
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict",
                    title: false
                }],
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".condetail_update").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.updateBusinessModel(data);
        });
        $(".condetail_delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            var infoBox = EUI.MessageBox({
                title: g.lang.tiShiText,
                msg: g.lang.ifDelMsgText,
                buttons: [{
                    title: g.lang.sureText,
                    selected: true,
                    handler: function () {
                        infoBox.remove();
                        var myMask = EUI.LoadMask({
                            msg: g.lang.nowDelMsgText,
                        });
                        EUI.Store({
                            url: "http://localhost:8081/flow/maindata/businessModel/delete",
                            params: {
                                id: rowData.id
                            },
                            success: function () {
                                myMask.hide();
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            },
                            failure: function () {
                                myMask.hide();
                            }
                        });
                    }
                }, {
                    title: g.lang.cancelText,
                    handler: function () {
                        infoBox.remove();
                    }
                }]
            });
        });
    },
    updateBusinessModel: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateBusinessModelText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBusinessModel",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    name: "id",
                    width: 220,
                    maxLength: 10,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "应用模块ID",
                    labelWidth: 90,
                    name: "appModule.id",
                    width: 220,
                    value: g.appModule,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "应用模块",
                    readonly: true,
                    labelWidth: 90,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName,
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    name: "name",
                    width: 220,
                    maxLength: 10,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    labelWidth: 90,
                    name: "className",
                    width: 220,
                    value: data.className
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonBeanText,
                    labelWidth: 90,
                    name: "conditonBean",
                    width: 220,
                    value: data.conditonBean
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateBusinessModel");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputNameMsgText
                        });
                        return;
                    }
                    if (!data.className) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputClassPathMsgText
                        });
                        return;
                    }
                    if (!data.conditonBean) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputConditonBeanText
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputDepictMsgText
                        });
                        return;
                    }
                    g.saveBusinessModel(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addBusinessModel: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewBusinessModelText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBusinessModel",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "应用模块ID",
                    labelWidth: 90,
                    name: "appModule.id",
                    width: 220,
                    value: g.appModule,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "应用模块",
                    readonly: true,
                    labelWidth: 90,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName,
                }, {
                    title: g.lang.nameText,
                    xtype: "TextField",
                    labelWidth: 90,
                    name: "name",
                    width: 220,
                    maxLength: 10,
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    labelWidth: 90,
                    name: "className",
                    width: 220,
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonBeanText,
                    labelWidth: 90,
                    name: "conditonBean",
                    width: 220,
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    name: "depict",
                    width: 220,
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addBusinessModel");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.className) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputClassPathMsgText,
                        });
                        return;
                    }
                    if (!data.conditonBean) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputConditonBeanText,
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputDepictMsgText,
                        });
                        return;
                    }
                    /* if (!data["appModule.name"]) {
                     EUI.ProcessStatus({
                     success: false,
                     msg: g.lang.chooseBelongToAppModuleText,
                     });
                     return;
                     }*/
                    g.saveBusinessModel(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveBusinessModel: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText,
        });
        EUI.Store({
            url: "http://localhost:8081/flow/maindata/businessModel/update",
            params: data,
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
                myMask.hide();
            }
        });
        win.close();
        myMask.hide();
    }
});