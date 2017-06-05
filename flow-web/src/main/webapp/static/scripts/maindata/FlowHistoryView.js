/**
 * 显示页面
 */
EUI.FlowHistoryView = EUI.extend(EUI.CustomUI, {
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initTbar(), this.initGrid()]
        });
        this.addEvents();
    },
    initTbar: function () {
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
                title: "流程信息",
                handler: function () {
                   Flow.FlowHistory({
                        instanceId: "DE3412A7-3FBE-11E7-9EFA-6C498F234A3D"
                    })
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: "请输入任务名进行搜索",
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("gridPanel").setPostParams({
                                Q_LK_flowTaskName: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_flowTaskName: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        };
    },
    initGrid: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "gridPanel",
            style: {
                "border-radius": "3px"
            },
            gridCfg: {
                //     loadonce:true,
                url: _ctxPath + "/flowHistory/listFlowHistory",
                postData: {
                    S_createdDate: "ASC"
                },
                colModel: [{
                    label: "操作",
                    name: "operate",
                    index: "taskStatus",
                    width: 100,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('COMPLETED' == rowObject.taskStatus) {
                            strVar = "<div class='btn_operate'>"
                                + "<div class='rollBackBtn' title='撤销'>撤销</div>"
                        }
                        return strVar;
                    }
                }, {
                    label: "ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, /*{
                 label : "流程名称",
                 name : "flowName",
                 index : "flowName"
                 },*/{
                    label: "任务名",
                    name: "flowTaskName",
                    index: "flowTaskName"
                }, {
                    label: "流程实例",
                    name: "flowInstance.flowName",
                    index: "flowInstance.flowName"
                }, {
                    label: "任务表单URL",
                    name: "taskFormUrl",
                    index: "taskFormUrl",
                    hidden: true
                }, {
                    label: "任务状态",
                    name: "taskStatus",
                    index: "taskStatus",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('COMPLETED' == rowObject.taskStatus) {
                            strVar = "已办";
                        }
                        else if ('CANCLE' == rowObject.taskStatus) {
                            strVar = "已撤销";
                        }
                        return strVar;
                    }
                }, {
                    label: "代理状态",
                    name: "proxyStatus",
                    index: "proxyStatus"
                }, /*{
                 label : "流程实例ID" ,
                 name : "flowInstanceId",
                 index : "flowInstanceId"
                 },{
                 label : "流程定义ID" ,
                 name : "flowDefinitionId",
                 index : "flowDefinitionId"
                 },*/{
                    label: "执行人名称",
                    name: "executorName",
                    index: "executorName"
                }, {
                    label: "执行人账号",
                    name: "executorAccount",
                    index: "executorAccount"
                }, {
                    label: "任务开始时间",
                    name: "actStartTime",
                    index: "actStartTime"
                }, {
                    label: "任务结束时间",
                    name: "actEndTime",
                    index: "actEndTime"
                }, {
                    label: "任务执行时长",
                    name: "actDurationInMillis",
                    index: "actDurationInMillis",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        var value = rowObject.actDurationInMillis;
                        var day = Math.floor(value/(60*60*1000*24));
                        var hour = Math.floor((value - day*60*60*1000*24)/(60*60*1000));
                        var minute = Math.floor((value - day*60*60*1000*24- hour*60*60*1000)/(60*1000));
                        var second = Math.floor((value - day*60*60*1000*24- hour*60*60*1000 - minute*60*1000)/1000);
                        if(day > 0 ){
                            strVar += day+"天";
                        }
                        if(hour > 0 ){
                            strVar += hour+"小时";
                        }
                        if(minute > 0 ){
                            strVar += minute+"分";
                        }
                        if(second > 0 ){
                            strVar += second+"秒";
                        }
                        return strVar;
                    }

                }, {
                    label: "最后更新时间",
                    name: "lastModifiedDate",
                    index: "lastModifiedDate"
                },
                    /*    ,{
                     label : "候选人账号" ,
                     name : "candidateAccount",
                     index : "candidateAccount"
                     },{
                     label : "执行时间" ,
                     name : "executeDate",
                     index : "executeDate"
                     },*/{
                        label: "描述",
                        name: "depict",
                        index: "depict"
                    }/*{
                     label : "创建人" ,
                     name : "createdBy",
                     index : "createdBy"
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
                     name : "lastModifiedDate",
                     index : "lastModifiedDate"
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
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".rollBackBtn").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.showCompleteWin(rowData);
        });
    },
    showCompleteWin: function (rowData) {
        var g = this;
        console.log(rowData);
        var infoBox = EUI.MessageBox({
            title: "提示",
            msg: "确定撤销当前任务吗？",
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: "正在执行"
                    });
                    EUI.Store({
                        url: _ctxPath + "/flowHistory/rollBackTask",
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