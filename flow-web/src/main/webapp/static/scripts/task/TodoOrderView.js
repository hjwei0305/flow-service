// 待办单据
//已办单据
EUI.TodoOrderView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    initComponent: function () {
        this.initHtml();
        this.getTodoOrderView();
        this.showPage();
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
            '                <div class="content-page"></div>';
    },
    //已办单据界面内容部分的循环
    showTodoOrderView: function (menu) {
        var html = "";
        for (var i = 0; i < _menu.length; i++) {
            var item = _menu[i];
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
        $(".invoice-info",'#'+this.renderTo).html(html);
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
        $("#"+this.renderTo).css("display", "block");
    },
    hide: function () {
        $("#"+this.renderTo).css("display", "none");
    },
    addEvents: function () {

    }
});