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
                displayText: g.lang.searchByNameMsgText,
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
                shrinkToFit: false,//固定宽度
                url : _ctxPath +"/flowTask/listFlowTask",
                postData:{
                    S_createdDate: "ASC"
                },
                colModel : [{
                    label : g.lang.operateText,
                    name : "operate",
                    index : "operate",
                    width : 100,
                    align : "center",
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = "<div class='btn_operate'>"
                            + "<div class='agreeBtn'>"+g.lang.passText+"</div>"
                            + "<div class='nagreeBtn'>"+g.lang.rejectText+"</div></div>";
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
                    index : "flowName"
                },*/{
                    label : g.lang.taskNameText,
                    name : "taskName",
                    index : "taskName",
                    width:130
                },/*{
                    label : "任务定义KEY",
                    name : "taskDefKey",
                    index : "taskDefKey"
                },*/{
                    label : g.lang.taskFormURLText,
                    name : "taskFormUrl",
                    index : "taskFormUrl",
                   hidden : true
                },{
                    label : g.lang.taskStatusText ,
                    name : "taskStatus",
                    index : "taskStatus",
                    align:"center",
                    width:130,
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = '';
                        if('INIT' == rowObject.taskStatus){
                            strVar = g.lang.todoText;
                        }
                       else if('CANCLE' == rowObject.taskStatus){
                            strVar = g.lang.reversedText;
                        }else if('COMPLETED' == rowObject.taskStatus){
                            strVar = g.lang.doneText;
                        }
                        return strVar;
                    }
                },{
                    label : g.lang.agentStatusText ,
                    name : "proxyStatus",
                    index : "proxyStatus",
                    width:130
                },/*{
                    label : "流程实例ID" ,
                    name : "flowInstanceId",
                    index : "flowInstanceId"
                },{
                    label : "流程定义ID" ,
                    name : "flowDefinitionId",
                    index : "flowDefinitionId"
                },*/{
                    label : g.lang.processorNameText,
                    name : "executorName",
                    index : "executorName",
                    width:130
                },{
                    label : g.lang.processorAccountText,
                    name : "executorAccount",
                    index : "executorAccount",
                    width:130
                },{
                    label : g.lang.candidateAccountText,
                    name : "candidateAccount",
                    index : "candidateAccount",
                    width:130
                },/*{
                    label : "执行时间" ,
                    name : "executeDate",
                    index : "executeDate"
                },*/{
                    label : g.lang.depictText ,
                    name : "depict",
                    index : "depict",
                    width:130
                },{
                    label : g.lang.createTimeText ,
                    name : "createdDate",
                    index : "createdDate",
                    width:130
                }/*{
                    label : "创建人" ,
                    name : "creatorName",
                    index : "creatorName"
                },{
                    label : "创建时间" ,
                    name : "createdDate",
                    index : "createdDate"
                },{
                    label : "最后更新者" ,
                    name : "lastModifiedBy",
                    index : "lastModifiedBy"
                },{
                    label : "最后更新时间" ,
                    name : "lastEditedDate",
                    index : "lastEditedDate"
                },{
                    label : "引擎流程任务ID" ,
                    name : "actTaskId",
                    index : "actTaskId"
                },{
                    label : "优先级" ,
                    name : "priority",
                    index : "priority"
                },{
                    label : "所属人" ,
                    name : "ownerAccount",
                    index : "ownerAccount"
                },{
                    label : "所属人名称" ,
                    name : "ownerName",
                    index : "ownerName"
                },{
                    label : "实际任务类型" ,
                    name : "actType",
                    index : "actType"
                },{
                    label : "签收时间" ,
                    name : "actClaimTime",
                    index : "actClaimTime"
                },{
                    label : "实际触发时间" ,
                    name : "actDueDate",
                    index : "actDueDate"
                },{
                    label : "实际任务定义KEY" ,
                    name : "actTaskKey",
                    index : "actTaskKey"
                },{
                    label : "关联流程实例的ID(隐藏)" ,
                    name : "flowInstance.id",
                    index : "flowInstance.id"
                },{
                    label : "关联流程实例" ,
                    name : "flowInstance.name",
                    index : "flowInstance.name"
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
            title: g.lang.hintText,
            msg:  g.lang.passCurrentTaskMsgText,
            buttons: [{
                title: g.lang.sureText,
                iconCss:"ecmp-common-ok",
               selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: "正在执行"
                    });
                    EUI.Store({
                        url:  _ctxPath +"/flowTask/completeTask",
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
                iconCss:"ecmp-common-delete",
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
            title: g.lang.hintText,
            msg:  g.lang.rejectCurrentTaskMsgText,
            buttons: [{
                title: g.lang.sureText,
                iconCss:"ecmp-common-ok",
               selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: g.lang.processingText
                    });
                    EUI.Store({
                        url:  _ctxPath +"/flowTask/rejectTask",
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
                iconCss:"ecmp-common-delete",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    }
});