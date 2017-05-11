/**
 * 显示页面
 */
EUI.FlowTypeView = EUI.extend(EUI.CustomUI, {
    businessModel: "",
    businessModelName: "",
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
            isOverFlow:false,
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + "业务实体" + "</span>",
                id: "coboId",
                async: false,
                colon: false,
                labelWidth: 70,
                store: {
                    url: _ctxPath +"/maindata/flowType/findAllBusinessModelName"
                },
                reader: {
                    name: "name",
                    filed: ["id"]
                },
                afterLoad: function (data) {
                    if (!data) {
                        return;
                    }
                    var cobo = EUI.getCmp("coboId");
                    cobo.setValue(data[0].name);
                    g.businessModel = data[0].id;
                    g.businessModelName = data[0].name;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath +"/maindata/flowType/find",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            "Q_EQ_businessModel.id": data[0].id
                        }
                    }, true)
                },
                afterSelect: function (data) {
                    //console.log(data);
                    g.businessModel = data.data.id;
                    g.businessModelName = data.data.name;
                    EUI.getCmp("gridPanel").setPostParams({
                            "Q_EQ_businessModel.id": data.data.id
                        }
                    ).trigger("reloadGrid");
                }
            }, {
                xtype: "Button",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addFlowType();
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
                "border-radius": "3px"
            },
            gridCfg: {
                loadonce: true,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "50%",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_operate'>"
                            + "<div class='condetail_update' title='编辑'></div>"
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
                }, {
                    label: "businessModelId",
                    name: "businessModel.id",
                    index: "businessModel.id",
                    title: false,
                    hidden: true
                }, {
                    label: this.lang.belongToBusinessModelText,
                    name: "businessModel.name",
                    index: "businessModel.name",
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
            g.updateFlowType(data);
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
                            msg: g.lang.nowDelMsgText
                        });
                        EUI.Store({
                            url: _ctxPath +"/maindata/flowType/delete",
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
                    handler: function () {
                        infoBox.remove();
                    }
                }]
            });
        });
    },
    updateFlowType: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateFlowTypeText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateFlowType",
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
                    title: "业务实体ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 220,
                    value: g.businessModel,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "业务实体",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "businessModelName",
                    width: 220,
                    value: g.businessModelName
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
                    xtype: "TextArea",
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
                    var form = EUI.getCmp("updateFlowType");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowType(data);
                }
            }, {
                title: this.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addFlowType: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewFlowTypeText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowType",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "业务实体ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 220,
                    value: g.businessModel,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "业务实体",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "businessModelName",
                    width: 220,
                    value: g.businessModelName
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 220,
                    maxLength: 10
                }, {
                    xtype: "TextField",
                    title: this.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: this.lang.depictText,
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
                    var form = EUI.getCmp("addFlowType");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowType(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveFlowType: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath +"/maindata/flowType/update",
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