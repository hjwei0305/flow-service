EUI.MainPageView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    data: null,
    menudata: null,
    dataWait: null,
    menu:null,
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            data: this.data,
            menudata: this.menudata,
            dataWait: this.dataWait,
            menu:this.menu,
            html: this.getHtml()
        });
        this.showTodoTaskView(true);
        this.showCompleteOrderView(true);
        this.initChooseDate();
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
            '            <div id="todoTask-content" class="top-content"> </div>' +
            '            <div id="completeTask-content" class="top-content"> </div>' +
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
            '            <div id="todoOrder-content" class="center-content"></div>' +
            '            <div id="completeOrder-content" class="center-content"></div>' +
            '   </div>';
    },
    //添加事件
    addEvents: function () {
        var g = this;
        $(".task-work").bind("click",function () {
            $(".history-work").removeClass("active");
            $(this).addClass("active");
            g.showTodoTaskView(true);
        });
        $(".history-work").bind("click",function () {
            $(".task-work").removeClass("active");
            $(this).addClass("active");
            g.showCompleteTaskView(true);
            $("#completeTask-content").addClass("rim");
        });
        $(".wait-invoices").bind("click",function () {
            $(".taken-invoices").removeClass("active");
            $(this).addClass("active");
            g.showTodoOrderView(true);
            $("#todoOrder-content").addClass("rim");
        });
        $(".taken-invoices").bind("click",function () {
            $(".wait-invoices").removeClass("active");
            $(this).addClass("active");
            g.showCompleteOrderView(true);
        });
    },
    //待办界面
    showTodoTaskView: function (visiable) {
        if (visiable) {
            this.showCompleteTaskView(false);
            if (this.todoTaskView) {
                this.todoTaskView.show();
                return;
            }
            this.todoTaskView = new EUI.TodoTaskView({
                renderTo: "todoTask-content"
            });
        } else if (this.todoTaskView) {
            this.todoTaskView.hide();
        }
    },
    //已办界面
    showCompleteTaskView: function (visiable) {
        if (visiable) {
            this.showTodoTaskView(false);
            if (this.completeTaskView) {
                this.completeTaskView.show();
                return;
            }
            this.completeTaskView = new EUI.CompleteTaskView({
                renderTo: "completeTask-content"
            });
        } else if (this.completeTaskView) {
            this.completeTaskView.hide();
        }
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
    }
});