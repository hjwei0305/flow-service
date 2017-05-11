/**
 * 显示页面
 */
EUI.FlowTaskView = EUI.extend(EUI.CustomUI, {
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
            isOverFlow:false,
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
                "border-radius" : "3px"
            },
            gridCfg : {
           //     loadonce:true,
                	url : _ctxPath +"/maindata/flowTask/find",
                postData:{
                    S_createdDate: "ASC"
                },
                colModel : [{
                    label : "操作",
                    name : "operate",
                    index : "operate",
                    width : 100,
                    align : "center",
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = "<div class='btn_operate'>"
                            + "<div class='agreeBtn'>通过</div>"
                            + "<div class='nagreeBtn'>驳回</div></div>";
                        return strVar;
                    }
                },{
                    label : "ID",
                    name : "id",
                    index : "id",
                    hidden : true
                },/*{
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
                    title : false,
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = '';
                        if('INIT' == rowObject.taskStatus){
                            strVar = "待办";
                        }
                       else if('CANCLE' == rowObject.taskStatus){
                            strVar = "已撤销";
                        }else if('COMPLETED' == rowObject.taskStatus){
                            strVar = "已办";
                        }
                        return strVar;
                    }
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
                },{
                    label : "创建时间" ,
                    name : "createdDate",
                    index : "createdDate",
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
                },{
                    label : "引擎流程任务ID" ,
                    name : "actTaskId",
                    index : "actTaskId",
                    title : false
                },{
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
            var rowData=EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.showCompleteWin(rowData);
        });
        $(".nagreeBtn").live("click",function(){
            var rowData=EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.showRejectTaskWin(rowData);
        });
    },
    showCompleteWin : function(rowData) {
        var g = this;
        console.log(rowData);
        var infoBox = EUI.MessageBox({
            title: "提示",
            msg:  "确定通过当前任务吗？",
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: "正在执行"
                    });
                    EUI.Store({
                        url:  _ctxPath +"/maindata/flowTask/completeTask",
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
                title: "取消",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    },
    showRejectTaskWin : function(rowData) {
        var g = this;
        console.log(rowData);
        var infoBox = EUI.MessageBox({
            title: "提示",
            msg:  "确定驳回当前任务吗？",
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: "正在执行"
                    });
                    EUI.Store({
                        url:  _ctxPath +"/maindata/flowTask/rejectTask",
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
                title: "取消",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    }
});