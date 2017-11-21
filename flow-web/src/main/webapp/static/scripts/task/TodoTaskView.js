// 待办部分
EUI.TodoTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    params: {
        page: 1,
        rows: 10,
        modelId: null,
        S_createdDate: "DESC",
        Quick_value: null
    },
    initComponent: function () {
        var g = this;
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            title: "我的工作 > 待办工作",
            items: [{
                xtype: "ToolBar",
                height: 33,
                region: "north",
                padding: 0,
                border: false,
                afterRender: function () {
                    EUI.container.Container.superclass.afterRender.call(this);
                    this.content.css("paddingTop", 12);
                },
                items: this.initToolBar()
            }, {
                xtype: "Container",
                region: "center",
                padding: 0,
                html: '<div class="info-left todo-info"></div><div class="load-more"><span>获取更多</span></div>'
                + '<div class="empty-data"><div class="not-data-msg">------------您当前没有待办事项------------</div></div>'
            }]
        });
        this.dataDom = $(".todo-info", "#" + this.renderTo);
        this.emptyDom = $(".empty-data", "#" + this.renderTo);
        this.loadMoreDom = $(".load-more", "#" + this.renderTo);
        this.getTodoData();
        this.addEvents();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "Label",
            content: "<span>全部</span><span class='todo-count'>25</span>",
            customCss: "module-all",
            onClick: function () {
                if (!g.params.modelId) {
                    return;
                }
                g.params.modelId = null;
                g.refresh();
            }
        }, {
            xtype: "ComboBox",
            name: "businessModelName",
            field: ["businessModeId"],
            displayText: "筛选",
            async: false,
            store: {
                url: _ctxPath + "/flowTask/listFlowTaskHeader"
            },
            afterSelect: function (data) {
                g.params.modelId = data.data.businessModeId;
                g.refresh();
            },
            afterClear:function () {
                g.params.page = 1;
                g.params.modelId = null;
                g.getTodoData();
            }
        }, {
            xtype: "SearchBox",
            name: "Quick_value",
            onSearch: function (value) {
                g.params.page = 1;
                g.params.Quick_value = value;
                g.getTodoData();
            }
        }, {
            xtype: "Button",
            title: "批量审批",
            handler: function () {
                g.hide();
                new EUI.BatchApproveListView({
                    returnBack: function () {
                        g.show();
                    }
                });
            }
        }, "->", {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-eui-leaf' style='vertical-align: middle;color:#3671cf;'></i><span>已办工作</span>",
            onClick: function () {
                $("body").trigger("completetask");
            }
        }, {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-sys-syslog' style='vertical-align: middle;color:#3671cf;'></i><span>我的单据</span>",
            onClick: function () {
                $("body").trigger("myorder");
            }
        }];
    },

    //待办内容部分的数据调用
    getTodoData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTaskWithAllCount",
            params: this.params,
            success: function (result) {
                if (result.allTotal > 0) {
                    $(".todo-count").text(result.allTotal).show();
                } else {
                    $(".todo-count").text(0).hide();
                }
                if (result.records == 0) {
                    g.params.page = 1;
                    g.showEmptyWorkInfo();
                } else if (result.rows.length > 0) {
                    g.params.page = result.page;
                    g.showTodoData(result.rows);
                } else {
                    if (g.params.page > 0) {
                        g.params.page--;
                    }
                    EUI.ProcessStatus({
                        success: true,
                        msg: "没有更多数据"
                    });
                }
                myMask.hide();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (g.params.page > 0) {
                    g.params.page--;
                }
            }
        });
    },
    //待办里面内容部分的循环
    showTodoData: function (items) {
        var g = this;
        this.showContent();
        for (var j = 0; j < items.length; j++) {
            var rejectHtml = items[j].canReject ? '<div class="todo-btn reject-btn"><i class="ecmp-common-return reject-icon" title="驳回"></i><span>驳回</span></div>' : '';
            var nodeType = JSON.parse(items[j].taskJsonDef).nodeType;
            var claimTaskHtml = nodeType == "SingleSign" && !items[j].actClaimTime ? '<div class="todo-btn claim-btn"><i class="ecmp-common-claim claim-icon" title="签收"></i><span>签收</span></div>' : '';
            var flowInstanceCreatorId = items[j].flowInstance ? items[j].flowInstance.creatorId : "";
            var endFlowHtml = items[j].canSuspension && flowInstanceCreatorId == items[j].executorId ? '<div class="todo-btn endFlow-btn"><i class="ecmp-flow-end endFlow-icon icon-size" title="终止"></i><span>终止</span></div>' : '';
            var itemdom = $('<div class="info-item">' +
                '                 <div class="item">' +
                '                     <span class="flow-text">' + items[j].flowName + '_' + items[j].taskName + '</span>' +
                '                 </div>' +
                '                 <div class="item flow-digest">' +
                '                     <span class="digest">' + items[j].flowInstance.businessCode + '-' + items[j].flowInstance.businessModelRemark + '</span></span>' +
                '                 </div>' +
                '                 <div class="item operation">' +
                '                     <div class="end">'
                + claimTaskHtml +
                '                          <div class="todo-btn approve-btn"><i class="ecmp-eui-edit end-icon handle-icon-size" title="审批"></i><span>处理</span></div>'
                + rejectHtml + endFlowHtml +
                '                          <div class="todo-btn look-approve-btn"><i class="ecmp-common-view look-icon look-approve" title="查看表单"></i><span>查看表单</span></div>' +
                '                          <div class="todo-btn flowInstance-btn"><i class="ecmp-flow-history time-icon flowInstance icon-size" title="流程历史"></i><span>流程历史</span></div>' +
                '                     </div>' +
                '                     <span class="item-right task-item-right">' +
                '                          <div class="userName">发起人：' + items[j].creatorName + '</div>' +
                '                          <div class="todo-date"><i class="ecmp-flow-history flow-time-icon time-icon-size" title="创建时间"></i><span>' + this.countDate(items[j].createdDate) + '</span></div>' +
                '                     </span>' +
                '                 </div>' +
                '</div>');
            itemdom.data(items[j]);
            this.dataDom.append(itemdom);
        }
        EUI.resize(this.boxCmp);
    }
    ,
    //计算时间几天前
    countDate: function (startTime) {
        if (!startTime) {
            return;
        }
        var g = this;
        var date = new Date();
        var endTime = date.getTime();
        startTime = startTime.replace(new RegExp("-", "gm"), "/");
        startTime = new Date(startTime).getTime();
        var time = endTime - startTime;
        if (time <= 60000) {//如果结束时间小于开始时间
            return "刚刚";
        } else {
            //计算出相差天数
            var days = Math.floor(time / (24 * 3600 * 1000));
            if (days > 0) {
                return days + '天前';
            }
            //计算出小时数
            var leave1 = time % (24 * 3600 * 1000);   //计算天数后剩余的毫秒数
            if (leave1 == 0) {//如果leave1=0就不需要在做计算，直接把0赋给hours
                return hours = 0;
            } else {
                var hours = Math.floor(leave1 / (3600 * 1000));
                if (hours > 0) {
                    return hours + '小时前';
                }
            }
            //计算相差分钟数
            var leave2 = leave1 % (3600 * 1000);        //计算小时数后剩余的毫秒数
            var minutes = Math.floor(leave2 / (60 * 1000));
            return minutes + '分钟前';
        }
    }
    ,
    show: function () {
        this.boxCmp.show();
    }
    ,
    hide: function () {
        this.boxCmp.hide();
    }
    ,
    showContent: function () {
        this.dataDom.show();
        this.loadMoreDom.show();
        this.emptyDom.hide();
        if(this.params.page == 1){
            this.dataDom.empty();
        }
    },
    //当页面没有数据时的显示内容
    showEmptyWorkInfo: function () {
        this.emptyDom.show();
        this.dataDom.hide();
        this.loadMoreDom.hide();
        this.dataDom.empty();
        EUI.resize(this.boxCmp);
    }
    ,
    addEvents: function () {
        var g = this;
        $(".not-data-msg", "#" + this.renderTo).bind("click", function () {
            g.params.page = 1;
            g.getTodoData();
        });
        this.loadMoreDom.click(function () {
            g.params.page += 1;
            g.getTodoData();
        });
        g.approveViewWindow();
        g.lookApproveViewWindow();
        g.flowInstanceWindow();
        g.showRejectWindow();
        g.claimEvent();
        g.endFlowEvent();
    }
    ,
    //点击打开审批界面的新页签
    approveViewWindow: function () {
        var g = this;
        $(".approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var taskConfig = JSON.parse(data.taskJsonDef);
            var workPageUrl = data.taskFormUrlXiangDui;
            var joinStr = workPageUrl.indexOf("?") != -1 ? "&" : "?";
            var tab = {
                title: data.taskName,
                url: workPageUrl + joinStr + "id=" + data.flowInstance.businessId + "&taskId=" + data.id,
                id: data.id
            };
            g.addTab(tab);
        });
    }
    ,
    refresh: function (modelId) {
        this.params.page = 1;
        this.getTodoData();
    }
    ,
    //点击打开查看表单界面的新页签
    lookApproveViewWindow: function () {
        var g = this;
        $(".look-approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var url = data.flowInstance.flowDefVersion.flowDefination.flowType.businessModel.lookUrl;
            var joinStr = url.indexOf("?") != -1 ? "&" : "?";
            var tab = {
                title: "查看表单",
                url: data.webBaseAddress + url + joinStr + "id=" + data.flowInstance.businessId,
                id: data.flowInstance.businessId
            };
            g.addTab(tab);
        });
    }
    ,
    //点击打开流程历史的新页签
    flowInstanceWindow: function () {
        var g = this;
        $(".flowInstance-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            EUI.FlowHistory({
                businessId: data.flowInstance.businessId,
                instanceId: data.flowInstance.id
            })
        });
    }
    ,
    //驳回
    showRejectWindow: function () {
        var g = this;
        $(".reject-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var win = EUI.Window({
                title: "驳回意见",
                height: 100,
                items: [{
                    xtype: "TextArea",
                    title: "驳回意见",
                    name: 'opinion',
                    id: "opinion",
                    labelWidth: 90,
                    width: 220,
                    height: 80,
                    allowBlank: false
                }],
                buttons: [{
                    title: "取消",
                    handler: function () {
                        win.remove();
                    }
                }, {
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var opinion = EUI.getCmp("opinion").getValue();
                        if (!opinion) {
                            EUI.ProcessStatus({
                                success: false,
                                msg: "请输入驳回意见"
                            });
                            return;
                        }

                        var myMask = EUI.LoadMask({
                            msg: "处理中，请稍后.."
                        });
                        EUI.Store({
                            url: _ctxPath + "/flowClient/rejectTask",
                            params: {
                                taskId: data.id,
                                opinion: opinion
                            },
                            success: function (result) {
                                myMask.hide();
                                if (result.success) {
                                    win.close();
                                    //TODO:刷新当前页+
                                    window.location.reload();

                                } else {
                                    EUI.ProcessStatus(result);
                                }
                            },
                            failure: function (result) {
                                myMask.hide();
                                EUI.ProcessStatus(result);
                            }
                        })
                    }
                }]
            })
        });
    }
    ,
    //签收事件
    claimEvent: function () {
        var g = this;
        $(".claim-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var message = EUI.MessageBox({
                border: true,
                title: "温馨提示",
                showClose: true,
                msg: "您确定要签收吗",
                buttons: [{
                    title: "取消",
                    handler: function () {
                        message.remove();
                    }
                }, {
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var myMask = EUI.LoadMask({
                            msg: "正在签收，请稍候..."
                        });
                        EUI.Store({
                            url: _ctxPath + "/flowClient/claimTask",
                            params: {taskId: data.id},
                            success: function (status) {
                                myMask.remove();
                                EUI.ProcessStatus(status);
                                //重新获取数据
                                g.getTodoData();
                            },
                            failure: function (status) {
                                myMask.hide();
                                EUI.ProcessStatus(status);
                            }
                        });
                        message.remove();
                    }
                }]
            });
        });
    }
    ,
    //终止事件
    endFlowEvent: function () {
        var g = this;
        $(".endFlow-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var message = EUI.MessageBox({
                border: true,
                title: "温馨提示",
                showClose: true,
                msg: "您确定要终止吗",
                buttons: [{
                    title: "取消",
                    handler: function () {
                        message.remove();
                    }
                }, {
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var myMask = EUI.LoadMask({
                            msg: "正在终止，请稍候..."
                        });
                        EUI.Store({
                            url: _ctxPath + "/flowInstance/endFlowInstance/",
                            params: {id: data.flowInstance.id},
                            success: function (status) {
                                myMask.remove();
                                EUI.ProcessStatus(status);
                                //重新获取数据
                                g.getTodoData();
                            },
                            failure: function (status) {
                                myMask.hide();
                                EUI.ProcessStatus(status);
                            }
                        });
                        message.remove();
                    }
                }]
            });
        });
    }
    ,
    //在新的窗口打开（模拟新页签的打开方式）
    addTab: function (tab) {
        if (parent.homeView) {
            parent.homeView.addTab(tab);//获取到父窗口homeview，在其中新增页签
        } else {
            window.open(tab.url);
        }
    }
})
;