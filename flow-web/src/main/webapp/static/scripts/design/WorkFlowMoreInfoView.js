/**
 * 流程设计界面
 */
EUI.WorkFlowMoreInfoView = EUI.extend(EUI.CustomUI, {
    renderTo: "moreinfo",
    isCopy: false,//参考创建
    isFromVersion: false,//流程定义版本参考创建（true）
    businessModelId: null,
    businessModelCode: null,
    afterConfirm: null,
    afterCancel: null,
    startUEL: null,

    initComponent: function () {
        var g = this;
        this.form = EUI.FormPanel({
            width: 895,
            renderTo: this.renderTo,
            height: "auto",
            style: {
                "border-top": "none"
            },
            itemspace: 0,
            defaultConfig: {
                xtype: "Container",
                layout: "auto",
                height: "auto",
                padding: 3
            },
            items: [{
                itemspace: 15,
                items: [{
                    xtype: "NumberField",
                    title: "优先级",
                    labelWidth: 100,
                    allowNegative: false,
                    width: 181,
                    name: "priority"
                }, {
                    xtype: "CheckBox",
                    title: "允许为子流程",
                    labelWidth: 110,
                    name: "subProcess"
                }]
            }, {
                itemspace: 12,
                items: [{
                    xtype: "ComboBox",
                    title: "启动前事件",
                    id: "beforeStart",
                    name: "beforeStartServiceName",
                    field: ["beforeStartServiceId"],
                    labelWidth: 100,
                    width: 322,
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
            }, {
                items: [{
                    xtype: "ComboBox",
                    title: "启动后事件",
                    id: "afterStart",
                    name: "afterStartServiceName",
                    field: ["afterStartServiceId"],
                    labelWidth: 100,
                    width: 322,
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
                    xtype: "CheckBox",
                    title: "同步调用",
                    labelFirst: false,
                    name: "afterStartServiceAync"
                }]
            }, {
                items: [{
                    xtype: "ComboBox",
                    title: "结束前事件",
                    id: "beforeEnd",
                    name: "beforeEndServiceName",
                    field: ["beforeEndServiceId"],
                    labelWidth: 100,
                    width: 322,
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
            }, {
                items: [{
                    xtype: "ComboBox",
                    title: "结束后事件",
                    id: "afterEnd",
                    name: "afterEndServiceName",
                    field: ["afterEndServiceId"],
                    labelWidth: 100,
                    width: 322,
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
            }, {
                items: [{
                    xtype: "TextField",
                    readonly: true,
                    title: "启动条件",
                    labelWidth: 100,
                    width: 322,
                    submitName: false,
                    id: "startUEL",
                    name: "logicUel"
                }, {
                    xtype: "Label",
                    content: "<span class='ecmp-eui-setting'></span>",
                    style: {
                        cursor: "pointer"
                    },
                    onClick: function () {
                        new EUI.UELSettingView({
                            title: "流程启动条件",
                            data: g.startUEL,
                            showName: false,
                            businessModelId: g.params.businessModelId,
                            businessModelCode: g.params.businessModelCode,
                            flowTypeId: EUI.getCmp("formPanel").getFormValue().flowTypeId,
                            afterConfirm: function (data) {
                                EUI.getCmp("startUEL").setValue(data.logicUel);
                                g.startUEL = data;
                            }
                        });
                    }
                }]
            }, this.initTbar()]
        });
        if (this.data) {
            this.loadData(this.data);
        }
    },
    initTbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            items: ["->", {
                xtype: "Button",
                title: "取消",
                handler: function () {
                    g.hide();
                    g.afterCancel && g.afterCancel.call(g);
                }
            }, {
                xtype: "Button",
                title: "确定",
                selected: true,
                handler: function () {
                    var data = g.getData();
                    if (data) {
                        g.afterConfirm && g.afterConfirm.call(g, data);
                    }
                    g.hide();
                }
            }]
        };
    }
    ,
    show: function () {
        this.form.show();
    }
    ,
    hide: function () {
        this.form.hide();
    }
    ,
    reset: function () {
        this.form.reset();
    },
    checkValid: function () {
        return this.form.isValid();
    },
    getData: function () {
        if (!this.checkValid()) {
            return null;
        }
        var data = this.form.getFormValue();
        data.startUEL = this.startUEL;
        return data;
    },
    loadData: function (data) {
        this.startUEL = data.startUEL;
        if(data.startUEL) {
            data.logicUel = data.startUEL.logicUel;
        }
        this.form.loadData(data);
    },
    updateParams: function (businessModelId, businessModelCode) {
        this.businessModelId = businessModelId;
        this.businessModelCode = businessModelCode;
        EUI.getCmp("beforeStart").store.params.busModelId = businessModelId;
        EUI.getCmp("afterStart").store.params.busModelId = businessModelId;
        EUI.getCmp("beforeEnd").store.params.busModelId = businessModelId;
        EUI.getCmp("afterEnd").store.params.busModelId = businessModelId;

    }
})
;