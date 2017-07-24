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
            items: [this.initLeft(), this.initCenterContainer()]
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
        $("#defFlow > .ecmp-common-view").live("click", function () {
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
                title: g.lang.sureText,
                iconCss:"ecmp-common-ok",
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
                iconCss:"ecmp-common-delete",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    },
    lookPropertyWindow: function (rowData) {
        var g = this;
        var Wind = EUI.Window({
            title: g.lang.flowDefinitionVersionText,
            width: 800,
            layout: "border",
            height: 500,
            padding: 8,
            itemspace: 0,
            items: [this.initWindTbar(rowData), this.initWindGrid(rowData)],
            afterClose:function () {
                g.remove();
            }
        });
        g.addDefVersionWinEvents();
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
            items: [ {
                xtype: "Button",
                //copyText: "参考创建",
                title: g.lang.copyText,
                iconCss:"ecmp-common-copy",
                selected:true,
                handler: function() {
                    var rowData =EUI.getCmp("defViesonGridPanel").getSelectRow();
                    if (rowData && rowData.id) {
                        rowData.orgCode=rowData["flowDefination.orgCode"];
                        g.copyFlowDefination(rowData,true);
                    } else {
                        // copyHintMessage:"请选择一条要参考的行项目!",
                        g.message(g.lang.copyHintMessage);
                    }

                }
            },'->', {
                xtype: "SearchBox",
                displayText: g.lang.searchByNameMsgText,
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
                        //viewFlowDefText:"查看流程定义"
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
                    name: "name",
                    index: "name"
                }, {
                    label: g.lang.definitionIDText,
                    name: "actDefId",
                    index: "actDefId",
                    hidden: true
                }, {
                    label: g.lang.definitionKEYText,
                    name: "defKey",
                    index: "defKey"
                }, {
                    label: g.lang.deployIDText,
                    name: "actDeployId",
                    index: "actDeployId",
                    hidden: true
                }, {
                    label: g.lang.versionText,
                    name: "versionCode",
                    index: "versionCode",
                    align: "right"
                }, {
                    label: g.lang.priorityText,
                    name: "priority",
                    index: "priority"
                }, {
                    label: g.lang.depictText,
                    name: "depict",
                    index: "depict"
                },{
                    label: g.lang.flowDefinitionStatusText,
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
                }]
            }
        };
    },

    viewFlowDefnation: function (data) {
        var g = this;
        var tab = {
            title: g.lang.viewFlowDefText+data.name,
            url: _ctxPath + "/design/showLook?id="+data.id+"&viewFlowDefByVersionId=true"
        };
        g.addTab(tab);
    },
    updateFlowDefnation: function (data) {
        var g = this;
        var tab = {
            title: g.lang.editFlowDefinitionText+data.name,
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data.orgCode+"&id="+ data.id+"&businessModelId="+data["flowType.businessModel.id"],
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
        var tab = {
            // copyFlowDefinitionText: "参考创建流程定义",
            title: g.lang.copyFlowDefinitionText,
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data.orgCode+"&id="+id+"&businessModelId="+businessModelId+"&isCopy="+true+"&isFromVersion="+isFromVersion,
        };
        g.addTab(tab);
    },
    updateFlowDefVersion: function (data) {
        var g = this;
        var tab = {
            // copyFlowDefinitionText: "参考创建流程定义",
            title: g.lang.copyFlowDefinitionText,
            url: _ctxPath + "/design/show?orgId=" + g.selectedNodeId +"&orgCode="+data["flowDefination.orgCode"]+"&id="+data["flowDefination.id"]+"&businessModelId="+data["flowDefination.flowType.businessModel.id"],
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
            items: [g.initTitle("组织机构"),'->', {
                xtype:"SearchBox",
                width:209,
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
            searchField:["code","name"],
            showField: "name",
            style: {
                "background": "#fff"
            },
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
                    //copyText: "参考创建",
                    title: g.lang.copyText,
                    iconCss:"ecmp-common-copy",
                    handler: function() {
                        var rowData =EUI.getCmp("gridPanel").getSelectRow();
                        if (rowData && rowData.id) {
                            g.copyFlowDefination(rowData,false);
                        } else {
                            // copyHintMessage:"请选择一条要参考的行项目!",
                            g.message(g.lang.copyHintMessage);
                        }

                    }
                }, '->', {
                    xtype: "SearchBox",
                    displayText: g.lang.searchByNameMsgText,
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
                        label: g.lang.operateText,
                        name: "operate",
                        index: "operate",
                        width: 120,
                        align: "left",
                        formatter : function(cellvalue, options, rowObject) {
                            var str='<div id="defFlow"><i class="ecmp-common-edit icon-space fontcusor" title="'+g.lang.editText+'"></i>'+
                                '<i class="ecmp-common-delete  icon-space fontcusor" title="'+g.lang.deleteText+'"></i>' +
                                '<i class="ecmp-common-view icon-space fontcusor" title="'+g.lang.flowDefinitionVersionText+'"></i>';
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
                        width:110
                    }, {
                        label: g.lang.latestVersionIDText,
                        name: "lastVersionId",
                        index: "lastVersionId",
                        hidden: true
                    }, {
                        label: g.lang.definitionKEYText,
                        name: "defKey",
                        index: "defKey",
                        width:110
                    }, {
                        label: g.lang.flowTypeText,
                        name: "flowType.name",
                        index: "flowType.name",
                        width:110
                    }, {
                        label: g.lang.businessEntityIDText,
                        name: "flowType.businessModel.id",
                        index: "flowType.businessModel.id",
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
                        width:110,
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
                        width:80
                    }],
                    shrinkToFit: false,//固定宽度
                    ondbClick: function () {
                        var rowData = EUI.getCmp("gridPanel").getSelectRow();
                        g.getValues(rowData.id);
                    }
                }
            }]
        }
    },
    //激活或冻结流程
    activateOrFreezeFlow:function (gridId,data,url,active) {
        var g = this;
        var infoBox = EUI.MessageBox({
            //hintText: 提示
            title: g.lang.hintText,
            // activateHintMessageText:"您确定要激活吗？",
            // freezeHintMessageText:"您确定要冻结吗？",
            msg: active ? g.lang.activateHintMessageText : g.lang.freezeHintMessageText,
            buttons: [{
                //okText: 确定
                title: g.lang.okText,
                iconCss: "ecmp-common-ok",
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        // activateMaskMessageText: "正在激活，请稍候...",
                        // freezeMaskMessageText: "正在冻结，请稍候...",
                        msg: active ? g.lang.activateMaskMessageText : g.lang.freezeMaskMessageText,
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
                            }
                            EUI.ProcessStatus(status);
                        }
                    });
                }
            }, {
                //cancelText:取消
                title: g.lang.cancelText,
                iconCss: "ecmp-common-delete",
                handler: function () {
                    infoBox.remove();
                }
            }]
        });
    },
    remove: function () {
        $("#defVersion>.ecmp-common-edit").die();
        $("#defVersion>.ecmp-common-view").die();
        $("#defVersion>.ecmp-common-suspend").die();
        $("#defVersion>.ecmp-common-activate").die();
    },
    initTitle: function (title) {
        return {
            xtype: "Container",
            region: "north",
            border: false,
            width:80,
            padding:0,
            height:30,
            isOverFlow: false,
            style:{
                "margin-top":"10px"
            },
            html: "<div style='font-size:17px;overflow:hidden;title:" + title + ";'>" + title + "</div>"
        }
    },
    message: function (msg) {
        var g = this;
        var message = EUI.MessageBox({
            border: true,
            //tiShiText: "提示",
            title: g.lang.tiShiText,
            showClose: true,
            msg: msg,
            buttons: [{
                //okText: "确定",
                title: g.lang.okText,
                iconCss: "ecmp-common-ok",
                selected:true,
                handler: function () {
                    message.remove();
                }
            }]
        });
    },
});