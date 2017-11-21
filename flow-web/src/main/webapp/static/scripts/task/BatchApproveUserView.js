/**
 * 批量审批列表界面
 */
EUI.BatchApproveUserView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    taskIds: null,
    afterSubmit: null,
    initComponent: function () {
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            items: [{
                xtype: "ToolBar",
                height: 30,
                padding: 0,
                border: false,
                items: this.initToolBar()
            }, {
                xtype: "Container",
                height: "auto",
                padding: 0,
                border: true,
                style: {
                    "border-radius": "2px"
                },
                html: '<div class="info-left todo-info"></div>'
            }]
        });
        this.loadData();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "Label",
            content: "我的工作>批量处理>选择下步执行人"
        }, "->", {
            xtype: "Button",
            title: "返回",
            handler: function () {
                g.boxCmp.remove();
            }
        }, {
            xtype: "Button",
            title: "确定",
            selected: true,
            handler: function () {

            }
        }];
    },
    loadData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowClient/getSelectedCanBatchNodesInfo",
            params: {
                taskIds: this.taskIds
            },
            success: function (result) {
                g.showData(result.data);
                myMask.hide();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    showBatchUserPage: function () {

    },


    showData: function (data) {
        var html = "";
        for (var j = 0; j < data.length; j++) {
            var itemdata = data[j];
            html += '<div class="process_box">' +
                '<div class="task_type_title">' + itemdata.name + '</div>' +
                this.getNextNodesHtml(itemdata.executorSet)
                '</div>';
        }
        $(".todo-info", '#' + this.renderTo).append(html);
        EUI.resize(this.boxCmp);
    },
    getNextNodesHtml: function (data) {
        var html = "";
        for (var i = 0; i < data.length; i++) {
            html += '<div class="task_info">' +
                '        <div class="task_info_title">'+data[i].name+'</div>' +
                '        <div class="task_info_opinion">' +
                '            <div class="operator_title">意&nbsp;&nbsp;&nbsp;见：</div>' +
                '            <div class="info_right">同意</div>' +
                '        </div>' +
                '        <div class="task_info_operator">' +
                '            <div class="operator_title">执行人：</div>' +
                '            <div class="operator_info">' +
                '                <div>' +
                '                    <div class="info_radio ecmp-eui-radio"></div>' +
                '                    <div>宋天成 四川虹信软件股份有限公司-金融创新部-产品经理</div>' +
                '                </div>' +
                '                <div>' +
                '                    <div class="info_radio ecmp-eui-radio"></div>' +
                '                    <div>宋天成 四川虹信软件股份有限公司-金融创新部-产品经理</div>' +
                '                </div>' +
                '                <div>' +
                '                    <div class="info_radio ecmp-eui-radio"></div>' +
                '                    <div>宋天成 四川虹信软件股份有限公司-金融创新部-产品经理</div>' +
                '                </div>' +
                '                <div>' +
                '                    <div class="info_radio ecmp-eui-radio"></div>' +
                '                    <div>宋天成 四川虹信软件股份有限公司-金融创新部-产品经理</div>' +
                '                </div>' +
                '                <div>' +
                '                    <div class="info_radio ecmp-eui-radio"></div>' +
                '                    <div>宋天成 四川虹信软件股份有限公司-金融创新部-产品经理</div>' +
                '                </div>' +
                '            </div>' +
                '        </div>' +
                '    </div>';
        }
        return html;
    },
    submit: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在提交,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTaskWithAllCount",
            params: this.params,
            success: function (result) {
                myMask.hide();
                g.afterSubmit && g.afterSubmit.call(g);
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    }
});