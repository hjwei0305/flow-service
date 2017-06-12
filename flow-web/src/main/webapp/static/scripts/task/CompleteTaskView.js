// 已办部分
EUI.CompleteTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    modelId: null,
    pageInfo: {
        page: 1,
        rows: 15,
        total: 1
    },
    initComponent: function () {
        this.initHtml();
        this.getCompleteData();
        this.addEvents();
    },
    initHtml: function () {
        var html =this.getCompleteTaskHtml();
        $("#" + this.renderTo).append(html);
    },
    //已办界面的外层容器部分
    getCompleteTaskHtml: function () {
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
    //已办内容部分的数据调用
    getCompleteData: function (modelId) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowHistory/listFlowHistory",
            params: {
                modelId: modelId,
                S_createdDate: "DESC",
                page: this.pageInfo.page,
                rows: this.pageInfo.rows
            },
            success: function (result) {
                myMask.hide();
                if (result.rows) {
                    g.pageInfo.page = result.page;
                    g.pageInfo.total = result.total;
                    g.getCompleteHtml(result.rows);
                    g.showPage(result.records);//数据请求成功后再给总条数赋值
                    $(".one").val(g.pageInfo.page);//数据请求成功后在改变class为one的val值，避免了点击下一页时val值变了却没有获取成功数据
                }
            },
            failurle: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        })
    },
    //已办内容部分的循环
    getCompleteHtml: function (items) {
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
                '                                <span class="flow-text">' + items[j].flowName+'_'+items[j].flowTaskName +':'+'<span class="digest">111</span></span>' +
                '                            </div>' +
                '                            <div class="item">' +
                '                                <div class="end">' +
                '                                    <div class="todo-btn"><i class="backout-icon" title="撤销"></i></div>' +
                '                                    <div class="todo-btn"><i class="look-icon look-approve" title="查看表单"></i></div>' +
                '                                    <div class="todo-btn"><i class="time-icon flowInstance" title="流程历史"></i></div>' +
                '                                </div>' +
                '                                <span class="item-right task-item-right">' +
                '                                    <div class="userName">发起人：' + items[j].creatorName + '</div>' +
                '                                    <div class="todo-date"><i class="time-icon" title="流程历史"></i>处理时间：'+items[j].actEndTime+'</div>' +
                '                                </span>' +
                '                            </div>' +
                '</div>');
            itemdom.data(items[j]);
            $(".todo-info",'#'+this.renderTo).append(itemdom);
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
            g.getCompleteData();
        });
        //上一页
        $(".prev-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == 1) {
                return;
            }
            g.pageInfo.page--;
            g.getCompleteData();
        });
        //下一页
        $(".next-page", "#" + this.renderTo).live("click", function () {
            if (g.pageInfo.page == g.pageInfo.total) {
                return;
            }
            g.pageInfo.page++;
            g.getCompleteData();
        });
        //尾页
        $(".end-page", "#" + this.renderTo).live("click", function () {//点击尾页时
            if (g.pageInfo.page == g.pageInfo.total) {//如果page=total
                return; //就直接return
            }
            g.pageInfo.page = g.pageInfo.total;//page=total
            g.getCompleteData();//请求page=total时的数据
        });
    },
    show: function () {
        $("#"+this.renderTo).css("display","block");
    },
    hide: function () {
        $("#"+this.renderTo).css("display", "none");
    },
    addEvents: function () {
        var g=this;
        g.pagingEvent();
    }
});