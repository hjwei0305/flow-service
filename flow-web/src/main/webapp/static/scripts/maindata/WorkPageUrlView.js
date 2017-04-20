/**
 * 工作页面
 */
EUI.WorkPageUrlView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModuleId: "",
    initComponent: function () {
        var appModuleName = EUI.util.getUrlParam("appModuleName");
        var appModuleId = EUI.util.getUrlParam("appModuleId");
        this.appModuleName = appModuleName;
        this.appModuleId = appModuleId;
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initTop(), this.initCenter()]
        });
        this.addEvents();
    },
    initTop: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 50,
            padding: 0,
            style: {
                overflow: "hidden"
            },
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "应用模块",
                labelWidth: 70,
                id:"coboId",
                async:false,
                colon:false,
                name: "appModule.name",
                // submitValue:{
                //     "appModule.id":"appModule.id"
                // },
                store: {
                    url: "http://localhost:8081/flow/maindata/workPageUrl/findAllAppModuleName",
                },
                field: ["appModule.id"],
                reader: {
                    name: "name",
                    field: ["id"]
                },
                afterLoad : function(data) {
                    var cobo = EUI.getCmp("coboId");
                    cobo.setValue(data[0].name)
                    g.appModuleId = data[0].id,
                    g.appModuleName = data[0].name
                },
                afterSelect: function (data) {
                    // console.log(data);
                    g.appModuleId = data.data.id,
                    g.appModuleName = data.data.name,
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_EQ_appModuleId: data.data.id
                        }
                    ).trigger("reloadGrid");
                },
            }, {
                xtype: "Button",
                title: this.lang.addBtnText,
                selected: true,
                handler: function () {
                    g.addWorkPageUrl();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: "请输入名称进行搜索",
                onSearch: function (value) {
                    console.log(value);
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_EQ_name: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        }
    },
    initCenter: function () {
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
                url: "http://localhost:8081/flow/maindata/workPageUrl/find",
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: '80%',
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail-operate'>" +
                            "<div class='condetail-update'></div>" +
                            "<div class='condetail-delete'></div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "名称",
                    name: "name",
                    index: "name",
                    width: '95%'
                }, {
                    label: "URL界面地址",
                    name: "url",
                    index: "url",
                    width: '95%'
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict"
                }],
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValue(rowData.id);
                }
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".condetail-update").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.updateWorkPageUrl(data);
        });
        $(".condetail-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            var infoBox = EUI.MessageBox({
                title: g.lang.tiShiText,
                msg: g.lang.ifDelMsgText,
                buttons: [{
                    title: g.lang.sureText,
                    selected: true,
                    handler: function () {
                        infoBox.remove();
                        g.deleteGridData();
                        // EUI.getCmp("gridPanel").refreshGrid();

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
    updateWorkPageUrl: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateWorkPageUrlText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateWorkPageUrl",
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
                    name: "appModuleId",
                    width: 220,
                    value:g.appModuleId,
                    hidden : true
                }, {
                    xtype: "TextField",
                    title: "应用模块",
                    readonly:true,
                    labelWidth: 90,
                    name: "appModuleName",
                    width: 220,
                    value:g.appModuleName,
                },{
                    xtype: "TextField",
                    title: "名称",
                    labelWidth: 90,
                    name: "name",
                    width: 220,
                    maxLength: 10,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: "URL界面地址",
                    labelWidth: 90,
                    name: "url",
                    width: 220,
                    value: data.url
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
                    var form = EUI.getCmp("updateWorkPageUrl");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请输入名称",
                        });
                        return;
                    }
                    if (!data.url) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请输入URL界面地址",
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
                    g.saveWorkPageUrl(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addWorkPageUrl: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewWorkPageUrlText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addWorkPageUrl",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "应用模块ID",
                    labelWidth: 90,
                    name: "appModuleId",
                    width: 220,
                    value:g.appModuleId,
                    hidden : true
                }, {
                    xtype: "TextField",
                    title: "应用模块",
                    readonly:true,
                    labelWidth: 90,
                    name: "appModuleName",
                    width: 220,
                    value:g.appModuleName,
                },{
                    xtype: "TextField",
                    title: "名称",
                    labelWidth: 90,
                    name: "name",
                    width: 220,
                    maxLength: 10
                }, {
                    xtype: "TextField",
                    title: "URL界面地址",
                    labelWidth: 90,
                    name: "url",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addWorkPageUrl");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请输入名称",
                        });
                        return;
                    }
                    if (!data.url) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请输入URL界面地址",
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请输入描述",
                        });
                        return;
                    }if (!data.appModuleName) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请选择应用模块",
                        });
                        return;
                    }
                    g.saveWorkPageUrl(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveWorkPageUrl: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText,
        });
        EUI.Store({
           url: "http://localhost:8081/flow/maindata/workPageUrl/update",
            params: data,
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
                myMask.hide();
            }
            /*success : function(status) {
             if (!status.success) {
             new EUI.ProcessStatus({
             msg : status.msg,
             success : false
             });
             } else {
             new EUI.ProcessStatus({
             msg : "操作成功！",
             success : true
             });
             win.close();
             g.reloadGrid();
             }
             mask.hide();
             },
             failure : function(re) {
             new EUI.ProcessStatus({
             msg : "操作失败，请稍后再试。"
             });
             mask.hide();
             }*/
        });
        win.close();
        myMask.hide();
    },
    deleteGridData: function () {
        var g = this;
        var rowData = EUI.getCmp("gridPanel").getSelectRow();
        var myMask = EUI.LoadMask({
            msg: g.lang.nowDelMsgText
        });
        EUI.Store({
            url: "http://localhost:8081/flow/maindata/workPageUrl/delete",
            params: {
                id: rowData.id
            },
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").deleteRow(rowData.id);
                // EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
                myMask.hide();
            }
        });
    }
});
