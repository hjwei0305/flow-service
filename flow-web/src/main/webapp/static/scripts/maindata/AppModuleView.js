/**
 * 显示页面
 */
EUI.AppModuleView = EUI.extend(EUI.CustomUI, {
    initComponent: function () {
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
            height: 40,
            padding: 0,
            style: {
                overflow: "hidden"
            },
            border: false,
            items: [{
                xtype: "Button",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addAppModule();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: "请输入名称进行搜索",
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
                //     loadonce:true,
                url: _ctxPath + "/maindata/appModule/find",
                postData: {
                    //  Q_EQ_code : "1",
                    S_code: "ASC"
                },
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "50%",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_operate'>"
                            + "<div class='condetail_update'title='编辑'></div>"
                            + "<div class='condetail_delete' title='删除'></div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.codeText,
                    name: "code",
                    index: "code",
                    title: false
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name",
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
            //  var tabPanel=parent.homeView.getTabPanel();
            console.log(data);
            g.updateAppModule(data);
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
                            url: _ctxPath +"/maindata/appModule/delete",
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
    updateAppModule: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateAppModuleText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateAppModule",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "id",
                    width: 220,
                    maxLength: 10,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 220,
                    maxLength: 10,
                    value: data.code
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateAppModule");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputCodeMsgText,
                        // });
                        return;
                    }
                    if (!data.name) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputNameMsgText,
                        // });
                        return;
                    }
                    if (!data.depict) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputDepictMsgText,
                        // });
                        return;
                    }
                    g.saveAppModule(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addAppModule: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewAppModuleText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addAppModule",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 220,
                    maxLength: 10
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addAppModule");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputCodeMsgText,
                        // });
                        return;
                    }
                    if (!data.name) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputNameMsgText,
                        // });
                        return;
                    }
                    if (!data.depict) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputDepictMsgText,
                        // });
                        return;
                    }
                    g.saveAppModule(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveAppModule: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText,
        });
        EUI.Store({
            url: _ctxPath +"/maindata/appModule/update",
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