/**
 * 显示页面
 */
EUI.BusinessModelView = EUI.extend(EUI.CustomUI, {
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
                    g.addBusinessModel();
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
                	url : "http://localhost:8081/flow/maindata/businessModel/find",
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
                    label : this.lang.nameText,
                    name : "name",
                    index : "name",
                    title : false
                },{
                    label : this.lang.classPathText,
                    name : "className",
                    index : "className",
                    title : false
                },{
                    label : this.lang.depictText,
                    name : "depict",
                    index : "depict",
                    title : false
                },{
                    label : "appModuleId",
                    name : "appModule.id",
                    index : "appModule.id",
                    title : false,
                    hidden : true
                },{
                    label : this.lang.belongToAppModuleText,
                    name : "appModule.name",
                    index : "appModule.name",
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
            g.updateBusinessModel(data);
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
                            url : "http://localhost:8081/flow/maindata/businessModel/delete",
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
    updateBusinessModel : function(data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title : g.lang.updateBusinessModelText,
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "updateBusinessModel",
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
                    title : g.lang.nameText,
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                    maxLength : 10,
                    value:data.name
                }, {
                    xtype : "TextField",
                    title : g.lang.classPathText,
                    labelWidth : 90,
                    name : "className",
                    width : 220,
                    value:data.className
                }, {
                    xtype : "TextField",
                    title : g.lang.depictText,
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                    value:data.depict
                },{
                    xtype : "ComboBox",
                    title : g.lang.belongToAppModuleText,
                    labelWidth : 90,
                    name : "appModule.name",
                    width : 220,
                     value:data["appModule.name"]||"",
                    submitValue:{
                        "appModule.id":data["appModule.id"]||""
                    },
                    store : {
                        url : "http://localhost:8081/flow/maindata/businessModel/findAllAppModuleName",
                    },
                    field : ["appModule.id"],
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
                    var form = EUI.getCmp("updateBusinessModel");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.className) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputClassPathMsgText,
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
                    g.saveBusinessModel(data);
                }
            }, {
                title : g.lang.cancelText,
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    addBusinessModel : function() {
        var g = this;
        win = EUI.Window({
            title : g.lang.addNewBusinessModelText,
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "addBusinessModel",
                padding : 0,
                items : [{
                    xtype : "TextField",
                    title : g.lang.nameText,
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                    maxLength : 10,
                }, {
                    xtype : "TextField",
                    title : g.lang.classPathText,
                    labelWidth : 90,
                    name : "className",
                    width : 220,
                }, {
                    xtype : "TextField",
                    title : g.lang.depictText,
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                }, {
                    xtype : "ComboBox",
                    title : g.lang.belongToAppModuleText,
                    labelWidth : 90,
                    name : "appModule.name",
                    width : 220,
                    // value:data["appModule.name"]||"",
                    // submitValue:{
                    //     id:data["appModule.id"]||""
                    // },
                    store : {
                        url : "http://localhost:8081/flow/maindata/businessModel/findAllAppModuleName",
                    },
                    field : ["appModule.id"],
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
                    var form = EUI.getCmp("addBusinessModel");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.className) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : g.lang.inputClassPathMsgText,
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
                    if (!data["appModule.name"]) {
                        EUI.ProcessStatus({
                            success : false,
                            msg :  g.lang.chooseBelongToAppModuleText,
                        });
                        return;
                    }
                    g.saveBusinessModel(data);
                }
            }, {
                title : g.lang.cancelText,
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    saveBusinessModel : function(data) {
        var g = this;
        console.log(data);
		var  myMask = EUI.LoadMask({
					msg : g.lang.nowSaveMsgText,
				});
        EUI.Store({
            url : "http://localhost:8081/flow/maindata/businessModel/update",
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