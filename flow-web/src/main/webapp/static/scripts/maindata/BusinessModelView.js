/**
 * 显示页面
 */
EUI.BusinessModelView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModule: "",
    businessModelId: "",
    appModuleCode:"",
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initToolbar(), this.initLeftGrid()]
        });
        this.addEvents();
    },
    initToolbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + this.lang.modelText + "</span>",
                labelWidth: 70,
                id: "coboId",
                async: false,
                colon: false,
                name: "appModule.name",
                store: {
                    url: _ctxPath + "/businessModel/listAllAppModule"
                },
                field: ["appModuleId"],
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
                    g.appModuleCode = data[0].code;
                    var gridPanel = EUI.getCmp("gridPanel").setGridParams({
                        url: _ctxPath + "/businessModel/listBusinessModel",
                        loadonce: false,
                        datatype: "json",
                        postData: {
                            Q_EQ_appModuleId: data[0].id
                        }
                    }, true)
                },
                afterSelect: function (data) {
                    if (!data) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: this.lang.chooseAppModelText
                        });
                        return;
                    }
                    // console.log(data);
                    g.appModule = data.data.id;
                    g.appModuleName = data.data.name;
                    g.appModuleCode = data.data.code;
                    EUI.getCmp("gridPanel").setPostParams({
                            Q_EQ_appModuleId: data.data.id
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
                shrinkToFit:true,//固定宽度
                loadonce: true,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: 130,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                         // var strVar = "<div class='condetail_operate'>" +
                         //    "<div class='condetail_look' title='"+g.lang.showConditionPropertiesText+"'></div>" +
                         //    "<div class='condetail_set' title='"+g.lang.configWorkSpaceText+"'></div>"
                         //    + "<div class='condetail_setServiceUrl' title='"+g.lang.configServerLocationText+"'></div>"
                         //    + "<div class='condetail_update' title='"+g.lang.editText+"'></div>"
                         //    + "<div class='condetail_delete' title='"+g.lang.deleteText+"'></div></div>";

                        return '<i class="ecmp-common-view icon-space" title="'+g.lang.showConditionPropertiesText+'"></i>'+
                          '<i class="ecmp-common-configuration icon-space" title="'+g.lang.configWorkSpaceText+'"></i>'+
                          '<i class="ecmp-common-set icon-space" title="'+g.lang.configServerLocationText+'"></i>'+
                          '<i class="ecmp-common-edit icon-space" title="'+g.lang.editText+'"></i>'+
                          '<i class="ecmp-common-delete" title="'+g.lang.deleteText+'"></i>' ;
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name"
                }, {
                    label: this.lang.classPathText,
                    name: "className",
                    index: "className"
                }, {
                    label: this.lang.conditonBeanText,
                    name: "conditonBean",
                    index: "conditonBean"
                }, {
                    label: this.lang.applyModuleCodeText,
                    name: "appModuleCode",
                    index: "appModuleCode"
                }, {
                    label: this.lang.dataAccessObjectText,
                    name: "daoBean",
                    index: "daoBean"
                }, {
                    label: this.lang.formURLText,
                    name: "lookUrl",
                    index: "lookUrl"
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict"
                }/*, {
                    label: this.lang.workPageText,
                    name: "workPage",
                    index: "workPage"
                }*/],
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

    deleteBusinessModel: function (rowData) {
        var g = this;
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
                        url: _ctxPath + "/businessModel/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            if (result.success) {
                                EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                            }
                        },
                        failure: function (result) {
                            EUI.ProcessStatus(result);
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
    },
    updateBusinessModel: function (data) {
        var g = this;
        console.log(data);
        win = EUI.Window({
            title: g.lang.updateBusinessModelText,
            height: 400,
            width:380,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBusinessModel",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 115,
                    allowBlank: false,
                    name: "id",
                    width: 220,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.appModelIdText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleId",
                    width: 220,
                    value: g.appModule,
                    hidden: true
                },{
                    xtype: "TextField",
                    title: g.lang.modelText,
                    readonly: true,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName
                }, {
                    xtype: "TextField",
                    title: this.lang.applyModuleCodeText,
                    readonly: true,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleCode",
                    width: 220,
                    value: data.appModuleCode
                },  {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "name",
                    width: 220,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "className",
                    width: 220,
                    value: data.className
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonBeanText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "conditonBean",
                    width: 220,
                    value: data.conditonBean
                },{
                    xtype: "TextField",
                    title: this.lang.dataAccessObjectText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "daoBean",
                    width: 220,
                    value: data.daoBean
                }, {
                    xtype: "TextArea",
                    title: this.lang.formURLText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "lookUrl",
                    width: 220,
                    value: data.lookUrl
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }/*, {
                 xtype: "TextField",
                 title: g.lang.workPageText,
                 labelWidth: 115,
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
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
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
            height: 400,
            width:380,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBusinessModel",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: g.lang.appModelIdText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleId",
                    width: 220,
                    value: g.appModule,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.modelText,
                    readonly: true,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleName",
                    width: 220,
                    value: g.appModuleName
                },{
                    xtype: "TextField",
                    title: this.lang.applyModuleCodeText,
                    readonly: true,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleCode",
                    width: 220,
                    value: g.appModuleCode
                }, {
                    title: g.lang.nameText,
                    xtype: "TextField",
                    labelWidth: 115,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "className",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonBeanText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "conditonBean",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: this.lang.dataAccessObjectText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "daoBean",
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: this.lang.formURLText,
                    labelWidth: 115,
                    allowBlank: false,
                    name: "lookUrl",
                    width: 220
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 115,
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
            url: _ctxPath + "/businessModel/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (result.success) {
                    EUI.getCmp("gridPanel").grid.trigger("reloadGrid");
                    win.close();
                }
            },
            failure: function () {
                myMask.hide();
            }
        });
    },
    lookPropertyWindow: function (data) {
        var g = this;
        var Wind = EUI.Window({
            title: g.lang.conditionPropertyText,
            id: "propertyWind",
            width: 500,
            items: [{
                xtype: "GridPanel",
                id: "innerWindow",
                style: {
                    "border": "1px solid #aaa"
                },
                gridCfg: {
                    loadonce: true,
                    hasPager: false,
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
    getProperty: function (data) {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/businessModel/getPropertiesForConditionPojo",
            params: {
                conditonPojoClassName: data.conditonBean
            },
            success: function (status) {
                // EUI.ProcessStatus(status);
                console.log(status);
                g.handleProperty(status);
                /*var data = g.handleProperty(status);
                 EUI.getCmp("innerWindow").setDataInGrid(data);*///添加数据到dom的方法
            }/*,
            failure: function (response) {
                EUI.ProcessStatus(response);
            }*/
        });
    },
    //js将从后台获取到的object数据转化为数组
    handleProperty: function (data) {
        console.log(data);
        var properties = [];
        for (var key in data) {
            properties.push({
                key: key,
                name: data[key]
            });
        }
        console.log(properties);
        EUI.getCmp("innerWindow").setDataInGrid(properties);
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
    showServiceUrlWindow: function (data) {
        var g = this;
        var win = EUI.Window({
            title:g.lang.serviceUrlText,
            width: 800,
            layout: "border",
            height: 450,
            padding: 8,
            itemspace: 0,
            items: [this.initWindTbar(data), this.initWindGrid(data)]
        });
    },
    initWindTbar: function (data) {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "Button",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addFlowServiceUrl(data);
                }
            }, '->', {
                xtype: "SearchBox",
                displayText:this.lang.searchNameText,
                onSearch: function (value) {
                    console.log(value);
                    if (!value) {
                        EUI.getCmp("serviceUrlGridPanel").setPostParams({
                                Q_LK_name: ""
                            }
                        ).trigger("reloadGrid");
                    }
                    EUI.getCmp("serviceUrlGridPanel").setPostParams({
                            Q_LK_name: value
                        }
                    ).trigger("reloadGrid");
                }
            }]
        };
    },
    initWindGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "serviceUrlGridPanel",
            style: {
                "border-radius": "3px"
            },
            gridCfg: {
                //     loadonce:true,
                url: _ctxPath + "/flowServiceUrl/listServiceUrl",
                postData: {
                    "Q_EQ_businessModel.id": data.id,
                    S_code: "ASC"
                },
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "50%",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        return  "<i class='ecmp-common-edit icon-space' title='"+g.lang.editText+"'></i>"+
                                "<i class='ecmp-common-delete' title='"+g.lang.deleteText+"'></i>";
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.codeText,
                    name: "code",
                    index: "code"
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name"
                }, {
                    label: "URL",
                    name: "url",
                    index: "url"
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict"
                }],
                ondbClick: function () {
                    // var rowData = EUI.getCmp("gridPanel").getSelectRow();
                    // g.getValues(rowData.id);
                }
            }
        };
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
                url: _ctxPath + "/businessModel/listAllNotSelectEdByAppModuleId",
                postData: {
                    appModuleId: g.appModule,
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
                    name: "url",
                    index: "url"
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
            border: false,
            html: //"<div class='arrow-right'></div>" +
            // "<div class='arrow-left'></div>"
            '<div class="ecmp-common-moveright arrow-right"></div>' +
            '<div class="ecmp-common-leftmove arrow-left"></div>'
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
                url: _ctxPath + "/businessModel/listAllSelectEdByAppModuleId",
                postData: {
                    appModuleId: g.appModule,
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
    operateBtnEvents: function () {
        var g = this;
        $(".ecmp-common-edit").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.updateBusinessModel(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            console.log(rowData);
            g.deleteBusinessModel(rowData);
        });
        $(".ecmp-common-view").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            g.lookPropertyWindow(data);
            g.getProperty(data);
        });
        $(".ecmp-common-configuration").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.showWorkPageWindow(data);
        });
        $(".ecmp-common-set").live("click", function () {
            var data = EUI.getCmp("gridPanel").getSelectRow();
            console.log(data);
            g.businessModelId = data.id;
            g.showServiceUrlWindow(data);
        });
        $(".ecmp-common-edit").live("click", function () {
            var rowData = EUI.getCmp("serviceUrlGridPanel").getSelectRow();
            console.log(rowData);
            g.deleteServiceUrl(rowData);
        });
        $(".ecmp-common-delete").live("click", function () {
            var data = EUI.getCmp("serviceUrlGridPanel").getSelectRow();
            console.log(data);
            g.updateServiceUrl(data);
        });
    },
    addWorkPageEvent: function () {
        var g = this, selectData = [];
        $(".ecmp-common-moveright").live("click", function () {
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
        $(".ecmp-common-leftmove").live("click", function () {
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
        for (var i = 0; i < gridData.length; i++) {
            result += gridData[i].id + ",";
        }
        result = (result.substring(result.length - 1) == ',') ? result.substring(0, result.length - 1) : result;
        console.log(result);
        var mask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/businessModel/saveSetWorkPage",
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
    },
    addFlowServiceUrl: function (data) {
        var g = this;
        var win = EUI.Window({
            title:g.lang.addServiceUrlText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowServiceUrl",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title:g.lang.businessModelIdText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 220,
                    value: data.id,
                    hidden:true
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: "URL",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "url",
                    width: 220
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addFlowServiceUrl");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowServiceUrl(data, win);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    saveFlowServiceUrl: function (data, winCmp) {
        var g = this;
        console.log(data);
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/flowServiceUrl/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                if (result.success) {
                    EUI.getCmp("serviceUrlGridPanel").grid.trigger("reloadGrid");
                    winCmp.remove();
                }
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
                myMask.hide();
            }
        });
    },
    updateServiceUrl: function (data) {
        var g = this;
        console.log(data);
        var win = EUI.Window({
            title: g.lang.updateServiceUrlText,
            height: 250,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateFlowServiceUrl",
                padding: 0,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "id",
                    width: 220,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.businessModelIdText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 220,
                    value: g.businessModelId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "code",
                    width: 220,
                    value: data.code
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "name",
                    width: 220,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: "URL",
                    labelWidth: 90,
                    allowBlank: false,
                    name: "url",
                    width: 220,
                    value: data.url
                }, {
                    xtype: "TextField",
                    title: g.lang.depictText,
                    labelWidth: 90,
                    allowBlank: false,
                    name: "depict",
                    width: 220,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateFlowServiceUrl");
                    if (!form.isValid()) {
                        return;
                    }
                    var data = form.getFormValue();
                    console.log(data);
                    g.saveFlowServiceUrl(data, win);
                }
            }, {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            }]
        });
    },
    deleteServiceUrl: function (rowData) {
        var g = this;
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
                        url: _ctxPath + "/flowServiceUrl/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            if (result.success) {
                                EUI.getCmp("serviceUrlGridPanel").grid.trigger("reloadGrid");
                            }
                        },
                        failure: function (result) {
                            EUI.ProcessStatus(result);
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
    }
});




