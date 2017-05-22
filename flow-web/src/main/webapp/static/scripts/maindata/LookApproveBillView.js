/*
* 查看内置审批单
* */
EUI.LookApproveBillView = EUI.extend(EUI.CustomUI, {
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            width:500,
            height:360,
            itemspace: 0,
            style:{
                "background":"#fff",
                "border":"1px solid #b5b8c8"
            },
            items: [this.initTop(),this.initCenter()/*,this.initDown()*/]
        });
        this.showFindData();
        $("#aa").find('input').css("font-weight","bold");
    },
    initTop:function () {
        var g=this;
        return {
            xtype:"Container",
            region:"north",
            id:"top",
            border:false,
            height:70,
            html:"<div class='title'>内置审批单</div>"
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
                id:"aa",
                readonly:true,
                colon:false,
                style:{
                    "font-weight":"bolder"
                }
            },{
                xtype:"TextArea",
                title:"<span class='name'>说明</span>",
                id:"workCaption",
                name:"workCaption",
                readonly:true,
                width:300,
                height:170,
                colon:false
            }]
        }
    },
    showFindData:function () {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/lookApproveBill/getApproveBill",
            params: {
                id: "0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7"
            },
            success:function (result) {
                EUI.ProcessStatus(result);
                if (result.success) {
                    EUI.getCmp("lookBill").loadData(result.data);
                }
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
            }
        })

    }
});