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

window.Flow = {};
EUI.ns("Flow.flow");

Flow.FlowApprove = function (options) {
    return new Flow.flow.FlowApprove(options);
};
Flow.flow.FlowApprove = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    busId: null,
    taskId: null,
    desionType: 0,//0表示单选、1多选，2不需要选择
    instanceId: null,
    iframeHeight: 400,
    pageUrl: null,
    submitUrl: null,
    goNext: null,
    iframe: null,
    toChooseUserData: null,

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
            '            <div class="flow-ordernum">业务单号：</div>' +
            '            <div style="padding: 0 0 20px 20px;">' +
            '                <div class="flow-info-creater">制单人：</div>' +
            '                <div class="flow-info-createtime"></div>' +
            '            </div>' +
            '        </div>' +
            '        <div class="flow-info-item" style="border-left:1px solid #dddddd ;">' +
            '            <div>' +
            '                <div class="flow-info-text">上一步执行人：</div>' +
            '                <div class="flow-info-excutor"></div>' +
            '            </div>' +
            '            <div style="padding-top: 6px;">' +
            '                <div class="flow-info-text">上一步审批意见：</div>' +
            '                <div class="flow-info-remark"></div>' +
            '            </div>' +
            '        </div>' +
            '    </div>';
    },
    initOperateHtml: function () {
        return '<div style="height: 200px;">' +
            '        <div class="flow-decision">' +
            '            <div class="flow-nodetitle">决策：</div>' +
            '            <div class="flow-decision-box">' +
            '        </div></div>' +
            '        <div class="flow-info-item" style="border-left:1px solid #dddddd;">' +
            '            <div class="flow-nodetitle">处理意见</div>' +
            '            <textarea class="flow-remark"></textarea>' +
            '            <span class="flow-btn flow-next">下一步</span>' +
            '        </div>' +
            '    </div>';
    },
    initFrameHtml: function () {
        var html = '<div>' +
            '        <div class="flow-order-titlebox">' +
            '            <div style="display: inline-block;">' +
            '                表单明细' +
            '            </div>' +
            '            <div class="close">收起</div>' +
            '        </div>';
        html += '<iframe class="flow-iframe" src="' + this.pageUrl + '" style="height:' + this.iframeHeight + 'px"></iframe>';
        return html += "</div>";
    },
    initChooseUserHtml: function () {
        return '<div class="flow-chooseuser">' +
            '    <div class="chooseuser-title">选择下一步执行人</div>' +
            '<div class="flow-operate">' +
            '        <div class="flow-btn pre-step">' +
            '            上一步' +
            '        </div>' +
            '        <div class="flow-btn submit">' +
            '            提交' +
            '        </div>' +
            '    </div>';
    },
    addEvents: function () {
        var g = this;
        $(".flow-next").bind("click", function () {
            if ($(this).text() == "完成") {
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
            $(".expand").text("收起").addClass("close");
        }, function () {
            $(".flow-iframe").hide();
            $(".expand").text("展开").addClass("expand");
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
                $(".flow-next").text("完成");
            } else {
                $(".flow-next").text("下一步");
            }
        });
        //执行人选择
        $(".flow-user-item").live("click", function () {
            var type = $(this).attr("type");
            if (type == "common") {
                if ($(this).hasClass("select")) {
                    return;
                }
                $(this).addClass("select");
                $(this).siblings().removeClass("select");
            } else {
                if ($(this).hasClass("select")) {
                    $(this).removeClass("select");
                } else {
                    $(this).addClass("select");
                }
            }
        });

        $(".submit").bind("click", function () {
            g.submit();
        });
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
        $(".flow-ordernum").text("业务单号：" + data.businessCode);
        $(".flow-info-creater").text("制单人：" + data.createUser);
        $(".flow-info-excutor").text(data.prUser);
        $(".flow-info-remark").text(data.prOpinion);

    },
    getNodeInfo: function () {
        var g = this;
        var mask = EUI.LoadMask({
            msg: "正在加载数据，请稍候..."
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
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            var iconCss = "choose-radio";
            if (item.uiType == "checkbox") {
                iconCss = "choose-checkbox";
                this.desionType = 1;
            } else if (item.uiType == "readOnly") {
                iconCss = "";
                this.desionType = 2;
            }
            if (!item.gateWayName) {
                html += '<div class="flow-decision-item" id="' + item.id + '" type="' + item.type.toLowerCase() + '">' +
                    '<div class="choose-icon ' + iconCss + '"></div>' +
                    '<div class="excutor-item-title"><div class="approve-arrows-right"></div><div>' + item.name + '</div></div></div>';
            } else {
                html += '<div class="flow-decision-item" id="' + item.id + '" type="' + item.type.toLowerCase() + '">' +
                    '<div class="choose-icon ' + iconCss + '"></div>' +
                    '<div class="excutor-item-title"><div class="gateway-name">' + item.gateWayName + '</div><div class="approve-arrows-right"></div><div>' + item.name + '</div></div></div>';
            }
        }
        if (data.length == 1 && data[0].type.toLowerCase() == "endevent") {
            $(".flow-next").text("完成");
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
                    msg: "请选择下一步执行节点"
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
                    msg: "请选择下一步执行节点"
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
            msg: "正在保存，请稍候..."
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
                    if ($(".flow-next").text() == "完成") {
                        g.close();
                        return;
                    }
                    if (status.data == "EndEvent") {
                        var msgbox = EUI.MessageBox({
                            title: "操作提示",
                            msg: "当前操作流程将会结束，是否继续？",
                            buttons: [{
                                title: "确定",
                                handler: function () {
                                    g.submit(true);
                                    msgbox.remove();
                                }
                            }, {
                                title: "取消", handler: function () {
                                    msgbox.remove();
                                }
                            }]
                        })
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
       var data = this.toChooseUserData;
        $(".flow-approve").hide();
        $(".flow-chooseuser").show();
        $(".flow-node-box").remove();
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var node = data[i];
            var nodeType = "普通任务";
            var iconCss = "choose-radio";
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
            for (var j = 0; j < node.executorSet.length; j++) {
                var item = node.executorSet[j];
                if(!item.positionId){
                    nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
                        '<div class="choose-icon ' + iconCss + '"></div>' +
                        '<div class="excutor-item-title">姓名：' + item.name +
                        '，组织机构：' + item.organizationName + '，编号：' + item.code + '</div>' +
                        '</div>';
                }else{
                    nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
                        '<div class="choose-icon ' + iconCss + '"></div>' +
                        '<div class="excutor-item-title">姓名：' + item.name + '，岗位：' + item.positionName +
                        '，组织机构：' + item.organizationName + '，编号：' + item.code + '</div>' +
                        '</div>';
                }
            }
            nodeHtml += "</div></div>";
            html += nodeHtml;
        }
        $(".chooseuser-title").after(html);
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
                    msg: "请选择[" + data.name + "]的执行人"
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
            msg: "正在保存，请稍候..."
        });
        EUI.Store({
            url: this.submitUrl,
            params: {
                taskId: this.taskId,
                businessId: this.busId,
                opinion: $(".flow-remark").val(),
                endEventId: endEventId,
                taskList: isEnd ? "" : JSON.stringify(this.getSelectedUser())
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
    }
});