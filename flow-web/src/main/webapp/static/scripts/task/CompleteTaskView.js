// 已办部分
EUI.CompleteTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    params: {
        page: 1,
        rows: 10,
        records:0,
        modelId: null,
        S_createdDate: "DESC",
        Quick_value: null
    },
    initComponent: function () {
        var g = this;
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            title: "我的工作 > 已办任务",
            layout: "border",
            items: [{
                xtype: "ToolBar",
                height: 33,
                padding: 0,
                border: false,
                region: "north",
                items: this.initToolBar()
            }, {
                xtype: "Container",
                region: "center",
                padding: 0,
                html: '<div class="info-left todo-info"></div><div class="load-more"><span>获取更多</span></div>'
                + '<div class="empty-data"><div class="not-data-msg">------------您当前没有已完成的事项------------</div></div>'
            }]
        });
        this.dataDom = $(".todo-info", "#" + this.renderTo);
        this.emptyDom = $(".empty-data", "#" + this.renderTo);
        this.loadMoreDom = $(".load-more", "#" + this.renderTo);
        this.getData();
        this.addEvents();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "ComboBox",
            name: "businessModelName",
            displayText: "全部业务模块",
            store: {
                url: _ctxPath + "/flowTask/listFlowTaskHeader"
            },
            afterSelect: function (data) {
                g.params.modelId = data.data.businessModeId;
                g.refresh();
            },
            afterClear: function () {
                g.params.page = 1;
                g.params.modelId = null;
                g.getData();
            }
        }, {
            xtype: "SearchBox",
            name: "Quick_value",
            onSearch: function (value) {
                g.params.page = 1;
                g.params.Quick_value = value;
                g.getData();
            }
        }, "->", {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-eui-leaf' style='vertical-align: middle;color:#3671cf;'></i><span>待办工作</span>",
            onClick: function () {
                $("body").trigger("todotask");
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
    //已办内容部分的数据调用
    getData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowHistory/listFlowHistory",
            params: this.params,
            success: function (result) {
                g.params.records = result.records;
                if (result.records == 0) {
                    g.showEmptyWorkInfo();
                } else if (result.rows.length > 0) {
                    g.params.page = result.page;
                    g.showData(result.rows);
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
        })
    },
    //已办内容部分的循环
    showData: function (items) {
        var g = this;
        this.showContent();
        for (var j = 0; j < items.length; j++) {
            var backoutHtml = (items[j].canCancel == true && items[j].taskStatus == "COMPLETED" && items[j].flowInstance.ended == false)
                ? '<div class="todo-btn flow-backout-btn"><i class="ecmp-flow-backout backout-icon" title="撤回"></i><span>撤回</span></div>' : "";
            var itemdom = $('<div class="info-item">' +
                '<div class="item">' +
                '    <span class="flow-text">' + items[j].flowName + '_' + items[j].flowTaskName + '</span>' +
                '</div>' +
                '<div class="item flow-digest">' +
                '   <span class="digest">' + items[j].flowInstance.businessCode + '-' + items[j].flowInstance.businessModelRemark + '</span></span>' +
                '</div>' +
                '<div class="item">' +
                '    <div class="end">' +
                backoutHtml +
                '        <div class="todo-btn look-approve-btn"><i class="ecmp-common-view look-icon look-approve" title="查看表单"></i><span>查看表单</span></div>' +
                '        <div class="todo-btn flowInstance-btn"><i class="ecmp-flow-history time-icon flowInstance icon-size" title="流程历史"></i><span>流程历史</span></div>' +
                '    </div>' +
                '    <span class="item-right task-item-right">' +
                '        <div class="userName">发起人：' + items[j].flowInstance.creatorName + '</div>' +
                '        <div class="todo-date"><i class="ecmp-flow-history flow-time-icon time-icon-size" title="流程历史"></i><span>处理时间：' + items[j].actEndTime + '</span></div>' +
                '    </span>' +
                '</div>' +
                '</div>');
            itemdom.data(items[j]);
            this.dataDom.append(itemdom);
        }
        EUI.resize(this.boxCmp);
    },
    show: function () {
        this.boxCmp.show();
    },
    hide: function () {
        this.boxCmp.hide();
    },
    showContent: function () {
        this.dataDom.show();
        if (this.params.records > 10) {
            this.loadMoreDom.show();
        } else {
            this.loadMoreDom.hide();
        }
        this.emptyDom.hide();
        if (this.params.page == 1) {
            this.dataDom.empty();
        }
    },
    //当页面没有数据时的显示内容
    showEmptyWorkInfo: function () {
        this.emptyDom.show();
        this.dataDom.hide();
        this.loadMoreDom.hide();
        this.dataDom.empty();
    }
    ,
    addEvents: function () {
        var g = this;
        $(".not-data-msg", "#" + this.renderTo).bind("click", function () {
            g.params.page = 1;
            g.getData();
        });
        this.loadMoreDom.click(function () {
            g.params.page += 1;
            g.getData();
        });
        g.lookApproveViewWindow();
        g.flowInstanceWindow();
        g.backOutWindow();
    },
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
    },
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
    },
    //撤销
    backOutWindow: function () {
        var g = this;
        $(".flow-backout-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var win = EUI.Window({
                title: "撤回",
                height: 100,
                items: [{
                    xtype: "TextArea",
                    title: "撤回说明",
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
                        g.doRevoke(data);
                    }
                }]
            })
        });
    },
    doRevoke: function (data) {
        var opinion = EUI.getCmp("opinion").getValue();
        var myMask = EUI.LoadMask({
            msg: "处理中，请稍后.."
        });
        EUI.Store({
            url: _ctxPath + "/flowClient/cancelTask",
            params: {
                preTaskId: data.id,
                opinion: opinion
            },
            success: function (result) {
                myMask.hide();
                if (result.success) {
                    win.close();
                    g.refresh();
                } else {
                    EUI.ProcessStatus(result);
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        })
    },
    //在新的窗口打开（模拟新页签的打开方式）
    addTab: function (tab) {
        if (parent.homeView) {
            parent.homeView.addTab(tab);//获取到父窗口homeview，在其中新增页签
        } else {
            window.open(tab.url);
        }
    },
    refresh: function () {
        this.params.page = 1;
        $(".todo-info", '#' + this.renderTo).empty();
        this.getData();
    }
});