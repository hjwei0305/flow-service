/**
 * 显示页面
 */
EUI.FlowInstanceView = EUI.extend(EUI.CustomUI, {
    initComponent: function () {
        this.gridCmp=EUI.GridPanel({
            renderTo: this.renderTo,
            title: "流程实例管理",
            border: true,
            tbar: this.initTbar(),
            gridCfg: this.initGrid()
        });
        this.inFlow=true;
        this.addEvents();
    },
    initTbar: function () {
        var g = this;
        return   [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + this.lang.modelText + "</span>",
                labelWidth: 60,
                width:170,
                id: "appModuleComboBoxId",
                async: false,
                canClear: false,
                colon: false,
                name: "appModuleName",
                store: {
                    url: _ctxPath + "/businessModel/listAllAppModule"
                },
                field: ["appModuleId"],
                reader: {
                    name: "name",
                    field: ["id"]
                },
                afterLoad: function (data) {
                    var cobo = EUI.getCmp("appModuleComboBoxId");
                    // cobo.setValue(data[0].name);
                    cobo.setSubmitValue({
                        appModuleId:data[0].id,
                        appModuleName:data[0].name,
                    });
                    EUI.getCmp("businessModelComboBoxId").store.params.appModuleId = 	data[0].id;
                    g.getFlowInstance();
                },
                afterSelect: function (data) {
                    EUI.getCmp("businessModelComboBoxId").store.params.appModuleId = 	data.data.id;
                    EUI.getCmp("businessModelComboBoxId").setValue("");
                    EUI.getCmp("flowTypeComboBoxId").setValue("");
                    EUI.getCmp("flowTypeComboBoxId").store.params.businessModelId =null;
                    g.getFlowInstance();
                }
            },{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + "业务实体" + "</span>",
                labelWidth: 60,
                width:170,
                id: "businessModelComboBoxId",
                  async: true,
                colon: false,
                name: "businessModelName",
                field: ["businessModelId"],
                reader: {
                    name: "name",
                    field: ["id"]
                },
                loadonce:false,
                store:{
                    url: _ctxPath + "/businessModel/listBusinessModuleByAppModelId",
                    params: {
                         appModuleId:null
                    }
                },
                afterSelect: function (data) {
                    EUI.getCmp("flowTypeComboBoxId").store.params.businessModelId = data.data.id;
                    EUI.getCmp("flowTypeComboBoxId").setValue("");
                    g.getFlowInstance();
                }
            },{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + "流程类型" + "</span>",
                labelWidth: 60,
                width:170,
                id: "flowTypeComboBoxId",
                async: true,
                colon: false,
                name: "flowTypeName",
                field: ["flowTypeId"],
                reader: {
                    name: "name",
                    field: ["id"]
                },
                loadonce:false,
                store:{
                    url: _ctxPath + "/flowInstance/listFlowTypeByBusinessModelId",
                    params: {
                        businessModelId:null
                    }
                },
                afterSelect: function (data) {
                    g.getFlowInstance();
                }
            },{
            xtype: "CheckBox",
            labelWidth: 80,
            width:170,
            title: g.lang.inFlowText,
            checked:true,
            name: "inFlow",
            id:"inFlowBox",
            onChecked:function (value) {
                g.inFlow=value;
                g.getFlowInstance();
            }
        },'->', {
                xtype: "SearchBox",
                width:180,
                id:"searchBox",
                displayText: g.lang.searchByNameText,
                onSearch: function (value) {
                    g.getFlowInstance();
                }
            }];
    },
    initGrid: function () {
        var g = this;
        return{
                loadonce: true,
                datatype: "local",
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: 80,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var endFlowClass="end ecmp-flow-delete fontcusor";
                        if(!rowObject.manuallyEnd && !rowObject.ended){
                            var endFlowClass="processing ecmp-flow-delete fontcusor";
                        }
                        var lookUrl=rowObject.flowDefVersion.flowDefination.flowType.businessModel.lookUrl;
                        return  '<i class="ecmp-common-view icon-space fontcusor" targetUrl="'+ lookUrl +'" title="'+g.lang.viewOrderText+'"></i><i class="ecmp-flow-history condetail-flowHistory icon-space fontcusor" title="'+g.lang.showHistoryText+'"></i><i class="'+endFlowClass +'" title="'+g.lang.endFlowText+'"></i>';
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                },{
                    name: "businessId",
                    index: "businessId",
                    hidden: true
                },{
                    name: "webBaseAddress",
                    index: "webBaseAddress",
                    hidden: true
                },{
                    label: g.lang.flowNameText,
                    name: "flowName",
                    index: "flowName",
                    width:170,
                    title: false
                } ,{label: g.lang.businessCodeText,
                    name: "businessCode",
                    index: "businessCode",
                    width:140,
                    title: false},{label: g.lang.businessModelRemarkText,
                    name: "businessModelRemark",
                    index: "businessModelRemark",
                    width:240,
                    title: false}, {
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
                } , {
                    label: g.lang.whetherSuspendText,
                    name: "suspended",
                    index: "suspended",
                    align: "center",
                    title: false,
                    hidden:true,
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
                    label: g.lang.flowStatusText,
                    name: "ended",
                    index: "ended",
                    align: "center",
                    width:110,
                    title: false,
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = '';
                        if (rowObject.manuallyEnd) {
                            strVar = g.lang.forceEndText;
                        }
                        else if (rowObject.ended) {
                            strVar = g.lang.endEventText;
                        }else{
                            strVar = g.lang.processingText;
                        }
                        return strVar;
                    }
                }],
                shrinkToFit: false //固定宽度
        };
    },
    addEvents: function () {
        var g = this;
        $(".ecmp-common-view").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            var lookUrl=$(this).attr("targetUrl");
            if(!lookUrl||lookUrl==""){
                return ;
            }
            var tab = {
                title: g.lang.viewOrderText,
                url: _basePath + data.webBaseAddress+lookUrl + "?id=" + data.businessId,
                id: data.businessId
            };
            g.addTab(tab);
        });
        $(".ecmp-flow-history").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            EUI.FlowHistory({
                businessId: data.businessId,
                instanceId: data.id
            })
        });
        $(".processing.ecmp-flow-delete").live("click", function () {
            g.endFlow();
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = g.gridCmp.getSelectRow();
            g.deleteFlowInstance(rowData);
        });
    },
    //在新的窗口打开（模拟新页签的打开方式）
    addTab: function (tab) {
        if (parent.homeView) {
            parent.homeView.addTab(tab);//获取到父窗口homeview，在其中新增页签
        } else {
            window.open(tab.url);
        }
    },
    deleteFlowInstance:function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.ifDelMsgText,
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
                                g.gridCmp.grid.trigger("reloadGrid");
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
    },
    showTaskHistoryWind: function (data) {
        var g = this;
        var win = EUI.Window({
            title: g.lang.taskDoneText,
            iconCss: "ecmp-eui-look",
            width: 1100,
            height: 500,
            padding: 8,
            items: [this.initWindGrid(data)]
        });
    },
    endFlow:function () {
        var g=this;
        var data = g.gridCmp.getSelectRow();
        var message = EUI.MessageBox({
            border: true,
            title: g.lang.tiShiText,
            showClose: true,
            msg: g.lang.endFlowMsgText,
            buttons: [{
                title:g.lang.cancelText,
                handler: function () {
                    message.remove();
                }
            },{
                title:g.lang.okText,
                selected: true,
                handler: function () {
                    var myMask = EUI.LoadMask({
                        msg: g.lang.endMask
                    });
                    EUI.Store({
                        url: _ctxPath+"/flowInstance/endForceFlowInstance/",
                        params: {id: data.id},
                        success: function (status) {
                            myMask.remove();
                            EUI.ProcessStatus(status);
                            g.gridCmp.grid.trigger("reloadGrid");
                        },
                        failure: function (status) {
                            myMask.hide();
                            EUI.ProcessStatus(status);
                        }
                    });
                    message.remove();
                }
            }]
        });
    },
    getFlowInstance:function () {
      var g=this;
        g.gridCmp.grid[0].p.postData={};
        var postData={
            "Q_EQ_flowDefVersion.flowDefination.flowType.businessModel.appModule.id":EUI.getCmp("appModuleComboBoxId").getSubmitValue().appModuleId,
            "Q_EQ_flowDefVersion.flowDefination.flowType.businessModel.id": EUI.getCmp("businessModelComboBoxId").getSubmitValue().businessModelId,
            "Q_EQ_flowDefVersion.flowDefination.flowType.id":  EUI.getCmp("flowTypeComboBoxId").getSubmitValue().flowTypeId,
            "Quick_value": EUI.getCmp("searchBox").getValue(),
            "S_createdDate":"DESC"
        };
        if(g.inFlow){
            postData["Q_EQ_manuallyEnd__Boolean"]=false;
            postData["Q_EQ_ended__Boolean"]=false;
        }
        g.gridCmp.setGridParams({
            url: _ctxPath + "/flowInstance/listFlowInstance",
            loadonce: false,
            datatype: "json",
            postData:postData
        }, true)
    },
    initWindTbar: function () {
        var g = this;
        return  ['->', {
                xtype: "SearchBox",
                displayText: g.lang.searchByNameText,
                onSearch: function (value) {
                    EUI.getCmp("flowHistoryGrid").setPostParams({
                            Q_LK_flowTaskName: value
                        },true);
                }
            }];
    },
    initWindGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            border: true,
            tbar: g.initWindTbar(),
            id:"flowHistoryGrid",
            gridCfg: {
                url: _ctxPath + "/flowInstance/listFlowHistory",
                postData: {
                    "Q_EQ_flowInstance.id": data.id,
                    S_createdDate: "ASC"
                },
                colModel: [  {
                    label: "ID",
                    name: "id",
                    index: "id",
                    hidden: true
                },  {
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
                },  {
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
                    {
                        label: g.lang.depictText,
                        name: "depict",
                        index: "depict"
                    }]
            }
        }
    }
});