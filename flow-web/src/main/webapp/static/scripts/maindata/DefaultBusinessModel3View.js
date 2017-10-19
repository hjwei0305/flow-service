/**
 * 采购页面
 */
EUI.DefaultBusinessModel3View = EUI.extend(EUI.CustomUI, {
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
            items: [this.initTree(), this.initGrid()]
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
            title: "编辑销售申请",
            iconCss:"ecmp-eui-edit",
            height: 430,
            width:430,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBuiltInApprove",
                padding: 10,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "id",
                    colon:false,
                    width: 290,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "组织机构ID",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgId",
                    colon:false,
                    width: 290,
                    value:g.selectedNodeId,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "组织机构",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgName",
                    colon:false,
                    width: 290,
                    readonly:true,
                    value:g.selectedNodeName,
                    style:{"margin-left":30}
                },{
                    xtype: "TextField",
                    title: "组织机构code",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgCode",
                    colon:false,
                    width: 290,
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
                    width: 290,
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
                    width: 290,
                    readonly:true,
                    value:g.selectedNodeCodePath,
                    hidden:true
                },{
                    xtype: "Label",
                    height:25,
                    isOverFlow:false,
                    content:  "<span style='font-weight: bold'>" + "申请概要" + "</span>"
                },{
                    xtype: "TextField",
                    title: "业务类型",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "name",
                    colon:false,
                    width: 290,
                    style:{"margin-left":30}
                },{
                    xtype: "TextField",
                    title: "申请说明",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "applyCaption",
                    colon:false,
                    width: 290,
                    style:{"margin-left":30}
                },{
                    xtype: "Label",
                    height:25,
                    isOverFlow:false,
                    content:  "<span style='font-weight: bold'>" + "申请详情" + "</span>"
                },{
                    xtype: "NumberField",
                    title: "单价",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "unitPrice",
                    colon:false,
                    width: 290,
                    precision:2,
                    style:{"margin-left":30}
                },{
                    xtype: "NumberField",
                    title: "数量",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "count",
                    colon:false,
                    width: 290,
                    style:{"margin-left":30}
                },{
                    xtype: "TextArea",
                    title: "备注说明",
                    labelWidth: 70,
                    name: "workCaption",
                    id:"caption",
                    width: 290,
                    height:130,
                    colon:false,
                    allowBlank:false,
                    style:{"margin-left":30}
                }]
            }],
            buttons: [{
                title: "取消",
                handler: function () {
                    win.remove();
                }
            },{
                title: "保存",
               selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateBuiltInApprove");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveBuiltInApprove(data);
                }
            }]
        });
        EUI.getCmp("updateBuiltInApprove").loadData(data);
    },

    //启动流程
    startFlow: function (data) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: "确定立即启动流程吗？",
            buttons: [{
                title: "取消",
                handler: function () {
                    infoBox.remove();
                }
            },{
                title: "确定",
               selected: true,
                handler: function () {
                    infoBox.remove();
                    Flow.FlowStart({
                        businessId: data.id,
                        businessModelCode:'com.ecmp.flow.entity.DefaultBusinessModel3',
                        url: _ctxPath + "/defaultBusinessModel3/startFlow",
                        afterSubmit:function(){
                            EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                        }
                    })
                }
            }]
        });
    },
    //删除按钮
    deleteBuiltInApproveWin: function (data) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.ifDelMsgText,
            buttons: [{
                title: "取消",
                handler: function () {
                    infoBox.remove();
                }
            },{
                title: "确定",
               selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: "正在删除，请稍后..."
                    });
                    EUI.Store({
                        url: _ctxPath + "/defaultBusinessModel3/delete",
                        params: {
                            id: data.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                        },
                        failure: function (result) {
                            EUI.ProcessStatus(result);
                            myMask.hide();
                        }
                    });
                }
            }]
        });
    },
    initTopBar: function () {
        var g = this;
        return  ['->', {
                xtype:"SearchBox",
                width: 200,
                displayText: g.lang.searchDisplayText,
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

            }];
    },
    initTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            title: "组织机构",
            tbar: this.initTopBar(),
            region: "west",
            url: _ctxPath + "/flowDefination/listAllOrgs",
            border: true,
            id: "treePanel",
            searchField:["name"],
            showField: "name",
            style: {
                "background": "#fff"
            },
            onSelect: function (node) {
                    g.selectedNodeId = node.id;
                    g.selectedNodeName = node.name;
                    g.selectedNodeCode = node.code;
                    g.selectedNodeTenantCode = node.tenantCode;
                    g.selectedNodeCodePath = node.codePath;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/defaultBusinessModel3/list",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_orgId: g.selectedNodeId
                        }
                    }, true)
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
    initGridBar: function(){
        var g = this;
        return [{
            xtype: "Button",
            title: "新增",
            iconCss:"ecmp-common-add",
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
            width: 200,
            displayText: "请输入名称进行搜索",
            onSearch: function (value) {
                EUI.getCmp("gridPanel").setPostParams({
                        Q_LK_name: value
                    },true);
            }
        }];
    },
    initGrid: function () {
        var g = this;
        return  {
                xtype: "GridPanel",
                title: "销售单据管理",
                tbar: this.initGridBar(),
                region: "center",
                id: "gridPanel",
                style: {
                    "border-radius": "3px"
                },
                gridCfg: {
                    loadonce: true,
                    datatype: "local",
                    shrinkToFit: false,//固定宽度
                    postData: {
                        S_createdDate: "DESC"
                    },
                    colModel: [{
                        label: "操作",
                        name: "operate",
                        index: "operate",
                        width: 100,
                        align: "center",
                        formatter: function (cellvalue, options, rowObject) {
                            if(	"INIT" == rowObject.flowStatus){
                                return   "<i class='ecmp-common-edit condetail-update icon-space' title='编辑'></i>"+
                                    "<i class='ecmp-common-delete condetail-delete icon-space' title='删除'></i>"+
                                    "<i class='ecmp-flow-start condetail-start' title='启动流程'></i>";
                            }else  if ("INPROCESS" == rowObject.flowStatus || "COMPLETED"  == rowObject.flowStatus) {
                                return "<i class='ecmp-flow-history condetail-flowHistory' title='流程历史'></i>";
                            }
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
                        width: 110
                    }, {
                        label: "申请说明",
                        name: "applyCaption",
                        index: "applyCaption",
                        width: 110
                    }, {
                        label: "当前流程状态",
                        name: "flowStatus",
                        index: "flowStatus",
                        hidden: false,
                        width: 110,
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
                        width: 80
                    }, {
                        label: "数量",
                        name: "count",
                        index: "count",
                        width: 80
                    }, {
                        label: "金额",
                        name: "sum",
                        index: "sum",
                        width: 80
                    }, {
                        label: "工作说明",
                        name: "workCaption",
                        index: "workCaption",
                        width: 110
                    }],
                    ondbClick: function () {
                        var rowData = EUI.getCmp("gridPanel").getSelectRow();
                        g.getValues(rowData.id);
                    }
                }
        };
    },
    addBuiltInApprove: function () {
        var g = this;
        win = EUI.Window({
            title: "新增销售申请",
            iconCss:"ecmp-eui-add",
            height: 430,
            width:430,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBuiltInApprove",
                padding: 10,
                items: [{
                    xtype: "TextField",
                    title: "组织机构ID",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgId",
                    colon:false,
                    width: 290,
                    value:g.selectedNodeId,
                    hidden:true
                },{
                    xtype: "TextField",
                    title: "组织机构",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgName",
                    colon:false,
                    width: 290,
                    readonly:true,
                    value:g.selectedNodeName,
                    style:{
                        "margin-left":30
                    }
                },{
                    xtype: "TextField",
                    title: "组织机构code",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "orgCode",
                    colon:false,
                    width: 290,
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
                    width: 290,
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
                    width: 290,
                    readonly:true,
                    value:g.selectedNodeCodePath,
                    hidden:true
                },{
                    xtype: "Label",
                    height:25,
                    isOverFlow:false,
                    content:  "<span style='font-weight: bold'>" + "申请概要" + "</span>"
                },{
                    xtype: "TextField",
                    title: "业务类型",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "name",
                    colon:false,
                    width: 290,
                    style:{"margin-left":30}
                },{
                    xtype: "TextField",
                    title: "申请说明",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "applyCaption",
                    colon:false,
                    width: 290,
                    style:{"margin-left":30}
                },{
                    xtype: "Label",
                    height:25,
                    isOverFlow:false,
                    content:  "<span style='font-weight: bold'>" + "申请详情" + "</span>"
                },{
                    xtype: "NumberField",
                    title: "单价",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "unitPrice",
                    colon:false,
                    width: 290,
                    precision:2,
                    style:{"margin-left":30}
                },{
                    xtype: "NumberField",
                    title: "数量",
                    allowBlank:false,
                    labelWidth: 70,
                    name: "count",
                    colon:false,
                    width: 290,
                    style:{"margin-left":30}
                },{
                    xtype: "TextArea",
                    title: "备注说明",
                    labelWidth: 70,
                    name: "workCaption",
                    id:"caption",
                    width: 290,
                    height:130,
                    colon:false,
                    allowBlank:false,
                    style:{"margin-left":30}
                }]
            }],
            buttons: [{
                title: "取消",
                handler: function () {
                    win.remove();
                }
            },{
                title:"保存",
               selected: true,
                handler: function () {
                    var form = EUI.getCmp("addBuiltInApprove");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveBuiltInApprove(data);
                }
            }]
        });
    },
    //保存
    saveBuiltInApprove: function (data) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在保存，请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/defaultBusinessModel3/save",
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
    },
    initTitle: function (title) {
        return {
            xtype: "Container",
            region: "north",
            border: false,
            width:80,
            padding:0,
            height:30,
            isOverFlow: false,
            style:{
                "margin-top":"10px"
            },
            html: "<div style='font-size:17px;overflow:hidden;title:" + title + ";'>" + title + "</div>"
        }
    }
});