/**
 * 显示页面
 */
EUI.FlowDefinationView = EUI.extend(EUI.CustomUI, {
    renderTo: "",
    isEdit: false,
    selectedNode: null,  //当前选中的节点
    tenantCode: "10011",
    flowDefinationId: "",
    flowDefinationName: "",
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
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
            // parent.homeView.addTab({
            //     title: data.name,
            //     url: _ctxPath + "/design"
            // });
            // g.updateDefVersion(data);
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
                            url: _ctxPath + "/maindata/flowDefination/delete",
                            params: {
                                id: rowData.id
                            },
                            success: function () {
                                myMask.hide();
                                EUI.getCmp("gridPanel").refreshGrid();
                            },
                            failure: function () {
                                myMask.hide();
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
                            url: _ctxPath + "/maindata/flowDefination/deleteDefVieson",
                            params: {
                                id: rowData.id
                            },
                            success: function () {
                                myMask.hide();
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            },
                            failure: function () {
                                myMask.hide();
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
            style: {
                overflow: "hidden"
            },
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
    }
    ,
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
                url: _ctxPath + "/maindata/flowDefination/findDefVersion",
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
                        var strVar = "<div class='condetail_updateDefVersion'></div>";
                        if (!rowObject.actDeployId) {
                            strVar += "<div class='condetail_deleteDefVersion'></div>"
                        }
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
                    maxLength: 10,
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
                    maxLength: 10,
                    value: data.lastVersionId
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
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.defKey) {
                        return;
                    }
                    if (!data.depict) {
                        return;
                    }
                    if (!data.startUel) {
                        return;
                    }
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
// updateDefVersion: function (data) {
//     var g = this;
//     console.log(data);
//     win = EUI.Window({
//         title: "修改流程定义版本",
//         height: 350,
//         padding: 15,
//         items: [{
//             xtype: "FormPanel",
//             id: "updateDefVersion",
//             padding: 0,
//             items: [{
//                 xtype: "TextField",
//                 title: "ID",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "id",
//                 width: 220,
//                 maxLength: 10,
//                 value: data.id,
//                 hidden: true
//             }, {
//                 xtype: "TextField",
//                 title: "所属流程定义ID",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "flowDefination.id",
//                 width: 220,
//                 value: g.flowDefinationId,
//                 hidden: true
//             }, {
//                 xtype: "TextField",
//                 title: "名称",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "name",
//                 width: 220,
//                 maxLength: 10,
//                 value: data.name
//             }, {
//                 xtype: "TextField",
//                 title: "定义ID",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "actDefId",
//                 width: 220,
//                 value: data.actDefId
//             }, {
//                 xtype: "TextField",
//                 title: "定义KEY",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "defKey",
//                 width: 220,
//                 value: data.defKey
//             }, {
//                 xtype: "TextField",
//                 title: "部署ID",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "actDeployId",
//                 width: 220,
//                 value: data.actDeployId
//             }, {
//                 xtype: "TextField",
//                 title: "启动条件UEL",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "startUel",
//                 width: 220,
//                 value: data.startUel
//             }, {
//                 xtype: "TextField",
//                 title: "版本号",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "versionCode",
//                 width: 220,
//                 value: data.versionCode
//             }, {
//                 xtype: "TextField",
//                 title: "优先级",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "priority",
//                 width: 220,
//                 value: data.priority
//             }, {
//                 xtype: "TextField",
//                 title: "描述",
//                 labelWidth: 90,
//                 allowBlank: false,
//                 name: "depict",
//                 width: 220,
//                 value: data.depict
//             }]
//         }],
//         buttons: [{
//             title: g.lang.saveText,
//             selected: true,
//             handler: function () {
//                 var form = EUI.getCmp("updateDefVersion");
//                 var data = form.getFormValue();
//                 console.log(data);
//                 if (!data.name) {
//                     return;
//                 }
//                 if (!data.actDefId) {
//                     return;
//                 }
//                 if (!data.defKey) {
//                     return;
//                 }
//                 if (!data.actDeployId) {
//                     return;
//                 }
//                 if (!data.versionCode) {
//                     return;
//                 }
//                 if (!data.priority) {
//                     return;
//                 }
//                 if (!data.depict) {
//                     return;
//                 }
//                 g.saveFlowDefVersion(data);
//             }
//         }, {
//             title: this.lang.cancelText,
//             handler: function () {
//                 win.remove();
//             }
//         }]
//     });
// },
    addFlowDefination: function () {
        var g = this;
        win = EUI.Window({
            title: "新增流程定义",
            height: 260,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowDefination",
                padding: 0,
                items: [{
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
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.defKey) {
                        return;
                    }
                    if (!data.depict) {
                        return;
                    }
                    if (!data.startUel) {
                        return;
                    }
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
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        return;
                    }
                    if (!data.actDefId) {
                        return;
                    }
                    if (!data.defKey) {
                        return;
                    }
                    if (!data.actDeployId) {
                        return;
                    }
                    if (!data.versionCode) {
                        return;
                    }
                    if (!data.priority) {
                        return;
                    }
                    if (!data.depict) {
                        return;
                    }
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
            url: _ctxPath + "/maindata/flowDefination/update",
            params: data,
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
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
            url: _ctxPath + "/maindata/flowDefination/updateDefVersion",
            params: data,
            success: function () {
                myMask.hide();
                EUI.getCmp("defViesonGridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
                myMask.hide();
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
            padding: 1,
            layout: "border",
            items: [this.initTopBar(), this.initTree()]
        }
    },
    initTopBar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 50,
            border: false,
            width: 418,
            padding: 10,
            style: {
                overflow: "hidden"
            },
            items: ['->',{
                xtype: "SearchBox",
                displayText: "请输入名称进行搜索",
            }]
        };
    },
// initBottomBar: function () {
//     var g = this;
//     return {
//         xtype: "Container",
//         region: "south",
//         height: 50,
//         padding: 7,
//         style: {
//             overflow: "hidden",
//         },
//         items: [{
//             xtype: "SearchBox",
//             id: "searchBox_treePanel",
//             //searchDisplayText:"请输入代码或名称查询"
//             displayText: g.lang.searchDisplayText,
//             onSearch: function (v) {
//
//             },
//             afterClear: function () {
//
//             }
//         }]
//     }
// },
    initTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            region: "center",
            border: true,
            id: "treePanel",
            showField: "name",
            onSelect: function (node) {
                if (node.parentId) {
                    EUI.getCmp("editsave").setDisable(false);
                    g.editFormCmp.getCmpByName("name").setReadOnly(false);
                    g.editFormCmp.getCmpByName("frozen").setReadOnly(false);
                    g.editFormCmp.getCmpByName("rank").setReadOnly(false);
                } else {

                    EUI.getCmp("editsave").setDisable(true);
                    g.editFormCmp.getCmpByName("name").setReadOnly(true);
                    g.editFormCmp.getCmpByName("frozen").setReadOnly(true);
                    g.editFormCmp.getCmpByName("rank").setReadOnly(true);
                }
                g.editFormCmp.loadData(node)
                g.selectedNode = node;
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
    }
    ,
    initCenterContainer: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            itemspace: 0,
            padding: 0,
            style: {
                overflow: "auto"
            },
            items: [{
                xtype: "ToolBar",
                region: "north",
                // padding: "10",
                height: "50",
                border: false,
                items: [{
                    xtype: "ComboBox",
                    title: "流程类型",
                    id: "coboId",
                    async: false,
                    colon: false,
                    labelWidth: 70,
                    store: {
                        url: _ctxPath + "/maindata/flowDefination/findAllFlowType"
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
                            url: _ctxPath + "/maindata/flowDefination/find",
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
                xtype: "Container",
                region: "center",
                height: "697",
                items: [{
                    xtype: "GridPanel",
                    region: "center",
                    id: "gridPanel",
                    border:true,
                    style: {
                        "border-radius": "3px"
                    },
                    gridCfg: {
                        loadonce: true,
                        colModel: [{
                            label: this.lang.operateText,
                            name: "operate",
                            index: "operate",
                            width: "50",
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
            }]
        }
    },
    deleteTreeNode: function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            //hintText: 提示
            title: g.lang.hintText,
            //deleteHintMessageText:您确定要删除吗？
            msg: g.lang.deleteHintMessageText,
            buttons: [{
                //okText: 确定
                title: g.lang.okText,
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        //deleteMaskMessageText: 正在删除，请稍候...
                        msg: g.lang.deleteMaskMessageText
                    });
                    EUI.Store({
                        //  url: "../Organization/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            var status = {
                                msg: result.msg,
                                success: result.success,
                                showTime: result.success ? 2 : 60
                            };
                            if (status.success) {
                                g.gridCmp.deleteRow(rowData.id);
                                g.treeCmp.deleteItem(rowData.id);
                                var parentData = g.treeCmp.getParentData(rowData.id);
                                for (var i = 0; i < parentData.children.length; i++) {
                                    if (parentData.children[i].id == node.data.id) {
                                        parentData.children.splice(i, 1);
                                    }
                                    if (parentData.children.length == 0) {
                                        parentData.children = null;
                                        break;
                                    }
                                }
                            }
                            EUI.ProcessStatus(status);
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
                }
            }, {
                //cancelText:取消
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    },
    getFormItems: function (isEdit) {
        var g = this;
        return [{
            xtype: "TextField",
            hidden: true,
            name: "id"
        }, {
            xtype: "TextField",
            hidden: true,
            name: "tenantCode"
        }, {
            xtype: "TextField",
            hidden: true,
            name: "parentId"
        }, {
            xtype: "TextField",
            //codeText:"代码"
            title: g.lang.codeText,
            name: "code",
            maxlength: 6,
            //allowBlank: false,
            hidden: !isEdit,
            readonly: true
        }, {
            xtype: "TextField",
            //nameText:"名称"
            title: g.lang.nameText,
            name: "name",
            maxlength: 100,
            allowBlank: false
        }, {
            xtype: "CheckBox",
            //frozenText:'是否冻结'
            title: g.lang.frozenText,
            name: "frozen"
        }, {
            xtype: "TextField",
            //rankText:'排序'
            title: g.lang.rankText,
            name: "rank",
            allowBlank: false
        }];
    },
    add: function () {
        var g = this;
        g.editWin = EUI.Window({
            //addText: 新增
            title: g.lang.addText,
            height: 200,
            padding: 15,
            width: 500,
            items: [{
                xtype: "FormPanel",
                id: "createForm",
                padding: 0,
                defaultConfig: {
                    labelWidth: 100,
                    width: 340
                },
                items: g.getFormItems(false)
            }],
            buttons: [{
                // saveText:保存
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    g.save();
                }
            }, {
                //cancelText:取消
                title: g.lang.cancelText,
                handler: function () {
                    g.editWin.remove();
                }
            }]
        });
        g.createFormCmp = EUI.getCmp("createForm");
        g.createFormCmp.getCmpByName("parentId").setValue(g.selectedNode.id);
    }
    ,
    save: function () {
        var g = this, data;
        if (g.isEdit) {
            if (!g.editFormCmp.isValid()) {
                g.message("有必输项未输入，请确认！");
                return;
            }
            data = g.editFormCmp.getFormValue();
        } else {
            if (!g.createFormCmp.isValid()) {
                g.message("有必输项未输入，请确认！");
                return;
            }
            data = g.createFormCmp.getFormValue();
            delete data.id;
        }

        data.tenantCode = g.tenantCode;
        var myMask = EUI.LoadMask({
            //saveMaskMessageText:"正在保存，请稍候..."
            msg: g.lang.saveMaskMessageText
        });
        EUI.Store({
            //url: "../Organization/save",
            params: data,
            success: function (result) {
                myMask.hide();
                var status = {
                    msg: result.msg,
                    success: result.success,
                    showTime: result.success ? 2 : 60
                };
                if (status.success) {
                    if (!g.isEdit) {
                        g.editWin.remove();
                    }
                    var nodeData = result.data;
                    var parentData = g.treeCmp.getNodeData(nodeData.parentId);
                    if (g.isEdit) {
                        for (var i = 0; i < parentData.children.length; i++) {
                            if (parentData.children[i].id == nodeData.id) {
                                parentData.children[i].code = nodeData.code;
                                parentData.children[i].name = nodeData.name;
                                parentData.children[i].frozen = nodeData.frozen;
                                parentData.children[i].rank = nodeData.rank;
                            }
                        }
                    } else {
                        parentData.children.push(nodeData);
                    }
                    parentData.children.sort(function (a, b) {
                        return a.rank - b.rank;
                    });
                    g.treeCmp.initTree();
                    g.treeCmp.setSelect(nodeData.id);
                    //g.getOrgTreeData(result.data);
                    //g.gridCmp.reset();
                }
                EUI.ProcessStatus(status);
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
    getOrgTreeData: function (rowData) {
        var g = this;
        var myMask = EUI.LoadMask({
            //queryMaskMessageText: "正在努力获取数据，请稍候...",
            msg: g.lang.queryMaskMessageText
        });
        EUI.Store({
            async: false,
            //    url: _ctxPath + "/maindata/flowDefination/findOrganization",
            url: _ctxPath + "/maindata/flowDefination/findOrgTreeByTenantCode",
            params: {tenantCode: g.tenantCode},
            success: function (result) {
                myMask.hide();
                if (result.success) {
                    g.treeCmp.setData(result.data);
                }
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
    }
    ,
    openNodeMoveWin: function () {
        var g = this, nodeData = g.selectedNode;
        g.moveNodeId = nodeData.id;
        g.nodeMoveWin = EUI.Window({
            title: "移动节点【" + nodeData.name + "】到：",
            height: 360,
            width: 340,
            buttonAlign: 'center',
            items: [{
                xtype: "TreePanel",
                data: g.getMoveToData(nodeData),
                onSelect: function (node) {
                    if (node.id == nodeData.id || nodeData.parentId == node.id || node.parentId == nodeData.id) {
                        $(".selected", this.dom).removeClass("selected");
                        g.targetNode = null;
                        return;
                    }
                    g.targetNode = node;
                }
            }],
            buttons: [{
                title: "确定",
                width: 40,
                selected: true,
                handler: function () {
                    g.moveNodeToSave();
                }
            }, {
                title: "取消",
                width: 40,
                handler: function () {
                    g.nodeMoveWin.close();
                }
            }]
        });
    },
    getMoveToData: function (moveNode) {
        var g = this;
        var data = g.treeCmp.data;
        return g._getMoveData(data, moveNode);
    },
    _getMoveData: function (nodes, moveNode) {
        if (!moveNode) return [];
        var result = [];
        for (var i = 0; i < nodes.length; i++) {
            if (moveNode.parentId == nodes[i].id || (nodes[i]["children"] && (nodes[i].id == moveNode.id))) {
                result = result.concat(this._getMoveData(nodes[i]["children"], moveNode));
            } else if (moveNode.id == nodes[i].parentId) {
                continue;
            }
            else {
                result.push(nodes[i]);
            }
        }
        return result;
    },
    moveNodeToSave: function () {
        var g = this;
        var selNode = g.targetNode;
        if (selNode) {
            var myMask = EUI.LoadMask({
                //queryMaskMessageText: "正在努力获取数据，请稍候...",
                msg: g.lang.queryMaskMessageText
            });
            EUI.Store({
                //url: '../Organization/move/',
                params: {nodeId: g.moveNodeId, targetParentId: selNode.id},
                success: function (result) {
                    myMask.hide();
                    if (result.success) {
                        g.nodeMoveWin.close();
                        g.getOrgTreeData();
                        // g.treeCmp = EUI.getCmp("treePanel");
                        g.treeCmp.setSelect(g.moveNodeId);
                    }
                    EUI.ProcessStatus({
                        msg: result.msg,
                        success: result.success,
                        showTime: result.success ? 2 : 60
                    });
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
        } else {
            //moveHintMessageText: "请选择您要移动的节点！"
            g.message(g.lang.moveHintMessageText);
            return false;
        }
    },
    /*
     setNodeFreezedClass: function (node) {
     var freezedItem = $("#" + node.id);
     if (freezedItem == []) {
     return;
     }
     var itemcmp = $(freezedItem[0].children[0]);
     if (node.IsFreeze) {
     itemcmp.find("span").addClass("ux-tree-freezed");
     var itemdom = itemcmp[0];
     var title = "(已冻结)" + itemdom.title;
     itemcmp.attr("title", title);
     } else {
     itemcmp.find("span").removeClass("ux-tree-freezed");
     var itemdom = itemcmp[0];
     var title = itemdom.title.replace("(已冻结)", "");
     itemcmp.attr("title", title);
     }
     },
     */
    message: function (msg) {
        var g = this;
        var message = EUI.MessageBox({
            border: true,
            //hintText: "提示",
            title: g.lang.hintText,
            showClose: true,
            msg: msg,
            buttons: [{
                //okText:"确定",
                title: g.lang.okText,
                handler: function () {
                    message.remove();
                }
            }]
        });
    }
});