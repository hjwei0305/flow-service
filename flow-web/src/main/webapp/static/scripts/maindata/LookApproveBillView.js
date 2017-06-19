/*
* 查看内置审批单
* */
EUI.LookApproveBillView = EUI.extend(EUI.CustomUI, {
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            style:{
                "background":"#fff"
            },
            items:[{
                xtype:"Container",
                layout: "border",
                border: false,
                padding: 8,
                width:500,
                height:500,
                id:"lookApprove",
                itemspace: 0,
                style:{
                    "background":"#fff",
                    "border":"1px solid #b5b8c8",
                    "margin":"0 auto"
                },
                items: [this.initTop(),this.initCenter(),this.initDown()]
            }]

        });
        this.showFindData();
        $("#name").find('input').css("font-weight","bold");
    },
    initTop:function () {
        var g=this;
        return {
            xtype:"Container",
            region:"north",
            id:"top",
            border:false,
            height:70,
            html:"<div class='title'>业务申请单</div>"
        }
    },
    initCenter:function () {
        var g=this;
        return {
            xtype:"FormPanel",
            id:"lookBill",
            border:false,
            region:"center",
            items:[{
                xtype:"TextField",
                title:"<span class='name'>名称</span>",
                name:"name",
                width:300,
                id:"name",
                // readonly:true,
                colon:false,
                style:{
                    "font-weight":"bolder"
                }
            },{
                xtype:"TextField",
                title:"<span class='name'>单价</span>",
                name:"unitPrice",
                width:300,
                id:"unitPrice",
                // readonly:true,
                colon:false,
                style:{
                    "font-weight":"bolder"
                }
            },{
                xtype:"TextField",
                title:"<span class='name'>数量</span>",
                name:"count",
                width:300,
                id:"count",
                // readonly:true,
                colon:false,
                style:{
                    "font-weight":"bolder"
                }
            },{
                xtype:"TextArea",
                title:"<span class='name'>说明</span>",
                id:"workCaption",
                name:"workCaption",
                // readonly:true,
                width:300,
                height:170,
                colon:false
            }]
        }
    },
    initDown:function () {
        var g=this;
        return{
            xtype:"Container",
            region:"south",
            height:55,
            id:"btn",
            border:false,
            padding:0,
            items:[{
                xtype:"Button",
                title:"保存",
                id:"save",
                selected:true,
                handler:function () {
                    var form = EUI.getCmp("lookBill");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveLookApprove(data);
                }
            },{
                xtype:"Button",
                title:"取消",
                id:"cancel"
            }]
        }
    },
    showFindData:function () {
        var g = this;
        var myMask=EUI.LoadMask({
            msg:"正在加载，请稍后..."
        });
        EUI.Store({
            url: _ctxPath + "/lookApproveBill/getApproveBill",
            params: {
                id: EUI.util.getUrlParam("id")
            },
            success:function (result) {
                myMask.hide();
                if (result.success) {
                    EUI.getCmp("lookBill").loadData(result.data);
                }else{
                    EUI.ProcessStatus(result);
                }
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        })

    },
    checkIsValid:function () {
        EUI.ProcessStatus({
           success:true,
            msg:"表单验证成功"
        });
        return true;
    },
    saveLookApprove: function (data) {
        var g = this;
        var mask = EUI.LoadMask({
            msg: "正在保存，请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/builtInApprove/save",
            params: data,
            success: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
            },
            failure: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
            }
        });
    }
});