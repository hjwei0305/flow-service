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
    firstTime: true,
    searchName: null,
    nowPage: null,
    totalPage: null,
    mainViewBarClick: false,
    initComponent: function () {
        this.getModelList();
        // this.getTestNavData(_data);
        this.addEvents();
    },
    initHtml: function (data) {
        $("#" + this.renderTo).empty();
        var html = this.getNavbarHtml(data) + this.getTodoTaskHtml();
        $("#" + this.renderTo).append(html);
    },
    //导航部分的外层容器
    getNavbarHtml: function (data) {
        var g = this;
        var html = '<div class="content-navbar">';
        if (data.length < 7) {
            html += '<div class="navbar"></div>';
        } else {
            html += '      <i class="ecmp-common-prepage page-btn arrow-left pre"></i>' +
                '         <div class="navbar"></div>' +
                '         <i class="ecmp-common-nextpage page-btn arrow-right next"></i>';
        }
        html += '</div>';
        return html;
    },


    //导航测试数据
    /*getTestNavData: function () {
        var g = this;
        g.initHtml(_data);
        g.getNavHtml(_data);
        g.nowPage = 1;
        g.totalPage = Math.ceil(_data.length / 6);
        g.upPageBtn();

    },*/
    upPageBtn: function () {
        if (this.nowPage == 1) {
            $(".pre").removeClass("page-gray").addClass("page-btn")
        } else {
            $(".pre").removeClass("page-btn").addClass("page-gray")
        }
        if (this.totalPage == this.nowPage) {
            $(".next").removeClass("page-gray").addClass("page-btn")
        } else {
            $(".next").removeClass("page-btn").addClass("page-gray")
        }
    },

    //导航部分的数据调用
    getModelList: function (modelId) {
        var g = this;
        var myMask;
        if (g.firstTime) {
            myMask = EUI.LoadMask({
                msg: "正在加载请稍候..."
            });
        }
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTaskHeader",
            success: function (status) {
                if (g.firstTime) {
                    myMask.hide();
                }
                if (!status.data) {
                    g.getNotWorkData();
                    return;
                }
                g.nowPage = 1;
                g.totalPage = Math.ceil(status.data.length / 6);
                g.initHtml(status.data);
                g.getNavHtml(status.data);
                //默认显示第一个模块的列表
                if (modelId) {
                    g.modelId = modelId;
                    if ($(".navber-count[data-id='" + modelId + "']").length <= 0) {
                        g.modelId = status.data[0].businessModeId;
                    }
                } else {
                    g.modelId = status.data[0].businessModeId;
                }
                if (g.modelId) {
                    $(".navber-count[data-id='" + g.modelId + "']").click();//对当前模块或者第一个模块模拟点击事件进行刷新
                }
                g.upPageBtn();

            },
            failure: function (result) {
                if (g.firstTime) {
                    myMask.hide();
                }
                EUI.ProcessStatus(result);
            }
        });
    },
    //当页面没有数据时的显示内容
    getNotWorkData: function () {
        $("#" + this.renderTo).empty();
        var html = '<div class="todo-not-data">' +
            '<div class="not-data-msg">------------您当前没有需要处理的工作------------</div></div>';
        $("#" + this.renderTo).append(html)
            .css("height", "100%");
    },
    //导航部分的内容的循环
    getNavHtml: function (data) {
        var g = this;
        var html = "";
        $(".navbar", '#' + this.renderTo).empty();
        for (var i = 0; i < data.length; i++) {
            var page = parseInt(i / 6) + 1;
            var item = data[i];
            if (i == 0) {
                html += '                    <div page="' + page + '" class="navber-count nav-select" data-id="' + item.businessModeId + '">' +
                    '                            <div class="navbar-circle">' + item.count + '</div>' +
                    '                            <div class="navber-text">' + item.businessModelName + '</div>' +
                    '                        </div>';
            }
            else {
                html += '                    <div page="' + page + '" class="navber-count" data-id="' + item.businessModeId + '">' +
                    '                            <div class="navbar-circle">' + item.count + '</div>' +
                    '                            <div class="navber-text">' + item.businessModelName + '</div>' +
                    '                        </div>';
            }

        }
        $(".navbar", '#' + this.renderTo).append(html);
        $(".navber-count:gt(5)", ".navbar").hide();
    },
    //待办的外层
    getTodoTaskHtml: function () {
        return '            <div class="content-info">' +
            '                    <div class="info-left todo-info"></div>' +
            '               </div>' +
            '               <div class="content-page">' +
            '                   <div class="record-total">共0条记录</div>' +
            '                    <div class="pege-right">' +
            '                        <a href="#" class="first-page pageDisable"><首页</a>' +
            '                        <a href="#" class="prev-page pageDisable"><上一页</a>' +
            '                        <input value="1" class="one">' +
            '                        <a href="#" class="next-page pageDisable">下一页></a>' +
            '                        <a href="#" class="end-page pageDisable">尾页></a>' +
            '                     </div>' +
            '               </div>';
    },
    //待办内容部分的数据调用
    getTodoData: function (isSearch) {
        var g = this;
        var myMask;
        if (g.firstTime) {
            myMask = EUI.LoadMask({
                msg: "正在加载请稍候..."
            });
        }
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {
                modelId: this.modelId,
                S_createdDate: "DESC",
                page: this.pageInfo.page,
                rows: this.pageInfo.rows,
                Quick_value: this.searchName
            },
            success: function (result) {
                if (g.firstTime) {
                    myMask.hide();
                    g.firstTime = false;
                }
                if (result.rows) {
                    g.pageInfo.page = result.page;
                    g.pageInfo.total = result.total;
                    g.records = result.records;
                    g.pageJudge();
                    if (g.records > 0) {
                        $(".nav-select>.navbar-circle").text(g.records);
                    } else {
                        if (!isSearch) {
                            $(".nav-select").css("display", "none");
                        }
                        if (!$(".navber-count").hasClass("nav-select")) {
                            $('div', '#' + this.renderTo).css("display", "none");
                            g.getNotWorkData();
                            return;
                        }
                    }
                    g.getTodoHtml(result.rows);
                    g.showPage(result.records);//数据请求成功后再给总条数赋值
                    $(".one").val(g.pageInfo.page);//数据请求成功后在改变class为one的val值，避免了点击下一页时val值变了却没有获取成功数据
                }
            },
            failure: function (result) {
                if (g.firstTime) {
                    myMask.hide();
                    g.firstTime = false;
                }
                EUI.ProcessStatus(result);
            }
        });
    },
    //待办分页的判断
    pageJudge: function () {
        var g = this;
        if (g.pageInfo.page == 1 && g.pageInfo.total > 1 && g.pageInfo.page < g.pageInfo.total) {
            $(".next-page", "#" + this.renderTo).removeClass("pageDisable");
            $(".next-page", "#" + this.renderTo).mouseenter(function () {
                $(this).addClass("hover");
            }).mouseleave(function () {
                $(this).removeClass("hover");
            });
            $(".end-page", "#" + this.renderTo).removeClass("pageDisable");
            $(".end-page", "#" + this.renderTo).mouseenter(function () {
                $(this).addClass("hover");
            }).mouseleave(function () {
                $(this).removeClass("hover");
            });
            $(".first-page", "#" + this.renderTo).addClass("pageDisable");
            $(".prev-page", "#" + this.renderTo).addClass("pageDisable");
        } else if (g.pageInfo.page > 1 && g.pageInfo.page < g.pageInfo.total) {
            $("a", "#" + this.renderTo).removeClass("pageDisable");
            $("a", "#" + this.renderTo).mouseenter(function () {
                $(this).addClass("hover");
            }).mouseleave(function () {
                $(this).removeClass("hover");
            });
        } else if (g.pageInfo.page > 1 && g.pageInfo.page == g.pageInfo.total) {
            $(".first-page", "#" + this.renderTo).removeClass("pageDisable");
            $(".first-page", "#" + this.renderTo).mouseenter(function () {
                $(this).addClass("hover");
            }).mouseleave(function () {
                $(this).removeClass("hover");
            });
            $(".prev-page", "#" + this.renderTo).removeClass("pageDisable");
            $(".prev-page", "#" + this.renderTo).mouseenter(function () {
                $(this).addClass("hover");
            }).mouseleave(function () {
                $(this).removeClass("hover");
            });
            $(".next-page", "#" + this.renderTo).addClass("pageDisable");
            $(".end-page", "#" + this.renderTo).addClass("pageDisable");
        }
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
            var rejectHtml = items[j].canReject ? '<div class="todo-btn reject-btn"><i class="ecmp-common-return reject-icon" title="驳回"></i><span>驳回</span></div>' : '';
            var nodeType = JSON.parse(items[j].taskJsonDef).nodeType;
            var claimTaskHtml = nodeType == "SingleSign" && !items[j].actClaimTime ? '<div class="todo-btn claim-btn"><i class="ecmp-common-claim claim-icon" title="签收"></i><span>签收</span></div>' : '';
            var flowInstanceCreatorId = items[j].flowInstance ? items[j].flowInstance.creatorId : "";
            var endFlowHtml = items[j].canSuspension && flowInstanceCreatorId == items[j].executorId ? '<div class="todo-btn endFlow-btn"><i class="ecmp-flow-end endFlow-icon icon-size" title="终止"></i><span>终止</span></div>' : '';
            var itemdom = $('<div class="info-item">' +
                '                 <div class="item">' +
                // '                  <div class="checkbox"></div>' +
                '                     <span class="flow-text">' + items[j].flowName + '_' + items[j].taskName + '</span>' +
                '                 </div>' +
                '                 <div class="item flow-digest">' +
                '                     <span class="digest">' + items[j].flowInstance.businessCode + '-' + items[j].flowInstance.businessModelRemark + '</span></span>' +
                '                 </div>' +
                '                 <div class="item">' +
                '                     <div class="end">'
                + claimTaskHtml +
                '                          <div class="todo-btn approve-btn"><i class="ecmp-common-handle end-icon handle-icon-size" title="审批"></i><span>处理</span></div>'
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
            $(".todo-info", '#' + this.renderTo).append(itemdom);
        }
    },
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
    },
    //底部翻页部分
    showPage: function (records) {
        $(".record-total", "#" + this.renderTo).text("共" + records + "条记录");
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
        g.claimEvent();
        g.endFlowEvent();
        // g.checkBoxEvents();
        g.pagingEvent();
        g.navbarEvent();
    },
    //导航的点击事件
    navbarEvent: function () {
        var g = this;
        $(".navber-count").live("click", function () {
            $(this).addClass("nav-select").siblings().removeClass("nav-select");
            var id = $(this).attr("data-id");
            g.modelId = id;
            if (!g.mainViewBarClick) {
                EUI.getCmp("searchBox").reset();
                g.searchName = null;
                g.mainViewBarClick = false;
            }
            g.resetPageBar();
            //重新获取数据
            g.getTodoData();
        });
        $(".arrow-left").live("click", function () {
            if (!$(this).hasClass("page-btn")) {
                g.nowPage--;
                $(".navber-count", ".navbar").hide("fast");
                $(".navber-count[page='" + g.nowPage + "']", ".navbar").show("fast");
                g.upPageBtn();
            }
        });
        $(".arrow-right").live("click", function () {
            if (!$(this).hasClass("page-btn")) {
                g.nowPage++;
                $(".navber-count", ".navbar").hide(300);
                $(".navber-count[page='" + g.nowPage + "']", ".navbar").show(300);
                g.upPageBtn();
            }
        });
    },
    resetPageBar: function () {
        this.pageInfo.page = 1;
        this.pageInfo.rows = 10;
        this.pageInfo.total = 1;

        $(".first-page", "#" + this.renderTo).addClass("pageDisable");
        $(".prev-page", "#" + this.renderTo).addClass("pageDisable");
        $(".next-page", "#" + this.renderTo).addClass("pageDisable");
        $(".end-page", "#" + this.renderTo).addClass("pageDisable");
        $(".one", "#" + this.renderTo).val(1);
    },
    //点击打开审批界面的新页签
    approveViewWindow: function () {
        var g = this;
        $(".approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
          //  var taskConfig = JSON.parse(data.taskJsonDef);
            var workPageUrl = data.taskFormUrlXiangDui;
            var joinStr = data.webBaseAddress.indexOf("?") != -1 ? "&" : "?";
            var tab = {
                title: data.taskName,
                url: workPageUrl + joinStr + "id=" + data.flowInstance.businessId + "&taskId=" + data.id,
                id: data.id
            };
            g.addTab(tab);
        });
    },
    refresh: function () {
        this.getModelList(this.modelId);
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
                businessId: data.flowInstance.businessId,
                instanceId: data.flowInstance.id
            })
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
    },
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
                            url:  _ctxPath +"/flowClient/claimTask",
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
    },
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