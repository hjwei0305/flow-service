// 待办部分
EUI.TodoTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    modelId: null,
    pageInfo: {
        page: 1,
        rows: 10,
        total: 1
    },
    records: null,
    initComponent: function () {
        this.initHtml();
        this.getModelList();
        this.addEvents();
    },
    initHtml: function () {
        var html = this.getNavbarHtml() + this.getTodoTaskHtml();
        $("#" + this.renderTo).append(html);
    },
    //导航部分的外层容器
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
        var myMask = EUI.LoadMask({
            msg: "正在加载请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTaskHeader",
            success: function (status) {
                myMask.hide();
                if(!status.data){
                    return;
                }
                g.getNavHtml(status.data);
                //默认显示第一个模块的列表
                g.modelId = status.data[0].businessModeId;
                g.getTodoData();
            },
            failure: function (result) {
                myMask.hide();
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
                html += '                    <div class="navber-count nav-select" data-id="' + item.businessModeId + '">' +
                    '                            <div class="navbar-circle">' + item.count + '</div>' +
                    '                            <div class="navber-text">' + item.businessModelName + '</div>' +
                    '                        </div>';
            }
            else {
                html += '                    <div class="navber-count" data-id="' + item.businessModeId + '">' +
                    '                            <div class="navbar-circle">' + item.count + '</div>' +
                    '                            <div class="navber-text">' + item.businessModelName + '</div>' +
                    '                        </div>';
            }

        }
        $(".navbar").append(html);
    },
    //待办的外层
    getTodoTaskHtml: function () {
        // return '             <div class="content-skip">' +
        // '		            <div class="check-box"><div class="check-all"></div><label>全选</label></div>' +
        // '		            <div class="end-icon end-all"></div>' +
        // '		            <div class="reject-icon"></div>' +
        // '	             </div>' +
        return '            <div class="content-info">' +
            '                    <div class="info-left todo-info"></div>' +
            '               </div>' +
            '               <div class="content-page">' +
            '                   <div class="record-total">共0条记录</div>' +
            '                    <div class="pege-right">' +
            '                        <a href="#" class="first-page"><首页</a>' +
            '                        <a href="#" class="prev-page"><上一页</a>' +
            '                        <input value="1" class="one">' +
            '                        <a href="#" class="next-page">下一页></a>' +
            '                        <a href="#" class="end-page">尾页></a>' +
            '                     </div>' +
            '               </div>';
    },
    //待办内容部分的数据调用
    getTodoData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {
                modelId: this.modelId,
                S_createdDate: "DESC",
                page: this.pageInfo.page,
                rows: this.pageInfo.rows
            },
            success: function (result) {
                myMask.hide();
                if (result.rows) {
                    g.pageInfo.page = result.page;
                    g.pageInfo.total = result.total;
                    g.records = result.records;
                    if (g.records>0){
                        $(".nav-select>.navbar-circle").text(g.records);
                    }else{
                        $(".nav-select").css("display","none");
                    }
                    g.getTodoHtml(result.rows);
                    g.showPage(result.records);//数据请求成功后再给总条数赋值
                    $(".one").val(g.pageInfo.page);//数据请求成功后在改变class为one的val值，避免了点击下一页时val值变了却没有获取成功数据
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
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
            var rejectHtml = items[j].canReject ? '<div class="todo-btn reject-btn"><i class="reject-icon" title="驳回"></i><span>驳回</span></div>' : '';
            var itemdom = $('<div class="info-item">' +
                '                 <div class="item">' +
                // '                  <div class="checkbox"></div>' +
                '                     <span class="flow-text">' + items[j].flowName + '_' + items[j].taskName + '</span>' +
                '                 </div>' +
                '                 <div class="item flow-digest">' +
                '                     <span class="digest">' + items[j].flowInstance.businessCode + '-' + items[j].flowInstance.businessModelRemark + '</span></span>' +
                '                 </div>' +
                '                 <div class="item">' +
                '                     <div class="end">' +
                '                          <div class="todo-btn approve-btn"><i class="end-icon" title="审批"></i><span>处理</span></div>'
                + rejectHtml +
                '                          <div class="todo-btn look-approve-btn"><i class="look-icon look-approve" title="查看表单"></i><span>查看表单</span></div>' +
                '                          <div class="todo-btn flowInstance-btn"><i class="time-icon flowInstance" title="流程历史"></i><span>流程历史</span></div>' +
                '                     </div>' +
                '                     <span class="item-right task-item-right">' +
                '                          <div class="userName">发起人：' + items[j].creatorName + '</div>' +
                '                          <div class="todo-date"><i class="flow-time-icon" title="流程历史"></i><span>' + this.countDate(items[j].createdDate) + '</span></div>' +
                '                     </span>' +
                '                 </div>' +
                '</div>');
            itemdom.data(items[j]);
            $(".todo-info", '#' + this.renderTo).append(itemdom);
        }
    },
    //计算时间几天前
    countDate: function (startTime) {
        var g = this;
        var date = new Date();
        var endTime = date.getTime();
        startTime = new Date(startTime).getTime();
        var time = endTime - startTime;
        if (time <= 60000) {//如果结束时间小于开始时间
            return "刚刚";
        } else {
            //计算出相差天数
            var days = Math.floor(time / (24 * 3600 * 1000));
            if (days > 0) {
                return days + '天';
            }
            //计算出小时数
            var leave1 = time % (24 * 3600 * 1000);   //计算天数后剩余的毫秒数
            if (leave1 == 0) {//如果leave1=0就不需要在做计算，直接把0赋给hours
                return hours = 0;
            } else {
                var hours = Math.floor(leave1 / (3600 * 1000));
                if (hours > 0) {
                    return hours + '小时';
                }
            }
            //计算相差分钟数
            var leave2 = leave1 % (3600 * 1000);        //计算小时数后剩余的毫秒数
            var minutes = Math.floor(leave2 / (60 * 1000));
            return minutes + '分钟前';
        }
    },
    //底部翻页部分
    showPage: function (records) {
        $(".record-total").text("共" + records + "条记录");
    },
    //底部翻页绑定事件
    pagingEvent: function () {
        var g = this;
        //首页
        $(".first-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == 1) {
                return;
            }
            g.pageInfo.page = 1;
            g.getTodoData();
        });
        //上一页
        $(".prev-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == 1) {
                return;
            }
            g.pageInfo.page--;
            g.getTodoData();
        });
        //下一页
        $(".next-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == g.pageInfo.total) {
                return;
            }
            g.pageInfo.page++;
            g.getTodoData();
        });
        //尾页
        $(".end-page", "#" + this.renderTo).live("click", function () {//点击尾页时
            if (g.pageInfo.page == g.pageInfo.total) {//如果page=total
                return; //就直接return
            }
            g.pageInfo.page = g.pageInfo.total;//page=total
            g.getTodoData();//请求page=total时的数据
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
        // g.checkBoxEvents();
        g.pagingEvent();
        g.navbarEvent();
    },
    //导航的点击事件
    navbarEvent: function () {
        var g = this;
        $(document).ready(function () {
            $(".navber-count").live("click", function () {
                $(this).addClass("nav-select").siblings().removeClass("nav-select");
                var id = $(this).attr("data-id");
                g.modelId = id;
                //重新获取数据
                g.getTodoData();
            })
        })
    },
    //点击打开审批界面的新页签
    approveViewWindow: function () {
        var g = this;
        $(".approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            //var url = data.flowInstance.flowDefVersion.flowDefination.flowType.businessModel.lookUrl;
            var taskConfig = JSON.parse(data.taskJsonDef);
            var workPageUrl = taskConfig.nodeConfig.normal.workPageUrl;
            console.log(workPageUrl);
            var tab = {
                title: "审批界面",
                url: _ctxPath + workPageUrl + "?id=" + data.flowInstance.businessId + "&taskId=" + data.id,
                id: data.flowInstance.businessId
            };
            g.addTab(tab);
        });
    },
    //点击打开查看表单界面的新页签
    lookApproveViewWindow: function () {
        var g = this;
        $(".look-approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var url = data.flowInstance.flowDefVersion.flowDefination.flowType.businessModel.lookUrl;
            var tab = {
                title: "查看表单",
                url: _ctxPath + url + "?id=" + data.flowInstance.businessId,
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
            Flow.FlowHistory({
                businessId: data.flowInstance.businessId
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
                        win.close();
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
    }
    /*//选项框的点击事件
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
     }*/
})
;