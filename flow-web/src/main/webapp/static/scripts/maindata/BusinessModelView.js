/**
 * 显示页面
 */
EUI.BusinessModelView = EUI.extend(EUI.CustomUI, {
    appModuleName: "",
    appModule: "",
    businessModelId: "",
    appModuleCode:"",
    initComponent: function () {
        this.gridCmp=EUI.GridPanel({
            renderTo: this.renderTo,
            title: "业务实体",
            border: true,
            tbar: this.initTbar(),
            gridCfg: this.initGrid()
        });
        this.addEvents();
    },
    //业务实体主页面-工具栏
    initTbar: function () {
        var g = this;
        return  [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + this.lang.modelText + "</span>",
                labelWidth: 70,
                id: "coboId",
                async: false,
                canClear: false,
                colon: false,
                name: "appModule.name",
                store: {
                    url: _ctxPath + "/businessModel/listAllAppModule"
                },
                field: ["id"],
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
                    g.appModule = data[0];
                    g.appModuleName = data[0].name;
                    g.appModuleCode = data[0].code;
                    g.gridCmp.setGridParams({
                        url: _ctxPath + "/businessModel/listBusinessModel",
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
                            msg: this.lang.chooseAppModelText
                        });
                        return;
                    }
                    g.appModule = data.data;
                    g.appModuleName = data.data.name;
                    g.appModuleCode = data.data.code;
                    g.gridCmp.setPostParams({
                        "Q_EQ_appModule.id": data.data.id
                        },true);
                }
            }, {
                xtype: "Button",
                iconCss:"ecmp-common-add",
                title: this.lang.addResourceText,
                selected: true,
                handler: function () {
                    g.addBusinessModel();
                }
            },{
            xtype: "Button",
            title: g.lang.copyText,
            iconCss:"ecmp-common-copy",
            handler: function() {
                var rowData =g.gridCmp.getSelectRow();
                if (rowData && rowData.id) {
                    rowData.id=null;
                    rowData.name=rowData.name+"_COPY";
                    rowData.className=rowData.className+"_COPY";
                    g.addBusinessModel(rowData,true);
                } else {
                    EUI.ProcessStatus({msg:g.lang.copyHintMessage,success: false});
                }
            }
        }, '->', {
                xtype: "SearchBox",
                displayText: this.lang.searchByNameText,
                onSearch: function (value) {
                    g.gridCmp.setPostParams({
                            Q_LK_name: value
                        },true);
                }
            }];
    },
    //业务实体主页面-数据列表
    initGrid: function () {
        var g = this;
        return  {
                shrinkToFit: false,//固定宽度
                loadonce: true,
                datatype: "local",
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: 180,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        return    '<i class="ecmp-common-edit icon-space update-businessModel fontcusor" title="' + g.lang.editText + '"></i>' +
                            '<i class="ecmp-common-delete icon-space delete-businessModel fontcusor" title="' + g.lang.deleteText + '"></i>'+
                            '<i class="ecmp-common-configuration icon-space fontcusor" title="' + g.lang.configWorkSpaceText + '"></i>' +
                            '<i class="ecmp-common-set icon-space fontcusor" title="' + g.lang.configServerLocationText + '"></i>'+
                            '<i class="ecmp-common-account config-excutor icon-space fontcusor" title="' + g.lang.configExecutorText + '"></i>'+
                            '<i class="ecmp-common-view fontcusor" title="' + g.lang.showConditionPropertiesText + '"></i>' ;

                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.nameText,
                    name: "name",
                    index: "name",
                    width:120
                }, {
                    label: this.lang.classPathText,
                    name: "className",
                    index: "className",
                    width:350
                },
                //     {
                //     label: this.lang.conditonBeanText,
                //     name: "conditonBean",
                //     index: "conditonBean",
                //     width:450
                // },
                    {
                    label: this.lang.applyModuleCodeText,
                    name: "appModuleCode",
                    index: "appModuleCode",
                    width:120
                }, {
                        name: "appModule.id",
                        index: "appModule.id",
                        hidden: true
                    },
                    {
                        name: "conditonProperties",
                        index: "conditonProperties",
                        hidden: true
                    },
                    {
                        name: "conditonPValue",
                        index: "conditonPValue",
                        hidden: true
                    },
                    {
                        name: "conditonPSValue",
                        index: "conditonPSValue",
                        hidden: true
                    },
                    {
                        name: "conditonStatusRest",
                        index: "conditonStatusRest",
                        hidden: true
                    },
                    {
                        name: "conditonStatusRest",
                        index: "conditonStatusRest",
                        hidden: true
                    },
                    {
                        name: "completeTaskServiceUrl",
                        index: "completeTaskServiceUrl",
                        hidden: true
                    },
                //     {
                //     label: this.lang.dataAccessObjectText,
                //     name: "daoBean",
                //     index: "daoBean",
                //     width:220
                // },
                    {
                        label: this.lang.businessDetailServiceUrlText,
                        name: "businessDetailServiceUrl",
                        index: "businessDetailServiceUrl",
                        width:400
                    },
                    {
                    label: this.lang.formURLText,
                    name: "lookUrl",
                    index: "lookUrl",
                    width:400
                }, {
                    label: this.lang.depictText,
                    name: "depict",
                    index: "depict",
                    width:400,
                    hidden:true
                }, {
                    label: "所属应用模块",
                    name: "appModuleName",
                    index: "appModuleName",
                    width:220,
                    hidden:true
                }],
                ondbClick: function () {
                    var rowData = g.gridCmp.getSelectRow();
                    g.getValue(rowData.id);
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
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            },{
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
                                g.gridCmp.grid.trigger("reloadGrid");
                            }
                        },
                        failure: function (result) {
                            EUI.ProcessStatus(result);
                            myMask.hide();
                        }
                    });
                }
            }]
        });
    },
    updateBusinessModel: function (data) {
        var g = this;
        win = EUI.Window({
            title: g.lang.updateBusinessModelText,
            iconCss:"ecmp-eui-edit",
            height: 480,
            width:560,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateBusinessModel",
                padding: 0,
                defaultConfig:{
                  labelWidth: 180,
                  width: 380
                },
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "id",
                    // width: 300,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.appModelIdText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "appModule.id",
                    // width: 300,
                    value: g.appModule.id,
                    hidden: true
                },{
                    xtype: "TextField",
                    title: g.lang.modelText,
                    readonly: true,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleName",
                    // width: 300,
                    value: g.appModuleName +"--"+data.appModuleCode
                }, {
                    xtype: "TextField",
                    title: this.lang.applyModuleCodeText,
                    readonly: true,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "appModuleCode",
                    // width: 300,
                    value: data.appModuleCode,
                    hidden:true
                },  {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "name",
                    // width: 300,
                    value: data.name
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "className",
                    // width: 300,
                    value: data.className
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonPropertiesText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonProperties",
                    // width: 300,
                    value: data.conditonProperties
                },{
                    xtype: "TextField",
                    title: this.lang.conditonPValueText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonPValue",
                    // width: 300,
                    value: data.conditonPValue
                },{
                    xtype: "TextField",
                    title: this.lang.conditonPSValueText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonPSValue",
                    // width: 300,
                    value: data.conditonPSValue
                },{
                    xtype: "TextField",
                    title: this.lang.conditonStatusRestText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonStatusRest",
                    // width: 300,
                    value: data.conditonStatusRest
                },{
                    xtype: "TextField",
                    title: this.lang.completeTaskServiceUrlText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "completeTaskServiceUrl",
                    // width: 300,
                    value: data.completeTaskServiceUrl
                },{
                    xtype: "TextField",
                    title: this.lang.businessDetailServiceUrlText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "businessDetailServiceUrl",
                    // width: 300,
                    value: data.businessDetailServiceUrl
                },{
                    xtype: "TextField",
                    title: this.lang.formURLText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "lookUrl",
                    // width: 300,
                    value: data.lookUrl
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    // labelWidth: 115,
                    name: "depict",
                    // width: 300,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateBusinessModel");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveBusinessModel(data);
                }
            }]
        });
    },
    addBusinessModel: function (data,isCopy) {
        var g = this,baseItem=[];
        if(isCopy){
            baseItem=[
                {
                    xtype: "ComboBox",
                    title: g.lang.modelText,
                    canClear: false,
                    allowBlank:false,
                    name: "appModuleName",
                    store: {
                        url: _ctxPath + "/businessModel/listAllAppModule"
                    },
                    field: ["appModule.id","appModuleCode"],
                    reader: {
                        name: "name",
                        field: ["id","code"]
                    }
                }];

        }else {
            baseItem=[{
                xtype: "TextField",
                title: g.lang.appModelIdText,
                // labelWidth: 115,
                allowBlank: false,
                name: "appModule.id",
                // width: 300,
                value: g.appModule.id,
                hidden: true
            }, {
                xtype: "TextField",
                title: g.lang.modelText,
                readonly: true,
                // labelWidth: 115,
                allowBlank: false,
                name: "appModuleName",
                // width: 300,
                value: g.appModuleName +"--"+g.appModuleCode
            },{
                xtype: "TextField",
                title: this.lang.applyModuleCodeText,
                readonly: true,
                // labelWidth: 115,
                allowBlank: false,
                name: "appModuleCode",
                // width: 300,
                value: g.appModuleCode,
                hidden:true
            }]
        }
        win = EUI.Window({
            title: isCopy?g.lang.copyBusinessModelText: g.lang.addNewBusinessModelText,
            iconCss:isCopy?"ecmp-common-copy":"ecmp-eui-add",
            iconCss:"ecmp-eui-add",
            height: 480,
            width:560,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addBusinessModel",
                padding: 0,
                defaultConfig:{
                    labelWidth: 180,
                    width: 380
                },
                items: baseItem.concat([{
                    title: g.lang.nameText,
                    xtype: "TextField",
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "name",
                    // width: 300
                }, {
                    xtype: "TextField",
                    title: g.lang.classPathText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "className",
                    // width: 300
                }, {
                    xtype: "TextField",
                    title: g.lang.conditonPropertiesText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonProperties",
                    // width: 300
                },{
                    xtype: "TextField",
                    title: this.lang.conditonPValueText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonPValue",
                    // width: 300
                },{
                    xtype: "TextField",
                    title: this.lang.conditonPSValueText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonPSValue",
                    // width: 300
                },{
                    xtype: "TextField",
                    title: this.lang.conditonStatusRestText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "conditonStatusRest",
                    // width: 300
                }, {
                    xtype: "TextField",
                    title: this.lang.completeTaskServiceUrlText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "completeTaskServiceUrl",
                    // width: 300
                }, //businessDetailServiceUrlText
                 {
                        xtype: "TextField",
                        title: this.lang.businessDetailServiceUrlText,
                        // labelWidth: 115,
                        allowBlank: false,
                        name: "businessDetailServiceUrl",
                        // width: 300
                 },
                    {
                    xtype: "TextField",
                    title: this.lang.formURLText,
                    // labelWidth: 115,
                    allowBlank: false,
                    name: "lookUrl",
                    // width: 300
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    // labelWidth: 115,
                    name: "depict",
                    // width: 300
                }])
            }],
            buttons: [ {
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addBusinessModel");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveBusinessModel(data);
                }
            }]
        });
        if(isCopy){
            EUI.getCmp("addBusinessModel").loadData(data);
        }
    },
    saveBusinessModel: function (data) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/businessModel/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                g.gridCmp.grid.trigger("reloadGrid");
                win.close();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    lookPropertyWindow: function (data) {
        var g = this;
        var Wind = EUI.Window({
            title: g.lang.conditionPropertyText,
            iconCss:"ecmp-eui-look",
            id: "propertyWind",
            width: 515,
            height:410,
            isOverFlow:false,
            items: [{
                xtype: "GridPanel",
                id: "innerWindow",
                style: {
                    "border": "1px solid #aaa"
                },
                gridCfg: {
                    loadonce: true,
                    datatype: "local",
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
        var myMask = EUI.LoadMask({
            msg: g.lang.queryMaskMessageText,
            target: EUI.getCmp("innerWindow")
        });
        EUI.Store({
            url: _ctxPath + "/businessModel/getPropertiesForConditionPojo",
            params: {
                businessModelCode: data.className
            },
            success: function (status) {
                myMask.hide();
                g.handleProperty(status);
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    //js将从后台获取到的object数据转化为数组
    handleProperty: function (data) {
        var properties = [];
        for (var key in data) {
            properties.push({
                key: key,
                name: data[key]
            });
        }
        EUI.getCmp("innerWindow").setDataInGrid(properties);
    },
    //工作界面配置
    showWorkPageWindow: function (data) {
        var g = this;
        g.workPageSetWind = EUI.Window({
            title: g.lang.workPageSetText,
            iconCss: "ecmp-eui-setting",
            width: 800,
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
                title: g.lang.cancelText,
                handler: function () {
                    g.workPageSetWind.remove();
                }
            },{
                title: g.lang.sureText,
                selected: true,
                handler: function () {
                    g.saveWorkPageSet(data);
                }
            }]
        });
    },
    //服务地址管理页面
    showServiceUrlWindow: function (data) {
        var g = this;
        var win = EUI.Window({
            title:g.lang.serviceUrlText,
            iconCss: "ecmp-eui-setting",
            width: 800,
            height: 450,
            padding: 8,
            items: [this.initWindGrid(data)]
        });
    },
    //自定义执行人管理页面
    showExecutorConfigWindow: function (data) {
        var g = this;
        var win = EUI.Window({
            title: this.lang.configExecutorText+" ["+this.lang.businessModelText+":"+data.name+"]",
            iconCss: "ecmp-eui-setting",
            width: 800,
            height: 450,
            padding: 8,
            items: [this.initExecutorConfigWindGrid(data)]
        });
    },
    initExecutorConfigWindTbar: function (data) {
        var g = this;
        return  [{
                xtype: "Button",
                title: this.lang.addResourceText,
                iconCss:"ecmp-common-add",
                selected: true,
                handler: function () {
                    g.addExecutorConfig(data);
                }
            }, '->', {
                xtype: "SearchBox",
                id: "searchBox",
                displayText:this.lang.searchByCodeOrNameText,
                onSearch: function (value) {
                    EUI.getCmp("executorConfigGridPanel").localSearch(value);
                }
            }];
    },
    initExecutorConfigWindGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            tbar: g.initExecutorConfigWindTbar(data),
            border: true,
            id: "executorConfigGridPanel",
            style: {
                "border-radius": "3px"
            },
            searchConfig: {
                searchCols: ["name","code"]
            },
            gridCfg: {
                loadonce:true,//一次查询所有数据
                shrinkToFit: false,
                url: _ctxPath + "/flowExecutorConfig/list",
                postData: {
                    "Q_EQ_businessModel.id": data.id,
                    "S_lastEditedDate": "DESC"
                },
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: 80,
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        return  "<i class='ecmp-common-edit icon-space update-executorconfig fontcusor' title='"+g.lang.editText+"'></i>"+
                            "<i class='ecmp-common-delete delete-executorconfig fontcusor' title='"+g.lang.deleteText+"'></i>";
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.codeText,
                    width: 80,
                    name: "code",
                    index: "code"
                }, {
                    label: this.lang.nameText,
                    width: 150,
                    name: "name",
                    index: "name"
                },{
                    label: this.lang.apiLocationText,
                    width: 380,
                    name: "url",
                    index: "url"
                },{
                    label: this.lang.paramText,
                    width: 200,
                    name: "param",
                    index: "param"
                }, {
                    label: this.lang.depictText,
                    width: 400,
                    name: "depict",
                    index: "depict"
                }, {
                    name: "businessModel.id",
                    index: "businessModel.id",
                    hidden: true
                }]

            }
        };
    },
    addExecutorConfig: function (data) {
        var g = this;
        var win = EUI.Window({
            title: this.lang.addExecutorConfigText,
            iconCss:"ecmp-eui-add",
            width: 380,
            height: 380,
            padding: 15,
            isOverFlow: false,
            items: [{
                xtype: "FormPanel",
                id: "addExecutorConfig",
                padding: 13,
                items: [{
                    xtype: "TextField",
                    title: this.lang.businessModelIdText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 260,
                    value: data.id,
                    hidden:true
                }, {
                    xtype: "TextField",
                    title: this.lang.codeText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "code",
                    width: 260
                }, {
                    xtype: "TextField",
                    title: this.lang.nameText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "name",
                    width: 260
                },{
                    xtype: "TextArea",
                    title: this.lang.apiLocationText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "url",
                    width: 260,
                    height:60
                },{
                    xtype: "TextArea",
                    title: this.lang.paramText,
                    labelWidth: 80,
                    allowBlank: true,
                    name: "param",
                    width: 260,
                    height:60
                }, {
                    xtype: "TextArea",
                    title: this.lang.depictText,
                    labelWidth: 80,
                    allowBlank: true,
                    name: "depict",
                    width: 260,
                    height: 80
                }]
            }],
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addExecutorConfig");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveExecutorConfig(data, win);
                }
            }]
        });
    },
    saveExecutorConfig: function (data, winCmp) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/flowExecutorConfig/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                EUI.getCmp("executorConfigGridPanel").refreshGrid();
                EUI.getCmp("searchBox").setValue("");
                winCmp.remove();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    //服务地址主页面-列表的工具栏
    initWindTbar: function (data) {
        var g = this;
        return [{
                xtype: "Button",
                title: this.lang.addResourceText,
                iconCss:"ecmp-common-add",
                selected: true,
                handler: function () {
                    g.addFlowServiceUrl(data);
                }
            }, '->', {
                xtype: "SearchBox",
                displayText:this.lang.searchByNameText,
                onSearch: function (value) {
                    EUI.getCmp("serviceUrlGridPanel").setPostParams({
                            Q_LK_name: value
                        },true);
                }
            }];
    },
    //服务地址主页面-列表
    initWindGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            border: true,
            id: "serviceUrlGridPanel",
            style: {
                "border-radius": "3px"
            },
            tbar: this.initWindTbar(data),
            gridCfg: {
                url: _ctxPath + "/flowServiceUrl/listServiceUrl",
                postData: {
                    "Q_EQ_businessModel.id": data.id,
                    S_code: "ASC"
                },
                shrinkToFit: false,
                colModel: [{
                    label: this.lang.operateText,
                    name: "operate",
                    index: "operate",
                    width: "30%",
                    align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        return  "<i class='ecmp-common-edit icon-space update-serviceurl fontcusor' title='"+g.lang.editText+"'></i>"+
                            "<i class='ecmp-common-delete delete-serviceurl fontcusor' title='"+g.lang.deleteText+"'></i>";
                    }
                }, {
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: this.lang.codeText,
                    width: 100,
                    name: "code",
                    index: "code"
                }, {
                    label: this.lang.nameText,
                    width: 180,
                    name: "name",
                    index: "name"
                }, {
                    label: "URL",
                    width: 350,
                    name: "url",
                    index: "url"
                }, {
                    label: this.lang.depictText,
                    width: 400,
                    name: "depict",
                    index: "depict"
                }]
            }
        };
    },
    //工作界面配置-未分配
    getRightGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            width: 370,
            height: 300,
            title:"未分配",
            id: "workPageSet",
            region: "east",
            tbar:[{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + this.lang.modelText + "</span>",
                labelWidth: 70,
                id: "tbarAppModule",
                async: false,
                canClear: false,
                colon: false,
                name: "appModuleName",
                field:["appModuleId"],
                store: {
                    url: _ctxPath + "/businessModel/listAllAppModule"
                },
                reader:{
                    name: "name",
                    field:["id"]
                },
                afterRender:function () {
                    this.loadData({
                        "appModuleName": g.appModule.name,
                        "appModuleId": g.appModule.id
                    });
                },
                afterSelect: function (data) {
                    if (!data) {
                        EUI.ProcessStatus({
                            success: false,
                            msg: this.lang.chooseAppModelText
                        });
                        return;
                    }
                    EUI.getCmp("workPageSet").setPostParams({
                        "appModule.id": data.data.id
                    },true);
                }
            }],
            gridCfg: {
                url: _ctxPath + "/businessModel/listAllNotSelectEdByAppModuleId",
                postData: {
                    'appModule.id': g.appModule.id,
                    businessModelId: data.id
                },
                hasPager: false,
                shrinkToFit: false,
                multiselect: true,
                colModel: [{
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: g.lang.nameText,
                    width: 150,
                    name: "name",
                    index: "name"
                }, {
                    label: g.lang.urlViewAddressText,
                    width: 350,
                    name: "url",
                    index: "url"
                }]
            }
        };
    },
    //工作界面配置-左右选择按钮
    getCenterIcon: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            width: 40,
            height: 300,
            border: false,
            html:
            '<div class="ecmp-common-moveright arrow-right"></div>' +
            '<div class="ecmp-common-leftmove arrow-left"></div>'
        }
    },
    //工作界面配置-已分配
    getLeftGrid: function (data) {
        var g = this;
        return {
            xtype: "GridPanel",
            width: 370,
            height: 300,
            title:"已分配",
            id: "workPageSelect",
            region: "west",
            gridCfg: {
                loadonce: true,
                url: _ctxPath + "/businessModel/listAllSelectEdByAppModuleId",
                postData: {
                    // 'appModule.id': g.appModule.id,
                    businessModelId: data.id
                },
                hasPager: false,
                shrinkToFit: false,
                multiselect: true,
                colModel: [{
                    name: "id",
                    index: "id",
                    hidden: true
                },{
                    name: "appModuleId",
                    index: "appModuleId",
                    hidden: true
                }, {
                    label: g.lang.nameText,
                    width: 150,
                    name: "name",
                    index: "name"
                }, {
                    label: g.lang.urlViewAddressText,
                    width: 350,
                    name: "url",
                    index: "url"
                }]

            }
        }
    },
    operateBtnEvents: function () {
        var g = this;
        $(".update-businessModel").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.updateBusinessModel(data);
        });
        $(".delete-businessModel").live("click", function () {
            var rowData = g.gridCmp.getSelectRow();
            g.deleteBusinessModel(rowData);
        });
        $(".ecmp-common-view").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.lookPropertyWindow(data);
            g.getProperty(data);
        });
        $(".ecmp-common-configuration").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.showWorkPageWindow(data);
        });
        $(".ecmp-common-set").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.businessModelId = data.id;
            g.showServiceUrlWindow(data);
        });
        $(".delete-serviceurl").live("click", function () {
            var rowData = EUI.getCmp("serviceUrlGridPanel").getSelectRow();
            g.deleteServiceUrl(rowData);
        });
        $(".update-serviceurl").live("click", function () {
            var data = EUI.getCmp("serviceUrlGridPanel").getSelectRow();
            g.updateServiceUrl(data);
        });
        $(".config-excutor").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.showExecutorConfigWindow(data);
        });
        $(".update-executorconfig").live("click", function () {
            var data = EUI.getCmp("executorConfigGridPanel").getSelectRow();
            g.updateExecutorConfig(data);
        });
        $(".delete-executorconfig").live("click", function () {
            var rowData = EUI.getCmp("executorConfigGridPanel").getSelectRow();
            g.deleteExecutorConfig(rowData);
        });
    },
    updateExecutorConfig: function (data) {
        var g = this;
        var win = EUI.Window({
            title: this.lang.updatExecutorConfigText,
            iconCss:"ecmp-eui-edit",
            width:380,
            height: 380,
            padding: 15,
            isOverFlow: false,
            items: [{
                xtype: "FormPanel",
                id: "updateExecutorConfig",
                padding: 13,
                items: [{
                    xtype: "TextField",
                    title: this.lang.businessModelIdText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 260,
                    value: data["businessModel.id"],
                    hidden: true
                },{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 80,
                    allowBlank: false,
                    name: "id",
                    width: 260,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: this.lang.codeText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "code",
                    width: 260,
                    value: data.code
                }, {
                    xtype: "TextField",
                    title: this.lang.nameText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "name",
                    width: 260,
                    value: data.name
                }, {
                    xtype: "TextArea",
                    title: this.lang.apiLocationText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "url",
                    width: 260,
                    height:60,
                    value: data.url
                },{
                    xtype: "TextArea",
                    title: this.lang.paramText,
                    labelWidth: 80,
                    allowBlank: true,
                    name: "param",
                    width: 260,
                    height:60,
                    value: data.param
                }, {
                    xtype: "TextArea",
                    title: this.lang.depictText,
                    labelWidth: 80,
                    allowBlank: true,
                    name: "depict",
                    width: 260,
                    height: 80,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateExecutorConfig");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveExecutorConfig(data, win);
                }
            }]
        });
    },
    deleteExecutorConfig: function (rowData) {
        var g = this;
        var infoBox = EUI.MessageBox({
            title: g.lang.tiShiText,
            msg: g.lang.ifDelMsgText,
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            },{
                title: g.lang.sureText,
                selected: true,
                handler: function () {
                    infoBox.remove();
                    var myMask = EUI.LoadMask({
                        msg: g.lang.nowDelMsgText
                    });
                    EUI.Store({
                        url: _ctxPath + "/flowExecutorConfig/delete",
                        params: {
                            id: rowData.id
                        },
                        success: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                            EUI.getCmp("executorConfigGridPanel").refreshGrid();
                            EUI.getCmp("searchBox").setValue("");
                         //   EUI.getCmp("executorConfigGridPanel").deleteRow(rowData.id);
                        },
                        failure: function (result) {
                            myMask.hide();
                            EUI.ProcessStatus(result);
                        }
                    });
                }
            }]
        });
    },
    addWorkPageEvent: function () {
        var g = this, selectData = [];
        $(".arrow-right").live("click", function () {
            var rightGrid = EUI.getCmp("workPageSelect");
            var rowDatas = rightGrid.getSelectRow();
            var gridPanel = EUI.getCmp("workPageSet");
            var selectData = gridPanel.getGridData();
            for (var i = 0; i < rowDatas.length; i++) {
                var item = rowDatas[i];
                if (!g.isInArray(item, selectData)) {
                    if(item.appModuleId == EUI.getCmp("tbarAppModule").getSubmitValue().appModuleId){
                        gridPanel.grid.addRowData(item.id, item);
                    }
                    rightGrid.deleteRow(item.id);
                }
            }
        });
        $(".arrow-left").live("click", function () {
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
        var result = "";
        for (var i = 0; i < gridData.length; i++) {
            result += gridData[i].id + ",";
        }
        result = (result.substring(result.length - 1) == ',') ? result.substring(0, result.length - 1) : result;
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
            iconCss: "ecmp-eui-add",
            width: 380,
            height: 300,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "addFlowServiceUrl",
                padding: 13,
                items: [{
                    xtype: "TextField",
                    title:g.lang.businessModelIdText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 260,
                    value: data.id,
                    hidden:true
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "code",
                    width: 260
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "name",
                    width: 260
                }, {
                    xtype: "TextArea",
                    title: "URL",
                    labelWidth: 80,
                    allowBlank: false,
                    name: "url",
                    width: 260,
                    height:60
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 80,
                    name: "depict",
                    width: 260,
                    height:80
                }]
            }],
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("addFlowServiceUrl");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveFlowServiceUrl(data, win);
                }
            }]
        });
    },
    saveFlowServiceUrl: function (data, winCmp) {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: g.lang.nowSaveMsgText
        });
        EUI.Store({
            url: _ctxPath + "/flowServiceUrl/save",
            params: data,
            success: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
                EUI.getCmp("serviceUrlGridPanel").grid.trigger("reloadGrid");
                winCmp.remove();
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
                myMask.hide();
            }
        });
    },
    updateServiceUrl: function (data) {
        var g = this;
        var win = EUI.Window({
            title: g.lang.updateServiceUrlText,
            iconCss: "ecmp-eui-edit",
            width: 380,
            height: 300,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "updateFlowServiceUrl",
                padding: 13,
                items: [{
                    xtype: "TextField",
                    title: "ID",
                    labelWidth: 80,
                    allowBlank: false,
                    name: "id",
                    width: 260,
                    value: data.id,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.businessModelIdText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "businessModel.id",
                    width: 260,
                    value: g.businessModelId,
                    hidden: true
                }, {
                    xtype: "TextField",
                    title: g.lang.codeText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "code",
                    width: 260,
                    value: data.code
                }, {
                    xtype: "TextField",
                    title: g.lang.nameText,
                    labelWidth: 80,
                    allowBlank: false,
                    name: "name",
                    width: 260,
                    value: data.name
                }, {
                    xtype: "TextArea",
                    title: "URL",
                    labelWidth: 80,
                    allowBlank: false,
                    name: "url",
                    width: 260,
                    height:60,
                    value: data.url
                }, {
                    xtype: "TextArea",
                    title: g.lang.depictText,
                    labelWidth: 80,
                    name: "depict",
                    width: 260,
                    height:80,
                    value: data.depict
                }]
            }],
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    win.remove();
                }
            },{
                title: g.lang.saveText,
                selected: true,
                handler: function () {
                    var form = EUI.getCmp("updateFlowServiceUrl");
                    if (!form.isValid()) {
                        EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
                        return;
                    }
                    var data = form.getFormValue();
                    g.saveFlowServiceUrl(data, win);
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
                title: g.lang.cancelText,
                handler: function () {
                    infoBox.remove();
                }
            },{
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
                            EUI.getCmp("serviceUrlGridPanel").grid.trigger("reloadGrid");
                        },
                        failure: function (result) {
                            EUI.ProcessStatus(result);
                            myMask.hide();
                        }
                    });
                }
            }]
        });
    }
});




