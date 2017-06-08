// 待办部分
EUI.TodoTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    todoData:null,
    pageSize:8,
    pageNum:1,
    initComponent: function () {
        this.initHtml();
        this.getNavHtml(_data);
        this.getTodoData();
        this.showPage();
        this.addEvents();
    },
    initHtml: function () {
        var html = this.getNavbarHtml() + this.getTodoTaskHtml();
        $("#" + this.renderTo).append(html);
    },
    //导航部分的外层
    getNavbarHtml: function () {
        return '<div class="content-navbar">' +
            '         <i class="arrow-left pre"></i>' +
            '         <div class="navbar"></div>' +
            '         <i class="arrow-right next"></i>' +
            '   </div>';
    },
    //导航部分的数据调用
    getModelList: function () {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {},
            success: function (status) {
                if (!status.success) {
                    EUI.ProcessStatus(status);
                    return;
                }
                g.getNavHtml(status.data);

            },
            failurle: function (result) {
                EUI.ProcessStatus(result);
            }
        })
    },
    //导航部分的内容的循环
    getNavHtml: function (data) {
        var g = this;
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            if (i == 0) {
                html += '                    <div class="navber-count">' +
                    '                            <div class="navbar-circle select">' + item.count + '</div>' +
                    '                            <div class="navber-text select-text">' + item.name + '</div>' +
                    '                        </div>';
            }
            else {
                html += '                    <div class="navber-count">' +
                    '                            <div class="navbar-circle">' + item.count + '</div>' +
                    '                            <div class="navber-text">' + item.name + '</div>' +
                    '                        </div>';
            }

        }
        $(".navbar").append(html);
    },
    //待办的外层
    getTodoTaskHtml: function () {
        return '             <div class="content-skip">' +
            '		            <div class="check-box"><div class="check-all"></div><label>全选</label></div>' +
            '		            <div class="end-icon end-all"></div>' +
            '		            <div class="reject-icon"></div>' +
            '	             </div>' +
            '               <div class="content-info">' +
            '                    <div class="info-left todo-info"></div>' +
            '               </div>' +
            '               <div class="content-page">' +
            '                   <div class="record-total">共条记录</div>' +
            '                    <div class="pege-right">' +
            '                        <a href="#" class="first-page"><首页</a>' +
            '                        <a href="#" class="prev-page"><上一页</a>' +
            '                        <input value="1" class="one">' +
            '                        <a href="#" class="next-page">下一页></a>' +
            '                        <a href="#" class="end-page">尾页></a>' +
            '                     </div>'+
            '               </div>';
    },
    //待办内容部分的数据调用
    getTodoData: function (modelId) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {
                modelId: modelId,
                S_createdDate: "DESC",
                page: this.pageNum,
                rows: this.pageSize
            },
            success: function (result) {
                myMask.hide();
                if (result.rows) {
                    g.getTodoHtml(result.rows);
                    g.showPage(result.records);
                    g.pagingEvent(result.page,result.total);
                }
            },
            failurle: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        })
    },

    //待办里面内容部分的循环
    getTodoHtml: function (items) {
        var g = this;
        $(".todo-info", '#' + this.renderTo).empty();
        for (var j = 0; j < items.length; j++) {
            /* var status = items[j].taskStatus;
             var statusStr = "待办";
             if (status == "INPROCESS") {
             statusStr = "处理中";
             } else if (status == "COMPLETE") {
             statusStr = "结束";
             }*/
            var itemdom = $('<div class="info-item">' +
                '                            <div class="item">' +
                '                                <div class="checkbox"></div>' +
                '                                <span class="flow-text">' + items[j].taskName + '</span>' +
                // '                                <span class="item-right over-text">' + statusStr + '</span>' +
                '                            </div>' +
                '                            <div class="item user">'
                +
                '                                <span class="userName">' + items[j].creatorName + '</span>' +
                '                                <span class="item-right userName">' + items[j].createdDate + '</span>' +
                '                            </div>' +
                '                            <div class="item">' +
                '                                <div class="end">' +
                '                                    <i class="end-icon" title="审批"></i>' +
                '                                    <i class="reject-icon" title="驳回"></i>' +
                '                                    <i class="look-icon look-approve" title="查看表单"></i>' +
                '                                    <i class="time-icon flowInstance" title="流程历史"></i>' +
                '                                </div>' +
                '                                <span class="item-right">' +
                '                                </span>' +
                '                            </div>' +
                '</div>');
            itemdom.data(items[j]);
            $(".todo-info", '#' + this.renderTo).append(itemdom);
        }
    },
    //底部翻页部分
    showPage: function (records) {
        $(".record-total").text("共"+records+"条记录");
    },
    //底部翻页绑定事件
    pagingEvent: function (page,total) {
        var g = this;
        var n = $(".one").val();
        //首页
        $(".first-page").live("click", function () {

        });
        //上一页
        $(".prev-page").live("click", function () {

        });
        //下一页
        $(".next-page").live("click", function (page,total) {

        });
        //尾页
        $(".end-page").live("click", function (total) {
            this.pageNum=total;
            g.getTodoData(this.pageNum);
        });
    },
    show: function () {
        $("#" + this.renderTo).css("display", "block");
    },
    hide: function () {
        $("#" + this.renderTo).css("display", "none");
    },
    addEvents: function () {
        var g = this;
        g.approveViewWindow();
        g.lookApproveViewWindow();
        g.flowInstanceWindow();
        g.showRejectWindow();
        g.checkBoxEvents();
        g.pagingEvent();
    },
    //点击打开审批界面的新页签
    approveViewWindow: function () {
        var g = this;
        $(".end-icon").live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var tab = {
                title: "审批界面",
                url: _ctxPath + "/builtInApprove/approve?id=" + data.flowInstance.businessId + "&taskId=" + data.id,
                id: data.flowInstance.businessId
            };
            g.addTab(tab);
        });
    },
    //点击打开查看审批界面的新页签
    lookApproveViewWindow: function () {
        var g = this;
        $(".look-approve").live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var tab = {
                title: "查看表单",
                url: _ctxPath + "/lookApproveBill/show?id=" + data.flowInstance.businessId,
                id: data.flowInstance.businessId
            };
            g.addTab(tab);
        });
    },
    //点击打开流程历史的新页签
    flowInstanceWindow: function () {
        var g = this;
        $(".flowInstance").live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            Flow.FlowHistory({
                instanceId: data.flowInstance.id
            })
            // var tab = {
            //     title: "流程历史",
            //     url: _ctxPath + "/flowInstance/show?id=" + data.flowInstance.id,
            //     id: data.flowInstance.id
            // };
            // g.addTab(tab);
        });
    },
    //驳回
    showRejectWindow: function () {
        var g = this;
        $(".reject-icon").live("click", function () {
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
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var opinion = EUI.getCmp("opinion");
                        if (!opinion) {
                            EUI.ProcessStatus({
                                success:false,
                                msg:"请输入驳回意见"
                            })
                            return;
                        }
                        var myMask = EUI.LoadMask({
                            msg: "处理中，请稍后.."
                        });
                        EUI.Store({
                            url: _ctxPath + "/builtInApprove/rejectTask",
                            params: {
                                taskId: data.id,
                                opinion: opinion
                            },
                            success: function (result) {
                                myMask.hide();
                                if (result.success) {
                                    //TODO:刷新当前页
                                    win.close();
                                }else{
                                    EUI.ProcessStatus(result);
                                }
                            },
                            failure: function (result) {
                                myMask.hide();
                                EUI.ProcessStatus(result);
                            }
                        })
                    }
                }, {
                    title: "取消",
                    handler: function () {
                        win.remove();
                    }
                }]
            })
        });
    },
    //在新的窗口打开（模拟新页签的打开方式）
    addTab: function (tab) {
        if (parent.homeView) {
            parent.homeView.addTab(tab);//获取到父窗口homeview，在其中新增页签
        } else {
            window.open(tab.url);
        }
    },
    //选项框的点击事件
    checkBoxEvents: function () {
        var g = this;
        //点击单个选项框
        $(".checkbox").live("click", function () {
            if (!$(this).hasClass("checked")) {
                $(this).addClass("checked");

            } else {
                $(this).removeClass("checked");
            }
        });

        //点击全选框
        $(".check-all").click(function () {
            if (!$(this).hasClass("checked")) {
                $(this).addClass("checked");
                $(".checkbox").addClass("checked");
            } else {
                $(this).removeClass("checked");
                $(".checkbox").removeClass("checked");
            }
        })

    }
})
;