/**
 * 显示页面
 */
EUI.BusinessModelView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModule: "",
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initTbar(), this.initLeftGrid()]
        });
        this.addEvents();
    },
    initTbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            style: {
                overflow: "hidden"
            },
            border: false,
            items: [{
                xtype: "ComboBox",
                title: this.lang.modelText,
                labelWidth: 70,
                id: "coboId",
                async: false,
                colon: false,
                name: "appModule.name",
                // submitValue:{
                //     "appModule.id":"appModule.id"
                // },
                store: {
                    url: _ctxPath +"/maindata/businessModel/findAllAppModuleName",
                },
                field: ["appModule.id"],
                reader: {
                    name: "name",
                    field: ["id"]
                },
                afterLoad: function (data) {
                    if (!data) {
                        return;
                    }
                    var cobo = EUI.getCmp("coboId");
                    cobo.setValue(data[0].name);
                    g.appModule = data[0].id;
                    g.appModuleName = data[0].name;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath +"/maindata/businessModel/find",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            "Q_EQ_appModule.id": data[0].id
                        }
                    }, true)
                },
                afterSelect: function (data) {
                    if (!data) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: "请选择应用模块"
                        });
                        return;
                    }
                    // console.log(data);
                    g.appModule = data.data.id;
                    g.appModuleName = data.data.name;
                    EUI.getCmp("gridPanel").setPostParams({
                            "Q_EQ_appModule.id": data.data.id
                        }
                    ).trigger("reloadGrid");
                }
            }, {
                xtype: "Button",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addBusinessModel();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: this.lang.searchNameText,
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("gridPanel").setPostParams({
                                Q_LK_name: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_LK_name: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        };
    },
    initLeftGrid: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "gridPanel",
            style: {
                "border-radius": "3px"
            },
            gridCfg: {
                loadonce: true,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: '98%',
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        var strVar = "<div class='condetail_operate'>" +
                            "<div class='condetail_look'></div>" +
                            "<div class='condetail_set'></div>"
                            + "<div class='condetail_update'></div>"
                            + "<div class='condetail_delete'></div></div>";
                        return strVar;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name",
                    title: false
                }, {
                    label: this.lang.classPathText,
                    name: "className",
                    index: "className",
                    title: false
                }, {
                    label: this.lang.conditonBeanText,
                    name: "conditonBean",
                    index: "conditonBean",
                    title: false
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict",
                    title: false
                }, {
                    label: this.lang.workPageText,
                    name: "workPage",
                    index: "workPage",
                    title: false
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
        this.operateBtnEvents();
        this.addWorkPageEvent();
    },

    updateBusinessModel: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateBusinessModelText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBusinessModel",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "id",
                    width: 220,
                    maxLength: 10,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "应用模块ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "appModule.id",
                    width: 220,
                    value: g.appModule,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "应用模块",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220,
                    maxLength: 10,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "className",
                    width: 220,
                    value: data.className
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonBeanText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "conditonBean",
                    width: 220,
                    value: data.conditonBean
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }/*, {
                    xtype: "TextField",
                    title: g.lang.workPageText,
                    labelWidth: 90,
                    name: "workPage",
                    width: 220,
                    value: data.workPage
                }*/]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateBusinessModel");
                    var data = form.getFormValue();
                    console.log(data);
                    if (!data.name) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputNameMsgText
                        // });
                        return;
                    }
                    if (!data.className) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputClassPathMsgText
                        // });
                        return;
                    }
                    if (!data.conditonBean) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputConditonBeanText
                        // });
                        return;
                    }
                    if (!data.depict) {
                        // EUI.ProcessStatus({
                        //     success: false,
                        //     msg: g.lang.inputDepictMsgText
                        // });
                        return;
                    }
                    g.saveBusinessModel(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    addBusinessModel: function () {
        var g = this;
        win = EUI.Window({
            title: g.lang.addNewBusinessModelText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBusinessModel",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "应用模块ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "appModule.id",
                    width: 220,
                    value: g.appModule,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: "应用模块",
                    readonly: true,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName
                }, {
                    title: g.lang.nameText,
                    xtype: "TextField",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "className",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonBeanText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "conditonBean",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220
                }/*, {
                    xtype: "TextField",
                    title: g.lang.workPageText,
                    labelWidth: 90,
                    name: "workPage",
                    width: 220
                }*/]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addBusinessModel");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);

                    /* if (!data["appModule.name"]) {
                     EUI.ProcessStatus({
                     success: false,
                     msg: g.lang.chooseBelongToAppModuleText,
                     });
                     return;
                     }*/
                    g.saveBusinessModel(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveBusinessModel: function (data) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath +"/maindata/businessModel/update",
            params: data,
            success: function () {
                myMask.hide();
                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
            },
            failure: function () {
                myMask.hide();
            }
        });
        win.close();
        myMask.hide();
    },
    lookPropertyWindow: function (data) {
        var g = this;
        var Wind = EUI.Window({
            title: g.lang.conditionPropertyText,
            id:"propertyWind",
            width: 500,
            items: [{
                xtype: "GridPanel",
                id:"innerWindow",
                style: {
                    "border": "1px solid #aaa"
                },
                gridCfg: {
                    loadonce: true,
                    colModel: [{
                        name: "id",
                        index: "id",
                        hidden: true
                    }, {
                        label: g.lang.propertyText,
                        name: "key",
                        index: "key"
                    }, {
                        label: g.lang.nameText,
                        name: "name",
                        index: "name"
                    }]
                }
            }]
        });
    },
    //属性界面的数据调用
    getProperty:function (data) {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/maindata/businessModel/findConditionProperty",
            params:{
                conditonPojoClassName: data.conditonBean
            },
            success:function (status) {
                EUI.ProcessStatus(status);
                console.log(status);
                g.handleProperty(status);
                /*var data = g.handleProperty(status);
                EUI.getCmp("innerWindow").setDataInGrid(data);*///添加数据到dom的方法
            },
            failure:function (response) {
                EUI.ProcessStatus(response);
            }
        });
    },
    //js将从后台获取到的object数据转化为数组
    handleProperty:function (data) {
        var properties = [];
        for(var key in data){
            properties.push({
                key:key,
                name:data[key]
            });
        }
        console.log(properties);
        EUI.getCmp("innerWindow").setDataInGrid(properties);
       /* for(var k in properties){

        }*/
        // console.log(properties[k].name);
        //return properties;
    },
    showWorkPageWindow: function (data) {
        var g = this;
        g.workPageSetWind = EUI.Window({
            title: g.lang.workPageSetText,
            width: 700,
            height: 400,
            items: [{
                xtype: "Container",
                border: false,
                layout: "border",
                items: [this.getLeftGrid(data),
                    this.getCenterIcon(),
                    this.getRightGrid(data)
                ]
            }],
            buttons: [{
                title: g.lang.sureText,
                selected: true,
                handler: function () {
                    // var form=EUI.getCmp("workPageSelect");
                    // var data=form.getFormValue();
                    g.saveWorkPageSet(data);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    g.workPageSetWind.remove();
                }
            }]
        });
    },
    getLeftGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            width: 310,
            height: 300,
            id: "workPageSet",
            region: "west",
            style: {
                "border": "1px solid #aaa"
            },
            gridCfg: {
                //loadonce: true,
                url: _ctxPath + "/maindata/businessModel/findNotSelectEdByAppModuleId",
                postData:{
                    appModuleId:g.appModule,
                    businessModelId: data.id
                },
                hasPager: false,
                multiselect: true,
                colModel: [{
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: g.lang.nameText,
                    name: "name",
                    index: "name"
                }, {
                    label: g.lang.urlViewAddressText,
                    name: "className",
                    index: "className"
                }]
                //添加固定数据测试
                // data: [{
                //     name: "lll",
                //     id: "1",
                //     url: "sjhvs"
                // }, {
                //     name: "fshd",
                //     id: "2",
                //     url: "dvndk"
                // }]
            }
        };
    },
    getCenterIcon: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            width: 50,
            height: 300,
            border: true,
            html: "<div class='arrow-right'></div>" +
            "<div class='arrow-left'></div>"
        }
    },
    getRightGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            width: 310,
            height: 300,
            id: "workPageSelect",
            region: "east",
            style: {
                "border": "1px solid #aaa"
            },
            gridCfg: {
                 loadonce: true,
                url: _ctxPath + "/maindata/businessModel/findSelectEdByAppModuleId",
                postData:{
                    appModuleId:g.appModule,
                    businessModelId: data.id
                },
                 hasPager: false,
                multiselect: true,
                // data:[],
                colModel: [{
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: g.lang.nameText,
                    name: "name",
                    index: "name"
                }, {
                    label: g.lang.urlViewAddressText,
                    name: "url",
                    index: "url"
                }]
            }
        }
    },
    operateBtnEvents:function () {
        var g=this;
        $(".condetail_update").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.updateBusinessModel(data);
        });
        $(".condetail_delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            var infoBox = EUI.MessageBox({
                title: g.lang.tiShiText,
                msg: g.lang.ifDelMsgText,
                buttons: [{
                    title: g.lang.sureText,
                    selected: true,
                    handler: function () {
                        infoBox.remove();
                        var myMask = EUI.LoadMask({
                            msg: g.lang.nowDelMsgText
                        });
                        EUI.Store({
                            url: _ctxPath + "/maindata/businessModel/delete",
                            params: {
                                id: rowData.id
                            },
                            success: function () {
                                myMask.hide();
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            },
                            failure: function () {
                                myMask.hide();
                            }
                        });
                    }
                }, {
                    title: g.lang.cancelText,
                    handler: function () {
                        infoBox.remove();
                    }
                }]
            });
        });
        $(".condetail_look").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.lookPropertyWindow(data);
            g.getProperty(data);
        });
        $(".condetail_set").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.showWorkPageWindow(data);
        });
    },
    addWorkPageEvent: function () {
        var g = this, selectData = [];
        $(".arrow-right").live("click", function () {
            var leftGrid = EUI.getCmp("workPageSet");
            var rowDatas = leftGrid.getSelectRow();
            var gridPanel = EUI.getCmp("workPageSelect");
            var selectData = gridPanel.getGridData();
            for (var i = 0; i < rowDatas.length; i++) {
                var item = rowDatas[i];
                if (!g.isInArray(item, selectData)) {
                    gridPanel.grid.addRowData(item.id, item);
                    leftGrid.deleteRow(item.id);
                }
            }
        });
        $(".arrow-left").live("click", function () {
            var rightGrid = EUI.getCmp("workPageSelect");
            var rowDatas = rightGrid.getSelectRow();
            var gridPanel = EUI.getCmp("workPageSet");
            var selectData = gridPanel.getGridData();
            for (var i = 0; i < rowDatas.length; i++) {
                var item = rowDatas[i];
                if (!g.isInArray(item, selectData)) {
                    gridPanel.grid.addRowData(item.id, item);
                    rightGrid.deleteRow(item.id);
                }
            }
        })
    },
    isInArray: function (item, array) {
        for (var i = 0; i < array.length; i++) {
            if (item.id == array[i].id) {
                return true;
            }
        }
        return false;
    },
    saveWorkPageSet: function (data) {
        var g = this;
        var gridData = EUI.getCmp("workPageSelect").getGridData();
        console.log(gridData);
        var result = "";
        for(var i=0; i<gridData.length; i++){
            result +=gridData[i].id + ",";
        }
        result=(result.substring(result.length-1)==',')?result.substring(0,result.length-1):result;
        console.log( result);
        var mask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/maindata/businessModel/saveSetWorkPage",
            params: {
                id: data.id,
                selectWorkPageIds: result
            },
            success: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
                if (status.success) {
                    EUI.getCmp("workPageSelect").grid.trigger("reloadGrid");
                    g.workPageSetWind.close();
                }
            },
            failure: function (status) {
                mask.hide();
                EUI.ProcessStatus(status);
            }
        });
    }
});




