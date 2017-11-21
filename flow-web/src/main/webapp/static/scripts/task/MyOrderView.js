//我的单据
EUI.MyOrderView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    nowOrderCmp: null,

    initComponent: function () {
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            title: "我的工作 > 我的单据",
            items: [{
                xtype: "ToolBar",
                region: "north",
                height: 33,
                padding: 0,
                border: false,
                items: this.initToolBar()
            }, {
                xtype: "Container",
                region: "center",
                border: true,
                padding: 0,
                html: this.getOrderCenterHtml()
            }]
        });
        this.initDate();
        this.showProcessingOrderView(true);
        this.addEvents();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "Label",
            style: {
                "margin-top": "5px"
            },
            content: "<span class='wait-invoices active'>流程中</span><span class='taken-work taken-invoices'>已完成</span>"
        }, {
            xtype: "FieldGroup",
            itemspace: 10,
            width: 350,
            items: [{
                xtype: "DateField",
                name: "startDate",
                id: "startDate",
                format: "Y-m-d",
                height: 14,
                width: 160,
                allowBlank: false,
                value: g.startTime,
                beforeSelect: function (data) {
                    var end = EUI.getCmp("endDate").getValue();
                    var result = g.checkDate(data.nowValue, end);
                    if (result) {
                        return true;
                    } else {
                        g.message("起始日期不能大于截止日期");
                        return false;
                    }
                },
                afterSelect: function (data) {
                    g.nowOrderCmp.refresh({
                        Q_GE_startDate__Date: data
                    });
                }
            }, "到", {
                xtype: "DateField",
                name: "endDate",
                id: "endDate",
                format: "Y-m-d",
                height: 14,
                width: 160,
                allowBlank: false,
                value: g.endTime,
                beforeSelect: function (data) {
                    var start = EUI.getCmp("startDate").getValue();
                    var result = g.checkDate(start, data.nowValue);
                    if (result) {
                        return true;
                    } else {
                        g.message("截止日期不能小于起始日期");
                        return false;
                    }
                },
                afterSelect: function (data) {
                    g.nowOrderCmp.refresh({
                        Q_LE_endDate__Date: data
                    });
                }
            }]
        }, {
            xtype: "SearchBox",
            name: "Quick_value",
            id:"searchbox",
            onSearch: function (value) {
                g.nowOrderCmp.refresh({
                    Quick_value: value
                });
            }
        }, "->", {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-eui-leaf' style='vertical-align: middle;color:#3671cf;'></i><span>待办工作</span>",
            onClick: function () {
                $("body").trigger("todotask");
            }
        }, {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-sys-syslog' style='vertical-align: middle;color:#3671cf;'></i><span>已办工作</span>",
            onClick: function () {
                $("body").trigger("completetask");
            }
        }];
    },
    getOrderTopHtml: function () {
        return '            <div class="center-top">' +
            '                <div class="top-header invoices-header">' +
            '                    <div class="header-left">' +
            '                        <span class="wait-invoices active">' + this.lang.orderInFlowText + '</span>' +
            '                        <span class="taken-work taken-invoices">' + this.lang.orderCompleteText + '</span>' +
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
    getOrderCenterHtml: function () {
        return '<div id="todoOrder-content" class="center-content"></div>' +
            '<div id="completeOrder-content" class="center-content"></div>';
    },
    //我的单据中的日历
    initDate: function () {
        var start = new Date();
        this.endDate = start.format("yyyy-MM-dd");
        this.startDate = new Date(start.setDate(start.getDate() - 30)).format("yyyy-MM-dd");
        this.setDate(this.startDate,this.endDate);
    },
    setDate: function (startDate, endDate) {
        EUI.getCmp("startDate").setValue(startDate);
        EUI.getCmp("endDate").setValue(endDate);
    },
    addEvents: function () {
        var g = this;
        $(".wait-invoices").bind("click", function () {
            $(".taken-invoices").removeClass("active");
            $(this).addClass("active");
            g.showProcessingOrderView(true);
            var params = g.processingOrderView.params;
            EUI.getCmp("searchbox").setValue(params.Quick_value);
            g.setDate(params.Q_GE_startDate__Date,params.Q_LE_endDate__Date);
        });
        $(".taken-invoices").bind("click", function () {
            $(".wait-invoices").removeClass("active");
            $(this).addClass("active");
            g.showCompleteOrderView(true);
            var params = g.completeOrderView.params;
            EUI.getCmp("searchbox").setValue(params.Quick_value);
            g.setDate(params.Q_GE_startDate__Date,params.Q_LE_endDate__Date);
        });
    },
    //已办单据
    showCompleteOrderView: function (visiable) {
        if (visiable) {
            this.showProcessingOrderView(false);
            if (this.completeOrderView) {
                this.completeOrderView.show();
                this.completeOrderView.getData();
                return;
            }
            this.completeOrderView = new EUI.CompleteOrderView({
                renderTo: "completeOrder-content",
                startDate: this.startDate,
                endDate: this.endDate
            });
            this.nowOrderCmp = this.completeOrderView;
        } else if (this.completeOrderView) {
            this.completeOrderView.hide();
        }
    },
    //待办单据
    showProcessingOrderView: function (visiable) {
        if (visiable) {
            this.showCompleteOrderView(false);
            if (this.processingOrderView) {
                this.processingOrderView.show();
                this.processingOrderView.getData();
                return;
            }
            this.processingOrderView = new EUI.ProcessingOrderView({
                renderTo: "todoOrder-content",
                startDate: this.startDate,
                endDate: this.endDate
            });
            this.nowOrderCmp = this.processingOrderView;
        } else if (this.processingOrderView) {
            this.processingOrderView.hide();
        }
    },
    show: function () {
        this.boxCmp.show();
    },
    hide: function () {
        this.boxCmp.hide();
    },
    refresh: function () {
        this.nowOrderCmp.refresh();
    },
    checkDate: function (sdate, edate) {
        var g = this;
        var start = new Date(sdate.replace("-", "/").replace("-", "/"));
        var end = new Date(edate.replace("-", "/").replace("-", "/"));
        if ((sdate || edate ) && start > end) {
            return false;
        }
        return true;
    },
    message: function (msg) {
        EUI.ProcessStatus({msg: msg, success: false});
    }
})
;