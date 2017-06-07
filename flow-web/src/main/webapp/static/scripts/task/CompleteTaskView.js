// 已办部分
EUI.CompleteTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    initComponent: function () {
        this.initHtml();
        this.getCompleteData();
        this.showPage();
        this.addEvents();
    },
    initHtml: function () {
        var html = this.getCompleteTaskHtml();
        $("#" + this.renderTo).append(html);
    },
    //已办界面的外层
    getCompleteTaskHtml: function () {
        return '           <div class="content-info">' +
            '                    <div class="info-left todo-info"></div>' +
            '               </div>' +
            '               <div class="content-page"></div>';
    },
    //已办内容部分的数据调用
    getCompleteData: function (modelId) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTask",
            params: {
                modelId: modelId
            },
            success: function (result) {
                myMask.hide();
                if (result.rows) {
                    g.getCompleteHtml(result.rows);
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
                // '                                <input type="checkbox" class="checkbox">' +
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
                '                                </div>' +
                '                                <span class="item-right">' +
                '                                </span>' +
                '                            </div>' +
                '</div>');
            itemdom.data(items[j]);
            $(".todo-info",'#'+this.renderTo).append(itemdom);
        }
    },
    //底部翻页部分
    showPage: function () {
        var html = "";
        html += '<div class="record-total">共23条记录</div>' +
            '                    <div class="pege-right">' +
            // '                        <a href="#" class="one">1</a>' +
            '                        <a href="#" class="first-page"><首页</a>' +
            '                        <a href="#" class="prev-page"><上一页</a>' +
            '                        <input value="1" class="one">' +
            '                        <a href="#" class="next-page">下一页></a>' +
            '                        <a href="#" class="end-page">尾页></a>' +
            '                     </div>';
        $(".content-page",'#'+this.renderTo).append(html);
    },
    show: function () {
        $("#"+this.renderTo).css("display","block");
    },
    hide: function () {
        $("#"+this.renderTo).css("display", "none");
    },
    addEvents: function () {

    }
});