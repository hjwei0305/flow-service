EUI.MainPageView = EUI.extend(EUI.CustomUI, {

    todoTaskView: null,
    completeTaskView: null,
    orderView: null,
    nowView: null,//当前显示的页面

    initComponent: function () {
        this.showTotoTask();
        this.addEvents();

    },
    showTotoTask: function () {
        if (this.todoTaskView) {
            this.todoTaskView.show();
            this.todoTaskView.refresh();
        } else {
            this.todoTaskView = new EUI.TodoTaskView({
                renderTo: "todotask"
            });
        }
        this.nowView = this.todoTaskView;
    },
    hideTodoTask: function () {
        this.todoTaskView && this.todoTaskView.hide();
    },
    showCompleteTask: function () {
        if (this.completeTaskView) {
            this.completeTaskView.show();
            this.completeTaskView.refresh();
        } else {
            this.completeTaskView = new EUI.CompleteTaskView({
                renderTo: "completetask"
            });
        }
        this.nowView = this.completeTaskView;
    },
    hideCompleteTask: function () {
        this.completeTaskView && this.completeTaskView.hide();
    },
    showMyOrder: function () {
        if (this.orderView) {
            this.orderView.show();
            this.orderView.refresh();
        } else {
            this.orderView = new EUI.MyOrderView({
                renderTo: "myorder"
            });
        }
        this.nowView = this.orderView;
    },
    hideMyOrder: function () {
        this.orderView && this.orderView.hide();
    },
    addEvents: function () {
        var g = this;
        $("body").bind({
            "todotask": function () {
                g.hideCompleteTask();
                g.hideMyOrder();
                g.showTotoTask();
            },
            "completetask": function () {
                g.hideTodoTask();
                g.hideMyOrder();
                g.showCompleteTask();
            },
            "myorder": function () {
                g.hideTodoTask();
                g.hideCompleteTask();
                g.showMyOrder();
            }
        });
        window.top.homeView && window.top.homeView.addTabListener("FLOW_PTSY", function (id, win) {
            console.log(win.mainPageView.nowView);
            // win.mainPageView.nowView.refresh();
        });
    }
});