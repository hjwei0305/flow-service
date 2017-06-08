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
                title: "<span style='font-weight: bold'>" + "流程定义版本" + "</span>",
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
                displayText: "请输入名称进行搜索",
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
                            + "<div class='flowHistoryBtn'>查看已办</div>"
                            + "<div class='deleteBtn'>删除</div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "流程名称",
                    name: "flowName",
                    index: "flowName",
                    title: false
                }/*, {
                    label: "业务ID",
                    name: "businessId",
                    index: "businessId",
                    title: false
                }*/, {
                    label: "开始时间",
                    name: "startDate",
                    index: "startDate",
                    align: "center",
                    title: false
                }, {
                    label: "结束时间",
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
                    label: "是否挂起",
                    name: "suspended",
                    index: "suspended",
                    align: "center",
                    title: false,
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('0' == rowObject.suspended) {
                            strVar = "否";
                        }
                        else if ('1' == rowObject.suspended) {
                            strVar = "是";
                        }
                        return strVar;
                    }
                }, {
                    label: "是否已经结束",
                    name: "ended",
                    index: "ended",
                    align: "center",
                    title: false,
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('0' == rowObject.ended) {
                            strVar = "否";
                        }
                        else if ('1' == rowObject.ended) {
                            strVar = "是";
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
        });
    },
    showTaskHistoryWind: function (data) {
        var g = this;
        console.log(data);
        var win = EUI.Window({
            title: "已办任务",
            layout: "border",
            width: 1400,
            height: 500,
            padding: 15,
            padding: 8,
            itemspace: 0,
            items: [this.initWindTbar(), this.initWindGrid(data)],
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
                displayText: "请输入名称进行搜索",
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
                 + "<div class='flowHistoryBtn'>查看已办</div>"
                 + "<div class='condetail_delete'></div></div>";
                 return strVar;
                 }
                 },*/ {
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
                    align: "center",
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
                    index: "executorAccount",
                    align: "center"
                }, {
                    label: "任务开始时间",
                    name: "actStartTime",
                    index: "actStartTime",
                    align: "center"
                }, {
                    label: "任务结束时间",
                    name: "actEndTime",
                    index: "actEndTime",
                    align: "center"
                }, {
                    label: "任务执行时长",
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
                            strVar += d + "天";
                        }
                        if (h > 0) {
                            strVar += h + "小时";
                        }
                        if (m > 0) {
                            strVar += m + "分";
                        }
                        if (s > 0) {
                            strVar += s + "钞";
                        }
                        return strVar;
                    }

                }, {
                    label: "最后更新时间",
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
                        label: "描述",
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