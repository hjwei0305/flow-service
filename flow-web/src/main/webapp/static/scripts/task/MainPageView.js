EUI.MainPageView = EUI.extend(EUI.CustomUI, {

    todoTaskView: null,
    completeTaskView: null,
    orderView: null,

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
    }
});