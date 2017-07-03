/**
 * 显示页面
 */
EUI.FlowInstanceView = EUI.extend(EUI.CustomUI, {
    flowDefVersionId: "",
    flowDefVersionName: "",
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
            isOverFlow:false,
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + g.lang.flowDefinitionVersionText + "</span>",
                id: "coboId",
                async: false,
                colon: false,
                labelWidth: 100,
                store: {
                    url: _ctxPath + "/flowInstance/listAllFlowDefVersion"
                },
                reader: {
                    name: "name",
                    filed: ["id"]
                },
                afterLoad: function (data) {
                    if (!data) {
                        return;
                    }
                    var cobo = EUI.getCmp("coboId");
                    cobo.setValue(data[0].name);
                    g.flowDefVersionId = data[0].id;
                    g.flowDefVersionName = data[0].name;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/flowInstance/listFlowInstance",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            "Q_EQ_flowDefVersion.id": data[0].id
                        }
                    }, true)
                },
                afterSelect: function (data) {
                    //console.log(data);
                    g.flowDefVersionId = data.data.id;
                    g.flowDefVersionName = data.data.name;
                    EUI.getCmp("gridPanel").setPostParams({
                            "Q_EQ_flowDefVersion.id": data.data.id
                        }
                    ).trigger("reloadGrid");
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: g.lang.searchByNameMsgText,
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("gridPanel").setPostParams({
                                Q_LK_flowName: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_flowName: value
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
                "border": "1px solid #aaa",
                "border-radius": "3px"
            },
            gridCfg: {
                loadonce: true,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: 80,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        // var strVar = "<div class='condetail_operate'>"
                        //     + "<div class='flowHistoryBtn'>"+g.lang.showDoneText+"</div>"
                        //     + "<div class='deleteBtn'> "+g.lang.deleteText+"</div></div>";
                        // return strVar;
                        //  return '<i class="ecmp-common-delete icon-space fontcusor" title="'+g.lang.deleteText+'"></i>'+
                        return  '<i class="ecmp-common-view  fontcusor" title="'+g.lang.showDoneText+'"></i>';
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: g.lang.flowNameText,
                    name: "flowName",
                    index: "flowName",
                    width:170,
                    title: false
                }/*, {
                    label: "业务ID",
                    name: "businessId",
                    index: "businessId",
                    title: false
                }*/, {
                    label: g.lang.startTimeText,
                    name: "startDate",
                    index: "startDate",
                    align: "center",
                    width:150,
                    title: false
                }, {
                    label: g.lang.endTimeText,
                    name: "endDate",
                    index: "endDate",
                    align: "center",
                    width:150,
                    title: false
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict",
                    width:170,
                    title: false,
                    hidden:true
                }/*, {
                    label: "引擎流程实例ID",
                    name: "actInstanceId",
                    index: "actInstanceId",
                    title: false,
                }*/, {
                    label: g.lang.whetherSuspendText,
                    name: "suspended",
                    index: "suspended",
                    align: "center",
                    title: false,
                    width:110,
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('0' == rowObject.suspended) {
                            strVar = g.lang.noText;
                        }
                        else if ('1' == rowObject.suspended) {
                            strVar = g.lang.yesText;
                        }
                        return strVar;
                    }
                }, {
                    label: g.lang.whetherDoneText,
                    name: "ended",
                    index: "ended",
                    align: "center",
                    width:110,
                    title: false,
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('0' == rowObject.ended) {
                            strVar = g.lang.noText;
                        }
                        else if ('1' == rowObject.ended) {
                            strVar = g.lang.yesText;
                        }
                        return strVar;
                    }
                }],
                shrinkToFit: false,//固定宽度
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".ecmp-common-view").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.showTaskHistoryWind(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.deleteFlowInstance(rowData);
        });
    },
    deleteFlowInstance:function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.ifDelMsgText,
            buttons: [{
                title: g.lang.sureText,
                iconCss:"ecmp-common-ok",
               selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: g.lang.nowDelMsgText
                    });
                    EUI.Store({
                        url: _ctxPath + "/flowInstance/delete",
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
    showTaskHistoryWind: function (data) {
        var g = this;
        console.log(data);
        var win = EUI.Window({
            title: g.lang.taskDoneText,
            layout: "border",
            width: 1100,
            height: 500,
            padding: 8,
            itemspace: 0,
            items: [this.initWindTbar(), this.initWindGrid(data)]
        });
    },
    initWindTbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            isOverFlow:false,
            border: false,
            items: ['->', {
                xtype: "SearchBox",
                displayText: g.lang.searchByNameMsgText,
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("flowHistoryGrid").setPostParams({
                                Q_LK_flowTaskName: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("flowHistoryGrid").setPostParams({
                            Q_LK_flowTaskName: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        }
    },
    initWindGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id:"flowHistoryGrid",
            style: {
                "border": "1px solid #aaa",
                "border-radius": "3px"
            },
            gridCfg: {
                //   loadonce: true,
                url: _ctxPath + "/flowInstance/listFlowHistory",
                postData: {
                    "Q_EQ_flowInstance.id": data.id,
                    S_createdDate: "ASC"
                },
                colModel: [/*{
                 label: this.lang.operateText,
                 name: "operate",
                 index: "operate",
                 width: "100",
                 align: "center",
                 formatter: function (cellvalue, options, rowObject) {
                 var strVar = "<div class='condetail_operate'>"
                 + "<div class='flowHistoryBtn'>"+g.lang.showDoneText+"</div>"
                 + "<div class='condetail_delete'></div></div>";
                 return strVar;
                 }
                 },*/ {
                    label: "ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, /*{
                 label : g.lang.flowNameText,
                 name : "flowName",
                 index : "flowName"
                 },*/{
                    label: g.lang.taskNameText,
                    name: "flowTaskName",
                    index: "flowTaskName"
                }, {
                    label: g.lang.flowInstanceText,
                    name: "flowInstance.flowName",
                    index: "flowInstance.flowName"
                }, {
                    label: g.lang.taskFormURLText,
                    name: "taskFormUrl",
                    index: "taskFormUrl",
                    hidden: true
                }, {
                    label: g.lang.taskStatusText,
                    name: "taskStatus",
                    index: "taskStatus",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('COMPLETED' == rowObject.taskStatus) {
                            strVar = g.lang.doneText;
                        }
                        else if ('CANCLE' == rowObject.taskStatus) {
                            strVar = g.lang.reversedText;
                        }
                        return strVar;
                    }
                }, {
                    label: g.lang.agentStatusText,
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
                    label: g.lang.processorNameText,
                    name: "executorName",
                    index: "executorName"
                }, {
                    label: g.lang.processorAccountText,
                    name: "executorAccount",
                    index: "executorAccount",
                    align: "center"
                }, {
                    label: g.lang.taskBeginTimeText,
                    name: "actStartTime",
                    index: "actStartTime",
                    align: "center"
                }, {
                    label: g.lang.taskEndTimeText,
                    name: "actEndTime",
                    index: "actEndTime",
                    align: "center"
                }, {
                    label: g.lang.taskProcessTimeText,
                    name: "actDurationInMillis",
                    index: "actDurationInMillis",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        var value = rowObject.actDurationInMillis;
                        var day = Math.floor(value / (60 * 60 * 1000 * 24));
                        var hour = Math.floor((value - day * 60 * 60 * 1000 * 24) / (60 * 60 * 1000));
                        var minute = Math.floor((value - day * 60 * 60 * 1000 * 24 - hour * 60 * 60 * 1000) / (60 * 1000));
                        var second = Math.floor((value - day * 60 * 60 * 1000 * 24 - hour * 60 * 60 * 1000 - minute * 60 * 1000) / 1000);
                        if (day > 0) {
                            strVar += day + "天";
                        }
                        if (hour > 0) {
                            strVar += hour + "小时";
                        }
                        if (minute > 0) {
                            strVar += minute + "分";
                        }
                        if (second > 0) {
                            strVar += second + "秒";
                        }
                        return strVar;
                    }

                }, {
                    label: g.lang.lastUpdateTimeText,
                    name: "lastEditedDate",
                    index: "lastEditedDate",
                    align: "center"
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
                        label: g.lang.depictText,
                        name: "depict",
                        index: "depict"
                    }],
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        }
    }
});