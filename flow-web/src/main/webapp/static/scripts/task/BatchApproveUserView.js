/**
 * 批量审批列表界面
 */
EUI.BatchApproveUserView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    taskIds: null,
    userData: null,//缓存后端返回的选人数据
    afterSubmit: null,
    returnBack: null,

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
                g.remove();
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
    showData: function (data) {
        var html = "";
        for (var j = 0; j < data.length; j++) {
            var itemdata = data[j];
            html += '<div class="process_box">' +
                '<div class="task_type_title">' + itemdata.name + '</div>' +
                this.getTaskHtml(itemdata.nodeGroupInfos)
                + '</div>';
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
                    this.getUserHtml(data[i].executorSet, data[i].uiType) +
                    '</div></div>';
            }
            html += '</div>';
        }
        return html;
    },
    getUserHtml: function (data, uiType) {
        var html = "";
        var itemCss = "user-item";
        var iconCss = "choose-radio";
        if (uiType === "checkbox") {
            iconCss = "choose-checkbox";
            itemCss += " select";
        }
        for (var i = 0; i < data.length; i++) {
            var userCss = itemCss;
            if (uiType != "checkbox" && i == 0) {
                userCss = itemCss + " select";
            }
            html += '<div class="' + userCss + '" id="' + data[i].id + '" uitype="' + uiType + '">' +
                '<div class="choose-icon ' + iconCss + '"></div>' +
                '<div>' + data[i].name + ' ' + data[i].organizationName + ' ' + (data[i].positionName || '') + '</div>' +
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
                var selectDoms = $(".user-item.select", $(doms[i]));
                if (taskdata.type != "EndEvent" && selectDoms.length == 0) {
                    EUI.ProcessStatus({success: false, msg: data.name + "--" + taskdata.name + "，未选择执行人"});
                    return;
                }
                var userIds = [];
                selectDoms.each(function () {
                    userIds.push($(this).attr("id"));
                });

                var  taskIdListCurrent = taskdata.ids;
                var  flowTaskCompleteListCurrent = {
                    nodeId: taskdata.nodeId,
                    userIds: userIds.join(","),
                    flowTaskType: taskdata.flowTaskType,
                    userVarName: taskdata.userVarName,
                    callActivityPath: taskdata.callActivityPath
                };
                if(submitData.length>0){
                    var mark = true;
                    for(var index in submitData){
                        var taskIdList  = submitData[index].taskIdList;
                        if(taskIdList.sort().toString()==taskIdListCurrent.sort().toString()){//a.sort().toString() == b.sort().toString()
                            submitData[index].flowTaskCompleteList.push(flowTaskCompleteListCurrent);
                            mark=false;
                            break;
                        }
                    }
                    if(mark){
                        submitData.push({
                            taskIdList: taskIdListCurrent,
                            flowTaskCompleteList: [flowTaskCompleteListCurrent]
                        });
                    }
                }else {
                    submitData.push({
                        taskIdList: taskIdListCurrent,
                        flowTaskCompleteList: [flowTaskCompleteListCurrent]
                    });
                }

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
                flowTaskBatchCompleteWebVoStrs: JSON.stringify(data)
            },
            success: function (result) {
                myMask.hide();
                g.remove();
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
            var uiType = $(this).attr("uitype");
            if (uiType === "checkbox") {
                if (dom.hasClass("select")) {
                    dom.removeClass("select");
                } else {
                    dom.addClass("select");
                }
            } else {
                dom.addClass("select").siblings().removeClass("select");
            }
        });
    },
    remove: function () {
        this.boxCmp.remove();
    }
});