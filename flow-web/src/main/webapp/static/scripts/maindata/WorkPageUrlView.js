/**
 * 工作页面
 */
EUI.WorkPageUrlView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModuleId: "",
    initComponent: function () {
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
            height: 40,
            padding: 0,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + this.lang.modelText + "</span>",
                labelWidth: 70,
                id: "coboId",
                async: false,
                colon: false,
                name: "appModule.name",
                // submitValue:{
                //     "appModule.id":"appModule.id"
                // },
                store: {
                    url: _ctxPath + "/workPageUrl/listAllAppModule"
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
                    g.appModuleId = data[0].id;
                    g.appModuleName = data[0].name;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/workPageUrl/listWorkPageUrl",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_appModuleId: data[0].id
                        }
                    }, true)
                },
                afterSelect: function (data) {
                    if (!data) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: this.lang.inputModelText
                        });
                        return;
                    }
                    // console.log(data);
                    g.appModuleId = data.data.id;
                    g.appModuleName = data.data.name;
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_EQ_appModuleId: data.data.id
                        }
                    ).trigger("reloadGrid");
                }
            }, {
                xtype: "Button",
                title: this.lang.addBtnText,
                iconCss:"ecmp-common-add",
              //  selected: true,
                handler: function () {
                    g.addWorkPageUrl();
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
        }
    },
    initCenter: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "gridPanel",
            style: {
                "border-radius": "3px"
            },
            gridCfg: {
                loadonce: true,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "30%",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        // var strVar = "<div class='condetail-operate'>" +
                        //     "<div class='condetail-update' title='"+g.lang.editText+"'></div>" +
                        //     "<div class='condetail-delete' title='"+g.lang.deleteText+"'></div></div>";
                        // return strVar;
                        return  '<i class="ecmp-common-edit icon-space fontcusor" title="'+g.lang.editText+'"></i>'+
                            '<i class="ecmp-common-delete fontcusor" title="'+g.lang.deleteText+'"></i>' ;

                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name",
                    width: '95%'
                }, {
                    label: this.lang.urlViewAddressText,
                    name: "url",
                    index: "url",
                    width: '150%'
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
        $(".ecmp-common-edit").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.updateWorkPageUrl(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            g.deleteWorkPageUrl(rowData);
        });
    },
    deleteWorkPageUrl:function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.ifDelMsgText,
            buttons: [{
                title: g.lang.sureText,
                iconCss:"ecmp-common-ok",
              //  selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: g.lang.nowDelMsgText
                    });
                    EUI.Store({
                        url: _ctxPath + "/workPageUrl/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            if (result.success) {
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            }
                        },
                        failure: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                        }
                    });
                }
            }, {
                title: g.lang.cancelText,
                iconCss:"ecmp-common-delete",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    },
    updateWorkPageUrl: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateWorkPageUrlText,
            height: 250,
            width:400,
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
                    width: 270,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.appModelIdText,
                    labelWidth: 90,
                    name: "appModuleId",
                    width: 270,
                    value: g.appModuleId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.modelText,
                    readonly: true,
                    labelWidth: 90,
                    name: "appModuleName",
                    width: 270,
                    value: g.appModuleName
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 270,
                    value: data.name
                }, {
                    xtype: "TextArea",
                    title: g.lang.urlViewAddressText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "url",
                    width: 270,
                    value: data.url
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 270,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                iconCss:"ecmp-common-save",
             //   selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateWorkPageUrl");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveWorkPageUrl(data);
                }
            }, {
                title: g.lang.cancelText,
                iconCss:"ecmp-common-delete",
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
                    title: g.lang.appModelIdText,
                    labelWidth: 90,
                    name: "appModuleId",
                    width: 220,
                    value: g.appModuleId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.modelText,
                    readonly: true,
                    labelWidth: 90,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.urlViewAddressText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "url",
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                iconCss:"ecmp-common-save",
             //   selected: true,
                handler: function () {
                    var form = EUI.getCmp("addWorkPageUrl");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveWorkPageUrl(data);
                }
            }, {
                title: g.lang.cancelText,
                iconCss:"ecmp-common-delete",
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
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/workPageUrl/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (result.success) {
                    EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                    win.close();
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    }
});
