EUI.MainPageView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    data: null,
    menudata: null,
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            data: this.data,
            menudata: this.menudata,
            html: this.getHtml()
        });
        this.initChooseDate();
        this.showModelItems(_data);
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
            '                    <span class="wait-work">待办工作</span>' +
            '                    <span class="taken-work">已办工作</span>' +
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
            '                        <span class="taken-work taken-invoices">已办单据</span>' +
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
            url: _ctxPath + "/maindata/flowTask/find",
            params: {},
            success: function (status) {
                if (!status.success){
                    EUI.ProcessStatus(status);
                    return;
                }
                g.showModelItems(status.data);
                // g.showTodoContent(status.items);

            },
            failurle:function (result) {
                EUI.ProcessStatus(result);
            }
        })
    },
    //已办界面内部部分的数据调用
    getTodo: function (modelId) {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/maindata/flowTask/find",
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
    //显示已办中的内容部分子组件
    showTodoContent: function (items) {
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
        $(".todo-info").html(html);
    },
    //底部翻页部分
    showPage: function () {
        var html = "";
        html += '<div class="record-total">共23条记录</div>' +
            '                    <div class="pege-right">' +
            '                        <a href="#" class="one">1</a>' +
            '                        <a href="#" class="num">2</a>' +
            '                        <a href="#" class="num">3</a>' +
            '                        <a href="#" class="num">4</a>' +
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
    //显示我的单据中的内容部分
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
        $(".invoice-info").append(html);
    },
    //添加事件
    addEvents: function () {
        var g = this;
        // g.navbarEvent();
        g.arrowEvent();
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
    //导航左右箭头的翻页效果
    arrowEvent: function (data) {
        var g=this;

    }
});