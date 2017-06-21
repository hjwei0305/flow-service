//已办单据
EUI.CompleteOrderView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    pageInfo: {
        page: 1,
        rows: 15,
        total: 1
    },
    initComponent: function () {
        this.initHtml();
        this.getTodoOrderData();
        this.addEvents();
    },
    initHtml: function () {
        var html = this.showCompleteOrderView();
        $("#" + this.renderTo).append(html);
    },
    //已办单据的数据调用
    getTodoOrderData: function () {
        var g = this;
        var start=new Date();
        var startDate=EUI.getCmp("dateField")?EUI.getCmp("dateField").getCmpByName("startDate").getValue():new Date(start.setDate(start.getDate()+(-6))).format("yyyy-MM-dd");
        var endDate=EUI.getCmp("dateField")?EUI.getCmp("dateField").getCmpByName("endDate").getValue():new Date().format("yyyy-MM-dd");
        var searchText=$(".header-left").next().children(":first").val();
        var myMask = EUI.LoadMask({
            msg: "正在加载请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowInstance/getMyBills",
            params: {
                Q_GE_startDate__Date:startDate,
                Q_LE_endDate__Date:endDate,
                Quick_value:searchText?searchText:null,
                Q_EQ_ended__Boolean: true,
                S_createdDate: "DESC",
                page: this.pageInfo.page,
                rows: this.pageInfo.rows
            },
            success: function (result) {
                myMask.hide();
                if (result) {
                    g.pageInfo.page = result.page;
                    g.pageInfo.total = result.total;
                    g.showCompleteView(result.rows);
                    g.showPage(result.records);//数据请求成功后再给总条数赋值
                    $(".one").val(g.pageInfo.page);//数据请求成功后在改变class为one的val值，避免了点击下一页时val值变了却没有获取成功数据
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        })
    },
    //已办单据界面外层容器
    showCompleteOrderView: function () {
        return '             <div class="content-info center-info">' +
            '                    <div class="info-left invoice-info"></div>' +
            '                </div>' +
            '               <div class="content-page">' +
            '                   <div class="record-total">共0条记录</div>' +
            '                    <div class="pege-right">' +
            '                        <a href="#" class="first-page"><首页</a>' +
            '                        <a href="#" class="prev-page"><上一页</a>' +
            '                        <input value="1" class="one">' +
            '                        <a href="#" class="next-page">下一页></a>' +
            '                        <a href="#" class="end-page">尾页></a>' +
            '                    </div>' +
            '               </div>';
    },
    //已办单据界面内容部分的循环
    showCompleteView: function (datas) {
        var html = "";
        $(".invoice-info", '#' + this.renderTo).empty();
        if (datas) {
            for (var i = 0; i < datas.length; i++) {
                var item = datas[i];
                html = $('<div class="info-items">' +
                    '                            <div class="item">' +
                    '                                <span class="flow-text">【' + item.businessCode + '】' + '-' + item.businessName + '</span>' +
                    '                                <span class="item-right general" title="流程结束时间">' + item.endDate + '</span>' +
                    '                            </div>' +
                    '                            <div class="item">' +
                    '                                <div class="remark">'+item.businessModelRemark+
                    '                                </div>'+
                    '                            </div>' +
                    '                            <div class="item item-right">' +
                    '                               <div class="end">' +
                    '                                    <div class="todo-btn look-approve-btn"><i class="look-icon look-approve" title="查看表单"></i><span>查看表单</span></div>' +
                    '                                    <div class="todo-btn todo-end-btn flowInstance-btn"><i class="time-icon flowInstance" title="流程历史"></i><span>流程历史</span></div>' +
                    '                               </div>' +
                    '                            </div>' +
                    '                        </div>');
                html.data(item);
                $(".invoice-info", '#' + this.renderTo).append(html);
            }
        }
    },
    //底部翻页部分
    showPage: function (records) {
        $(".record-total").text("共" + records + "条记录");
    },
    show: function () {
        $("#" + this.renderTo).css("display", "block");
    },
    hide: function () {
        $("#" + this.renderTo).css("display", "none");
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
            g.getTodoOrderData();
        });
        //上一页
        $(".prev-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == 1) {
                return;
            }
            g.pageInfo.page--;
            g.getTodoOrderData();
        });
        //下一页
        $(".next-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == g.pageInfo.total) {
                return;
            }
            g.pageInfo.page++;
            g.getTodoOrderData();
        });
        //尾页
        $(".end-page", "#" + this.renderTo).live("click", function () {//点击尾页时
            if (g.pageInfo.page == g.pageInfo.total) {//如果page=total
                return; //就直接return
            }
            g.pageInfo.page = g.pageInfo.total;//page=total
            g.getTodoOrderData();//请求page=total时的数据
        });
    },
    addEvents: function () {
        var g = this;
        g.pagingEvent();
        g.lookApproveViewWindow();
        g.flowInstanceWindow();
    },
    //点击打开查看表单界面的新页签
    lookApproveViewWindow: function () {
        var g = this;
        $(".look-approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-items");
            var data = itemdom.data();
            var tab = {
                title: "查看表单",
                url: _ctxPath + data.lookUrl + "?id=" + data.businessId,
                id: data.businessId
            };
            g.addTab(tab);
        });
    },
    //点击打开流程历史的新页签
    flowInstanceWindow: function () {
        var g = this;
        $(".flowInstance-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-items");
            var data = itemdom.data();
            Flow.FlowHistory({
                businessId: data.businessId
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
});