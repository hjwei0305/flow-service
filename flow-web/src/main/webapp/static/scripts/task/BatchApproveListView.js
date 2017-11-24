/**
 * 批量审批列表界面
 */
EUI.BatchApproveListView = EUI.extend(EUI.CustomUI, {
    renderTo: "batchlist",
    params: {
        page: 1,
        rows: 15,
        records:0,
        modelId: null,
        S_createdDate: "DESC",
        Quick_value: null
    },
    returnBack: null,

    initComponent: function () {
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            title: "我的工作 > 批量处理",
            layout: "border",
            items: [{
                xtype: "ToolBar",
                height: 30,
                padding: 0,
                border: false,
                region: "north",
                items: this.initToolBar()
            }, {
                xtype: "Container",
                region: "center",
                padding: 0,
                html: '<div class="info-left todo-info"></div><div class="load-more"><span>获取更多</span></div>'
                + '<div class="empty-data"><div class="not-data-msg">------------您当前没有可批量处理的单据------------</div></div>'
            }
            ]
        })
        ;
        this.dataDom = $(".todo-info", "#" + this.renderTo);
        this.emptyDom = $(".empty-data", "#" + this.renderTo);
        this.loadMoreDom = $(".load-more", "#" + this.renderTo);
        this.getData();
        this.addEvents();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "Button",
            title: "全选",
            id: "batchBtn",
            handler: function () {
                g.setBatchSelect();
            }
        }, {
            xtype: "Button",
            title: "批量处理",
            handler: function () {
                var checkedItems = $(".work_checkBox input:checked", "#" + g.renderTo);
                if (checkedItems.length == 0) {
                    EUI.ProcessStatus({success: false, msg: "请选择需要批量处理的待办项"});
                    return;
                }
                var taskIds = [];
                for (var i = 0; i < checkedItems.length; i++) {
                    taskIds.push($(checkedItems[i]).parent().parent().attr("id"));
                }
                g.hide();
                $("body").append('<div id="batchuser"></div>');
                new EUI.BatchApproveUserView({
                    renderTo: "batchuser",
                    taskIds: taskIds.join(","),
                    afterSubmit: function () {
                        g.show();
                        g.params.page = 1;
                        g.getData();
                    },
                    returnBack: function () {
                        this.remove();
                        g.show();
                    }
                });
            }
        }, {
            xtype: "ComboBox",
            name: "businessModelName",
            displayText: "筛选",
            async: false,
            store: {
                url: _ctxPath + "/flowTask/listFlowTaskHeader"
            },
            afterSelect: function (data) {
                g.setBatchSelect();
                g.params.modelId = data.data.businessModeId;
                g.params.page = 1;
                g.getData();
            },
            afterClear: function () {
                g.setBatchSelect();
                g.params.page = 1;
                g.params.modelId = null;
                g.getData();
            }
        }, {
            xtype: "SearchBox",
            name: "Quick_value",
            onSearch: function (value) {
                g.setBatchSelect();
                g.params.page = 1;
                g.params.Quick_value = value;
                g.getData();
            }
        }, {
            xtype: "Button",
            title: "返回",
            handler: function () {
                g.boxCmp.remove();
                g.returnBack && g.returnBack.call(g);
            }
        }, "->", {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-eui-leaf' style='vertical-align: middle;color:#3671cf;'></i><span>已办工作</span>",
            onClick: function () {
                g.hide();
                $("body").trigger("completetask");
            }
        }, {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-sys-syslog' style='vertical-align: middle;color:#3671cf;'></i><span>我的单据</span>",
            onClick: function () {
                g.hide();
                $("body").trigger("myorder");
            }
        }];
    },
    setBatchSelect: function () {
        var btn = EUI.getCmp("batchBtn");
        var title = btn.title;
        if (title == "全选") {
            $(".work_checkBox input", "#" + g.renderTo).attr("checked", true);
            btn.setTitle("取消");
        } else {
            $(".work_checkBox input", "#" + g.renderTo).removeAttr("checked");
            btn.setTitle("全选");
        }
    },
    getData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listCanBatchApprovalFlowTask",
            params: this.params,
            success: function (result) {
                g.params.records = result.records;
                if (result.records == 0) {
                    g.params.page = 1;
                    g.showEmptyWorkInfo();
                } else if (result.rows.length > 0) {
                    g.params.page = result.page;
                    g.showData(result.rows);
                } else {
                    if (g.params.page > 0) {
                        g.params.page--;
                    }
                    EUI.ProcessStatus({
                        success: true,
                        msg: "没有更多数据"
                    });
                }
                myMask.hide();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (g.params.page > 0) {
                    g.params.page--;
                }
            }
        });
    },
    showContent: function () {
        this.dataDom.show();
        if (this.params.records > 10) {
            this.loadMoreDom.show();
        } else {
            this.loadMoreDom.hide();
        }
        this.emptyDom.hide();
        if (this.params.page == 1) {
            this.dataDom.empty();
        }
    },
    //当页面没有数据时的显示内容
    showEmptyWorkInfo: function () {
        this.emptyDom.show();
        this.dataDom.hide();
        this.loadMoreDom.hide();
        this.dataDom.empty();
    }
    ,
    showData: function (data) {
        var html = "";
        this.showContent();
        for (var i = 0; i < data.length; i++) {
            var itemdata = data[i];
            html += '<div class="work_table_box" id="' + itemdata.id + '">' +
                '    <div class="work_checkBox">' +
                '        <input type="checkbox">' +
                '    </div>' +
                '    <div class="work_info">' +
                '        <div class="work_info_left">' +
                '            <div>' + itemdata.flowName + '_' + itemdata.taskName + '</div>' +
                '            <div>' + itemdata.flowInstance.businessCode + '-' + itemdata.flowInstance.businessModelRemark + '</div>' +
                '        </div>' +
                '        <div class="work_info_right">' +
                '            <div>发起人 : ' + itemdata.creatorName +
                '            </div>' +
                '            <div>' + this.countDate(itemdata.createdDate) + '</div>' +
                '        </div>' +
                '    </div>' +
                '</div>';
        }
        this.dataDom.append(html);
        EUI.resize(this.boxCmp);
    },
    //计算时间几天前
    countDate: function (startTime) {
        if (!startTime) {
            return;
        }
        var g = this;
        var date = new Date();
        var endTime = date.getTime();
        startTime = startTime.replace(new RegExp("-", "gm"), "/");
        startTime = new Date(startTime).getTime();
        var time = endTime - startTime;
        if (time <= 60000) {//如果结束时间小于开始时间
            return "刚刚";
        } else {
            //计算出相差天数
            var days = Math.floor(time / (24 * 3600 * 1000));
            if (days > 0) {
                return days + '天前';
            }
            //计算出小时数
            var leave1 = time % (24 * 3600 * 1000);   //计算天数后剩余的毫秒数
            if (leave1 == 0) {//如果leave1=0就不需要在做计算，直接把0赋给hours
                return hours = 0;
            } else {
                var hours = Math.floor(leave1 / (3600 * 1000));
                if (hours > 0) {
                    return hours + '小时前';
                }
            }
            //计算相差分钟数
            var leave2 = leave1 % (3600 * 1000);        //计算小时数后剩余的毫秒数
            var minutes = Math.floor(leave2 / (60 * 1000));
            return minutes + '分钟前';
        }
    },
    show: function () {
        this.boxCmp.show();
    }
    ,
    hide: function () {
        this.boxCmp.hide();
    },
    refresh: function (params) {
        this.params.page = 1;
        EUI.apply(this.params, params);
        this.getData();
    }
    ,
    addEvents: function () {
        var g = this;
        $(".not-data-msg", "#" + this.renderTo).bind("click", function () {
            g.params.page = 1;
            g.getData();
        });
        this.loadMoreDom.click(function () {
            g.params.page += 1;
            g.getData();
        });
    }
});