/**
 * 显示页面
 */
EUI.WaitDealtView = EUI.extend(EUI.CustomUI, {
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
            items:[/*{
                xtype : "Button",
                title : "查询",
                selected : true,
                handler : function(){

                }
            },*/{
                xtype : "Button",
                title : "新增资源",
                selected : true,
                handler : function() {
                    g.addAppModule();
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
                	url : "http://localhost:8081/flow/maindata/appModule/find",
                colModel : [{
                    label : "操作",
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
                    label : "代码",
                    name : "code",
                    index : "code",
                    title : false
                },{
                    label : "名字",
                    name : "name",
                    index : "name",
                    title : false
                },{
                    label : "描述",
                    name : "depict",
                    index : "depict",
                    title : false
                },/*{
                    label : "创建人",
                    name : "createdBy",
                    index : "createdBy",
                    title : false
                },{
                    label : "创建时间",
                    name : "createdDate",
                    index : "createdDate",
                    title : false
                },{
                    label : "最后更新者",
                    name : "lastModifiedBy",
                    index : "lastModifiedBy",
                    title : false
                },{
                    label : "最后更新时间",
                    name : "lastModifiedDate",
                    index : "lastModifiedDate",
                    title : false
                },*/]/*,
                data:[{
                    id:"1",
                    code:"code1",
                    name:"应用模块1",
                    depict:"描述1",
                    createdBy:"创建人1",
                    createdDate:"2017-3-30",
                    lastModifiedBy:"最后更新者1",
                    lastModifiedDate:"2017-3-30"
                },{
                    id:"2",
                    code:"code2",
                    name:"应用模块2",
                    depict:"描述2",
                    createdBy:"创建人2",
                    createdDate:"2017-3-30",
                    lastModifiedBy:"最后更新者2",
                    lastModifiedDate:"2017-3-30"
                },{
                    id:"3",
                    code:"code3",
                    name:"应用模块3",
                    depict:"描述3",
                    createdBy:"创建人3",
                    createdDate:"2017-3-30",
                    lastModifiedBy:"最后更新者3",
                    lastModifiedDate:"2017-3-30"
                }]*/,

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

            g.updateAppModule(data);
        });
        $(".condetail_delete").live("click",function(){
            var rowData=EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            var infoBox = EUI.MessageBox({
                title : "提示",
                msg : "确定删除吗？",
                buttons :[{
                    title : "确定",
                    selected : true,
                    handler : function(){
                        infoBox.remove();
                        var myMask = EUI.LoadMask({
                            msg : "正在删除,请稍后...."
                        });
                        EUI.Store({
                            url : "http://localhost:8081/flow/maindata/appModule/delete",
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
                       // EUI.getCmp("gridPanel").refreshGrid();

                    }
                },{
                    title : "取消",
                    handler : function(){
                        infoBox.remove();
                    }
                }]
            });
        });
    },
    updateAppModule : function(data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title : "修改实体模型",
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "updateAppModule",
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
                    title : "代码",
                    labelWidth : 90,
                    name : "code",
                    width : 220,
                    maxLength : 10,
                    value:data.code
                }, {
                    xtype : "TextField",
                    title : "名称",
                    labelWidth : 90,
                    name : "name",
                    width : 220,
                    value:data.name
                }, {
                    xtype : "TextField",
                    title : "描述",
                    labelWidth : 90,
                    name : "depict",
                    width : 220,
                    value:data.depict
                }/*, {
                    xtype : "TextField",
                    title : "创建人",
                    labelWidth : 90,
                    name : "createdBy",
                    width : 220,
                    value:data.createdBy
                }, {
                    xtype : "TextField",
                    title : "创建时间",
                    labelWidth : 90,
                    name : "createdDate",
                    width : 220,
                    value:data.createdDate
                }, {
                    xtype : "TextField",
                    title : "最后更新者",
                    labelWidth : 90,
                    name : "lastModifiedBy",
                    width : 220,
                    value:data.lastModifiedBy
                }, {
                    xtype : "TextField",
                    title : "最后更新时间",
                    labelWidth : 90,
                    name : "lastModifiedDate",
                    width : 220,
                    value:data.lastModifiedDate
                }*/]
            }],
            buttons : [{
                title : "保存",
                selected : true,
                handler : function() {
                    var form = EUI.getCmp("updateAppModule");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代码"
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入名称"
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入描述"
                        });
                        return;
                    }
                 /*   if (!data.createdBy) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入创建人"
                        });
                        return;
                    }
                    if (!data.createdDate) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入创建时间"
                        });
                        return;
                    }
                    if (!data.lastModifiedBy) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入最后更新者"
                        });
                        return;
                    }
                    if (!data.lastModifiedDate) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代最后更新时间"
                        });
                        return;
                    }*/
                    g.saveAppModule(data);
                }
            }, {
                title : "取消",
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    addAppModule : function() {
        var g = this;
        win = EUI.Window({
            title : "新增实体模型",
            height : 250,
            padding : 15,
            items : [{
                xtype : "FormPanel",
                id : "addAppModule",
                padding : 0,
                items : [{
                    xtype : "TextField",
                    title : "代码",
                    labelWidth : 90,
                    name : "code",
                    width : 220,
                    maxLength : 10
                }, {
                    xtype : "TextField",
                    title : "名称",
                    labelWidth : 90,
                    name : "name",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "描述",
                    labelWidth : 90,
                    name : "depict",
                    width : 220
                }/*, {
                    xtype : "TextField",
                    title : "创建人",
                    labelWidth : 90,
                    name : "createdBy",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "创建时间",
                    labelWidth : 90,
                    name : "createdDate",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "最后更新者",
                    labelWidth : 90,
                    name : "lastModifiedBy",
                    width : 220
                }, {
                    xtype : "TextField",
                    title : "最后更新时间",
                    labelWidth : 90,
                    name : "lastModifiedDate",
                    width : 220
                }*/]
            }],
            buttons : [{
                title : "保存",
                selected : true,
                handler : function() {
                    var form = EUI.getCmp("addAppModule");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代码"
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入名称"
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入描述"
                        });
                        return;
                    }
                    /*if (!data.createdBy) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入创建人"
                        });
                        return;
                    }
                    if (!data.createdDate) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入创建时间"
                        });
                        return;
                    }
                    if (!data.lastModifiedBy) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入最后更新者"
                        });
                        return;
                    }
                    if (!data.lastModifiedDate) {
                        EUI.ProcessStatus({
                            success : false,
                            msg : "请输入代最后更新时间"
                        });
                        return;
                    }*/
                    g.saveAppModule(data);

                }
            }, {
                title : "取消",
                handler : function() {
                    win.remove();
                }
            }]
        });
    },
    saveAppModule : function(data) {
        var g = this;
        console.log(data);
		var  myMask = EUI.LoadMask({
					msg : "正在保存，请稍候..."
				});
        EUI.Store({
            url : "http://localhost:8081/flow/maindata/appModule/update",
            params : data,
            success : function(){
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure : function(){
                myMask.hide();
            }
            /*success : function(status) {
                if (!status.success) {
                    new EUI.ProcessStatus({
                        msg : status.msg,
                        success : false
                    });
                } else {
                    new EUI.ProcessStatus({
                        msg : "操作成功！",
                        success : true
                    });
                    win.close();
                    g.reloadGrid();
                }
                mask.hide();
            },
            failure : function(re) {
                new EUI.ProcessStatus({
                    msg : "操作失败，请稍后再试。"
                });
                mask.hide();
            }*/
        });
        win.close();
        myMask.hide();
    }
});