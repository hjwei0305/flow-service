/**
 * 显示页面
 */
EUI.CustomExecutorView = EUI.extend(EUI.CustomUI, {
    businessModelId: "",
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
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + "业务实体" + "</span>",
                id: "coboId",
                async: false,
                colon: false,
                labelWidth: 70,
                store: {
                    url: _ctxPath + "/flowType/listAllBusinessModel"
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
                    g.businessModelId = data[0].id;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/customExecutor/listExecutor",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            // Q_EQ_businessModuleId: data[0].id
                            businessModuleId: data[0].id
                        }
                    }, true);
                },
                afterSelect: function (data) {
                    g.businessModelId = data.data.id;
                    EUI.getCmp("gridPanel").setPostParams({
                            // Q_EQ_businessModuleId: data.data.id
                            businessModuleId: data.data.id
                        }, true);
                }

            }, {
                xtype: "Button",
                title: "分配执行人",
                selected: true,
                handler: function () {
                    g.showSetExecutorWind();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: "请输入名称进行搜索",
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("gridPanel").setPostParams({
                                Q_LK_name: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_name: value
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
                "border-reduis": "3px"
            },
            gridCfg: {
                loadonce: true,
                colModel: [/*{
                 label: this.lang.operateText,
                 name: "operate",
                 index: "operate",
                 width: "50%",
                 align: "center",
                 formatter: function (cellvalue, options, rowObject) {
                 var strVar = "<div class='condetail_operate'>"
                 // + "<div class='condetail_update' title='编辑'></div>"
                 + "<div class='condetail_delete' title='删除'></div></div>";
                 return strVar;
                 }
                 }, */{
                    label: "用户ID",
                    name: "id",
                    index: "id",
                     hidden:true
                }, {
                    label: "用户名称",
                    name: "userName",
                    index: "userName",
                    align: "center"
                }, {
                    label: "员工编号",
                    name: "code",
                    index: "code",
                }, {
                    label: "组织机构",
                    name: "organization.name",
                    index: "organization.name",
                    align: "center"
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
        this.operateBtnEvents();
        this.addCustomExecutorEvent();

    },
    operateBtnEvents: function () {
        $(".condetail_delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.deleteExecuor(rowData);
        });
    },
    deleteExecuor: function (rowData) {
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
                        url: _ctxPath + "/customExecutor/delete",
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
        })
    },
    addCustomExecutorEvent: function () {
        var g = this, selectData = [];
        $(".arrow-right").live("click", function () {
            var leftGrid = EUI.getCmp("executorNotSelectedGrid");
            var rowDatas = leftGrid.getSelectRow();
            // var changedDatas=[];
            // for(var i=0;i<rowDatas.length;i++){
            //     var object = new Object();
            //     object.employeeId = rowDatas[i].id;
            //     object.employeeName = rowDatas[i].userName;
            //     changedDatas.push(object);
            // }
            // console.log(changedDatas);
            // console.log(rowDatas);
            var gridPanel = EUI.getCmp("executorSelectedGrid");
            var selectData = gridPanel.getGridData();
            //  console.log(selectData);
            for (var i = 0; i < rowDatas.length; i++) {
                var item = rowDatas[i];
                if (!g.isInArray(item, selectData)) {
                    gridPanel.grid.addRowData(item.id, item);
                    leftGrid.deleteRow(item.id);
                }
            }
        });
        $(".arrow-left").live("click", function () {
            var rightGrid = EUI.getCmp("executorSelectedGrid");
            var rowDatas = rightGrid.getSelectRow();
            // console.log(rowDatas);
            // var changedDatas = [];
            // for(var i=0; i<rowDatas.length;i++){
            //     var object = new Object();
            //     object.id = rowDatas[i].employeeId;
            //     object.userName = rowDatas[i].employeeName;
            //     changedDatas.push(object)
            // }
            // console.log(changedDatas)
            var gridPanel = EUI.getCmp("executorNotSelectedGrid");
            var selectData = gridPanel.getGridData();
            //  console.log(selectData);
            for (var i = 0; i < rowDatas.length; i++) {
                var item = rowDatas[i];
               // console.log(item);
                if (!g.isInArray(item, selectData)) {
                    gridPanel.grid.addRowData(item.id, item);
                    rightGrid.deleteRow(item.id);
                }
            }
        })
    },
    isInArray: function (item, array) {
        for (var i = 0; i < array.length; i++) {
            if (item.id == array[i].id) {
                return true;
            }
        }
        return false;
    },
    showSetExecutorWind: function () {
        var g = this;
        g.excutorSetWind = EUI.Window({
            title: "自定义执行人配置",
            width: 1000,
            height: 400,
            items: [{
                xtype: "Container",
                border: false,
                layout: "border",
                items: [this.getLeftGrid(),
                    this.getCenterIcon(),
                    this.getRightGrid()]
            }],
            buttons: [{
                title: "确定",
                selected: true,
                handler: function () {
                    g.saveExecutorSet();
                }
            }, {
                title: "取消",
                handler: function () {
                    g.excutorSetWind.remove();
                }
            }]
        })
    },
    getLeftGrid: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            width: 460,
            height: 300,
            id: "executorNotSelectedGrid",
            region: "west",
            style: {
                "border": "1px solid #aaa"
            },
            gridCfg: {
                url: _ctxPath + "/customExecutor/listAllExecutorNotSelected",
                postData: {
                    businessModelId: g.businessModelId
                },
                //   hasPager: false,
                multiselect: true,
                colModel: [{
                    label: "用户ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "用户名称",
                    name: "userName",
                    index: "userName",
                    align: "center"
                }, {
                    label: "员工编号",
                    name: "code",
                    index: "code"
                }, {
                    label: "组织机构",
                    name: "organization.name",
                    index: "organization.name",
                    align: "center"
                }]
            }
        }
    },
    getCenterIcon: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            width: 50,
            height: 300,
            border: false,
            html: "<div class='arrow-right'></div>" +
            "<div class='arrow-left'></div>"
        }
    },
    getRightGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            width: 460,
            height: 300,
            id: "executorSelectedGrid",
            region: "east",
            // style: {
            //     "border": "1px solid #aaa"
            // },
            gridCfg: {
                url: _ctxPath + "/customExecutor/listAllExecutorSelected",
                postData: {
                    businessModelId: g.businessModelId
                },
                hasPager: false,
                multiselect: true,
                // data:[],
                colModel: [{
                    label: "用户ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "用户名称",
                    name: "userName",
                    index: "userName",
                    align: "center"
                }, {
                    label: "员工编号",
                    name: "code",
                    index: "code"
                }, {
                    label: "组织机构",
                    name: "organization.name",
                    index: "organization.name",
                    align: "center"
                }]
            }
        }
    },
    saveExecutorSet: function () {
        var g = this;
        var gridData = EUI.getCmp("executorSelectedGrid").getGridData();
        var resultIds = "";
        for (var i = 0; i < gridData.length; i++) {
            resultIds += gridData[i].id + ",";
        }
        resultIds = (resultIds.substring(resultIds.length - 1) == ',') ? resultIds.substring(0, resultIds.length - 1) : resultIds;
        var mask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/customExecutor/saveSetCustomExecutor",
            params: {
                businessModelId: g.businessModelId,
                selectedCustomExecutorIds: resultIds
            },
            success: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
                if (status.success) {
                    g.excutorSetWind.close();
                    EUI.getCmp("gridPanel").refreshGrid();
                }
            },
            failure: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
            }
        });

    }
});