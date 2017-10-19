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
            items: ['->', {
                xtype: "SearchBox",
                displayText: g.lang.searchByTaskNameText,
                onSearch: function (value) {
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_flowTaskName: value
                        },true);
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
                shrinkToFit: false,//固定宽度
                url: _ctxPath + "/flowHistory/listFlowHistory",
                postData: {
                    S_createdDate: "ASC"
                },
                colModel: [{
                    label: g.lang.operateText,
                    name: "operate",
                    index: "taskStatus",
                    width: 80,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if ('COMPLETED' == rowObject.taskStatus) {
                            strVar= "<div class='ecmp-common-return rollBackBtn' title='"+g.lang.reverseText+"'></div>";
                        }
                        return strVar;
                    }
                }, {
                    label: "ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: g.lang.taskNameText,
                    name: "flowTaskName",
                    index: "flowTaskName",
                    width:110
                }, {
                    label: g.lang.flowInstanceText,
                    name: "flowInstance.flowName",
                    index: "flowInstance.flowName",
                    width:150
                }, {
                    label: g.lang.taskFormURLText,
                    name: "taskFormUrl",
                    index: "taskFormUrl",
                    hidden: true
                }, {
                    label: g.lang.taskStatusText,
                    name: "taskStatus",
                    index: "taskStatus",
                    align:"center",
                    width:110,
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
                    index: "proxyStatus",
                    width:110
                },{
                    label: g.lang.processorNameText,
                    name: "executorName",
                    index: "executorName",
                    width:110
                }, {
                    label: g.lang.processorAccountText,
                    name: "executorAccount",
                    index: "executorAccount",
                    width:110
                }, {
                    label: g.lang.taskBeginTimeText,
                    name: "actStartTime",
                    index: "actStartTime",
                    width:150
                }, {
                    label: g.lang.taskEndTimeText,
                    name: "actEndTime",
                    index: "actEndTime",
                    width:150
                }, {
                    label: g.lang.taskProcessTimeText,
                    name: "actDurationInMillis",
                    index: "actDurationInMillis",
                    width:110,
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        var value = rowObject.actDurationInMillis;
                        var day = Math.floor(value/(60*60*1000*24));
                        var hour = Math.floor((value - day*60*60*1000*24)/(60*60*1000));
                        var minute = Math.floor((value - day*60*60*1000*24- hour*60*60*1000)/(60*1000));
                        var second = Math.floor((value - day*60*60*1000*24- hour*60*60*1000 - minute*60*1000)/1000);
                        if(day > 0 ){
                            strVar += day+g.lang.dayText;
                        }
                        if(hour > 0 ){
                            strVar += hour+g.lang.hourText;
                        }
                        if(minute > 0 ){
                            strVar += minute+g.lang.minuteText;
                        }
                        if(second > 0 ){
                            strVar += second+g.lang.secondText;
                        }
                        return strVar;
                    }

                }, {
                    label: g.lang.lastUpdateTimeText,
                    name: "lastModifiedDate",
                    index: "lastModifiedDate",
                    width:150
                },
                  {
                        label: g.lang.depictText,
                        name: "depict",
                        index: "depict",
                        width:150
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
        $(".rollBackBtn").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.showCompleteWin(rowData);
        });
    },
    showCompleteWin: function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.reverseTaskMsgText,
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            },{
                title: g.lang.sureText,
               selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: g.lang.processingText
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
            }]
        });
    }
});