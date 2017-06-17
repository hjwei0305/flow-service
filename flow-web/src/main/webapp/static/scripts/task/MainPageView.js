EUI.MainPageView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    data: null,
    menudata: null,
    dataWait: null,
    menu: null,
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            data: this.data,
            menudata: this.menudata,
            dataWait: this.dataWait,
            menu: this.menu,
            html: this.getHtml()
        });
        this.showTodoTaskView(true);
        this.addEvents();
    },
    //拼接html内容
    getHtml: function () {
        return this.getTodoHtml();
    },
    //拼接已办界面的html
    getTodoHtml: function () {
        return ' <div class="taken-top">' +
            '            <div class="top-header">' +
            '                    <span class="wait-work task-work active">待办工作</span>' +
            '                    <span class="taken-work history-work">已办工作</span>' +
            '                    <span class="workOrder">我的单据</span>' +
            '                <div class="header-right">' +
            '                    <input class="search" type="text" placeholder="输入单据说明关键字查询"/>' +
            // '                    <span class="btn">待办项批量处理</span>' +
            '                </div>' +
            '            </div>' +
            '            <div id="todoTask-content" class="top-content"> </div>' +
            '            <div id="completeTask-content" class="top-content"> </div>' +
            '            <div id="order-content" class="top-content"> </div>' +
            '   </div>';

    },
    //添加事件
    addEvents: function () {
        var g = this;
        $(".task-work").bind("click", function () {
            $(this).siblings().removeClass("active");
            $(this).addClass("active");
            g.showTodoTaskView(true);
        });
        $(".history-work").bind("click", function () {
            $(this).siblings().removeClass("active");
            $(this).addClass("active");
            g.showCompleteTaskView(true);
            $("#completeTask-content").addClass("rim");
        });
        $(".workOrder").bind("click", function () {
            $(this).siblings().removeClass("active");
            $(this).addClass("active");
            g.showMyOrderView(true);
            $("#order-content").addClass("rim");
        });
    },
    //待办界面
    showTodoTaskView: function (visiable) {
        if (visiable) {
            this.showCompleteTaskView(false);
            this.showMyOrderView(false);
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
            this.showMyOrderView(false);
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
    //我的单据
    showMyOrderView: function (visiable) {
        if (visiable) {
            this.showTodoTaskView(false);
            this.showCompleteTaskView(false);
            if (this.myOrderView) {
                this.myOrderView.show();
                return;
            }
            this.myOrderView = new EUI.MyOrderView({
                renderTo: "order-content"
            });
        } else if (this.myOrderView) {
            this.myOrderView.hide();
        }
    }
});