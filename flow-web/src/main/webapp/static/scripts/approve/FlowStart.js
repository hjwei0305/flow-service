/**
 * 采购页面
 */
window.Flow = {};
EUI.ns("Flow.flow");
Flow.FlowStart = function (options) {
    return new Flow.flow.FlowStart(options);
};
Flow.flow.FlowStart = EUI.extend(EUI.CustomUI, {
    data: null,
    businessKey: null,
    businessModelCode: null,
    typeId: null,
    url: null,
    afterSubmit: null,
    selectedOrgId: null,  //当前选中的节点的ID
    chooseUserNode:null,
    initComponent: function () {
        this.getData();
    },
    getData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在启动，请稍后..."
        });
        EUI.Store({
            url: g.url,
            params: {
                businessModelCode: g.businessModelCode,
                businessKey: g.businessKey
            },
            success: function (result) {
                myMask.hide();
                if (!result.data.flowTypeList && !result.data.flowInstance && !result.data.nodeInfoList) {
                    var status = {
                        msg: "找不到流程定义",
                        success: false,
                        showTime: 4
                    };
                    EUI.ProcessStatus(status);
                    return;
                }
                if (!result.data.flowTypeList) {
                    var status = {
                        msg: "找不到流程类型",
                        success: false,
                        showTime: 4
                    };
                    EUI.ProcessStatus(status);
                    return;
                }
                if (result.data.flowTypeList) {
                    var flowTypeList = result.data.flowTypeList;
                    g.data = result.data;
                    g.showWind();
                    g.showChooseUser();
                }
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
                myMask.hide();
            }
        });
    },
    showWind: function () {
        var g = this;
        var item = [];
        if (this.data.flowTypeList.length == 1) {
            item = [this.initWindContainer()]
        } else {
            item = [this.initWindTbar(g.data), this.initWindContainer()]
        }
        g.win = EUI.Window({
            title: "流程启动",
            width: 700,
            isOverFlow: false,
            padding: 0,
            items: item,
            buttons: [{
                title: "提交",
                selected: true,
                handler: function () {
                    g.submit();
                }
            }, {
                title: "取消",
                handler: function () {
                    g.win.remove();
                }
            }]
        });
        g.addEvents();
    },
    addEvents: function () {
        var g = this;
        //执行人选择
        $(".flow-user-item").die().live("click", function () {
            var type = $(this).attr("type").toLowerCase();
            if (type == "common") {
                if ($(".flow-excutor-content").children("div").length == 1) {
                    if ($(".flow-user-item").hasClass("select")) {
                        $(".flow-user-item").removeClass("select");
                    } else {
                        $(this).addClass("select");
                    }
                } else {
                    $(".flow-user-item").removeClass("select");
                    $(this).addClass("select");
                }
            }
            if (type == "singlesign" || type == "countersign") {
                if ($(this).hasClass("select")) {
                    $(this).removeClass("select");
                } else {
                    $(this).addClass("select");
                }
            }
        });
        
        //选择任意执行人
        $(".choose-btn").die().live("click", function () {
            console.log()
            g.showChooseExecutorWind();
            return false;
        });

        //删除选择的执行人
        $(".choose-delete").die().live("click", function () {
             $(this).parent().remove();
        })
    },
    showChooseFlowTypeAndExecutorWind: function (flowTypeList) {
        var g = this;
        window = EUI.Window({
            title: "选择流程类型",
            width: 600,
            layout: "border",
            padding: 0,
            items: [this.initWindTbar(flowTypeList), this.initWindContainer()]
        });
    },
    initWindTbar: function (data) {
        var g = this;
        g.typeId = data.flowTypeList[0].id;
        var flowTypeList = data.flowTypeList;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 8,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "ComboBox",
                field: ["id"],
                width: 250,
                labelWidth: 100,
                name: "name",
                id: "flowTypeId",
                title: "<span style='font-weight: bold'>" + "流程类型" + "</span>",
                listWidth: 200,
                reader: {
                    name: "name",
                    field: ["id"]
                },
                data: flowTypeList,
                value: flowTypeList[0].name,
                submitValue: {
                    id: flowTypeList[0].id
                },
                afterSelect: function (data) {
                    var myMask = EUI.LoadMask({
                        msg: "正在加载，请稍候..."
                    });
                    g.typeId = data.data.id;
                    EUI.Store({
                        url: g.url,
                        params: {
                            businessKey: g.businessKey,
                            businessModelCode: g.businessModelCode,
                            typeId: g.typeId
                        },
                        success: function (result) {
                            myMask.hide();
                            if (!result.data.flowTypeList && !result.data.flowInstance && !result.data.nodeInfoList) {
                                var status = {
                                    msg: "流程定义未找到",
                                    success: false,
                                    showTime: 4
                                };
                                EUI.ProcessStatus(status);
                                //   $(".flow-node-box").empty();
                                $(".flow-node-box").remove();
                                return;
                            } else {
                                g.data = result.data;
                                g.showChooseUser();
                            }
                        }, failure: function (result) {
                            myMask.hide();
                        }
                    });
                }
            }]
        };
    },
    initWindContainer: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            id: "containerId",
            height: 400,
            border: true,
            style: {
                "border-radius": "3px"
            },
            html: g.getContainerHtml()
        };
    },
    getContainerHtml: function () {
        return '<div class="chooseExecutor"></div>';
    },
    showChooseUser: function () {
        var g = this;
        var data = this.data.nodeInfoList;
        if (data == null) {
            var status = {
                msg: "流程定义未找到",
                success: false,
                showTime: 4
            };
            EUI.ProcessStatus(status);
            return;
        }
        $(".flow-approve", this.win.dom).hide();
        $(".flow-chooseuser").show();
        $(".flow-node-box").remove();
        console.log(data)
        for (var i = 0; i < data.length; i++) {
            var node = data[i];
            var nodeType = "普通任务";
            var iconCss = "choose-radio";
            if(node.uiUserType == "AnyOne"){
                var html =  g.showAnyContainer(data);
                console.log(html)
                $(".chooseExecutor").after(html);
                return;
            }
            if (node.flowTaskType == "singleSign") {
                nodeType = "单签任务";
                iconCss = "choose-checkbox";
            } else if (node.flowTaskType == "countersign") {
                nodeType = "会签任务";
                iconCss = "choose-checkbox";
            }
            var nodeHtml = '<div class="flow-node-box" index="' + i + '">' +
                '<div class="flow-excutor-title">' + node.name + '-[' + nodeType +
                ']</div><div class="flow-excutor-content">';
            if (iconCss == "choose-radio") {
                if (node.executorSet.length == 1) {
                    var html = g.showUserItem(node, nodeHtml, iconCss, nodeType);
                    $(".chooseExecutor").after(html);
                    $(".flow-user-item").addClass("select");
                } else {
                    var html = g.showUserItem(node, nodeHtml, iconCss, nodeType);
                    $(".chooseExecutor").after(html);
                    $(".flow-excutor-content").children("div").eq(0).addClass("select");
                }
            }
            if (iconCss == "choose-checkbox") {
                var html = g.showUserItem(node, nodeHtml, iconCss, nodeType);
                $(".chooseExecutor").after(html);
                $(".flow-user-item").addClass("select");
            }
        }

    },
    showAnyContainer: function (data) {
        var g = this;
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var node = data[i];
            g.chooseUserNode = node;
            var nodeType = "任意执行人任务";
            var iconCss = "choose-delete";
            var nodeHtml = '<div class="flow-node-box" index="' + i + '">' +
                '<div class="flow-excutor-title">' + node.name + '-[' + nodeType +
                ']</div><div class="flow-excutor-content2">';

            // for (var j = 0; j < 3; j++) {
            //         nodeHtml += '<div id="78717F7C-4107-11E7-9AC3-ACE010C46AFD" class="flow-user-item select" type="common">'+
            //             '<div class="choose-icon choose-delete"></div>'+
            //             '<div class="excutor-item-title">姓名：小明，岗位：Java开发工程师，组织机构：研发部，编号：10011001</div>'+
            //             '</div>';
            // }

        }
        nodeHtml += "</div>" +
            '<button class="choose-btn">选择执行人</button>'+
            "</div>";
        return html += nodeHtml;
    },
    showUserItem: function (node, nodeHtml, iconCss, nodeType) {
        var html = "";
        for (var j = 0; j < node.executorSet.length; j++) {
            var item = node.executorSet[j];
            if (!item.positionId) {
                nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
                    '<div class="choose-icon ' + iconCss + '"></div>' +
                    '<div class="excutor-item-title">姓名：' + item.name +
                    '，组织机构：' + item.organizationName + '，编号：' + item.code + '</div>' +
                    '</div>';
            } else {
                nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
                    '<div class="choose-icon ' + iconCss + '"></div>' +
                    '<div class="excutor-item-title">姓名：' + item.name + '，岗位：' + item.positionName +
                    '，组织机构：' + item.organizationName + '，编号：' + item.code + '</div>' +
                    '</div>';
            }
        }
        nodeHtml += "</div></div>";
        return html += nodeHtml;
    },
    getSelectedUser: function () {
        var users = [];
        var nodeDoms = $(".flow-node-box");
        for (var i = 0; i < nodeDoms.length; i++) {
            var nodeDom = $(nodeDoms[i]);
            var index = nodeDom.attr("index");
            var data = this.data.nodeInfoList[index];
            var node = {
                nodeId: data.id,
                userVarName: data.userVarName,
                flowTaskType: data.flowTaskType
            };
            var itemDoms = $(".select", nodeDom);
            var ids = "";
            for (var j = 0; j < itemDoms.length; j++) {
                if (j > 0) {
                    ids += ",";
                }
                ids += $(itemDoms[j]).attr("id");
            }
            node.userIds = ids;
            users.push(node);
        }
        return users;
    },
    checkUserValid: function () {
        var nodeDoms = $(".flow-node-box");
        if (nodeDoms.length == 0) {
            return false;
        }
        for (var i = 0; i < nodeDoms.length; i++) {
            var nodeDom = $(nodeDoms[i]);
            var index = nodeDom.attr("index");
            var data = this.data.nodeInfoList[index];
            var itemDoms = $(".select", nodeDom);
            if (itemDoms.length == 0) {
                EUI.ProcessStatus({
                    success: false,
                    msg: "请选择[" + data.name + "]的执行人"
                });
                return false;
            }
        }
        return true;
    },
    submit: function () {
        var g = this;
        if (!g.checkUserValid()) {
            return;
        }
        var mask = EUI.LoadMask({
            msg: "正在启动，请稍候..."
        });
        EUI.Store({
            url: g.url,
            params: {
                businessKey: g.businessKey,
                businessModelCode: g.businessModelCode,
                typeId: g.typeId,
                opinion: null,
                taskList: JSON.stringify(this.getSelectedUser())
            },
            success: function (status) {
                mask.hide();
                var status = {
                    msg: "启动成功",
                    success: true
                };
                EUI.ProcessStatus(status);
                g.win.close();
                g.afterSubmit && g.afterSubmit.call(g);
            },
            failure: function (response) {
                mask.hide();
                EUI.ProcessStatus(response);
            }
        });
    },
    showChooseExecutorWind:function () {
        var g = this;
        g.chooseAnyOneWind = EUI.Window({
            title: "选择任意执行人",
            width: 800,
            layout: "border",
            height: 500,
            padding: 8,
            itemspace: 0,
            items: [this.initChooseUserWindLeft(), this.InitChooseUserGrid()]
        });
    },
    initChooseUserWindLeft:function () {
        var g = this;
        return {
            xtype: "Container",
            region: "west",
            border: false,
            width: 250,
            itemspace: 0,
            layout: "border",
            items: [this.initChooseUserWindTopBar(), this.initChooseUserWindTree()]
        }
    },
    initChooseUserWindTopBar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            border: false,
            padding: 0,
            isOverFlow: false,
            items: ['->', {
                xtype: "SearchBox",
                width: 120,
                displayText: "根据名称搜索",
                onSearch: function (v) {
                   EUI.getCmp("chooseAnyUserTree").search(v);
                    g.selectedOrgId = null;
                },
                afterClear: function () {
                    EUI.getCmp("chooseAnyUserTree").reset();
                    g.selectedOrgId = null;
                }

            }]
        };
    },
    initChooseUserWindTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            region: "center",
            id:"chooseAnyUserTree",
            url: _ctxPath + "/flowDefination/listAllOrgs",
            border: true,
            searchField: ["name"],
            showField: "name",
            style: {
                "background": "#fff"
            },
            onSelect: function (node) {
                g.selectedOrgId = node.id;
                //   EUI.getCmp("gridPanel").grid[0].p.postData={}
                var chooseUserGridPanel = EUI.getCmp("chooseUserGridPanel").setGridParams({
                    url: _ctxPath + "/customExecutor/listAllUser",
                    loadonce: false,
                    datatype: "json",
                    postData: {
                        organizationId: g.selectedOrgId
                    }
                }, true);
            },
            afterItemRender: function (nodeData) {
                if (nodeData.frozen) {
                    var nodeDom = $("#" + nodeData.id);
                    if (nodeDom == []) {
                        return;
                    }
                    var itemCmp = $(nodeDom[0].children[0]);
                    itemCmp.addClass("ux-tree-freeze");
                    itemCmp.find(".ux-tree-title").text(itemCmp.find(".ux-tree-title").text() + "(已冻结)");
                }
            },
            afterShowTree: function (data) {
                this.setSelect(data[0].id);
            }
        }
    },
    InitChooseUserGrid: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            itemspace: 0,
            layout: "border",
            border: false,
            items: [{
                xtype: "ToolBar",
                region: "north",
                isOverFlow: false,
                padding: 0,
                height: 40,
                border: false,
                items: [{
                    xtype: "Button",
                    title: "保存",
                    selected: true,
                    handler: function () {
                        var selectRow = EUI.getCmp("chooseUserGridPanel").getSelectRow();
                        console.log(selectRow);
                        g.addChooseUsersInContainer(selectRow);
                        g.chooseAnyOneWind.close();
                    }
                }, '->', {
                    xtype: "SearchBox",
                    displayText: "请输入名称进行搜索",
                    onSearch: function (value) {
                        console.log(value);
                        if (!value) {
                            EUI.getCmp("chooseUserGridPanel").setPostParams({
                                    Q_LK_name: ""
                                }
                            ).trigger("reloadGrid");
                        }
                        EUI.getCmp("chooseUserGridPanel").setPostParams({
                                Q_LK_name: value
                            }
                        ).trigger("reloadGrid");
                    }
                }]
            }, {
                xtype: "GridPanel",
                region: "center",
                id: "chooseUserGridPanel",
                style: {
                    "border-radius": "3px"
                },
                gridCfg: {
                loadonce: true,
               //   url: _ctxPath + "/customExecutor/listAllUser",
                    // postData:{
                    //     organizationId: g.selectedOrgId
                    // },
                    multiselect: true,
                    colModel: [/*{
                        label: "操作",
                        name: "operate",
                        index: "operate",
                        width: '25',
                        align: "center",
                        formatter: function (cellvalue, options, rowObject) {
                            if ("INIT" == rowObject.flowStatus) {
                                var strVar = "<div class='condetail-operate'>" +
                                    "<div class='condetail-start'title='启动流程'></div>"
                                    + "<div class='condetail-update' title='编辑'></div>"
                                    + "<div class='condetail-delete'  title='删除'></div>" +
                                    "</div>";
                            }
                            if ("INPROCESS" == rowObject.flowStatus || "COMPLETED"  == rowObject.flowStatus) {
                                var strVar = "<div class='condetail-operate'>" +
                                    "<div class='condetail-flowHistory'title='流程历史'></div>"
                                    + "<div class='condetail-update' title='编辑'></div>"
                                    + "<div class='condetail-delete'  title='删除'></div>" +
                                    "</div>";
                            }
                            return strVar;
                        }
                    }, */{
                        label: "用户ID",
                        name: "id",
                        index: "id",
                     //   hidden:true
                    }, {
                        label: "用户名称",
                        name: "creatorName",
                        index: "creatorName",
                        width:150,
                        align: "center"
                    }, {
                        label: "员工编号",
                        name: "code",
                        index: "code",
                        width:200
                    }, {
                        label: "组织机构",
                        name: "organization.name",
                        index: "organization.name",
                        width:150,
                        align: "center"
                    }],
                    // ondbClick: function () {
                    //     var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    //     g.getValues(rowData.id);
                    // }
                }
            }]
        }
    },
    addChooseUsersInContainer:function (selectRow) {
        var g = this;
        var html = "";
        for (var j = 0; j < selectRow.length; j++) {
            var item = selectRow[j];
            html += '<div class="flow-anyOneUser-item select" type="' + g.chooseUserNode.flowTaskType + '" id="' + item.id + '">' +
                '<div class="choose-icon choose-delete"></div>' +
                '<div class="excutor-item-title">姓名：' + item.creatorName +
                '，组织机构：' + item["organization.name"] + '，编号：' + item.code + '</div>' +
                '</div>';
        }
        $(".flow-excutor-content2").html(html);
    }
});