/*
 * 查看业务申请单
 * */
EUI.ReadyOnlyApproveView = EUI.extend(EUI.CustomUI, {
    id: EUI.util.getUrlParam("id"),
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            style: {
                "background": "#fff"
            },
            items: [{
                xtype: "Container",
                layout: "border",
                border: false,
                padding: 0,
                width: 650,
                height: 530,
                id: "lookApprove",
                itemspace: 0,
                style: {
                    "background": "#fff",
                    "border": "1px solid #b5b8c8",
                    "margin": "0 auto"
                },
                items: [this.initTop(), this.initCenter()]
            }]

        });
        this.showFindData();
    },
    initTop: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "north",
            id: "top",
            border: false,
            height: 50,
            padding:0,
            html: "<div class='flow-approve-title'>" +
            "<div class='bills-num'></div>" +
            "<div class='title'>业务申请单</div>" +
            "<div class='created-date'></div></div>"
        }
    },
    initCenter: function () {
        var g = this;
        return {
            xtype: "FormPanel",
            id: "lookBill",
            border: false,
            region: "center",
            padding:0,
            itemspace: 10,
            items: [{
                xtype:"Container",
                height:30,
                padding:0,
                style:{
                    "border-top":"1px solid #b5b8c8",
                    "border-bottom":"1px solid #b5b8c8"
                },
                html: "<div class='approve-title'>申请概要</div>"
            },{
                xtype: "TextField",
                title: "<span class='name'>业务类型</span>",
                name: "name",
                width: 413,
                id: "name",
                labelWidth: 100,
                readonly:true,
                colon: false,
                style:{
                    "padding-left":"20px"
                }
            }, {
                xtype: "TextField",
                title: "<span class='name'>申请说明</span>",
                name: "applyCaption",
                width: 413,
                id: "applyCaption",
                readonly:true,
                colon: false,
                labelWidth: 100,
                style:{
                    "padding-left":"20px"
                }
            },{
                xtype:"Container",
                height:30,
                padding:0,
                style:{
                    "border-top":"1px solid #b5b8c8",
                    "border-bottom":"1px solid #b5b8c8"
                },
                html: "<div class='approve-title'>申请详情</div>"
            }, {
                xtype: "NumberField",
                title: "<span class='name'>单价</span>",
                name: "unitPrice",
                width: 413,
                id: "unitPrice",
                readonly:true,
                colon: false,
                allowNegative : false,//不允许输入负数
                allowChar : "0123456789",// 允许输入的数字
                labelWidth: 100,
                style:{
                    "padding-left":"20px"
                }
            }, {
                xtype: "NumberField",
                title: "<span class='name'>数量</span>",
                name: "count",
                width: 413,
                id: "count",
                colon: false,
                allowNegative : false,//不允许输入负数
                allowChar : "0123456789",// 允许输入的数字
                readonly:true,
                labelWidth: 100,
                style:{
                    "padding-left":"20px"
                }
            }, {
                xtype: "NumberField",
                title: "<span class='name'>金额</span>",
                name: "sum",
                width: 413,
                id: "sum",
                allowNegative : false,//不允许输入负数
                allowChar : "0123456789",// 允许输入的数字
                readonly:true,
                colon: false,
                labelWidth: 100,
                style:{
                    "padding-left":"20px"
                }
            }, {
                xtype: "TextArea",
                title: "<span class='name'>备注说明</span>",
                id: "workCaption",
                name: "workCaption",
                readonly:true,
                width: 413,
                labelWidth: 100,
                height: 170,
                colon: false,
                style:{
                    "padding-left":"20px"
                }
            }]
        }
    },
    showFindData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载，请稍后..."
        });
        EUI.Store({
            url: _ctxPath + "/defaultBusinessModel2/getApproveBill2",
            params: {
                id: EUI.util.getUrlParam("id")
            },
            success: function (result) {
                myMask.hide();
                if (result.success) {
                    EUI.getCmp("lookBill").loadData(result.data);
                    g.showBillsTitleData(result.data);
                } else {
                    EUI.ProcessStatus(result);
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        })
    },
    showBillsTitleData:function (data) {
        $(".bills-num").text("NO：" + data.businessCode);
        $(".created-date").text(data.createdDate);
    },
    checkIsValid: function () {
        EUI.ProcessStatus({
            success: true,
            msg: "表单验证成功"
        });
        return true;
    }
});