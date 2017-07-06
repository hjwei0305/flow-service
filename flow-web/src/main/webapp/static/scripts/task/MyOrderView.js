//已办单据
EUI.MyOrderView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    currentItem:"wait-invoices",
    initComponent: function () {
        EUI.Container({
            renderTo:this.renderTo,
            layout:"border",
            items:[{
                xtype:"Container",
                region : "north",
                height:40,
                border:false,
                padding:0,
                isOverFlow:false,
                html:this.getOrderTopHtml()
            },{
                xtype:"Container",
                region : "center",
                border:false,
                padding:0,
                style:{
                   "border-top":"1px solid #eee"
                },
                html:this.getOrderCenterHtml()
            }]
        });
        this.showTodoOrderView(true);
        this.initChooseDate();
        this.initOrderSearchBox();
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
            '                    <div class="header-left">' +
            '                        <span class="wait-invoices active">待办单据</span>' +
            '                        <span class="taken-work taken-invoices">已办单据</span>' +
            '                        <div class="data">' +
            '                            <div id="dateField"></div>' +
            '                        </div>' +
            '                    </div>' +
            '                    <div class="header-right">' +
            // '                        <input class="search" type="text" placeholder="输入单据说明关键字查询"/>' +
            '                    </div>' +
            '                </div>' +
            '            </div>' +
            '            <div id="todoOrder-content" class="center-content"></div>' +
            '            <div id="completeOrder-content" class="center-content"></div>' +
            '   </div>';
    },
    getOrderTopHtml:function () {
        return '            <div class="center-top">' +
            '                <div class="top-header invoices-header">' +
            '                    <div class="header-left">' +
            '                        <span class="wait-invoices active">待办单据</span>' +
            '                        <span class="taken-work taken-invoices">已办单据</span>' +
            '                        <div class="data">' +
            '                            <div id="dateField"></div>' +
            '                        </div>' +
            '                    </div>' +
            '                    <div id="order-searchBox" class="header-right invoices-right">' +
            // '                        <input class="search" type="text" placeholder="输入单据说明关键字查询"/>' +
            '                    </div>' +
            '                </div>' +
            '            </div>';
    },
    getOrderCenterHtml:function () {
        return '         <div id="todoOrder-content" class="center-content"></div>' +
            '            <div id="completeOrder-content" class="center-content"></div>';
    },
    //我的单据中的日历
    initChooseDate: function () {
        var g=this;
        var start=new Date();
        g.endTime=start.format("yyyy-MM-dd");
        g.startTime=new Date(start.setDate(start.getDate()+(-6))).format("yyyy-MM-dd");
        EUI.FieldGroup({
            renderTo: "dateField",
            itemspace: 10,
            width: 266,
            items: [{
                xtype: "DateField",
                name:"startDate",
                format: "Y-m-d",
                height: 14,
                width: 100,
                allowBlank:false,
                value:g.startTime,
                beforeSelect:function (data) {
                    var end=EUI.getCmp("dateField").getCmpByName("endDate").getValue();
                    var result=g.checkDate(data.nowValue,end);
                    if(result){
                        return true;
                    }else {
                        g.message("起始日期不能大于截止日期");
                        return false;
                    }
                },
                afterSelect:function () {
                    g.refreshCurrentData();
                }
            }, "到", {
                xtype: "DateField",
                name:"endDate",
                format: "Y-m-d",
                height: 14,
                width: 100,
                allowBlank:false,
                value:g.endTime,
                beforeSelect:function (data) {
                    var start=EUI.getCmp("dateField").getCmpByName("startDate").getValue();
                    var result=g.checkDate(start,data.nowValue);
                    if(result){
                        return true;
                    }else {
                        g.message("截止日期不能小于起始日期");
                        return false;
                    }
                },
                afterSelect:function () {
                    g.refreshCurrentData();
                }
            }],
            getCmpByName : function() {
                var name = arguments[0];
                var items = this.items;
                return this.doGetCmp(items, name);
            },
            doGetCmp : function() {
                var items = arguments[0], name = arguments[1];
                if (items) {
                    for (var i = 0; i < items.length; i++) {
                        var item = EUI.getCmp(items[i]);
                        if (item.isFormField && item.name == name) {
                            return item;
                        } else {
                            var cmp = this.doGetCmp(item.items, name);
                            if (cmp != null) {
                                return cmp;
                            }
                        }
                    }
                }
                return null;
            }
        })
    },
    //搜索框
    initOrderSearchBox:function () {
        var g=this;
        EUI.SearchBox({
            renderTo:"order-searchBox",
            width:198,
            displayText:"输入单据说明关键字查询",
            editable:true,
            colon:false,
            onSearch:function (data) {
                g.quickSearch();
            }
        })
    },
    addEvents: function () {
        var g = this;
        $(".wait-invoices").bind("click", function () {
            g.currentItem="wait-invoices";
            $(".taken-invoices").removeClass("active");
            $(this).addClass("active");
            $("#order-searchBox input").val("").focus();
            g.showTodoOrderView(true);
        });
        $(".taken-invoices").bind("click", function () {
            g.currentItem="taken-invoices";
            $(".wait-invoices").removeClass("active");
            $(this).addClass("active");
            $("#order-searchBox input").val("").focus();
            g.showCompleteOrderView(true);
            $("#completeOrder-content").css("height", "100%");
        });
    },
    //已办单据
    showCompleteOrderView: function (visiable) {
        if (visiable) {
            this.showTodoOrderView(false);
            if (this.completeOrderView) {
                this.completeOrderView.show();
                this.completeOrderView.getTodoOrderData();
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
                this.todoOrderView.getTodoOrderData();
                return;
            }
            this.todoOrderView = new EUI.TodoOrderView({
                renderTo: "todoOrder-content"
            });
        } else if (this.todoOrderView) {
            this.todoOrderView.hide();
        }
    },
    show: function () {
        $("#" + this.renderTo).css("display", "block");
    },
    hide: function () {
        $("#" + this.renderTo).css("display", "none");
    },
    refreshCurrentData:function () {
        var g=this;
        var searchText=EUI.getCmp("order-searchBox").getValue();
        switch (g.currentItem) {
            case "wait-invoices":
                g.todoOrderView.searchName=searchText;
                g.todoOrderView.getTodoOrderData();
                break;
            case "taken-invoices":
                g.completeOrderView.searchName=searchText;
                g.completeOrderView.getTodoOrderData();
                break;
            default:
                break;
        }
    },
    checkDate:function (sdate,edate) {
        var g=this;
        var start = new Date(sdate.replace("-", "/").replace("-", "/"));
        var end = new Date(edate.replace("-", "/").replace("-", "/"));
        if ((sdate || edate )&& start > end) {
            return false;
        }
        return true;
    },
    message: function (msg) {
        var g = this;
        var message = EUI.MessageBox({
            border: true,
            title: "提示",
            showClose: true,
            msg: msg,
            buttons: [{
                title: "确定",
                iconCss:"ecmp-common-ok",
                handler: function () {
                    message.remove();
                }
            }]
        });
    },
    quickSearch:function () {
        var g=this;
        var start=EUI.getCmp("dateField").getCmpByName("startDate").getValue();
        var end=EUI.getCmp("dateField").getCmpByName("endDate").getValue();
        var result=g.checkDate(start,end);
        if(result){
            g.refreshCurrentData();
        }else {
            g.message("截止日期不能小于起始日期");
        }
    }
});