/**
 * 内置审批单页面
 */
EUI.BuiltInApproveView = EUI.extend(EUI.CustomUI, {
    renderTo: "",
    selectedNodeId: "",  //当前选中的节点的ID
    selectedNodeName: "",  //当前选中的节点的name
    selectedNodeCode:"",   //当前选中节点的code
    selectedNodeTenantCode:"",  //当前选中节点的租户代码
    selectedNodeCodePath:"",    //当前选中节点的codePath
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 0,
            items: [this.initLeft(), this.initCenterContainer()]
        });
        this.gridCmp = EUI.getCmp("gridPanel");
        this.treeCmp = EUI.getCmp("treePanel");
        this.addEvents();
    },
    addEvents: function () {
        var g = this;
        $(".condetail-flowHistory").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            Flow.FlowHistory({
                businessId: data.id
            })
        });
        $(".condetail-start").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.startFlow(data);
        });
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
        win = EUI.Window({
            title: "内置审批单",
            height: 350,
            width:420,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBuiltInApprove",
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 80,
                    name: "id",
                    allowBlank:false,
                    width: 270,
                    colon:false,
                    hidden: true
                },{
                    xtype: "TextField",
                    title: "组织机构Id",
                    allowBlank:false,
                    labelWidth: 80,
                    colon:false,
                    name: "orgId",
                    width: 270,
                    hidden: true
                },{
                    xtype: "TextField",
                    title: "组织机构",
                    labelWidth: 80,
                    colon:false,
                    allowBlank:false,
                    readonly:true,
                    name: "orgName",
                    width: 270
                },{
                    xtype: "TextField",
                    title: "组织机构code",
                    labelWidth: 80,
                    colon:false,
                    allowBlank:false,
                    name: "orgCode",
                    width: 270,
                    hidden: true
                },{
                    xtype: "TextField",
                    title: "组织机构orgPath",
                    labelWidth: 80,
                    colon:false,
                    allowBlank:false,
                    name: "orgPath",
                    width: 270,
                    hidden: true
                },{
                    xtype: "TextField",
                    title: "租户代码",
                    labelWidth: 80,
                    colon:false,
                    allowBlank:false,
                    name: "tenantCode",
                    width: 270,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "名称",
                    labelWidth: 80,
                    name: "name",
                    width: 270,
                    colon:false,
                    allowBlank:false
                }, {
                    xtype: "TextField",
                    title: "单价",
                    labelWidth: 80,
                    name: "unitPrice",
                    width: 270,
                    colon:false,
                    allowBlank:false
                }, {
                    xtype: "TextField",
                    title: "数量",
                    labelWidth: 80,
                    name: "count",
                    width: 270,
                    colon:false,
                    allowBlank:false
                }, {
                    xtype: "TextArea",
                    title: "说明",
                    labelWidth: 80,
                    name: "workCaption",
                    id:"caption",
                    colon:false,
                    width: 270,
                    height:130,
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
        EUI.getCmp("updateBuiltInApprove").loadData(data);
    },

    //启动流程
    startFlow: function (data) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: "提示",
            msg: "确定立即启动流程吗？",
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    infoBox.remove();
                    Flow.FlowStart({
                        businessKey: data.id,
                        businessModelCode:'com.ecmp.flow.entity.DefaultBusinessModel',
                        url: _ctxPath + "/builtInApprove/startFlow",
                        afterSubmit:function(){
                            EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                        }
                    })
                    // var myMask = EUI.LoadMask({
                    //     msg: "正在启动，请稍后..."
                    // });
                    // EUI.Store({
                    //     url: _ctxPath + "/builtInApprove/startFlow",
                    //     params: {
                    //         //  key:'test0607',
                    //         //以后切换成业务实体或者流程类型
                    //         //typeId:流程类型ID
                    //         businessModelCode:'com.ecmp.flow.entity.DefaultBusinessModel',//业务实体Code
                    //         businessKey: data.id
                    //     },
                    //     success: function (result) {
                    //         myMask.hide();
                    //         if(!result.data.flowTypeList && !result.data.flowInstance && !result.data.nodeInfoList){
                    //     var status = {
                    //                 msg:"找不到流程定义",
                    //                 success: false,
                    //                 showTime: 4
                    //             };
                    //             EUI.ProcessStatus(status);
                    //             return;
                    //         }
                    //         if(result.data.flowTypeList && !result.data.nodeInfoList){
                    //             var status = {
                    //                 msg:"流程配置有误",
                    //                 success: false,
                    //                 showTime: 4
                    //             };
                    //             EUI.ProcessStatus(status);
                    //             return;
                    //         }
                    //         if(result.data.nodeInfoList){
                    //             var flowTypeList  = result.data.flowTypeList;
                    //             Flow.FlowStart({
                    //                 businessKey: data.id,
                    //                 businessModelCode:'com.ecmp.flow.entity.DefaultBusinessModel',
                    //                 data:result.data,
                    //                 url:"builtInApprove"
                    //             })
                    //         }
                    //         //       EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                    //     },
                    //     failure: function (result) {
                    //         EUI.ProcessStatus(result);
                    //         myMask.hide();
                    //     }
                    // });
                }
            }, {
                title: "取消",
                handler: function () {
                    infoBox.remove();
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
    initLeft: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "west",
            border: false,
            width: 420,
            itemspace: 0,
            layout: "border",
            items: [this.initTopBar(), this.initTree()]
        }
    },
    initTopBar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            border: false,
            padding: 0,
            isOverFlow: false,
            items: ['->', {
                xtype:"SearchBox",
                width:159,
                displayText:g.lang.searchDisplayText,
                onSearch: function (v) {
                    g.treeCmp.search(v);
                    g.selectedNodeId = null;
                    g.selectedNodeName = null;
                    g.selectedNodeCode = null;
                    g.selectedNodeTenantCode = null;
                    g.selectedNodeCodePath = null;
                },
                afterClear: function () {
                    g.treeCmp.reset();
                    g.selectedNodeId = null;
                    g.selectedNodeName = null;
                    g.selectedNodeCode = null;
                    g.selectedNodeTenantCode = null;
                    g.selectedNodeCodePath = null;
                }

            }]
        };
    },
    initTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            region: "center",
            url: _ctxPath + "/flowDefination/listAllOrgs",
            border: true,
            id: "treePanel",
            searchField:["name"],
            showField: "name",
            style: {
                "background": "#fff"
            },
            onSelect: function (node) {
                console.log(node);
                if (node.children.length) {
                    g.selectedNodeId = node.id;
                    g.selectedNodeName = node.name;
                    g.selectedNodeCode = node.code;
                    g.selectedNodeTenantCode = node.tenantCode;
                    g.selectedNodeCodePath = node.codePath;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/builtInApprove/list",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_orgId: g.selectedNodeId
                        }
                    }, true)
                }
                if (!node.children.length) {
                    g.selectedNodeId = node.id;
                    g.selectedNodeName = node.name;
                    g.selectedNodeCode = node.code;
                    g.selectedNodeTenantCode = node.tenantCode;
                    g.selectedNodeCodePath = node.codePath;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/builtInApprove/list",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_orgId: g.selectedNodeId
                        }
                    }, true)
                }
            },
            afterItemRender: function (nodeData) {
                if (nodeData.frozen) {
                    var nodeDom = $("#" + nodeData.id);
                    if (nodeDom == []) {
                        return;
                    }
                    var itemCmp = $(nodeDom[0].children[0]);
                    itemCmp.addClass("ux-tree-freeze");
                    itemCmp.find(".ux-tree-title").text(itemCmp.find(".ux-tree-title").text() + "(已冻结)");
                }
            },
            afterShowTree:function(data){
                this.setSelect(data[0].id);
            }
        }
    },
     initCenterContainer: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            itemspace: 0,
            layout: "border",
            border: false,
            items: [{
                xtype: "ToolBar",
                region: "north",
                isOverFlow: false,
                padding: 0,
                height: 40,
                border: false,
                items: [{
                    xtype: "Button",
                    title: "新增",
                    selected: true,
                    handler: function () {
                        if(!g.selectedNodeId){
                            var status = {
                                msg:"请选择组织机构",
                                success: false,
                                showTime: 4
                            };
                            EUI.ProcessStatus(status);
                            return;
                        }
                        g.addBuiltInApprove();
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
            }, {
                xtype: "GridPanel",
                region: "center",
                id: "gridPanel",
                style: {
                    "border-radius": "3px"
                },
                gridCfg: {
                    loadonce: true,
                   // url: _ctxPath + "/builtInApprove/list",
                    postData: {
                        S_createdDate: "DESC"
                    },
                    colModel: [{
                        label: "操作",
                        name: "operate",
                        index: "operate",
                        width: '25',
                        align: "center",
                        formatter: function (cellvalue, options, rowObject) {
                            if(	"INIT" == rowObject.flowStatus){
                                var strVar = "<div class='condetail-operate'>" +
                                    "<div class='condetail-start'title='启动流程'></div>"
                                    + "<div class='condetail-update' title='编辑'></div>"
                                    + "<div class='condetail-delete'  title='删除'></div>" +
                                    "</div>";
                            }
                            if(	"INPROCESS" == rowObject.flowStatus){
                                var strVar = "<div class='condetail-operate'>"+
                                    "<div class='condetail-flowHistory'title='流程历史'></div>"
                                    + "<div class='condetail-update' title='编辑'></div>"
                                    + "<div class='condetail-delete'  title='删除'></div>" +
                                    "</div>";
                            }
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
                        index: "name",
                        width: '30%'
                    }, {
                        label: "当前流程状态",
                        name: "flowStatus",
                        index: "flowStatus",
                        hidden: false,
                        width: '40%',
                        formatter : function(cellvalue, options, rowObject) {
                            var strVar = '';
                            if('INIT' == rowObject.flowStatus){
                                strVar = "未启动";
                            }
                            else if('INPROCESS' == rowObject.flowStatus){
                                strVar = "处理中";
                            }else if('COMPLETED' == rowObject.flowStatus){
                                strVar = "流程结束";
                            }
                            return strVar;
                        }
                    }, {
                        label: "组织机构代码",
                        name: "orgCode",
                        index: "orgCode",
                        hidden: true
                    }, {
                        label: "组织机构Id",
                        name: "orgId",
                        index: "orgId",
                        width: '50%',
                        hidden: true
                    }, {
                        label: "组织机构名称",
                        name: "orgName",
                        index: "orgName",
                        width: '50%',
                        hidden: true
                    }, {
                        label: "组织机构层级路径",
                        name: "orgPath",
                        index: "orgPath",
                        width: '50%',
                        hidden: true
                    }, {
                        label: "租户代码",
                        name: "tenantCode",
                        index: "tenantCode",
                        width: '50%',
                         hidden: true
                    }, {
                        label: "优先级别",
                        name: "priority",
                        index: "priority",
                        hidden: true
                    }, {
                        label: "单价",
                        name: "unitPrice",
                        index: "unitPrice",
                        width: '50%'
                    }, {
                        label: "数量",
                        name: "count",
                        index: "count",
                        width: '50%'
                    }, {
                        label: "工作说明",
                        name: "workCaption",
                        index: "workCaption",
                        width: '50%'
                    }],
                    ondbClick: function () {
                        var rowData = EUI.getCmp("gridPanel").getSelectRow();
                        g.getValues(rowData.id);
                    }
                }
            }]
        }
    },
    addBuiltInApprove: function () {
        var g = this;
        win = EUI.Window({
            title: "新增内置表单",
            height: 360,
            width:400,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBuiltInApprove",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "组织机构ID",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgId",
                    colon:false,
                    width: 270,
                    value:g.selectedNodeId,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "组织机构",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgName",
                    colon:false,
                    width: 270,
                    readonly:true,
                    value:g.selectedNodeName
                },{
                    xtype: "TextField",
                    title: "组织机构code",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgCode",
                    colon:false,
                    width: 270,
                    readonly:true,
                    value:g.selectedNodeCode,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "组织机构租户代码",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "tenantCode",
                    colon:false,
                    width: 270,
                    readonly:true,
                    value:g.selectedNodeTenantCode,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "组织机构codePath",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgPath",
                    colon:false,
                    width: 270,
                    readonly:true,
                    value:g.selectedNodeCodePath,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "名称",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "name",
                    colon:false,
                    width: 270
                },{
                    xtype: "TextField",
                    title: "单价",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "unitPrice",
                    colon:false,
                    width: 270
                },{
                    xtype: "TextField",
                    title: "数量",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "count",
                    colon:false,
                    width: 270
                }, {
                    xtype: "TextArea",
                    title: "说明",
                    labelWidth: 70,
                    name: "workCaption",
                    id:"caption",
                    width: 270,
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