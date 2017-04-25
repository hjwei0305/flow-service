/**
 * Created by fly on 2017/4/18.
 */
EUI.UELSettingView = EUI.extend(EUI.CustomUI, {
    title: null,
    initComponent: function () {
        this.window = EUI.Window({
            width: 600,
            height: 460,
            padding: 15,
            buttons: this.getButtons(),
            items: [{
                xtype: "TabPanel",
                isOverFlow: false,
                defaultConfig: {
                    iframe: false,
                    closable: false
                },
                items: [this.getNormalTab(), this.getExcutorTab(),
                    this.getOperateTab(), this.getNotifyTab()]
            }]
        });
        this.initUEditor();
    },
    getButtons: function () {
        var g = this;
        return [{
            title: "保存配置",
            selected: true,
            handler: function () {
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
            title: "常规配置",
            xtype: "FormPanel",
            padding: 10,
            defaultConfig: {
                width: 300,
                xtype: "TextField",
                colon: false
            },
            style: {
                padding: "10px 30px"
            },

            items: [{
                title: "节点名称",
                name: "name"
            }, {
                title: "节点说明",
                name: "depict"
            }, {
                xtype: "ComboBox",
                title: "工作界面",
                name: "",
                field: [""],
                store: {
                    url: _ctxPath + ""
                }
            }, {
                xtype: "ComboBox",
                title: "服务调用",
                name: "",
                field: [""],
                store: {
                    url: _ctxPath + ""
                }
            }]
        };
    },
    getExcutorTab: function () {
        return {
            xtype: "FormPanel",
            title: "执行人配置",
            style: {
                padding: "20px 60px"
            },
            defaultConfig: {
                xtype: "FieldGroup",
                width: 570,
                labelWidth: 100,
            },
            items: [{
                title: "指定执行人",
                colon: false,
                items: [{
                    xtype: "TextField",
                    readonly: true,
                    width: 280,
                    name: ""
                }, {
                    xtype: "Button",
                    title: "选择",
                    handler: function () {

                    }
                }]
            }, {
                title: "指定岗位",
                colon: false,
                items: [{
                    xtype: "TextField",
                    readonly: true,
                    width: 280,
                    name: ""
                }, {
                    xtype: "Button",
                    title: "选择",
                    handler: function () {

                    }
                }]
            }, {
                title: "指定岗位类别",
                colon: false,
                items: [{
                    xtype: "TextField",
                    readonly: true,
                    width: 280,
                    name: ""
                }, {
                    xtype: "Button",
                    title: "选择",
                    handler: function () {

                    }
                }]
            }, {
                title: "流程发起人",
                hidden: true,
                colon: false,
                items: [{
                    xtype: "TextField",
                    readonly: true,
                    name: ""
                }, {
                    xtype: "Button",
                    title: "选择",
                    handler: function () {

                    }
                }]
            }, {
                title: "任意执行人",
                colon: false,
                items: [{
                    xtype: "TextField",
                    readonly: true,
                    width: 280,
                    name: ""
                }, {
                    xtype: "Button",
                    title: "选择",
                    handler: function () {

                    }
                }]
            }]
        };
    },
    getOperateTab: function () {
        return {
            title: "操作配置",
            xtype: "FormPanel",
            padding: 10,
            defaultConfig: {
                width: 300,
                xtype: "TextField",
                labelWidth: 140,
                colon: false
            },
            style: {
                padding: "10px 30px"
            },
            items: [{
                xtype: "CheckBox",
                title: "允许流程发起人终止",
                name: ""
            }, {
                xtype: "CheckBox",
                title: "允许上步撤回",
                name: ""
            }, {
                xtype: "CheckBox",
                title: "允许挂起流程",
                name: ""
            }, {
                xtype: "TextField",
                title: "挂起流程备注",
                name: "",
                hidden: true
            }]
        };
    },
    getNotifyTab: function () {
        return {
            title: "通知配置",
            xtype: "FormPanel",
            padding: 10,
            defaultConfig: {
                width: 300,
                xtype: "TextField",
                colon: false
            },
            items: [{
                xtype: "Container",
                width: "100%",
                height: "100%",
                html: "<script id='editor' type='text/plain' style='width:500px;height:400px;'></script>"
            }]
        };
    },
    initUEditor: function () {
        UE.getEditor('editor');
    }
})
;