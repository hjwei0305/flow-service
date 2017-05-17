/**
 * 内置审批单页面
 */
EUI.BuiltInApproveView = EUI.extend(EUI.CustomUI, {
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
                xtype: "Button",
                title: "新增",
                selected:true,
                handler: function () {
                    g.addBuiltInApprove();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: "请输入任务名进行搜索",
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("gridPanel").setPostParams({
                                Q_LK_taskName: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_taskName: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        };
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
                loadonce:true,
                /*url: _ctxPath + "/builtInApprove/listDefBusinessModel",
                postData: {
                    S_createdDate: "ASC"
                },*/
                colModel: [{
                    label: "操作",
                    name: "operate",
                    index: "operate",
                    width: '20%',
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail-operate'>" +
                            "<div class='condetail-start'></div>"
                            + "<div class='condetail-update'></div>"
                            + "<div class='condetail-delete'></div></div>";
                        return strVar;
                    }
                }, {
                    label: "ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "业务名称",
                    name: "name",
                    index: "name"
                }, {
                    label: "当前流程状态",
                    name: "flowStatus",
                    index: "flowStatus",
                    hidden: true
                }, {
                    label: "组织机构代码",
                    name: "orgCode",
                    index: "orgCode",
                    hidden: true
                }, {
                    label: "组织机构Id",
                    name: "orgId",
                    index: "orgId",
                    hidden: true
                }, {
                    label: "组织机构名称",
                    name: "orgName",
                    index: "orgName",
                    hidden: true
                }, {
                    label: "组织机构层级路径",
                    name: "orgPath",
                    index: "orgPath",
                    hidden: true
                }, {
                    label: "优先级别",
                    name: "priority",
                    index: "priority",
                    hidden: true
                }, {
                    label: "工作说明",
                    name: "workCaption",
                    index: "workCaption"
                }],
                data:[{
                    id:"1",
                    name:"ddd",
                    workCaption:"wwww"
                },{
                    id:"2",
                    name:"ccc",
                    workCaption:"aaaa"
                }],
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    //编辑和删除按钮的事件添加
    addEvents: function () {
        var g = this;
        $(".condetail-update").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.showBuiltInApproveWin(data);
        });
        $(".condetail-delete").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.deleteBuiltInApproveWin(data);
        });
    },
    //编辑按钮
    showBuiltInApproveWin: function (data) {
        var g = this;
        var win = EUI.Window({
            title: "内置审批单",
            height: 190,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBuiltInApprove",
                height:200,
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    name: "id",
                    width: 220,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "名称",
                    labelWidth: 70,
                    name: "name",
                    width: 220,
                    colon:false,
                    value: data.name,
                    allowBlank:false
                }, {
                    xtype: "TextArea",
                    title: "说明",
                    labelWidth: 70,
                    name: "workCaption",
                    id:"caption",
                    colon:false,
                    width: 220,
                    height:130,
                    value: data.workCaption,
                    allowBlank:false
                }]
            }],
            buttons: [{
                title: "保存",
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateBuiltInApprove");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveBuiltInApprove(data);
                }
            }, {
                title: "取消",
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    //删除按钮
    deleteBuiltInApproveWin: function (data) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: "提示",
            msg: "确定删除吗？",
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: "正在删除，请稍后..."
                    });
                    EUI.Store({
                        url: _ctxPath + "/builtInApprove/delete",
                        params: {
                            id: data.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            if (result.success) {
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            }
                        },
                        failure: function (result) {
                            EUI.ProcessStatus(result);
                            myMask.hide();
                        }
                    });
                }
            }, {
                title: "取消",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    },
    //新增按钮
    addBuiltInApprove: function () {
        var g = this;
        var win = EUI.Window({
            title: "新增内置表单",
            height: 190,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBuiltInApprove",
                height:200,
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "名称",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "name",
                    colon:false,
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: "说明",
                    labelWidth: 70,
                    name: "workCaption",
                    id:"caption",
                    width: 220,
                    height:130,
                    colon:false,
                    allowBlank:false
                }]
            }],
            buttons: [{
                title:"保存",
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addBuiltInApprove");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveBuiltInApprove(data);
                }
            }, {
                title: "取消",
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    //保存
    saveBuiltInApprove: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: "正在保存，请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/builtInApprove/save",
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
            }
        });
    }
});