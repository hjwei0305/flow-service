/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/24 13:55      陈飞(fly)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
if (!window.Flow) {
    window.Flow = {};
    EUI.ns("Flow.flow");
}

Flow.FlowApprove = function (options) {
    return new Flow.flow.FlowApprove(options);
};
Flow.flow.FlowApprove = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    busId: null,
    taskId: null,
    desionType: 0,//0表示单选、1多选，2不需要选择
    instanceId: null,
    iframeHeight: 600,
    pageUrl: null,
    submitUrl: null,
    manualSelected: false,//是否是人工选择的网关类型
    goNext: null,
    iframe: null,
    toChooseUserData: null,
    chooseUserNode: null,
    initComponent: function () {
        this.pageUrl += "?id=" + this.busId;
        EUI.Container({
            renderTo: this.renderTo,
            html: this.initHtml()
        });
        this.getHeadData();
        this.getNodeInfo();
        this.addEvents();
        this.iframe = $(".flow-iframe")[0].contentWindow;
    },
    initHtml: function () {
        var firstHtml = '<div class="flow-approve">' + this.initTopHtml() + this.initOperateHtml() + this.initFrameHtml() + '</div>';
        return firstHtml + this.initChooseUserHtml();
    },
    initTopHtml: function () {
        return '<div class="flow-info">' +
            '        <div class="flow-info-item">' +
            '            <div class="flow-ordernum">' + this.lang.businessUnitText + '</div>' +
            '            <div style="padding: 0 0 20px 20px;">' +
            '                <div class="flow-info-creater">' + this.lang.docMarkerText + '</div>' +
            '                <div class="flow-info-createtime"></div>' +
            '            </div>' +
            '        </div>' +
            '        <div class="flow-info-item" style="border-left:1px solid #dddddd ;">' +
            '            <div>' +
            '                <div class="flow-info-text">' + this.lang.preExecutorText + '</div>' +
            '                <div class="flow-info-excutor"></div>' +
            '            </div>' +
            '            <div style="padding-top: 6px;">' +
            '                <div class="flow-info-text">' + this.lang.preApprovalOpinionsText + '</div>' +
            '                <div class="flow-info-remark"></div>' +
            '            </div>' +
            '        </div>' +
            '    </div>';
    },
    initOperateHtml: function () {
        return '<div style="height: 200px;">' +
            '        <div class="flow-decision">' +
            '            <div class="flow-nodetitle">' + this.lang.decisionText + '</div>' +
            '            <div class="flow-decision-box">' +
            '        </div></div>' +
            '        <div class="flow-info-item" style="border-left:1px solid #dddddd;">' +
            '            <div class="flow-nodetitle flow-deal-opinion">' + this.lang.handlingSuggestionText + '</div>' +
            '            <div id="flow-deal-checkbox"></div>' +
            '            <textarea class="flow-remark"></textarea>' +
            '            <span class="flow-btn flow-next">' + this.lang.nextStepText + '</span>' +
            '        </div>' +
            '    </div>';
    },
    //处理意见中的选项框
    initDealCheckBox: function () {
        var g = this;
        EUI.RadioBoxGroup({
            renderTo: "flow-deal-checkbox",
            onlyOneChecked: true,
            items: [{
                title: "同意",
                name: "agree",
                value: true
            }, {
                title: "不同意",
                name: "disagree"
            }]
        })
    },
    initFrameHtml: function () {
        var html = '<div>' +
            '        <div class="flow-order-titlebox">' +
            '            <div style="display: inline-block;">' +
            this.lang.formDetailText +
            '            </div>' +
            '            <div class="close">' + this.lang.collectText + '</div>' +
            '        </div>';
        html += '<iframe class="flow-iframe" src="' + this.pageUrl + '" style="height:' + this.iframeHeight + 'px"></iframe>';
        return html += "</div>";
    },
    initChooseUserHtml: function () {
        return '<div class="flow-chooseuser">' +
            '    <div class="chooseuser-title">' + this.lang.chooseNextExecutorText + '</div>' +
            '<div class="flow-operate">' +
            '        <div class="flow-btn pre-step">' +
            this.lang.previousStepText +
            '        </div>' +
            '        <div class="flow-btn submit">' +
            this.lang.submitText +
            '        </div>' +
            '    </div>';
    },
    addEvents: function () {
        var g = this;
        $(".flow-next").bind("click", function () {
            if ($(this).text() == g.lang.finishText) {
                var endEventId = $(".select", ".flow-decision-box").attr("id");
                g.submit(true, endEventId);
            } else {
                g.goToNext();
            }
        });
        $(".pre-step").bind("click", function () {
            $(".flow-approve").show();
            $(".flow-chooseuser").hide();
        });

        $(".flow-order-titlebox").toggle(function () {
            $(".flow-iframe").show();
            $(".expand").text(g.lang.collectText).addClass("close");
        }, function () {
            $(".flow-iframe").hide();
            $(".expand").text(g.lang.spreadText).addClass("expand");
        });
        //决策选择
        $(".flow-decision-item").live("click", function () {
            if (g.desionType == 2) {
                return;
            }
            $(".flow-decision-item").removeClass("select");
            $(this).addClass("select");
            var type = $(this).attr("type");
            if (type.toLowerCase() == "endevent") {
                $(".flow-next").text(g.lang.finishText);
            } else {
                $(".flow-next").text(g.lang.nextStepText);
            }
        });
        //执行人选择
        $(".flow-user-item").live("click", function () {
            var type = $(this).attr("type");
            if (type == "common") {
                // if ($(this).hasClass("select")) {
                //     return;
                // }
                // $(this).addClass("select");
                // $(this).siblings().removeClass("select");
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
            } else {
                if ($(this).hasClass("select")) {
                    $(this).removeClass("select");
                } else {
                    $(this).addClass("select");
                }
            }
        });

        //选择任意执行人
        $(".choose-btn").die().live("click", function () {
            g.showChooseExecutorWind();
            return false;
        });

        //删除选择的执行人
        $(".choose-delete").die().live("click", function () {
            $(this).parent().remove();
        });

        $(".submit").bind("click", function () {
            g.submit();
        });
        g.showOmitContent();
    },
    getHeadData: function () {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/builtInApprove/getApprovalHeaderInfo",
            params: {
                taskId: this.taskId
            },
            success: function (status) {
                if (status.success) {
                    g.showHeadData(status.data);
                } else {
                    EUI.ProcessStatus(status);
                }
            },
            failure: function (response) {
                EUI.ProcessStatus(response);
            }
        });
    },
    showHeadData: function (data) {
        $(".flow-ordernum").text(this.lang.businessUnitText + data.businessCode);
        $(".flow-info-creater").text(this.lang.docMarkerText + data.createUser);
        $(".flow-info-excutor").text(data.prUser);
        $(".flow-info-remark").text(data.prOpinion);

    },
    getNodeInfo: function () {
        var g = this;
        var mask = EUI.LoadMask({
            msg: this.lang.queryMaskMessageText
        });
        EUI.Store({
            url: _ctxPath + "/builtInApprove/nextNodesInfo",
            params: {
                taskId: this.taskId
            },
            success: function (status) {
                mask.hide();
                if (status.success) {
                    g.showNodeInfo(status.data);
                } else {
                    EUI.ProcessStatus(status);
                }
            },
            failure: function (response) {
                mask.hide();
                EUI.ProcessStatus(response);
            }
        });
    },
    showNodeInfo: function (data) {
        var g=this;
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            var iconCss = "choose-radio";
            if (item.flowTaskType == "countersign") {
                html += '<div class="flow-decision-item" id="' + item.id + '" type="' + item.type.toLowerCase() + '">' +
                    '<div class="excutor-item-title"><div>' + item.name + '</div></div></div>';
                g.initDealCheckBox();
            }
            if (item.uiType == "checkbox") {
                iconCss = "choose-checkbox";
                this.manualSelected = true;
                this.desionType = 1;
            } else if (item.uiType == "readOnly") {
                iconCss = "";
                this.manualSelected = false;
                this.desionType = 2;
            } else {
                this.manualSelected = true;
            }
            var lineNameHtml = "";
            if (item.preLineName != "null") {
                lineNameHtml = '<div class="gateway-name">' + item.preLineName + '</div>';
            }
            html += '<div class="flow-decision-item" id="' + item.id + '" type="' + item.type.toLowerCase() + '">' +
                '<div class="choose-icon ' + iconCss + '"></div>' +
                '<div class="excutor-item-title">' + lineNameHtml + '<div class="approve-arrows-right"></div><div>' + item.name + '</div></div></div>';
        }
        if (data.length == 1 && data[0].type.toLowerCase() == "endevent") {
            $(".flow-next").text(this.lang.finishText);
        }
        $(".flow-decision-box").append(html);
    },
    getDesionIds: function () {
        var includeNodeIds = "";
        var doms;
        if (this.desionType != 2) {
            doms = $(".select", ".flow-decision-box");
            for (var i = 0; i < doms.length; i++) {
                includeNodeIds += $(doms[i]).attr("id");
            }
        }
        return includeNodeIds;
    },
    //检查审批输入是否有效
    checkIsValid: function () {
        if (this.desionType == 2) {
            return true;
        }
        if (this.desionType == 1) {
            var doms = $(".select", ".flow-decision-box");
            if (doms.length != 1) {
                EUI.ProcessStatus({
                    success: false,
                    msg: this.lang.chooseNextExecuteNodeText
                });
                return false;
            }
            return true;
        }
        if (this.desionType == 0) {
            var doms = $(".select", ".flow-decision-box");
            if (doms.length == 0) {
                EUI.ProcessStatus({
                    success: false,
                    msg: this.lang.chooseNextExecuteNodeText
                });
                return false;
            }
            return true;
        }
        return false;
    },
    goToNext: function () {
        if (!this.checkIsValid()) {
            return;
        }
        //执行子窗口方法
        if (this.goNext && !this.goNext.call(this)) {
            return;
        }
        var g = this;
        var mask = EUI.LoadMask({
            msg: this.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/builtInApprove/getSelectedNodesInfo",
            params: {
                taskId: this.taskId,
                businessId: this.busId,
                includeNodeIdsStr: this.getDesionIds()
            },
            success: function (status) {
                mask.hide();
                if (status.success) {
                    if ($(".flow-next").text() == g.lang.finishText) {
                        g.close();
                        return;
                    }
                    if (status.data == "EndEvent") {
                        var msgbox = EUI.MessageBox({
                            title: g.lang.operationHintText,
                            msg: g.lang.stopFlowMsgText,
                            buttons: [{
                                title: g.lang.sureText,
                                iconCss: "ecmp-common-ok",
                                handler: function () {
                                    g.submit(true);
                                    msgbox.remove();
                                }
                            }, {
                                title: g.lang.cancelText,
                                iconCss: "ecmp-common-delete",
                                handler: function () {
                                    msgbox.remove();
                                }
                            }]
                        });
                        return;
                    }
                    g.toChooseUserData = status.data;
                    g.showChooseUser();
                } else {
                    EUI.ProcessStatus(status);
                }
            },
            failure: function (response) {
                mask.hide();
                EUI.ProcessStatus(response);
            }
        });
    },
    showChooseUser: function () {
        var g = this;
        var data = this.toChooseUserData;
        console.log(data)
        $(".flow-approve").hide();
        $(".flow-chooseuser").show();
        $(".flow-node-box").remove();
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var node = data[i];
            var nodeType = this.lang.generalTaskText;
            var iconCss = "choose-radio";
            if (node.uiUserType == "AnyOne") {
                var html = g.showAnyContainer(data);
                $(".chooseuser-title").after(html);
                return;
            }
            if (node.flowTaskType == "singleSign") {
                nodeType = this.lang.singleSignTaskText;
                iconCss = "choose-checkbox";
            } else if (node.flowTaskType == "countersign") {
                nodeType = this.lang.counterSignTaskText;
                iconCss = "choose-checkbox";
            } else if (node.flowTaskType == "approve") {
                nodeType = this.lang.approveTaskText;
            }
            var nodeHtml = '<div class="flow-node-box" index="' + i + '">' +
                '<div class="flow-excutor-title">' + node.name + '-[' + nodeType +
                ']</div><div class="flow-excutor-content">';
            if (iconCss == "choose-radio") {
                if (node.executorSet.length == 1) {
                    var html = g.showUserItem(node, nodeHtml, iconCss, nodeType);
                    $(".chooseuser-title").after(html);
                    $(".flow-user-item").addClass("select");
                } else {
                    var html = g.showUserItem(node, nodeHtml, iconCss, nodeType);
                    $(".chooseuser-title").after(html);
                    $(".flow-excutor-content").children("div").eq(0).addClass("select");
                }
            }
            if (iconCss == "choose-checkbox") {
                var html = g.showUserItem(node, nodeHtml, iconCss, nodeType);
                $(".chooseuser-title").after(html);
                $(".flow-user-item").addClass("select");
            }

            // for (var j = 0; j < node.executorSet.length; j++) {
            //     var item = node.executorSet[j];
            //     if(!item.positionId){
            //         nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
            //             '<div class="choose-icon ' + iconCss + '"></div>' +
            //             '<div class="excutor-item-title">姓名：' + item.name +
            //             '，组织机构：' + item.organizationName + this.lang.number2Text + item.code + '</div>' +
            //             '</div>';
            //     }else{
            //         nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
            //             '<div class="choose-icon ' + iconCss + '"></div>' +
            //             '<div class="excutor-item-title">姓名：' + item.name + '，岗位：' + item.positionName +
            //             '，组织机构：' + item.organizationName + this.lang.number2Text + item.code + '</div>' +
            //             '</div>';
            //     }
            // }
            // nodeHtml += "</div></div>";
            // html += nodeHtml;
        }
        //  $(".chooseuser-title").after(html);
    },
    showAnyContainer: function (data) {
        var g = this;
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var node = data[i];
            g.chooseUserNode = node;
            var nodeType = g.lang.arbitraryExecutorText;
            var iconCss = "choose-delete";
            var nodeHtml = '<div class="flow-node-box" index="' + i + '">' +
                '<div class="flow-excutor-title">' + node.name + '-[' + nodeType +
                ']</div><div class="flow-excutor-content2">';
        }
        nodeHtml += "</div>" +
            '<div class="choose-btn">' + g.lang.chooseText + '</div>' +
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
                    '<div class="excutor-item-title">' + this.lang.nameText + item.name +
                    this.lang.organizationText + item.organizationName + this.lang.number2Text + item.code + '</div>' +
                    '</div>';
            } else {
                nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
                    '<div class="choose-icon ' + iconCss + '"></div>' +
                    '<div class="excutor-item-title">' + this.lang.nameText + item.name + this.lang.jobText + item.positionName +
                    this.lang.organizationText + item.organizationName + this.lang.number2Text + item.code + '</div>' +
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
            var data = this.toChooseUserData[index];
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
        for (var i = 0; i < nodeDoms.length; i++) {
            var nodeDom = $(nodeDoms[i]);
            var index = nodeDom.attr("index");
            var data = this.toChooseUserData[index];
            var itemDoms = $(".select", nodeDom);
            if (itemDoms.length == 0) {
                EUI.ProcessStatus({
                    success: false,
                    msg: this.lang.chooseMsgText + data.name + this.lang.executorMsgText
                });
                return false;
            }
        }
        return true;
    },
    submit: function (isEnd, endEventId) {
        var g = this;
        if (!isEnd && !this.checkUserValid()) {
            return;
        }
        var mask = EUI.LoadMask({
            msg: this.lang.nowSaveMsgText
        });
        EUI.Store({
            url: this.submitUrl,
            params: {
                taskId: this.taskId,
                businessId: this.busId,
                opinion: $(".flow-remark").val(),
                endEventId: endEventId,
                taskList: isEnd ? "" : JSON.stringify(this.getSelectedUser()),
                manualSelected: g.manualSelected//是否是人工网关选择
            },
            success: function (status) {
                mask.hide();
                if (status.success) {
                    g.close();
                } else {
                    EUI.ProcessStatus(status);
                }
            },
            failure: function (response) {
                mask.hide();
                EUI.ProcessStatus(response);
            }
        });
    },
    close: function () {
        if (parent.homeView) {
            parent.homeView.closeNowTab();
        } else {
            window.close();
        }
    },
    showOmitContent: function () {
        $(".gateway-name").live("mouseover", function () {
            var text = $(this).text();
            $(this).attr("title", text);
        })
    },
    showChooseExecutorWind: function () {
        var g = this;
        var isChooseOneTitle;
        var saveBtnIsHidden;
        if (g.chooseUserNode.flowTaskType == "common") {
            isChooseOneTitle = g.lang.chooseArbitraryExecutorMsgText;
            saveBtnIsHidden = true;
        } else {
            isChooseOneTitle = g.lang.chooseArbitraryExecutorText;
            saveBtnIsHidden = false;
        }
        g.chooseAnyOneWind = EUI.Window({
            title: isChooseOneTitle,
            width: 720,
            layout: "border",
            height: 500,
            padding: 8,
            itemspace: 0,
            items: [this.initChooseUserWindLeft(), this.InitChooseUserGrid()],
            buttons: [{
                title: g.lang.saveText,
                iconCss: "ecmp-common-save",
                selected: true,
                hidden: saveBtnIsHidden,
                handler: function () {
                    var selectRow = EUI.getCmp("chooseUserGridPanel").getSelectRow();
                    if (typeof(selectRow) == "undefined") {
                        return;
                    }
                    g.addChooseUsersInContainer(selectRow);
                    g.chooseAnyOneWind.close();
                }
            }, {
                title: g.lang.cancelText,
                iconCss: "ecmp-common-delete",
                handler: function () {
                    g.chooseAnyOneWind.remove();
                }
            }]
        });
    },
    initChooseUserWindLeft: function () {
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
                width: 140,
                displayText: g.lang.searchByNameMsgText,
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
            id: "chooseAnyUserTree",
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
                    loadonce: true,
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
                    itemCmp.find(".ux-tree-title").text(itemCmp.find(".ux-tree-title").text() + g.lang.freezeText);
                }
            },
            afterShowTree: function (data) {
                this.setSelect(data[0].id);
            }
        }
    },
    InitChooseUserGrid: function () {
        var g = this;
        var isShowMultiselect;
        if (g.chooseUserNode.flowTaskType == "common") {
            isShowMultiselect = false;
        } else {
            isShowMultiselect = true;
        }
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
                items: ['->', {
                    xtype: "SearchBox",
                    displayText: g.lang.seachByIdOrNameText,
                    onSearch: function (value) {
                        EUI.getCmp("chooseUserGridPanel").localSearch(value);
                    },
                    afterClear: function () {
                        EUI.getCmp("chooseUserGridPanel").restore();
                    }
                }]
            }, {
                xtype: "GridPanel",
                region: "center",
                id: "chooseUserGridPanel",
                searchConfig: {
                    searchCols: ["code"]
                },
                style: {"border-radius": "3px"},
                gridCfg: {
                    loadonce: true,
                    //   url: _ctxPath + "/customExecutor/listAllUser",
                    // postData:{
                    //     organizationId: g.selectedOrgId
                    // },
                    multiselect: isShowMultiselect,
                    colModel: [{
                        label: g.lang.userIDText,
                        name: "id",
                        index: "id",
                        hidden: true
                    }, {
                        label: g.lang.userNameText,
                        name: "user.userName",
                        index: "user.userName",
                        width: 150,
                        align: "center"
                    }, {
                        label: g.lang.userNumberText,
                        name: "code",
                        index: "code",
                        width: 200
                    }, {
                        label: g.lang.organization2Text,
                        name: "organization.name",
                        index: "organization.name",
                        width: 150,
                        align: "center",
                        hidden: true
                    }],
                    ondblClickRow: function () {
                        var html = "";
                        var rowData = EUI.getCmp("chooseUserGridPanel").getSelectRow();
                        html += '<div class="flow-anyOneUser-item select" type="' + g.chooseUserNode.flowTaskType + '" id="' + rowData.id + '">' +
                            '<div class="choose-icon choose-delete"></div>' +
                            '<div class="excutor-item-title">' + g.lang.nameText + rowData["user.userName"] +
                            g.lang.organizationText + rowData["organization.name"] + g.lang.number2Text + rowData.code + '</div>' +
                            '</div>';
                        $(".flow-excutor-content2").html(html);
                        g.chooseAnyOneWind.close();
                    }
                }
            }]
        }
    },
    addChooseUsersInContainer: function (selectRow) {
        var g = this;
        var html = "";
        var selectedUser = [];
        $(".flow-excutor-content2 > div").each(function (index, domEle) {
            selectedUser.push(domEle.id)
        });
        for (var j = 0; j < selectRow.length; j++) {
            var item = selectRow[j];
            if (!g.itemIdIsInArray(item.id, selectedUser)) {
                html += '<div class="flow-anyOneUser-item select" type="' + g.chooseUserNode.flowTaskType + '" id="' + item.id + '">' +
                    '<div class="choose-icon choose-delete"></div>' +
                    '<div class="excutor-item-title">' + g.lang.nameText + item["user.userName"] +
                    g.lang.organizationText + item["organization.name"] + g.lang.number2Text + item.code + '</div>' +
                    '</div>';
            }
        }
        $(".flow-excutor-content2").append(html);
    },
    itemIdIsInArray: function (id, array) {
        for (var i = 0; i < array.length; i++) {
            if (id == array[i]) {
                return true;
            }
        }
        return false;
    }
});