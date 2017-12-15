/**
 * 流程设计界面
 */
EUI.LookWorkFlowMoreInfoView = EUI.extend(EUI.CustomUI, {
    renderTo: "moreinfo",
    businessModelId: null,
    businessModelCode: null,
    afterConfirm: null,
    afterCancel: null,
    startUEL: null,
    width:875,
    parent:null,

    initComponent: function () {
        var g = this;
        this.form = EUI.FormPanel({
            width: g.width,
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
                    labelWidth: 90,
                    allowNegative: false,
                    width: 181,
                    readonly: true,
                    name: "priority"
                }, {
                    xtype: "CheckBox",
                    title: "允许为子流程",
                    labelWidth: 110,
                    readonly: true,
                    name: "subProcess"
                }]
            }, {
                itemspace: 12,
                items: [{
                    xtype: "ComboBox",
                    title: "启动前事件",
                    readonly: true,
                    id: "beforeStart",
                    name: "beforeStartServiceName",
                    field: ["beforeStartServiceId"],
                    labelWidth: 90,
                    width: 322,
                    loadonce:false,
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
                    readonly: true,
                    id: "afterStart",
                    loadonce:false,
                    name: "afterStartServiceName",
                    field: ["afterStartServiceId"],
                    labelWidth: 90,
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
                    title: "异步调用",
                    readonly: true,
                    labelFirst: false,
                    name: "afterStartServiceAync"
                }]
            }, {
                items: [{
                    xtype: "ComboBox",
                    title: "结束前事件",
                    id: "beforeEnd",
                    readonly: true,
                    loadonce:false,
                    name: "beforeEndServiceName",
                    field: ["beforeEndServiceId"],
                    labelWidth: 90,
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
                    readonly: true,
                    id: "afterEnd",
                    loadonce:false,
                    name: "afterEndServiceName",
                    field: ["afterEndServiceId"],
                    labelWidth: 90,
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
                    labelWidth: 90,
                    width: 302,
                    submitName: false,
                    id: "startUEL",
                    name: "logicUel"
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
                title: "收起",
                handler: function () {
                    g.hide();
                    g.parent.moreShow=false;
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
    }
})
;