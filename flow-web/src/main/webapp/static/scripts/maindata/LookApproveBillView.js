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
            xtype:"Container",
            id:"lookBill",
            border:false,
            region:"center",
            items:[{
                xtype:"TextField",
                title:"<span class='name'>名称</span>",
                name:"name",
                width:300,
                readonly:true,
                colon:false
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
    /*initDown:function () {
        var g=this;
        return {
            xtype:"Container",
            region:"south",
            id:"btn",
            border:false,
            items:[{
                xtype:"Button",
                title:"提交",
                id:"submit",
                width:50,
                height:30
            },{
                xtype:"Button",
                title:"重置",
                id:"reset",
                width:50,
                height:30
            }]
        }
    },*/
    /*getCenterData:function () {
        var g=this;
        EUI.Store({
            url: _ctxPath + "/lookApproveBill/getApproveBill",
            params: {
                S_createdDate: "ASC"
            },
            success:function (result) {
                EUI.ProcessStatus(result);
                    g.showFindData();

                    // var showData=EUI.getCmp("")
                    // EUI.getCmp("lookBill").setValue();
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
            }
        })
    },*/
    showFindData:function () {
        var g=this;
        var request=g.getRequest();
        var id=request['id'];
        console.log(id);
        EUI.Store({
            url: _ctxPath + "/lookApproveBill/getApproveBill",
            params: {
                id: "0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7"
            },
            success:function (result) {
                EUI.ProcessStatus(result);
                console.log("kkkkkkkkkkkkkkkk")

                // var showData=EUI.getCmp("")
                // EUI.getCmp("lookBill").setValue();
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
            }
        })

    },
    getRequest:function () {
        var g=this;
        var url=location.search;//获取url中“？”符后的字符串
        var theRequest=new Object();
        if (url.indexOf("?")!=-1){
            var str=url.substr(1);
            strs=str.split("&");
            for (var i=0;i<strs.length;i++){
                theRequest[strs[i].split("=")[0]]=(strs[i].split("=")[1]);
            }
        }
        return theRequest;
    }

});