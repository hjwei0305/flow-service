/**
 * 流程定义页面
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
            padding: 0,
            items: [this.initLeft(), this.initCenterContainer()]
        });
        this.gridCmp = EUI.getCmp("gridPanel");
        this.treeCmp = EUI.getCmp("treePanel");
        this.editFormCmp = EUI.getCmp("editForm");
       // this.getOrgTreeData();
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
            items: [this.initWindTbar(rowData), this.initWindGrid(rowData)]
        });
    },
    initWindTbar: function (rowData) {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            isOverFlow: false,
            border: false,
            items: [/*{
                xtype: "TextField",
                title: "流程定义",
                readonly: true,
                value: g.flowDefinationName
            },*/ '->', {
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
                    index: "actDefId",
                    hidden: true
                }, {
                    label: "定义KEY",
                    name: "defKey",
                    index: "defKey"
                }, {
                    label: "部署ID",
                    name: "actDeployId",
                    index: "actDeployId",
                    hidden: true
                }/*, {
                    label: "启动条件UEL",
                    name: "startUel",
                    index: "startUel"
                }*/, {
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
        var tab = {
            title: "流程审计界面",
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&id="+ data.id,
        };
        g.addTab(tab);
    },
    addFlowDefination: function () {
        var g = this;
        var tab = {
            title: "流程审计界面",
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId,
        };
        g.addTab(tab);
    },

    // addFlowDefVersion: function (rowData) {
    //     // var g = this;
    //     // parent.homeView.addTab({
    //     //     title: "新增流程定义",
    //     //     id: g.selectedNodeId,
    //     //     url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId
    //     // });
    //     console.log(rowData)
    //     var g = this;
    //     var tab = {
    //         title: "流程审计界面",
    //         url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&flowDefinationName="+ rowData.name+"&flowDefinationDefKey="+rowData.defKey+"&flowTypeId="+g.flowTypeId,
    //     };
    //     g.addTab(tab);
    // },
    addTab: function (tab) {
        window.open(tab.url);
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
                xtype:"SearchBox",
                width:159,
                displayText:g.lang.searchDisplayText,
                onSearch: function (v) {
                    g.treeCmp.search(v);
                },
                afterClear: function () {
                    g.treeCmp.reset();
                }

            }]
        };
    },
    initTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            region: "center",
            url: _ctxPath + "/flowDefination/listAllOrgs",
            border: true,
            id: "treePanel",
            searchField:["name"],
            showField: "name",
            style: {
                "background": "#fff"
            },
            onSelect: function (node) {
                console.log(node);
                if (node.children.length) {
                    g.selectedNode = "根节点";
                    console.log(g.selectedNode);
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
                if (!node.children.length) {
                    g.selectedNode = "子节点";
                    console.log(g.selectedNode);
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
            },
            afterShowTree:function(data){
                this.setSelect(data[0].id);
            }
        }
    },
    // getOrgTreeData: function (rowData) {
    //     var g = this;
    //     var myMask = EUI.LoadMask({
    //         //queryMaskMessageText: "正在努力获取数据，请稍候...",
    //         msg: g.lang.queryMaskMessageText
    //     });
    //     EUI.Store({
    //         async: false,
    //         url: _ctxPath + "/flowDefination/listAllOrgs",
    //         success: function (result) {
    //             myMask.hide();
    //             if(result.success){
    //                 g.treeCmp.setData(result.data,true);
    //                 g.treeCmp.setSelect(result.data[0].id)
    //             }
    //
    //         },
    //         failure: function (re) {
    //             myMask.hide();
    //             var status = {
    //                 msg: re.msg,
    //                 success: false,
    //                 showTime: 6
    //             };
    //             EUI.ProcessStatus(status);
    //         }
    //     });
    // },
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
                items: [/*{
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
                }, */{
                    xtype: "Button",
                    title: this.lang.addResourceText,
                    selected: true,
                    handler: function () {
                         console.log(g.selectedNodeId)
                        // console.log(g.selectedNode)
                        if(!g.selectedNodeId){
                                var status = {
                                    msg: "请选择组织机构",
                                    success: false,
                                    showTime: 6
                                };
                                EUI.ProcessStatus(status);
                                return;
                            }
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
                        width: "50"
                    }, {
                        label: "最新版本ID",
                        name: "lastVersionId",
                        index: "lastVersionId",
                        hidden: true
                    }, {
                        label: "定义KEY",
                        name: "defKey",
                        index: "defKey",
                        width: "50"
                    }, {
                        label: "流程类型",
                        name: "flowType.name",
                        index: "flowType.name",
                        width: "50"
                    }/*, {
                        label: "启动条件UEL",
                        name: "startUel",
                        index: "startUel",
                        width: "50"
                    }*/, {
                        label: "组织机构ID",
                        name: "orgId",
                        index: "orgId",
                        width: "50",
                        hidden: true
                    }, {
                        label: this.lang.depictText,
                        name: "depict",
                        index: "depict",
                        width: "50"
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