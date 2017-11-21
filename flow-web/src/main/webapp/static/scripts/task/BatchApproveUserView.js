/**
 * 批量审批列表界面
 */
EUI.BatchApproveUserView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    taskIds: null,
    userData: null,//缓存后端返回的选人数据
    afterSubmit: null,
    returnBack:null,

    initComponent: function () {
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            itemspace: 10,
            items: [{
                xtype: "ToolBar",
                height: 30,
                padding: 0,
                border: false,
                items: this.initToolBar()
            }, {
                xtype: "Container",
                height: "auto",
                padding: 0,
                border: true,
                style: {
                    "border-radius": "2px"
                },
                html: '<div class="info-left todo-info"></div>'
            }]
        });
        this.loadData();
        this.addEvents();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "Label",
            style: {
                "margin-top": "5px"
            },
            content: "我的工作 > 批量处理 > 选择下步执行人"
        }, "->", {
            xtype: "Button",
            title: "返回",
            handler: function () {
                g.boxCmp.remove();
                g.returnBack && g.returnBack.call(g);
            }
        }, {
            xtype: "Button",
            title: "确定",
            selected: true,
            handler: function () {
                g.submit();
            }
        }];
    },
    loadData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowClient/getSelectedCanBatchNodesInfo",
            params: {
                taskIds: this.taskIds
            },
            success: function (result) {
                g.userData = result.data;
                g.showData(result.data);
                myMask.hide();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    showBatchUserPage: function () {

    },
    showData: function (data) {
        var html = "";
        for (var j = 0; j < data.length; j++) {
            var itemdata = data[j];
            html += '<div class="process_box">' +
                '<div class="task_type_title">' + itemdata.name + '</div>' +
                this.getTaskHtml(itemdata.nodeGroupInfos)
            '</div>';
        }
        $(".todo-info", '#' + this.renderTo).append(html);
        EUI.resize(this.boxCmp);
    },
    getTaskHtml: function (data) {
        var html = "";
        for (var i = 0; i < data.length; i++) {
            html += '<div class="task_info">' +
                '        <div class="task_info_title">' + data[i].name + '</div>' +
                '        <div class="task_info_opinion">' +
                '            <div class="operator_title">意&nbsp;&nbsp;&nbsp;见：</div>' +
                '            <div class="info_right">同意</div>' +
                '        </div>';
            if (data[i].type != "EndEvent") {
                html += '<div class="task_info_operator">' +
                    '<div class="operator_title">执行人：</div>' +
                    '<div class="operator_info">' +
                    this.getUserHtml(data[i].executorSet)
                '</div></div></div>';
            } else {
                html += '</div>';
            }
        }
        return html;
    },
    getUserHtml: function (data) {
        var html = "";
        for (var i = 0; i < data.length; i++) {
            html += '<div class="user-item" id="' + data[i].id + '">' +
                '<div class="info_radio ecmp-eui-radio"></div>' +
                '<div>' + data[i].name + ' ' + data[i].organizationName + (data[i].positionName || '') + '</div>' +
                '</div>';
        }
        return html;
    },
    getSubmitData: function () {
        var submitData = [];
        var flowDoms = $(".process_box", "#" + this.renderTo);
        for (var k = 0; k < flowDoms.length; k++) {
            var data = this.userData[k];
            var doms = $(".task_info", $(flowDoms[k]));
            for (var i = 0; i < doms.length; i++) {
                var taskdata = data.nodeGroupInfos[i];
                var selectDoms = $(".user-item", $(doms[i]));
                if (taskdata.type != "EndEvent" && selectDoms.length == 0) {
                    EUI.ProcessStatus({success: false, msg: data.name + "--" + taskdata.name + "，未选择执行人"});
                    return;
                }
                var userIds = "";
                selectDoms.each(function () {
                    userIds += $(this).attr("id") + ",";
                });
                submitData.push({
                    taskIdList: taskdata.ids,
                    flowTaskCompleteList: [{
                        nodeId: taskdata.nodeId,
                        userIds: userIds,
                        flowTaskType: taskdata.flowTaskType,
                        userVarName: taskdata.userVarName,
                        callActivityPath: taskdata.callActivityPath
                    }]
                });
            }
        }
        return submitData;
    },
    submit: function () {
        var g = this;
        var data = this.getSubmitData();
        if (!data || data.length == 0) {
            return;
        }
        var myMask = EUI.LoadMask({
            msg: "正在提交,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowClient/completeTaskBatch",
            params: {
                flowTaskBatchCompleteWebVoStrs:JSON.stringify(data)
            },
            success: function (result) {
                myMask.hide();
                g.afterSubmit && g.afterSubmit.call(g);
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    addEvents: function () {
        $(".user-item", "#" + this.renderTo).live("click", function () {
            var dom = $(this);
            if (dom.hasClass("selected")) {
                dom.removeClass("selected");
                $(".info_radio", dom).removeClass("ecmp-eui-radioselect");
            } else {
                dom.addClass("selected");
                $(".info_radio", dom).addClass("ecmp-eui-radioselect");
            }
        });
    }
});