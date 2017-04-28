/**
 * 显示页面
 */
EUI.PositionView = EUI.extend(EUI.CustomUI, {
    initComponent : function(){
        EUI.Container({
            renderTo : this.renderTo,
            layout : "border",
            border : false,
            padding : 8,
            itemspace : 0,
            items : [this.initTbar(), this.initGrid()]
        });
        this.addEvents();
    },
    initTbar : function(){
        var g=this;
        return{
            xtype : "ToolBar",
            region : "north",
            height : 40,
            padding : 0,
            style : {
                overflow : "hidden"
            },
            border : false,
            items:['->',{
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
    initGrid : function(){
        var g=this;
        return {
            xtype : "GridPanel",
            region : "center",
            id : "gridPanel",
            style : {
                "border" : "1px solid #aaa",
                "border-radius" : "3px"
            },
            gridCfg : {
           //     loadonce:true,
                	url : "http://localhost:8081/flow/basic/position/findByOrgId",
                postData:{
                    orgId:"141414cd-5b9d-1de9-815b-9dce05050000"
                },
                colModel : [{
                    label : "操作",
                    name : "operate",
                    index : "operate",
                    width : 100,
                    align : "center",
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = "<div class='btn_operate'>"
                            + "<div class='agreeBtn'></div>"
                            + "<div class='nagreeBtn'></div></div>";
                        return strVar;
                    }
                },/*{
                    label : "ID",
                    name : "id",
                    index : "id",
                    //hidden : true
                },{
                    label : "流程名称",
                    name : "flowName",
                    index : "flowName",
                    title : false
                },*/{
                    label : "任务名",
                    name : "taskName",
                    index : "taskName",
                    title : false
                },/*{
                    label : "任务定义KEY",
                    name : "taskDefKey",
                    index : "taskDefKey",
                    title : false
                },*/{
                    label : "任务表单URL",
                    name : "taskFormUrl",
                    index : "taskFormUrl",
                    title : false,
                   hidden : true
                },{
                    label : "任务状态" ,
                    name : "taskStatus",
                    index : "taskStatus",
                    title : false
                },{
                    label : "代理状态" ,
                    name : "proxyStatus",
                    index : "proxyStatus",
                    title : false
                },/*{
                    label : "流程实例ID" ,
                    name : "flowInstanceId",
                    index : "flowInstanceId",
                    title : false
                },{
                    label : "流程定义ID" ,
                    name : "flowDefinitionId",
                    index : "flowDefinitionId",
                    title : false
                },*/{
                    label : "执行人名称" ,
                    name : "executorName",
                    index : "executorName",
                    title : false
                },{
                    label : "执行人账号" ,
                    name : "executorAccount",
                    index : "executorAccount",
                    title : false
                },{
                    label : "候选人账号" ,
                    name : "candidateAccount",
                    index : "candidateAccount",
                    title : false
                },/*{
                    label : "执行时间" ,
                    name : "executeDate",
                    index : "executeDate",
                    title : false
                },*/{
                    label : "描述" ,
                    name : "depict",
                    index : "depict",
                    title : false
                },/*{
                    label : "创建人" ,
                    name : "createdBy",
                    index : "createdBy",
                    title : false
                },{
                    label : "创建时间" ,
                    name : "createdDate",
                    index : "createdDate",
                    title : false
                },{
                    label : "最后更新者" ,
                    name : "lastModifiedBy",
                    index : "lastModifiedBy",
                    title : false
                },{
                    label : "最后更新时间" ,
                    name : "lastModifiedDate",
                    index : "lastModifiedDate",
                    title : false
                },*/{
                    label : "引擎流程任务ID" ,
                    name : "actTaskId",
                    index : "actTaskId",
                    title : false
                },/*{
                    label : "优先级" ,
                    name : "priority",
                    index : "priority",
                    title : false
                },{
                    label : "所属人" ,
                    name : "ownerAccount",
                    index : "ownerAccount",
                    title : false
                },{
                    label : "所属人名称" ,
                    name : "ownerName",
                    index : "ownerName",
                    title : false
                },{
                    label : "实际任务类型" ,
                    name : "actType",
                    index : "actType",
                    title : false
                },{
                    label : "签收时间" ,
                    name : "actClaimTime",
                    index : "actClaimTime",
                    title : false
                },{
                    label : "实际触发时间" ,
                    name : "actDueDate",
                    index : "actDueDate",
                    title : false
                },{
                    label : "实际任务定义KEY" ,
                    name : "actTaskKey",
                    index : "actTaskKey",
                    title : false
                },{
                    label : "关联流程实例的ID(隐藏)" ,
                    name : "flowInstance.id",
                    index : "flowInstance.id",
                    title : false
                },{
                    label : "关联流程实例" ,
                    name : "flowInstance.name",
                    index : "flowInstance.name",
                    title : false
                }*/],

                ondbClick : function(){
                    var rowData=EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    addEvents : function(){
        var g = this;
        $(".agreeBtn").live("click",function(){
            var data=EUI.getCmp("gridPanel").getSelectRow();
            //  var tabPanel=parent.homeView.getTabPanel();
            console.log(data);
            //g.updateFlowType(data);
        });
        $(".nagreeBtn").live("click",function(){
            var rowData=EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            // var infoBox = EUI.MessageBox({
            //     title : "提示",
            //     msg : "确定删除吗？",
            //     buttons :[{
            //         title : "确定",
            //         selected : true,
            //         handler : function(){
            //             infoBox.remove();
            //             var myMask = EUI.LoadMask({
            //                 msg : "正在删除,请稍后...."
            //             });
            //             EUI.Store({
            //                 url : "http://localhost:8081/flow/maindata/flowType/delete",
            //                 params : {
            //                     id:rowData.id
            //                 },
            //                 success : function(){
            //                     myMask.hide();
            //                     EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            //                 },
            //                 failure : function(){
            //                     myMask.hide();
            //                 }
            //             });
            //         }
            //     },{
            //         title : "取消",
            //         handler : function(){
            //             infoBox.remove();
            //         }
            //     }]
            // });
        });
    },
    updateFlowType : function(data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title : "修改流程类型",
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "updateFlowType",
                padding : 0,
                items : [{
                    xtype : "TextField",
                    title : "ID",
                    labelWidth : 90,
                    name : "id",
                    width : 220,
                    maxLength : 10,
                    value:data.id,
                 //   hidden : true
                },{
                    xtype : "TextField",
                    title : "代码",
                    labelWidth : 90,
                    name : "code",
                    width : 220,
                    maxLength : 10,
                    value:data.code
                }, {
                    xtype : "TextField",
                    title : "名称",
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                    value:data.name
                }, {
                    xtype : "TextField",
                    title : "描述",
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                    value:data.depict
                },{
                    xtype : "ComboBox",
                    title : "所属业务实体",
                    labelWidth : 90,
                    name : "businessModel.name",
                    width : 220,
                     value:data["businessModel.name"]||"",
                    submitValue:{
                        "businessModel.id":data["businessModel.id"]||""
                    },
                    store : {
                        url : "http://localhost:8081/flow/maindata/flowType/findAllBusinessModelName",
                    },
                    field : ["businessModel.id"],
                    reader : {
                        name : "name",
                        field : ["id"]
                    },
                }/*, {
                    xtype : "TextField",
                    title : "创建人",
                    labelWidth : 90,
                    name : "createdBy",
                    width : 220,
                    value:data.createdBy
                }, {
                    xtype : "TextField",
                    title : "创建时间",
                    labelWidth : 90,
                    name : "createdDate",
                    width : 220,
                    value:data.createdDate
                }, {
                    xtype : "TextField",
                    title : "最后更新者",
                    labelWidth : 90,
                    name : "lastModifiedBy",
                    width : 220,
                    value:data.lastModifiedBy
                }, {
                    xtype : "TextField",
                    title : "最后更新时间",
                    labelWidth : 90,
                    name : "lastModifiedDate",
                    width : 220,
                    value:data.lastModifiedDate
                }*/]
            }],
            buttons : [{
                title : "保存",
                selected : true,
                handler : function() {
                    var form = EUI.getCmp("updateFlowType");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代码"
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入名称"
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入描述"
                        });
                        return;
                    }
                    if (!data["businessModel.name"]) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请选择所属业务实体模型"
                        });
                        return;
                    }
                 /*   if (!data.createdBy) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入创建人"
                        });
                        return;
                    }
                    if (!data.createdDate) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入创建时间"
                        });
                        return;
                    }
                    if (!data.lastModifiedBy) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入最后更新者"
                        });
                        return;
                    }
                    if (!data.lastModifiedDate) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代最后更新时间"
                        });
                        return;
                    }*/
                    g.saveFlowType(data);
                }
            }, {
                title : "取消",
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    addFlowType : function() {
        var g = this;
        win = EUI.Window({
            title : "新增流程类型",
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "addFlowType",
                padding : 0,
                items : [{
                    xtype : "TextField",
                    title : "代码",
                    labelWidth : 90,
                    name : "code",
                    width : 220,
                    maxLength : 10,
                }, {
                    xtype : "TextField",
                    title : "名称",
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                }, {
                    xtype : "TextField",
                    title : "描述",
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                }, {
                    xtype : "ComboBox",
                    title : "所属业务实体",
                    labelWidth : 90,
                    name : "businessModel.name",
                    width : 220,
                    store : {
                        url : "http://localhost:8081/flow/maindata/flowType/findAllBusinessModelName",
                    },
                    field : ["businessModel.id"],
                    reader : {
                        name : "name",
                        field : ["id"]
                    },

                }/*, {
                    xtype : "TextField",
                    title : "创建人",
                    labelWidth : 90,
                    name : "createdBy",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "创建时间",
                    labelWidth : 90,
                    name : "createdDate",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "最后更新者",
                    labelWidth : 90,
                    name : "lastModifiedBy",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "最后更新时间",
                    labelWidth : 90,
                    name : "lastModifiedDate",
                    width : 220
                }*/]
            }],
            buttons : [{
                title : "保存",
                selected : true,
                handler : function() {
                    var form = EUI.getCmp("addFlowType");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代码"
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入名称"
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入描述"
                        });
                        return;
                    }
                    if (!data["businessModel.name"]) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请选择所属业务实体模型"
                        });
                        return;
                    }
                    g.saveFlowType(data);

                }
            }, {
                title : "取消",
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    saveFlowType : function(data) {
        var g = this;
        console.log(data);
		var  myMask = EUI.LoadMask({
					msg : "正在保存，请稍候..."
				});
        EUI.Store({
            url : "http://localhost:8081/flow/maindata/flowType/update",
            params : data,
            success : function(){
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure : function(){
                myMask.hide();
            }
            // success : function(status) {
            //     if (!status.success) {
            //         new EUI.ProcessStatus({
            //             msg : status.msg,
            //             success : false
            //         });
            //     } else {
            //         new EUI.ProcessStatus({
            //             msg : "操作成功！",
            //             success : true
            //         });
            //         win.close();
            //         g.reloadGrid();
            //     }
            //     mask.hide();
            // },
            // failure : function(re) {
            //     new EUI.ProcessStatus({
            //         msg : "操作失败，请稍后再试。"
            //     });
            //     mask.hide();
            // }
        });
        win.close();
        myMask.hide();
    }
});