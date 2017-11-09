/**
 * 流程定义页面
 */
EUI.FlowDefinationView = EUI.extend(EUI.CustomUI, {
    renderTo: "",
    isEdit: false,
    selectedNodeId: "",  //当前选中的节点的ID
    selectedNodeName: "",  //当前选中的节点的name
    selectedNodeOrgCode: "",  //当前选中的节点的组织机构代码
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 0,
            items: [this.initTree(), this.initGrid()]
        });
        this.gridCmp = EUI.getCmp("gridPanel");
        this.treeCmp = EUI.getCmp("treePanel");
        this.editFormCmp = EUI.getCmp("editForm");
        this.addEvents();
    },
    addEvents: function () {
        var g = this;
        this.operateBtnEvents();
    },
    operateBtnEvents: function () {
        var g = this;
        $("#defFlow >.ecmp-common-edit").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.updateFlowDefnation(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            g.deleteFlowDefinationWind(rowData);
        });
        $("#defFlow > .def-version").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            g.lookPropertyWindow(rowData);
        });
        $("#defFlow>.ecmp-common-activate").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            g.activateOrFreezeFlow("gridPanel",{id:rowData.id,status:'Activate'},"../flowDefination/activateOrFreezeFlowDef",true);
        });
        $("#defFlow>.ecmp-common-suspend").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            g.activateOrFreezeFlow("gridPanel",{id:rowData.id,status:'Freeze'},"../flowDefination/activateOrFreezeFlowDef",false);
        });
    },
    addDefVersionWinEvents:function () {
        var g=this;
        $("#defVersion>.ecmp-common-edit").live("click", function () {
            var data = EUI.getCmp("defViesonGridPanel").getSelectRow();
            g.updateFlowDefVersion(data);
        });
        $("#defVersion>.ecmp-common-view").live("click", function () {
            var rowData = EUI.getCmp("defViesonGridPanel").getSelectRow();
            g.viewFlowDefnation(rowData);
        });
        $("#defVersion>.ecmp-common-suspend").live("click", function () {
            var rowData = EUI.getCmp("defViesonGridPanel").getSelectRow();
            g.activateOrFreezeFlow("defViesonGridPanel",{id:rowData.id,status:'Freeze'},"../flowDefination/activateOrFreezeFlowVer",false);
        });
        $("#defVersion>.ecmp-common-activate").live("click", function () {
            var rowData = EUI.getCmp("defViesonGridPanel").getSelectRow();
            g.activateOrFreezeFlow("defViesonGridPanel",{id:rowData.id,status:'Activate'},"../flowDefination/activateOrFreezeFlowVer",true);
        });
    },
    deleteFlowDefinationWind:function(rowData){
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
                        url: _ctxPath + "/flowDefination/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
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
    lookPropertyWindow: function (rowData) {
        var g = this;
        var Wind = EUI.Window({
            title: g.lang.flowDefinitionVersionText,
            iconCss:"ecmp-eui-setting",
            width: 800,
            height: 500,
            padding: 8,
            items: [this.initWindGrid(rowData)],
            afterClose:function () {
                g.remove();
            }
        });
        g.addDefVersionWinEvents();
    },
    initWindTbar: function (rowData) {
        var g = this;
        return [{
                xtype: "Button",
                title: g.lang.copyText,
                iconCss:"ecmp-common-copy",
                selected:true,
                handler: function() {
                    var rowData =EUI.getCmp("defViesonGridPanel").getSelectRow();
                    if (rowData && rowData.id) {
                        rowData.orgCode=rowData["flowDefination.orgCode"];
                        g.copyFlowDefination(rowData,true);
                    } else {
                        EUI.ProcessStatus({msg:g.lang.copyHintMessage,success: false});
                    }
                }
            },'->', {
                xtype: "SearchBox",
                displayText: g.lang.searchByNameText,
                onSearch: function (value) {
                    EUI.getCmp("defViesonGridPanel").setPostParams({
                            Q_LK_name: value
                        },true);
                }
            }];
    },
    initWindGrid: function (rowData) {
        var g = this;
        return {
            xtype: "GridPanel",
            tbar: this.initWindTbar(rowData),
            id: "defViesonGridPanel",
            style: {
                "border-radius": "3px"
            },
            border: true,
            gridCfg: {
                shrinkToFit: false,
                url: _ctxPath + "/flowDefination/listDefVersion",
                postData: {
                    "Q_EQ_flowDefination.id": rowData.id,
                    S_versionCode:"ASC"
                },
                colModel: [{
                    label: g.lang.operateText,
                    name: "operate",
                    index: "operate",
                    align: "left",
                    width:120,
                    formatter : function(cellvalue, options, rowObject) {
                        var str='<div id="defVersion"><i class="ecmp-common-edit icon-space fontcusor" title="'+g.lang.editText+'"></i><i class="ecmp-common-view icon-space fontcusor" title="'+g.lang.viewFlowDefText+'"></i>';
                        if('Activate' == rowObject.flowDefinationStatus){
                            str=str+'<i class="ecmp-common-suspend fontcusor" title="'+g.lang.suspendText+'"></i><i class="ecmp-common-activate fontcusor" style="display: none"  title="'+g.lang.activeText+'"></i></div>';
                        }
                        else if('Freeze' == rowObject.flowDefinationStatus){
                            str=str+'<i class="ecmp-common-suspend fontcusor" style="display: none" title="'+g.lang.suspendText+'"></i><i class="ecmp-common-activate fontcusor"  title="'+g.lang.activeText+'"></i></div>';
                        }
                        return str;
                    }
                },{
                    name: "id",
                    index: "id",
                    hidden: true
                },{
                    name: "flowDefination.flowType.businessModel.id",
                    index: "flowDefination.flowType.businessModel.id",
                    hidden: true
                },{
                    name: "flowDefination.flowType.businessModel.className",
                    index: "flowDefination.flowType.businessModel.className",
                    hidden: true
                },{
                    name: "flowDefinationStatus",
                    index: "flowDefinationStatus",
                    hidden: true
                },{
                    name: "flowDefination.orgCode",
                    index: "flowDefination.orgCode",
                    hidden: true
                },{
                    name: "flowDefination.id",
                    index: "flowDefination.id",
                    hidden: true
                },{
                    name: "versionCode",
                    index: "versionCode",
                    hidden: true
                }, {
                    label: g.lang.nameText,
                    width: 240,
                    name: "name",
                    index: "name"
                }, {
                    label: g.lang.definitionIDText,
                    name: "actDefId",
                    index: "actDefId",
                    hidden: true
                }, {
                    label: g.lang.definitionKEYText,
                    width:180,
                    name: "defKey",
                    index: "defKey"
                }, {
                    label: g.lang.deployIDText,
                    width:100,
                    name: "actDeployId",
                    index: "actDeployId",
                    hidden: true
                }, {
                    label: g.lang.versionText,
                    width:80,
                    name: "versionCode",
                    index: "versionCode",
                    align: "right"
                }, {
                    label: g.lang.priorityText,
                    width:80,
                    name: "priority",
                    index: "priority"
                },{
                    label: g.lang.flowDefinitionStatusText,
                    width:120,
                    name: "flowDefinationStatusText",
                    index: "flowDefinationStatus",
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = '';
                        if('INIT' == rowObject.flowDefinationStatus){
                            strVar = g.lang.unReleasedText;
                        }else if('Activate' == rowObject.flowDefinationStatus){
                            strVar = g.lang.activeText;
                        }
                        else if('Freeze' == rowObject.flowDefinationStatus){
                            strVar = g.lang.suspendText;
                        }
                        return strVar;
                    }
                }, {
                    label: g.lang.depictText,
                    width:300,
                    name: "depict",
                    index: "depict"
                }]
            }
        };
    },

    viewFlowDefnation: function (data) {
        var g = this;
        var tab = {
            title: g.lang.viewFlowDefText+data.name,
            url: _ctxPath + "/design/showLook?id="+data.id
        };
        g.addTab(tab);
    },
    updateFlowDefnation: function (data) {
        var g = this;
        var tab = {
            title: g.lang.editFlowDefinitionText+data.name,
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data.orgCode+"&id="+ data.id+"&businessModelId="+data["flowType.businessModel.id"]+"&businessModelCode="+data["flowType.businessModel.className"],
            id:data.id
        };
        g.addTab(tab);
    },
    addFlowDefination: function () {
        var g = this;
        var tab = {
            title: g.lang.addFlowDefinitionText,
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId+"&orgCode="+g.selectedNodeOrgCode
        };
        g.addTab(tab);
    },
    copyFlowDefination: function (data,isFromVersion) {
        var g = this;
        var id= isFromVersion?data["flowDefination.id"]:data.id;
        var businessModelId=isFromVersion?data["flowDefination.flowType.businessModel.id"]:data["flowType.businessModel.id"];
        var businessModelCode=isFromVersion?data["flowDefination.flowType.businessModel.className"]:data["flowType.businessModel.className"];
        var url=_ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data.orgCode+"&orgName="+encodeURIComponent(encodeURIComponent(data.orgName))+"&id="+id+"&businessModelId="+businessModelId+"&businessModelCode="+businessModelCode+"&isCopy="+true+"&isFromVersion="+isFromVersion;
        if(isFromVersion){
            var url=_ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data.orgCode+"&id="+id+"&businessModelId="+businessModelId+"&businessModelCode="+businessModelCode+"&isCopy="+true+"&isFromVersion="+isFromVersion;
        }
        var tab = {
            title: g.lang.copyFlowDefinitionText,
            url:url
        };
        g.addTab(tab);
    },
    updateFlowDefVersion: function (data) {
        var g = this;
        var tab = {
            title: g.lang.copyFlowDefinitionText,
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data["flowDefination.orgCode"]+"&id="+data["flowDefination.id"]+"&businessModelId="+data["flowDefination.flowType.businessModel.id"]+"&businessModelCode="+data["flowDefination.flowType.businessModel.className"],
        };
        g.addTab(tab);
    },
    addTab: function (tab) {
        if(parent.homeView){
            parent.homeView.addTab(tab);//获取到父窗口homeview，在其中新增页签
        }else{
            window.open(tab.url);
        }
    },
    initTopBar: function () {
        var g = this;
        return ['->', {
                xtype:"SearchBox",
                width:220,
                displayText:g.lang.searchDisplayText,
                canClear: true,
                onSearch: function (v) {
                    g.treeCmp.search(v);
                    if(g.treeCmp.data&&g.treeCmp.data.length>0){
                        g.treeCmp.afterShowTree(g.treeCmp.data);
                    }
                    g.selectedNodeId = null;
                    g.selectedNodeOrgCode = null;
                    g.selectedNodeName = null;
                },
                afterClear: function () {
                    g.treeCmp.reset();
                    g.selectedNodeId = null;
                    g.selectedNodeOrgCode = null;
                    g.selectedNodeName = null;
                }

            }];
    },
    initTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            region: "west",
            title: "组织机构",
            tbar: this.initTopBar(),
            url: _ctxPath + "/flowDefination/listAllOrgs",
            border: true,
            id: "treePanel",
            searchField:["code","name"],
            showField: "name",
            onSelect: function (node) {
                g.selectedNodeId = node.id;
                g.selectedNodeName = node.name;
                g.selectedNodeOrgCode = node.code;
                var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                    url: _ctxPath + "/flowDefination/listFlowDefination",
                    loadonce: false,
                    datatype: "json",
                    postData: {
                        Q_EQ_orgId: g.selectedNodeId,
                        S_lastEditedDate:"DESC"
                    }
                }, true)
            },
            afterItemRender: function (nodeData) {
                if (nodeData.frozen) {
                    var nodeDom = $("#" + nodeData.id);
                    if (nodeDom == []) {
                        return;
                    }
                    var itemCmp = $(nodeDom[0].children[0]);
                    itemCmp.addClass("ux-tree-freeze");
                    itemCmp.find(".ux-tree-title").text(itemCmp.find(".ux-tree-title").text() + g.lang.FreezeText);
                }
            },
            afterShowTree:function(data){
                this.setSelect(data[0].id);
            }
        }
    },
    initGridTbar: function(){
        var g = this;
        return [{
            xtype: "Button",
            title: g.lang.addResourceText,
            iconCss:"ecmp-common-add",
            selected: true,
            handler: function () {
                if(!g.selectedNodeId){
                    var status = {
                        msg:g.lang.chooseOrganizationMsgText,
                        success: false,
                        showTime: 4
                    };
                    EUI.ProcessStatus(status);
                    return;
                }
                g.addFlowDefination();
            }
        },{
            xtype: "Button",
            title: g.lang.copyText,
            iconCss:"ecmp-common-copy",
            handler: function() {
                var rowData =EUI.getCmp("gridPanel").getSelectRow();
                if (rowData && rowData.id) {
                    rowData.orgName=EUI.getCmp("treePanel").getNodeData(rowData.orgId).name;
                    g.copyFlowDefination(rowData,false);
                } else {
                    EUI.ProcessStatus({msg:g.lang.copyHintMessage,success: false});
                }

            }
        }, '->', {
            xtype: "SearchBox",
            displayText: g.lang.searchByNameText,
            onSearch: function (value) {
                EUI.getCmp("gridPanel").setPostParams({
                        Q_LK_name: value
                    }
                ).trigger("reloadGrid");
            }
        }];
    },
    initGrid: function () {
        var g = this;
        return {
                xtype: "GridPanel",
                title: "流程定义管理",
                tbar: this.initGridTbar(),
                region: "center",
                id: "gridPanel",
                border: true,
                style: {
                    "border-radius": "3px"
                },
                gridCfg: {
                    loadonce: true,
                    datatype:"local",
                    colModel: [{
                        label: g.lang.operateText,
                        name: "operate",
                        index: "operate",
                        width: 120,
                        align: "left",
                        formatter : function(cellvalue, options, rowObject) {
                            var str='<div id="defFlow"><i class="ecmp-common-edit icon-space fontcusor" title="'+g.lang.editText+'"></i>'+
                                '<i class="ecmp-common-delete  icon-space fontcusor" title="'+g.lang.deleteText+'"></i>' +
                                '<i class="ecmp-common-version def-version icon-space fontcusor" title="'+g.lang.flowDefinitionVersionText+'"></i>';
                            if('Activate' == rowObject.flowDefinationStatus){
                                str=str+'<i class="ecmp-common-suspend fontcusor"  title="'+g.lang.suspendText+'"></i><i class="ecmp-common-activate fontcusor" style="display: none" title="'+g.lang.activeText+'"></i></div>';
                            }
                            else if('Freeze' == rowObject.flowDefinationStatus){
                                str=str+'<i class="ecmp-common-suspend fontcusor" style="display: none" title="'+g.lang.suspendText+'"></i><i class="ecmp-common-activate fontcusor"  title="'+g.lang.activeText+'"></i></div>';
                            }
                            return str;
                        }
                    }, {
                        name: "id",
                        index: "id",
                        hidden: true
                    }, {
                        name: "flowDefinationStatus",
                        index: "flowDefinationStatus",
                        hidden: true
                    },{
                        label: g.lang.nameText,
                        name: "name",
                        index: "name",
                        sortable: true,
                        width:300
                    }, {
                        label: g.lang.latestVersionIDText,
                        name: "lastVersionId",
                        index: "lastVersionId",
                        hidden: true
                    }, {
                        label: g.lang.definitionKEYText,
                        name: "defKey",
                        index: "defKey",
                        sortable: true,
                        width:280
                    }, {
                        label: g.lang.flowTypeText,
                        name: "flowType.name",
                        index: "flowType.name",
                        sortable: true,
                        width:110
                    }, {
                        label: g.lang.businessEntityIDText,
                        name: "flowType.businessModel.id",
                        index: "flowType.businessModel.id",
                        hidden: true
                    }, {
                        label: g.lang.businessEntityIDText,
                        name: "flowType.businessModel.className",
                        index: "flowType.businessModel.className",
                        hidden: true
                    },{
                        label: g.lang.launchConditionUELText,
                        name: "startUel",
                        index: "startUel",
                        width:110,
                        hidden:true
                    },{
                        label: g.lang.organizationIDText,
                        name: "orgId",
                        index: "orgId",
                        hidden: true
                    }, {
                        label: g.lang.organizationCodeText,
                        name: "orgCode",
                        index: "orgCode",
                        hidden: true
                    },{
                        label: g.lang.depictText,
                        name: "depict",
                        index: "depict",
                        width:110,
                        hidden:true
                    },{
                        label: g.lang.flowDefinitionStatusText,
                        name: "flowDefinationStatusText",
                        index: "flowDefinationStatus",
                        align:"center",
                        sortable: true,
                        width:120,
                        formatter : function(cellvalue, options, rowObject) {
                            var strVar = '';
                            if('INIT' == rowObject.flowDefinationStatus){
                                strVar = g.lang.unReleasedText;
                            }else if('Activate' == rowObject.flowDefinationStatus){
                                strVar = g.lang.activeText;
                            }
                            else if('Freeze' == rowObject.flowDefinationStatus){
                                strVar = g.lang.suspendText;
                            }
                            return strVar;
                        }
                    }, {
                        label: g.lang.priorityText,
                        name: "priority",
                        index: "priority",
                        sortable: true,
                        width:80
                    }],
                    shrinkToFit: false,//固定宽度
                    ondbClick: function () {
                        var rowData = EUI.getCmp("gridPanel").getSelectRow();
                        g.getValues(rowData.id);
                    }
                }
        };
    },
    //激活或冻结流程
    activateOrFreezeFlow:function (gridId,data,url,active) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: active ? g.lang.activateHintMessageText : g.lang.freezeHintMessageText,
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            },{
                title: g.lang.okText,
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: active ? g.lang.activateMaskMessageText : g.lang.freezeMaskMessageText
                    });
                    EUI.Store({
                        url: url,
                        params: data,
                        success: function (result) {
                            myMask.hide();
                            EUI.getCmp(gridId).grid.trigger("reloadGrid");
                            EUI.ProcessStatus(result);
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
            }]
        });
    },
    remove: function () {
        $("#defVersion>.ecmp-common-edit").die();
        $("#defVersion>.ecmp-common-view").die();
        $("#defVersion>.ecmp-common-suspend").die();
        $("#defVersion>.ecmp-common-activate").die();
    }
});