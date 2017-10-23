/**
 * 显示页面
 */
EUI.FlowTypeView = EUI.extend(EUI.CustomUI, {
    businessModel: "",
    businessModelName: "",
    initComponent: function () {
        this.gridCmp=EUI.GridPanel({
            renderTo: this.renderTo,
            title: "流程类型",
            border: true,
            tbar: this.initTbar(),
            gridCfg: this.initGrid()
        });
        this.addEvents();
    },
    initTbar: function () {
        var g = this;
        return [{
            xtype: "ComboGrid",
            title: "<span style='font-weight: bold'>" + g.lang.businessEntityText + "</span>",
            name: "bussinessModelName",
            id: "coboId",
            canClear: true,
            colon: false,
            field: ["id"],
            listWidth: 400,
            labelWidth: 85,
            editable:true,
            value:g.lang.totalText,
            showSearch:true,
            onSearch:function(value){
                this.grid.localSearch(value);
            },
            gridCfg: {
                url: _ctxPath + "/flowType/listAllBusinessModel",
                loadonce:true,
                colModel: [{
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name"
                }, {
                    label: "所属应用模块",
                    name: "appModuleName",
                    index: "appModuleName"
                }]
            },
            reader: {
                name: "name",
                filed: ["id"]
            },
            afterSelect: function (data) {
                g.businessModel = data.data.id;
                g.businessModelName = data.data.name;
                g.gridCmp.setPostParams({
                    "Q_EQ_businessModel.id": data.data.id
                },true);
            },
            afterClear:function(){
                var cobo = EUI.getCmp("coboId");
                cobo.setValue(g.lang.totalText);
                g.gridCmp.setPostParams({
                    "Q_EQ_businessModel.id": null
                },true);
            }
        }, {
            xtype: "Button",
            title: this.lang.addResourceText,
            iconCss:"ecmp-common-add",
            selected: true,
            handler: function () {
                g.addFlowType();
            }
        }, '->', {
            xtype: "SearchBox",
            displayText: g.lang.searchByNameText,
            onSearch: function (value) {
                g.gridCmp.setPostParams({
                    Q_LK_name: value
                },true);
            }
        }];
    },
    initGrid: function () {
        var g = this;
        return {
            shrinkToFit: false,//固定宽度
            url: _ctxPath + "/flowType/listFlowType",
            postData: {
                //S_createdDate: "ASC"
            },
            colModel: [{
                label: this.lang.operateText,
                name: "operate",
                index: "operate",
                width: 80,
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
                label: this.lang.codeText,
                name: "code",
                index: "code",
                width:270
            }, {
                label: this.lang.nameText,
                name: "name",
                index: "name",
                width:250
            }, {
                label: this.lang.depictText,
                name: "depict",
                index: "depict",
                width:250
            }, {
                label: "businessModelId",
                name: "businessModel.id",
                index: "businessModel.id",
                hidden: true
            }, {
                label: this.lang.belongToBusinessModelText,
                name: "businessModel.name",
                index: "businessModel.name",
                width:250
            }],
            ondbClick: function () {
                var rowData = g.gridCmp.getSelectRow();
                g.getValues(rowData.id);
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".ecmp-common-edit").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.updateFlowType(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = g.gridCmp.getSelectRow();
            g.deleteFlowType(rowData)
        });
    },
    deleteFlowType: function (rowData) {
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
                        url: _ctxPath + "/flowType/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            g.gridCmp.grid.trigger("reloadGrid");
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
    updateFlowType: function (data) {
        var g = this;

        win = EUI.Window({
            title: g.lang.updateFlowTypeText,
            iconCss: "ecmp-eui-edit",
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateFlowType",
                padding: 3,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "id",
                    width: 240,
                    hidden: true
                }, {
                    xtype: "ComboGrid",
                    title:  g.lang.businessEntityText,
                    name: "businessModel.name",
                    field: ["businessModel.id"],
                    listWidth: 400,
                    labelWidth: 90,
                    width: 240,
                    allowBlank: false,
                    showSearch:true,
                    onSearch:function(value){
                        this.grid.localSearch(value);
                    },
                    gridCfg: {
                        url: _ctxPath + "/flowType/listAllBusinessModel",
                        loadonce:true,
                        colModel: [{
                            name: "id",
                            index: "id",
                            hidden: true
                        }, {
                            label: this.lang.nameText,
                            name: "name",
                            index: "name"
                        }]
                    },
                    reader: {
                        name: "name",
                        field: ["id"]
                    }
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 240
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 240
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 240
                }]
            }],
            buttons: [{
                title: this.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateFlowType");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveFlowType(data);
                }
            }]
        });
        EUI.getCmp("updateFlowType").loadData(data);
    },
    addFlowType: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewFlowTypeText,
            iconCss: "ecmp-eui-add",
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowType",
                padding: 3,
                items: [{
                    xtype: "ComboGrid",
                    title:  g.lang.businessEntityText,
                    name: "businessModelName",
                    field: ["businessModel.id"],
                    listWidth: 400,
                    labelWidth: 90,
                    width: 240,
                    allowBlank: false,
                    showSearch:true,
                    onSearch:function(value){
                        this.grid.localSearch(value);
                    },
                    gridCfg: {
                        url: _ctxPath + "/flowType/listAllBusinessModel",
                        loadonce:true,
                        colModel: [{
                            name: "id",
                            index: "id",
                            hidden: true
                        }, {
                            label: this.lang.nameText,
                            name: "name",
                            index: "name"
                        }]
                    },
                    reader: {
                        name: "name",
                        field: ["id"]
                    }
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 240
                }, {
                    xtype: "TextField",
                    title: this.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 240
                }, {
                    xtype: "TextArea",
                    title: this.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 240
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
                    var form = EUI.getCmp("addFlowType");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveFlowType(data);
                }
            }]
        });
    },
    saveFlowType: function (data) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/flowType/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                g.gridCmp.grid.trigger("reloadGrid");
                win.close();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    }
});