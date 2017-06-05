EUI.MainPageView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    data: null,
    menudata: null,
    dataWait: null,
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            data: this.data,
            menudata: this.menudata,
            dataWait: this.dataWait,
            html: this.getHtml()
        });
        this.initChooseDate();
        // this.initCheckBox();
        this.showModelItems(_data);
        // this.showWaitModelItems(_dataWait);
        // this.getModelList();
        this.getTodo();
        this.showPage();
        this.getInvoiceList();
        this.addEvents();
    },
    //拼接html内容
    getHtml: function () {
        return this.getTodoHtml() + this.getMyOrderHtml();
    },
    //拼接已办界面的html
    getTodoHtml: function () {
        return ' <div class="taken-top">' +
            '            <div class="top-header">' +
            '                <span class="worktable">我的工作台</span>' +
            '                <div class="header-right">' +
            '                    <span class="wait-work task-work active">待办工作</span>' +
            '                    <span class="taken-work history-work">已办工作</span>' +
            '                    <input class="search" type="text" placeholder="输入单据说明关键字查询"/>' +
            '                    <span class="btn">待办项批量处理</span>' +
            '                </div>' +
            '            </div>' +
            '            <div class="top-content">' +
            '                <div class="content-navbar">' +
            '                    <i class="arrow-left pre"></i>' +
            '                    <div class="navbar"></div>' +
            '                    <i class="arrow-right next"></i>' +
            '                </div>' +
            '                <div class="content-skip">' +
            '		            <input type="checkbox" class="checkbox check-all">全选' +
            '		            <div class="end-icon end-all"></div>' +
            '		            <div class="reject-icon"></div>' +
            '	             </div>' +
            '               <div class="content-info">' +
            '                    <div class="info-left todo-info"></div>' +
            '               </div>' +
            '               <div class="content-page"></div>' +
            '            </div>' +
            '   </div>';

    },
    //拼接我的单据界面的html
    getMyOrderHtml: function () {
        return '<div class="taken-center">' +
            '            <div class="center-top">' +
            '                <div class="top-header">' +
            '                    <span class="worktable">我的单据</span>' +
            '                    <div class="header-right">' +
            '                        <span class="wait-work wait-invoices">待办单据</span>' +
            '                        <span class="taken-work taken-invoices active">已办单据</span>' +
            '                        <input class="search" type="text" placeholder="输入单据说明关键字查询"/>' +
            '                        <div class="data">' +
            '                            <div id="dateField"></div>' +
            '                        </div>' +
            '                    </div>' +
            '                </div>' +
            '            </div>' +
            '            <div class="center-content">' +
            '                <div class="content-info center-info">' +
            '                    <div class="info-left invoice-info"></div>' +
            '                </div>' +
            '            <div class="content-page"></div>' +
            '            </div>' +
            '   </div>';
    },
    //已办界面导航部分的数据调用
    getModelList: function () {
        var g = this;
        // g.showModelItems(this.data);
        // g.showTodoContent(this.data[0].items);
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {},
            success: function (status) {
                if (!status.success) {
                    EUI.ProcessStatus(status);
                    return;
                }
                g.showModelItems(status.data);
                // g.showTodoContent(status.items);

            },
            failurle: function (result) {
                EUI.ProcessStatus(result);
            }
        })
    },
    //已办界面内部部分的数据调用
    getTodo: function (modelId) {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {
                modelId: modelId
            },
            success: function (result) {
                if (result.rows) {
                    g.showTodoContent(result.rows);
                }
            },
            failurle: function (result) {
                EUI.ProcessStatus(result);
            }
        })
    },
    //显示已办中的导航部分子组件
    showModelItems: function (data) {
        var g = this;
        // console.log(data);
        var html = "";
        // var index=0;
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            if (i == 0) {
                html += '                        <div class="navber-count">' +
                    '                            <div class="navbar-circle select">' + item.count + '</div>' +
                    '                            <div class="navber-text select-text">' + item.name + '</div>' +
                    '                        </div>';
            }
            else {
                html += '                        <div class="navber-count">' +
                    '                            <div class="navbar-circle">' + item.count + '</div>' +
                    '                            <div class="navber-text">' + item.name + '</div>' +
                    '                        </div>';
            }

        }
        $(".navbar").append(html);
    },
    /* //显示待办中的导航部分子组件
     showWaitModelItems: function (dataWait) {
     // console.log(data);
     var g=this;
     var html = "";
     // var index=0;
     for (var i = 0; i < dataWait.length; i++) {
     var item = dataWait[i];
     if (i == 0) {
     html += '                        <div class="navber-count">' +
     '                            <div class="navbar-circle select">' + item.count + '</div>' +
     '                            <div class="navber-text select-text">' + item.name + '</div>' +
     '                        </div>';
     }
     else {
     html += '                        <div class="navber-count">' +
     '                            <div class="navbar-circle">' + item.count + '</div>' +
     '                            <div class="navber-text">' + item.name + '</div>' +
     '                        </div>';
     }

     }
     $(".navbar").append(html);
     },*/
    //显示已办中的内容部分子组件
    showTodoContent: function (items) {
        var g = this;
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
                '                                <input type="checkbox" class="checkbox">' +
                '                                <span class="flow-text">' + items[j].taskName + '</span>' +
                // '                                <span class="item-right over-text">' + statusStr + '</span>' +
                '                            </div>' +
                '                            <div class="item user">'
                +
                '                                <span class="userName">' + items[j].createdBy + '</span>' +
                '                                <span class="item-right userName">' + items[j].createdDate + '</span>' +
                '                            </div>' +
                '                            <div class="item">' +
                '                                <div class="end">' +
                '                                    <i class="end-icon"></i>' +
                '                                    <i class="reject-icon"></i>' +
                '                                    <i class="look-icon"></i>' +
                '                                    <i class="time-icon look-approve"></i>' +
                // '                                    <span class="end-text">' + items[j].end + '</span>' +
                '                                </div>' +
                '                                <span class="item-right">' +
                /*'                                    <i class="look-icon"></i>' +
                 '                                    <i class="time-icon look-approve"></i>' +*/
                '                                </span>' +
                '                            </div>' +
                '</div>');
            itemdom.data(items[j]);
            $(".todo-info").append(itemdom);
        }
    },
    //显示待办中的内容部分子组件
    showWaitContent: function (items) {
        var html = "";
        for (var j = 0; j < items.length; j++) {
            html += '<div class="info-item">' +
                '                            <div class="item">' +
                '                                <span class="flow-text">' + items[j].taskName + '</span>' +
                '                                <span class="item-right over-text">' + items[j].state + '</span>' +
                '                            </div>' +
                '                            <div class="item user">' +
                '                                <span class="userName">' + items[j].userName + '</span>' +
                '                                <span class="item-right userName">' + items[j].date + '</span>' +
                '                            </div>' +
                '                            <div class="item">' +
                '                                <div class="end">' +
                '                                    <i class="end-icon"></i>' +
                '                                    <span class="end-text">' + items[j].end + '</span>' +
                '                                </div>' +
                '                                <span class="item-right">' +
                '                                    <i class="look-icon"></i>' +
                '                                    <i class="time-icon"></i>' +
                '                                </span>' +
                '                            </div>' +
                '</div>';
        }
        $(".todo-info").append(html);
    },
    //待办工作的点击事件
    waitingWorkEvent: function () {
        var g = this;
        $(".task-work").live("click", function () {
            $(".history-work").removeClass("active");
            $(this).addClass("active");
        });
        // g.showWaitModelItems(dataWait);
    },
    //已办工作的点击事件
    todoWorkEvent: function () {
        var g = this;
        $(".history-work").live("click", function () {
            $(".task-work").removeClass("active");
            $(this).addClass("active");
        });
        // g.showModelItems(_data);
    },
    //底部翻页部分
    showPage: function () {
        var html = "";
        html += '<div class="record-total">共23条记录</div>' +
            '                    <div class="pege-right">' +
            // '                        <a href="#" class="one">1</a>' +
            '                        <a href="#" class="num"><首页</a>' +
            '                        <a href="#" class="num"><上一页</a>' +
            '                        <a href="#" class="one">1</a>' +
            '                        <a href="#" class="next-page">下一页></a>' +
            '                        <a href="#" class="end-page">尾页></a>' +
            '                     </div>';
        $(".content-page").append(html);
    },
    //我的单据中的日历
    initChooseDate: function () {
        EUI.FieldGroup({
            renderTo: "dateField",
            itemspace: 10,
            width: 266,
            items: [{
                xtype: "DateField",
                format: "Y-m-d",
                height: 14,
                width: 100
            }, "到", {
                xtype: "DateField",
                format: "Y-m-d",
                height: 14,
                width: 100
            }]
        })
    },
    //我的单据界面的数据调用
    getInvoiceList: function () {
        var g = this;
        g.showInvoiceContent(this.menudata);
        /*EUI.Store({
         url:_ctxPath + "/maindata/flowTask/find",
         params: {},
         success: function (status) {
         if (!status.success){
         EUI.ProcessStatus(status);
         return;
         }
         g.navbarEvent(status.menudata);
         },
         failurle:function(result){
         EUI.ProcessStatus(result);
         }
         })*/
    },
    //已办单据的点击事件（点击后显示已办单据的数据）
    todoInvoiceEvent: function () {
        var g = this;
        $(".taken-invoices").live("click", function () {
            $(".wait-invoices").removeClass("active");
            $(this).addClass("active");
            g.showInvoiceContent(_menudata);
        });
    },
    //待办单据的点击事件（点击后显示待办单据的数据）
    waitingInvoiceEvent: function () {
        var g = this;
        $(".wait-invoices").live("click", function () {
            $(".taken-invoices").removeClass("active");
            $(this).addClass("active");
            g.showWaitInvoiceContent(_menu);
        });
    },
    //显示我的单据中的已办单据内容部分
    showInvoiceContent: function (menudata) {
        var html = "";
        for (var i = 0; i < menudata.length; i++) {
            var item = menudata[i];
            html += '<div class="info-item">' +
                '                            <div class="item">' +
                '                                <span class="flow-text">' + item.flowtext + '</span>' +
                '                                <span class="item-right general">' + item.general + '</span>' +
                '                            </div>' +
                '                            <div class="item user">' +
                '                                <span class="user-name">' + item.useuName + '</span>' +
                '                                <span class="item-right userName">' + item.date + '</span>' +
                '                            </div>' +
                '                            <div class="item">' +
                '                                <span class="item-right">' +
                '                                    <i class="look-icon"></i>' +
                '                                    <i class="time-icon"></i>' +
                '                                </span>' +
                '                            </div>' +
                '                        </div>';
        }
        $(".invoice-info").html(html);
    },
    //显示我的单据中的待办单据内容部分
    showWaitInvoiceContent: function (_menu) {
        var html = "";
        for (var i = 0; i < _menu.length; i++) {
            var item = _menu[i];
            html += '<div class="info-item">' +
                '                            <div class="item">' +
                '                                <span class="flow-text">' + item.flowtext + '</span>' +
                '                                <span class="item-right general">' + item.general + '</span>' +
                '                            </div>' +
                '                            <div class="item user">' +
                '                                <span class="userName">' + item.useuName + '</span>' +
                '                                <span class="item-right userName">' + item.date + '</span>' +
                '                            </div>' +
                '                            <div class="item">' +
                '                                <span class="item-right">' +
                '                                    <i class="look-icon"></i>' +
                '                                    <i class="time-icon"></i>' +
                '                                </span>' +
                '                            </div>' +
                '                        </div>';
        }
        $(".invoice-info").html(html);
    },
    //添加事件
    addEvents: function () {
        var g = this;
        // g.navbarEvent();
        g.arrowEvent();
        g.todoInvoiceEvent(_menudata);
        g.waitingInvoiceEvent(_menu);
        g.waitingWorkEvent(_dataWait);
        g.todoWorkEvent();
        g.approveViewWindow();
        g.flowInstanceWindow();

    },
    //导航的点击事件
    navbarEvent: function () {
        var g = this;
        $(document).ready(function () {
            $(".navber-count").on("click", function () {
                // console.log( $(this).index());
                //获取当前导航的索引值
                var index = $(this).index();
                //添加被选中的导航的样式
                $(this).siblings().children(".navbar-circle").removeClass("select");
                $(this).children(".navbar-circle").addClass("select");
                $(this).siblings().children(".navber-text").removeClass("select-text");
                $(this).children(".navber-text").addClass("select-text");

                //加载响应内容
                g.showTodoContent(g.data[index].items);

            })
        })
    },
    //点击打开审批界面的新页签
    approveViewWindow: function () {
        var g = this;
        $(".look-approve").live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var tab = {
                title: "审批界面",
                url: _ctxPath + "/lookApproveBill/show?id=" + data.flowInstance.businessId,
                id: data.flowInstance.businessId
            };
            g.addTab(tab);
        });
    },
    //点击打开流程实例的新页签
    flowInstanceWindow: function () {
        var g = this;
        $(".reject-icon").live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var tab = {
                title: "流程实例",
                url: _ctxPath + "/flowInstance/show?id=" + data.flowInstance.id,
                id: data.flowInstance.id
            };
            g.addTab(tab);
        });
    },
    //导航左右箭头的翻页效果
    arrowEvent: function (data) {
        var g = this;

    },
    //在新的窗口打开（模拟新页签的打开方式）
    addTab: function (tab) {
        window.open(tab.url);
    }
});