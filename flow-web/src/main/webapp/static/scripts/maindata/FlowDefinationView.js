/**
 * 显示页面
 */
EUI.FlowDefinationView = EUI.extend(EUI.CustomUI, {
    renderTo: "",
    isEdit: false,
    selectedNode: null,  //当前选中的节点
    selectedNodeId: "",  //当前选中的节点的ID
    selectedNodeName: "",  //当前选中的节点的name
    flowDefinationId: "",
    flowDefinationName: "",
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            items: [this.initLeft(), this.initCenterContainer()]
        });
        this.gridCmp = EUI.getCmp("gridPanel");
        this.treeCmp = EUI.getCmp("treePanel");
        this.editFormCmp = EUI.getCmp("editForm");
        this.getOrgTreeData();
        this.addEvents();
    },
    addEvents: function () {
        var g = this;
        this.operateBtnEvents();
    },
    operateBtnEvents: function () {
        var g = this;
        $(".condetail_update").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.updateFlowDefnation(data);
        });
        $(".condetail_updateDefVersion").live("click", function () {
            var data = EUI.getCmp("defViesonGridPanel").getSelectRow();
        });

        $(".condetail_delete").live("click", function () {
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
                            url: _ctxPath + "/flowDefination/delete",
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
        $(".condetail_deleteDefVersion").live("click", function () {
            var rowData = EUI.getCmp("defViesonGridPanel").getSelectRow();
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
                            url: _ctxPath + "/flowDefination/deleteDefVieson",
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
        $(".condetail_look").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.flowDefinationId = rowData.id;
            g.flowDefinationName = rowData.name;
            g.lookPropertyWindow(rowData);
        });
    },
    lookPropertyWindow: function (rowData) {
        var g = this;
        var Wind = EUI.Window({
            title: "流程定义版本",
            width: 1000,
            layout: "border",
            height: 500,
            padding: 8,
            itemspace: 0,
            items: [this.initWindTbar(), this.initWindGrid(rowData)]
        });
    },
    initWindTbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "TextField",
                title: "流程定义",
                readonly: true,
                value: g.flowDefinationName
            }, {
                xtype: "Button",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addFlowDefVersion();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: "请输入名称进行搜索",
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("defViesonGridPanel").setPostParams({
                                Q_LK_name: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("defViesonGridPanel").setPostParams({
                            Q_LK_name: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        };
    },
    initWindGrid: function (rowData) {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "defViesonGridPanel",
            style: {
                "border-radius": "3px"
            },
            gridCfg: {
                // loadonce: true,
                url: _ctxPath + "/flowDefination/listDefVersion",
                postData: {
                    "Q_EQ_flowDefination.id": rowData.id
                },
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "100",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_defVerOperate'>";
                        if (rowObject.actDeployId) {
                            strVar += "<div class='condetail_updateDefVersion'></div>";
                        } else {
                            strVar += "<div class='condetail_updateDefVersion'></div>" +
                                "<div class='condetail_deleteDefVersion'></div>";
                        }
                        strVar += "</div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "名称",
                    name: "name",
                    index: "name"
                }, {
                    label: "定义ID",
                    name: "actDefId",
                    index: "actDefId"
                }, {
                    label: "定义KEY",
                    name: "defKey",
                    index: "defKey"
                }, {
                    label: "部署ID",
                    name: "actDeployId",
                    index: "actDeployId"
                }, {
                    label: "启动条件UEL",
                    name: "startUel",
                    index: "startUel"
                }, {
                    label: "版本号",
                    name: "versionCode",
                    index: "versionCode",
                    align: "right"
                }, {
                    label: "优先级",
                    name: "priority",
                    index: "priority"
                }, {
                    label: "描述",
                    name: "depict",
                    index: "depict"
                }]
            }
        };
    },
    updateFlowDefnation: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: "修改流程定义",
            height: 300,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateFlowDefination",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "id",
                    width: 220,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "流程类型ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "flowType.id",
                    width: 220,
                    value: g.flowTypeId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "流程类型",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "flowType.name",
                    width: 220,
                    value: g.flowTypeName
                }, {
                    xtype: "TextField",
                    title: "名称",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: "最新版本ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "lastVersionId",
                    width: 220,
                    value: data.lastVersionId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "定义KEY",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "defKey",
                    width: 220,
                    value: data.defKey
                }, {
                    xtype: "TextField",
                    title: "启动UEL",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "startUel",
                    width: 220,
                    value: data.startUel
                }, {
                    xtype: "TextField",
                    title: "组织机构ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "orgId",
                    width: 220,
                    value: data.orgId,
                    hidden: true
                }, {
                    xtype: "TextArea",
                    title: "描述",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateFlowDefination");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowDefination(data);
                }
            }, {
                title: this.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addFlowDefination: function () {
        var g = this;
        win = EUI.Window({
            title: "新增流程定义",
            height: 270,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowDefination",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "组织机构ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "orgId",
                    width: 220,
                    value: g.selectedNodeId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "组织机构",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "orgName",
                    width: 220,
                    readonly: true,
                    value: g.selectedNodeName
                }, {
                    xtype: "TextField",
                    title: "流程类型ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "flowType.id",
                    width: 220,
                    value: g.flowTypeId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "流程类型",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "flowType.name",
                    width: 220,
                    value: g.flowTypeName
                }, {
                    xtype: "TextField",
                    title: this.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "最新版本ID",
                    labelWidth: 90,
                    allowBlank: true,
                    name: "lastVersionId",
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "定义KEY",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "defKey",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "启动UEL",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "startUel",
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: this.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addFlowDefination");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowDefination(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addFlowDefVersion: function () {
        var g = this;
        win = EUI.Window({
            title: "新增流程定义版本",
            height: 350,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowDefVersion",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "流程定义ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "flowDefination.id",
                    width: 220,
                    value: g.flowDefinationId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "流程定义NAME",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "flowDefination.name",
                    width: 220,
                    value: g.flowDefinationName,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "名称",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "定义ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "actDefId",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "定义KEY",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "defKey",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "部署ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "actDeployId",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "启动UEL",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "startUel",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "版本号",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "versionCode",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "优先级",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "priority",
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: "描述",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addFlowDefVersion");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowDefVersion(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveFlowDefination: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/flowDefination/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (result.success) {
                    EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                }
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
                myMask.hide();
            }
        });
        win.close();
        myMask.hide();
    },
    saveFlowDefVersion: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/flowDefination/saveDefVersion",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (result.success) {
                    EUI.getCmp("defViesonGridPanel").grid.trigger("reloadGrid");
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
        win.close();
        myMask.hide();
    },
    initLeft: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "west",
            border: false,
            width: 420,
            itemspace: 0,
            layout: "border",
            items: [this.initTopBar(), this.initTree()]
        }
    },
    initTopBar: function () {
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
                displayText: "请输入名称进行搜索"
            }]
        };
    },
    initTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            region: "center",
            border: true,
            id: "treePanel",
            showField: "name",
            style: {
                "background": "#fff"
            },
            onSelect: function (node) {
                console.log(node);
                if (node.children.length) {
                    console.log("根节点");
                    g.selectedNodeId = "";
                    g.selectedNodeName = "";
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/flowDefination/listFlowDefination",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_orgId: g.selectedNodeId
                        }
                    }, true)
                }
                if (!node.children.length) {
                    console.log("子节点");
                    g.selectedNodeId = node.id;
                    g.selectedNodeName = node.name;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/flowDefination/listFlowDefination",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_orgId: g.selectedNodeId
                        }
                    }, true)
                }
                // if (node.parentId) {
                //     EUI.getCmp("editsave").setDisable(false);
                //     g.editFormCmp.getCmpByName("name").setReadOnly(false);
                //     g.editFormCmp.getCmpByName("frozen").setReadOnly(false);
                //     g.editFormCmp.getCmpByName("rank").setReadOnly(false);
                // } else {
                //     EUI.getCmp("editsave").setDisable(true);
                //     g.editFormCmp.getCmpByName("name").setReadOnly(true);
                //     g.editFormCmp.getCmpByName("frozen").setReadOnly(true);
                //     g.editFormCmp.getCmpByName("rank").setReadOnly(true);
                // }
                // g.editFormCmp.loadData(node)
                // g.selectedNode = node;
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
            }
        }
    },
    getOrgTreeData: function (rowData) {
        var g = this;
        var myMask = EUI.LoadMask({
            //queryMaskMessageText: "正在努力获取数据，请稍候...",
            msg: g.lang.queryMaskMessageText
        });
        EUI.Store({
            async: false,
            url: _ctxPath + "/flowDefination/listAllOrgs",
            success: function (data) {
                myMask.hide();
                g.treeCmp.setData(data);
            },
            failure: function (re) {
                myMask.hide();
                var status = {
                    msg: re.msg,
                    success: false,
                    showTime: 6
                };
                EUI.ProcessStatus(status);
            }
        });
    },
    initCenterContainer: function () {
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
                    xtype: "ComboBox",
                    title: "<span style='font-weight: bold'>" + "流程类型" + "</span>",
                    id: "coboId",
                    async: false,
                    colon: false,
                    labelWidth: 70,
                    store: {
                        url: _ctxPath + "/flowDefination/listAllFlowType"
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
                        g.flowTypeId = data[0].id;
                        g.flowTypeName = data[0].name;
                        var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                            url: _ctxPath + "/flowDefination/listFlowDefination",
                            loadonce: false,
                            datatype: "json",
                            postData: {
                                "Q_EQ_flowType.id": data[0].id
                            }
                        }, true)
                    },
                    afterSelect: function (data) {
                        //console.log(data);
                        g.flowTypeId = data.data.id;
                        g.flowTypeName = data.data.name;
                        EUI.getCmp("gridPanel").setPostParams({
                                "Q_EQ_flowType.id": data.data.id
                            }
                        ).trigger("reloadGrid");
                    }
                }, {
                    xtype: "Button",
                    title: this.lang.addResourceText,
                    selected: true,
                    handler: function () {
                        g.addFlowDefination();
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
            }, {
                xtype: "GridPanel",
                region: "center",
                id: "gridPanel",
                border: true,
                style: {
                    "border-radius": "3px"
                },
                gridCfg: {
                    loadonce: true,
                    colModel: [{
                        label: this.lang.operateText,
                        name: "operate",
                        index: "operate",
                        width: "30%",
                        align: "center",
                        formatter: function (cellvalue, options, rowObject) {
                            var strVar = "<div class='condetail_operate'>"
                                + "<div class='condetail_look'></div>"
                                + "<div class='condetail_update'></div>"
                                + "<div class='condetail_delete'></div></div>";
                            return strVar;
                        }
                    }, {
                        name: "id",
                        index: "id",
                        width: "50",
                        hidden: true
                    }, {
                        label: this.lang.nameText,
                        name: "name",
                        index: "name",
                        width: "50",
                        title: false
                    }, {
                        label: "最新版本ID",
                        name: "lastVersionId",
                        index: "lastVersionId",
                        title: false,
                        hidden: true
                    }, {
                        label: "定义KEY",
                        name: "defKey",
                        index: "defKey",
                        width: "50",
                        title: false
                    }, {
                        label: "启动条件UEL",
                        name: "startUel",
                        index: "startUel",
                        width: "50",
                        title: false
                    }, {
                        label: "组织机构ID",
                        name: "orgId",
                        index: "orgId",
                        width: "50",
                        title: false,
                        hidden: true
                    }, {
                        label: this.lang.depictText,
                        name: "depict",
                        index: "depict",
                        width: "50",
                        title: false
                    }],
                    ondbClick: function () {
                        var rowData = EUI.getCmp("gridPanel").getSelectRow();
                        g.getValues(rowData.id);
                    }
                }
            }]
        }
    }
});