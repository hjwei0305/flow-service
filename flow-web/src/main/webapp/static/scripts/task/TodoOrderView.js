// 待办单据
//已办单据
EUI.TodoOrderView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    pageInfo: {
        page: 1,
        rows: 15,
        total: 1
    },
    initComponent: function () {
        this.initHtml();
        this.getTodoOrderView();
        this.addEvents();
    },
    initHtml: function () {
        var html = this.showTodoOrderHtml;
        $("#" + this.renderTo).append(html);
    },
    //已办单据界面的数据调用
    getTodoOrderView: function () {
        var g = this;
        g.showTodoOrderView(this._menu);
    },
    //已办单据界面外层
    showTodoOrderHtml: function () {
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
    showTodoOrderView: function (menu) {
        var html = "";
        for (var i = 0; i < _menu.length; i++) {
            var item = _menu[i];
            html += '<div class="info-items">' +
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
        $(".invoice-info",'#'+this.renderTo).html(html);
    },
    //底部翻页部分
    showPage: function (records) {
        $(".record-total").text("共" + records + "条记录");
    },
    show: function () {
        $("#"+this.renderTo).css("display", "block");
    },
    hide: function () {
        $("#"+this.renderTo).css("display", "none");
    },
    addEvents: function () {

    }
});