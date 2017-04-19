/**
 * 工作页面
 */
EUI.WorkPageUrlView = EUI.extend(EUI.CustomUI, {
    initComponent: function () {

        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initTop(), this.initCenter()]
        });
        this.addEvents();
    },
    initTop: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 50,
            padding: 0,
            style: {
                overflow: "hidden"
            },
            border: false,
            items: [{
                xtype: "ComboBox",
                title: this.lang.modelText,
                labelWidth: 40,
                value: this.lang.modelValueText,
                colon: false
            }, {
                xtype: "Button",
                title: this.lang.addBtnText,
                selected: true,
                handler: function () {
                    var data = [{
                        code: "21",
                        id: "2"
                    }];
                    EUI.getCmp("gridPanel").setDataInGrid(data);
                    // g.addWorkPageUrl();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: this.lang.searchKeywordText,
                onSearch: function (value) {
                    console.log(value);
                }
            }]
        }
    },
    initCenter: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "gridPanel",
            style: {
                "border": "1px solid #aaa",
                "border-radius": "3px"
            },
            gridCfg: {
                url: "",
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: '80%',
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail-operate'>" +
                            "<div class='condetail-update'></div>" +
                            "<div class='condetail-delete'></div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.codeText,
                    name: "code",
                    index: "code",
                    width: '95%'
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name",
                    width: '95%'
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict"
                }],
                ondbClick: function () {
                    var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    g.getValue(rowData.id);
                }
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".condetail-update").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.updateWorkPageUrl(data);
        });
        $(".condetail-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            var infoBox = EUI.MessageBox({
                title: g.lang.tiShiText,
                msg: g.lang.ifDelMsgText,
                buttons: [{
                    title: g.lang.sureText,
                    selected: true,
                    handler: function () {
                        infoBox.remove();
                        g.deleteGridData();
                        // EUI.getCmp("gridPanel").refreshGrid();

                    }
                }, {
                    title: g.lang.cancelText,
                    handler: function () {
                        infoBox.remove();
                    }
                }]
            });
        });
    },
    updateWorkPageUrl: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateWorkPageUrlText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updatreWorkPageUrl",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    name: "id",
                    width: 220,
                    maxLength: 10,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    name: "code",
                    width: 220,
                    maxLength: 10,
                    value: data.code
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    name: "name",
                    width: 220,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateWorkPageUrl");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputCodeMsgText,
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputDepictMsgText,
                        });
                        return;
                    }
                    g.saveWorkPageUrl(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addWorkPageUrl: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewWorkPageUrlText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addWorkPageUrl",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    name: "code",
                    width: 220,
                    maxLength: 10
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addWorkPageUrl");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.code) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputCodeMsgText,
                        });
                        return;
                    }
                    if (!data.name) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputNameMsgText,
                        });
                        return;
                    }
                    if (!data.depict) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: g.lang.inputDepictMsgText,
                        });
                        return;
                    }
                    g.saveWorkPageUrl(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveWorkPageUrl: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText,
        });
        EUI.Store({
            url: "",
            params: data,
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
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
    },
    deleteGridData: function () {
        var g = this;
        var rowData = EUI.getCmp("gridPanel").getSelectRow();
        var myMask = EUI.LoadMask({
            msg: g.lang.nowDelMsgText
        });
        EUI.Store({
            url: "",
            params: {
                id: rowData.id
            },
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").deleteRow(rowData.id);
                // EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
                myMask.hide();
            }
        });
    }
});
