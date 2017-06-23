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
                    width: "100",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_operate'>"
                            + "<div class='flowHistoryBtn'>"+g.lang.showDoneText+"</div>"
                            + "<div class='deleteBtn'> "+g.lang.deleteText+"</div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: g.lang.flowNameText,
                    name: "flowName",
                    index: "flowName",
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
                    title: false
                }, {
                    label: g.lang.endTimeText,
                    name: "endDate",
                    index: "endDate",
                    align: "center",
                    title: false
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict",
                    title: false
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
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".flowHistoryBtn").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.showTaskHistoryWind(data);
        });
        $(".deleteBtn").live("click", function () {
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
                        var timeMill = rowObject.actDurationInMillis / 1000;
                        var h = Math.floor(timeMill / 60 / 60);
                        var m = Math.floor((timeMill - h * 60 * 60) / 60);
                        var s = Math.floor((timeMill - h * 60 * 60 - m * 60));
                        var d = parseInt(h / 24);
                        if (d > 0) {
                            strVar += d + g.lang.dayText;
                        }
                        if (h > 0) {
                            strVar += h + g.lang.hourText;
                        }
                        if (m > 0) {
                            strVar += m + g.lang.minuteText;
                        }
                        if (s > 0) {
                            strVar += s + g.lang.secondText;
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