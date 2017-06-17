//已办单据
EUI.MyOrderView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    initComponent: function () {
        this.initHtml();
        this.showCompleteOrderView(true);
        this.initChooseDate();
        this.addEvents();
    },
    initHtml: function () {
        var html = this.getMyOrderHtml();
        $("#" + this.renderTo).append(html);
    },
    //拼接我的单据界面的html
    getMyOrderHtml: function () {
        return '<div class="taken-center">' +
            '            <div class="center-top">' +
            '                <div class="top-header">' +
            '                    <span class="worktable">我的单据</span>' +
            '                    <div class="header-right">' +
            '                        <span class="wait-invoices">待办单据</span>' +
            '                        <span class="taken-work taken-invoices active">已办单据</span>' +
            '                        <div class="data">' +
            '                            <div id="dateField"></div>' +
            '                        </div>' +
            '                        <input class="search" type="text" placeholder="输入单据说明关键字查询"/>' +
            '                    </div>' +
            '                </div>' +
            '            </div>' +
            '            <div id="todoOrder-content" class="center-content"></div>' +
            '            <div id="completeOrder-content" class="center-content"></div>' +
            '   </div>';
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
    show: function () {
        $("#" + this.renderTo).css("display", "block");
    },
    hide: function () {
        $("#" + this.renderTo).css("display", "none");
    },
    addEvents: function () {
        var g = this;
        $(".wait-invoices").bind("click", function () {
            $(".taken-invoices").removeClass("active");
            $(this).addClass("active");
            g.showTodoOrderView(true);
        });
        $(".taken-invoices").bind("click", function () {
            $(".wait-invoices").removeClass("active");
            $(this).addClass("active");
            g.showCompleteOrderView(true);
        });
    },
    //已办单据
    showCompleteOrderView: function (visiable) {
        if (visiable) {
            this.showTodoOrderView(false);
            if (this.completeOrderView) {
                this.completeOrderView.show();
                return;
            }
            this.completeOrderView = new EUI.CompleteOrderView({
                renderTo: "completeOrder-content"
            });
        } else if (this.completeOrderView) {
            this.completeOrderView.hide();
        }
    },
    //待办单据
    showTodoOrderView: function (visiable) {
        if (visiable) {
            this.showCompleteOrderView(false);
            if (this.todoOrderView) {
                this.todoOrderView.show();
                return;
            }
            this.todoOrderView = new EUI.TodoOrderView({
                renderTo: "todoOrder-content"
            });
        } else if (this.todoOrderView) {
            this.todoOrderView.hide();
        }
    }
});