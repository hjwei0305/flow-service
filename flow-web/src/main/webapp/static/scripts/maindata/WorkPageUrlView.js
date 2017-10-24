/**
 * 工作页面
 */
EUI.WorkPageUrlView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModuleId: "",
    initComponent: function () {
        this.gridCmp=EUI.GridPanel({
            renderTo: this.renderTo,
            title: "工作界面配置",
            border: true,
            tbar: this.initTop(),
            gridCfg: this.initCenter()
        });
        this.addEvents();
    },
    initTop: function () {
        var g = this;
        return  [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + this.lang.modelText + "</span>",
                labelWidth: 70,
                id: "coboId",
                async: false,
                canClear: false,
                colon: false,
                name: "appModule.name",
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
                    g.gridCmp.setGridParams({
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
                            msg: g.lang.inputModelText
                        });
                        return;
                    }
                    g.appModuleId = data.data.id;
                    g.appModuleName = data.data.name;
                    g.gridCmp.setPostParams({
                            Q_EQ_appModuleId: data.data.id
                        },true);
                }
            }, {
                xtype: "Button",
                title: this.lang.addBtnText,
                iconCss:"ecmp-common-add",
                selected: true,
                handler: function () {
                    g.addWorkPageUrl();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: this.lang.searchByNameText,
                onSearch: function (value) {
                    g.gridCmp.setPostParams({
                            Q_LK_name: value
                        },true);
                }
            }];
    },
    initCenter: function () {
        var g = this;
        return  {
                loadonce: true,
                datatype:"local",
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "30%",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
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
                },{
                    label: this.lang.mustCommitText,
                    name: "mustCommit",
                    index: "mustCommit",
                    width: '50%',
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = '';
                        if(rowObject.mustCommit){
                            strVar = g.lang.yesText;
                        }else{
                            strVar = g.lang.noText;
                        }
                        return strVar;
                    }
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict"
                }],
                ondbClick: function () {
                    var rowData = g.gridCmp.getSelectRow();
                    g.getValue(rowData.id);
                }
            };

    },
    addEvents: function () {
        var g = this;
        $(".ecmp-common-edit").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.updateWorkPageUrl(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = g.gridCmp.getSelectRow();
            g.deleteWorkPageUrl(rowData);
        });
    },
    deleteWorkPageUrl:function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.ifDelMsgText,
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            },{
                title: g.lang.sureText,
                selected: true,
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
                                g.gridCmp.grid.trigger("reloadGrid");
                            }
                        },
                        failure: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                        }
                    });
                }
            }]
        });
    },
    updateWorkPageUrl: function (data) {
        var g = this;
        win = EUI.Window({
            title: g.lang.updateWorkPageUrlText,
            iconCss: "ecmp-eui-edit",
            height: 280,
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
                },{
                    xtype: "ComboBox",
                    width: 270,
                    labelWidth: 90,
                    allowBlank: false,
                    title: g.lang.mustCommitText,
                    displayText: "是否需要提交",
                    name: "mustCommit",
                    value: data.mustCommit,
                    reader: {name: 'name', field: ['value']},
                    field: ["mustCommit"],
                    data: [{'value': true, 'name': '是'}, {'value': false, 'name': '否'}]
                },{
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
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateWorkPageUrl");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveWorkPageUrl(data);
                }
            }]
        });
    },
    addWorkPageUrl: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewWorkPageUrlText,
            iconCss: "ecmp-eui-add",
            height: 280,
            width:400,
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
                    width: 270
                }, {
                    xtype: "ComboBox",
                    width: 270,
                    labelWidth: 90,
                    allowBlank: false,
                    title: g.lang.mustCommitText,
                    displayText: "是否需要提交",
                    name: "mustCommit",
                    reader: {name: 'name', field: ['value']},
                    field: ["mustCommit"],
                    data: [{'value': true, 'name': '是'}, {'value': false, 'name': '否'}]
                },{
                    xtype: "TextArea",
                    title: g.lang.urlViewAddressText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "url",
                    width: 270
                },  {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 270
                }]
            }],
            buttons: [
                {
                    title: g.lang.cancelText,
                    handler: function () {
                        win.remove();
                    }
                },{
                    title: g.lang.saveText,
                    selected: true,
                    handler: function () {
                        var form = EUI.getCmp("addWorkPageUrl");
                        if (!form.isValid()) {
                            EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                            return;
                        }
                        var data = form.getFormValue();
                        g.saveWorkPageUrl(data);
                    }
            }]
        });
    },
    saveWorkPageUrl: function (data) {
        var g = this;
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
                    g.gridCmp.grid.trigger("reloadGrid");
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
