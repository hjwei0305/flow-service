/**
 * 显示页面
 */
EUI.FlowTypeView = EUI.extend(EUI.CustomUI, {
    initComponent : function(){
        EUI.Container({
            renderTo : this.renderTo,
            layout : "border",
            border : false,
            padding : 8,
            itemspace : 0,
            items : [this.initTbar(), this.initGrid()]
        });
        this.addEvents();
    },
    initTbar : function(){
        var g=this;
        return{
            xtype : "ToolBar",
            region : "north",
            height : 50,
            padding : 10,
            style : {
                overflow : "hidden"
            },
            border : false,
            items:[{
                xtype : "Button",
                title : this.lang.addResourceText,
                selected : true,
                handler : function() {
                    g.addFlowType();
                }
            }]
        };
    },
    initGrid : function(){
        var g=this;
        return {
            xtype : "GridPanel",
            region : "center",
            id : "gridPanel",
            style : {
                "border" : "1px solid #aaa",
                "border-reduis" : "3px"
            },
            gridCfg : {
           //     loadonce:true,
                	url : "http://localhost:8081/flow/maindata/flowType/find",
                colModel : [{
                    label : this.lang.operateText,
                    name : "operate",
                    index : "operate",
                    width : 100,
                    align : "center",
                    formatter : function(cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_operate'>"
                            + "<div class='condetail_update'></div>"
                            + "<div class='condetail_delete'></div></div>";
                        return strVar;
                    }
                },{
                    name : "id",
                    index : "id",
                    hidden : true
                },{
                    label : this.lang.codeText,
                    name : "code",
                    index : "code",
                    title : false
                },{
                    label : this.lang.nameText,
                    name : "name",
                    index : "name",
                    title : false
                },{
                    label : this.lang.depictText,
                    name : "depict",
                    index : "depict",
                    title : false
                },{
                    label : "businessModelId",
                    name : "businessModel.id",
                    index : "businessModel.id",
                    title : false,
                   hidden : true
                },{
                    label : this.lang.belongToBusinessModelText,
                    name : "businessModel.name",
                    index : "businessModel.name",
                    title : false
                }],
                ondbClick : function(){
                    var rowData=EUI.getCmp("gridPanel").getSelectRow();
                    g.getValues(rowData.id);
                }
            }
        };
    },
    addEvents : function(){
        var g = this;
        $(".condetail_update").live("click",function(){
            var data=EUI.getCmp("gridPanel").getSelectRow();
            //  var tabPanel=parent.homeView.getTabPanel();
            console.log(data);
            g.updateFlowType(data);
        });
        $(".condetail_delete").live("click",function(){
            var rowData=EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            var infoBox = EUI.MessageBox({
                title : g.lang.tiShiText,
                msg : g.lang.ifDelMsgText,
                buttons :[{
                    title : g.lang.sureText,
                    selected : true,
                    handler : function(){
                        infoBox.remove();
                        var myMask = EUI.LoadMask({
                            msg : g.lang.nowDelMsgText,
                        });
                        EUI.Store({
                            url : "http://localhost:8081/flow/maindata/flowType/delete",
                            params : {
                                id:rowData.id
                            },
                            success : function(){
                                myMask.hide();
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            },
                            failure : function(){
                                myMask.hide();
                            }
                        });
                    }
                },{
                    title : g.lang.cancelText,
                    handler : function(){
                        infoBox.remove();
                    }
                }]
            });
        });
    },
    updateFlowType : function(data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title : g.lang.updateFlowTypeText,
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "updateFlowType",
                padding : 0,
                items : [{
                    xtype : "TextField",
                    title : "ID",
                    labelWidth : 90,
                    name : "id",
                    width : 220,
                    maxLength : 10,
                    value:data.id,
                   hidden : true
                },{
                    xtype : "TextField",
                    title : g.lang.codeText,
                    labelWidth : 90,
                    name : "code",
                    width : 220,
                    maxLength : 10,
                    value:data.code
                }, {
                    xtype : "TextField",
                    title : g.lang.nameText,
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                    value:data.name
                }, {
                    xtype : "TextField",
                    title : g.lang.depictText,
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                    value:data.depict
                },{
                    xtype : "ComboBox",
                    title : g.lang.belongToBusinessText,
                    labelWidth : 90,
                    name : "businessModel.name",
                    width : 220,
                     value:data["businessModel.name"]||"",
                    submitValue:{
                        "businessModel.id":data["businessModel.id"]||""
                    },
                    store : {
                        url : "http://localhost:8081/flow/maindata/flowType/findAllBusinessModelName",
                    },
                    field : ["businessModel.id"],
                    reader : {
                        name : "name",
                        field : ["id"]
                    },
                }]
            }],
            buttons : [{
                title : g.lang.saveText,
                selected : true,
                handler : function() {
                    var form = EUI.getCmp("updateFlowType");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputCodeMsgText,
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputDepictMsgText,
                        });
                        return;
                    }
                    if (!data["businessModel.name"]) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.chooseBelongToBusinessModelText,
                        });
                        return;
                    }
                    g.saveFlowType(data);
                }
            }, {
                title : this.lang.cancelText,
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    addFlowType : function() {
        var g = this;
        win = EUI.Window({
            title : g.lang.addNewFlowTypeText,
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "addFlowType",
                padding : 0,
                items : [{
                    xtype : "TextField",
                    title : g.lang.codeText,
                    labelWidth : 90,
                    name : "code",
                    width : 220,
                    maxLength : 10,
                }, {
                    xtype : "TextField",
                    title : this.lang.nameText,
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                }, {
                    xtype : "TextField",
                    title : this.lang.depictText,
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                }, {
                    xtype : "ComboBox",
                    title : g.lang.belongToBusinessText,
                    labelWidth : 90,
                    name : "businessModel.name",
                    width : 220,
                    store : {
                        url : "http://localhost:8081/flow/maindata/flowType/findAllBusinessModelName",
                    },
                    field : ["businessModel.id"],
                    reader : {
                        name : "name",
                        field : ["id"]
                    }
                }]
            }],
            buttons : [{
                title : g.lang.saveText,
                selected : true,
                handler : function() {
                    var form = EUI.getCmp("addFlowType");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputCodeMsgText,
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputDepictMsgText,
                        });
                        return;
                    }
                    if (!data["businessModel.name"]) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.chooseBelongToBusinessModelText,
                        });
                        return;
                    }
                    g.saveFlowType(data);

                }
            }, {
                title : g.lang.cancelText,
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    saveFlowType : function(data) {
        var g = this;
        console.log(data);
		var  myMask = EUI.LoadMask({
					msg : g.lang.nowSaveMsgText,
				});
        EUI.Store({
            url : "http://localhost:8081/flow/maindata/flowType/update",
            params : data,
            success : function(){
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure : function(){
                myMask.hide();
            }
        });
        win.close();
        myMask.hide();
    }
});