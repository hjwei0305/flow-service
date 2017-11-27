EUI.MainPageView = EUI.extend(EUI.CustomUI, {

    todoTaskView: null,
    completeTaskView: null,
    orderView: null,
    nowView: null,//当前显示的页面

    initComponent: function () {
        this.showTotoTask();
        this.nowView = this.todoTaskView;
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
        $("body").trigger("updatenowview",[this.todoTaskView]);
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
        $("body").trigger("updatenowview",[this.completeTaskView]);
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
    },
    hideMyOrder: function () {
        this.orderView && this.orderView.hide();
    },
    refresh: function () {
        this.nowView && this.nowView.refresh();
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
            },
            "updatenowview": function (event, view) {
                console.log(this);
                g.nowView = view;
            }
        });
        window.top.homeView && window.top.homeView.addTabListener("FLOW_PTSY", function (id, win) {
            win.mainPageView.refresh();
        });
    }
});