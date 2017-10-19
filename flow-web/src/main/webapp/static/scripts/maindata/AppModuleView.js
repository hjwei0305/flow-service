/**
 * 显示页面
 */
EUI.AppModuleView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    isEdit: false,
    initComponent: function () {
        EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            border: false,
            padding: 8,
            itemspace: 0,
            items: [this.initTbar(), this.initGrid()]
        });
        this.gridCmp = EUI.getCmp("gridPanel");
        this.addEvents();
    },
    initTbar: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 0,
            isOverFlow:false,
            border: false,
            items: [{
                xtype: "Button",
                //addText: "新增",
                title: this.lang.addText,
                iconCss:"ecmp-common-add",
                selected:true,
                CHECK_AUTH:"",
                handler: function () {
                    g.isEdit = false;
                    g.addAndEdit();
                }
            }, '->', {
                xtype: "SearchBox",
                displayText: g.lang.searchBoxText,
                onSearch: function (value) {
                    g.gridCmp.localSearch(value);
                },
                afterClear: function () {
                    g.gridCmp.localSearch("");
                }
            }]
        };
    },
    initGrid: function () {
        var g = this;
        return {
            xtype: "GridPanel",
            region: "center",
            id: "gridPanel",
            gridCfg: {
                url: _ctxPath + "/appModule/listAll",
                loadonce: true,
                searchConfig: {
                    searchCols: ["code", "name"]
                },
                colModel: [{label: this.lang.operateText, name: "operate", index: "operate", width: 80, align: "center",
                    formatter: function (cellvalue, options, rowObject) {
                        return "<i  class='ecmp-common-edit icon-space' title='"+g.lang.modifyText+"'></i><i class='ecmp-common-delete' title='"+g.lang.deleteText+"'></i>";
                    }
                },
                { name: "id", index: "id", hidden: true },
                { label: g.lang.codeText, name: "code", index: "code", width: 100, sortable: true },
                { label: g.lang.nameText, name: "name", index: "name", width: 100, sortable: true },
                { label: g.lang.remarkText, name: "remark", index: "remark", width: 180 },
                { label: g.lang.webBaseAddressText, name: "webBaseAddress", index: "webBaseAddress", width: 350 },
                { label: g.lang.apiBaseAddressText, name: "apiBaseAddress", index: "apiBaseAddress", width: 380},
                    //rankText: '排序',
                { name: "rank",index: "rank", label: g.lang.rankText, sortable: true, width: 80, align: "right" },],
                rowNum: 15,
                sortname: "rank",
                shrinkToFit: false
            }
        };
    },
    addEvents: function () {
        var g = this;
        $(".ecmp-common-edit").live("click", function () {
            var data = g.gridCmp.getSelectRow();
            g.isEdit = true;
            g.addAndEdit();
            g.editFormCmp.loadData(data);
        });
        $(".ecmp-common-delete").live("click", function () {
            var rowData = EUI.getCmp("gridPanel").getSelectRow();
            var infoBox = EUI.MessageBox({
                title: g.lang.deleteText,
                msg: g.lang.deleteHintMessageText,
                buttons: [{
                    title: g.lang.cancelText,
                    handler: function () {
                        infoBox.remove();
                    }
                },{
                    title: g.lang.okText,
                    selected:true,
                    handler: function () {
                        var myMask = EUI.LoadMask({
                            msg: g.lang.deleteMaskMessageText
                        });
                        EUI.Store({
                            url: _ctxPath + "/appModule/delete",
                            params: {
                                id: rowData.id
                            },
                            success: function (result) {
                                myMask.hide();
                                infoBox.remove();
                                var status = {
                                    msg: result.msg,
                                    success: result.success,
                                    showTime: result.success ? 2 : 60
                                };
                                if (status.success) {
                                    g.gridCmp.refreshGrid();
                                }
                                EUI.ProcessStatus(status);
                            },
                            failure: function (re) {
                                myMask.hide();
                                infoBox.remove();
                                var status = {
                                    msg: re.msg,
                                    success: false,
                                    showTime: 6
                                };
                                EUI.ProcessStatus(status);
                            }

                        });

                    }
                }]
            });
        });
    },
    addAndEdit: function () {
        var g = this;
        g.addAndEditWin = EUI.Window({
            title: g.isEdit ? g.lang.updateAppModuleText : g.lang.addNewAppModuleText,
            height: 250,
            width: 530,
            padding: 15,
            items: [{
                xtype: "FormPanel",
                id: "editForm",
                padding: 0,
                defaultConfig: {
                    xtype: "TextField",
                    labelWidth: 120,
                    width: 392
                },
                items: [{
                    name: "id",
                    hidden: true
                }, {
                    title: g.lang.codeText,
                    allowBlank: false,
                    name: "code",
                    maxlength: 20,
                    afterValid: function (value) {
                        EUI.TextField().afterValid.call(this,value);
                        this.dom.field.val(this.getValue().toUpperCase());
                    }
                }, {
                    title: g.lang.nameText,
                    allowBlank: false,
                    name: "name",
                    maxlength: 30
                }, {
                    title: g.lang.remarkText,
                    name: "remark",
                    maxlength: 255
                }, {
                    title: g.lang.rankText,
                    allowBlank: false,
                    name: "rank",
                    allowChar: "1234567890",
                    maxlength: 11,
                    minLengthText: g.lang.maxLengthText,
                    validateText: g.lang.PositiveIntegerText,
                    validater: function (value) {
                        if(value < 1) {
                            return false;
                        }
                        return true;
                    }
                }, {
                    title: g.lang.webBaseAddressText,
                    name: "webBaseAddress",
                    maxlength: 255
                }, {
                    title: g.lang.apiBaseAddressText,
                    name: "apiBaseAddress",
                    maxlength: 255
                }]
            }],
            buttons: [{
                title: g.lang.cancelText,
                handler: function () {
                    g.addAndEditWin.remove();
                }
            },{
                title: g.lang.saveText,
                selected:true,
                handler: function () {
                    g.save();
                }
            }]
        });
        g.editFormCmp = EUI.getCmp("editForm");
    },
    save: function () {
        var g = this;
        if (!g.editFormCmp.isValid()) {
            EUI.ProcessStatus({success: false,msg:g.lang.unFilledText});
            return;
        }
        var data = g.editFormCmp.getFormValue();
        if (isNaN(Number(data.rank))) {
            g.message(g.lang.needRankNumberText);
            return;
        }
        if (!g.isEdit) {
            delete data.id;
        }
        var myMask = EUI.LoadMask({
            msg: g.lang.saveMaskMessageText,
        });
        EUI.Store({
                url: _ctxPath + "/appModule/save",
                params: data,
                success: function (result) {
                    myMask.hide();
                    var status = {
                        msg: result.msg,
                        success: result.success,
                        showTime: result.success ? 2 : 60
                    };
                    if (status.success) {
                        g.addAndEditWin.remove();
                        g.gridCmp.refreshGrid();
                    }
                    EUI.ProcessStatus(status);
                },
                failure: function (re) {
                    myMask.hide();
                    var status = {
                        msg: re.msg,
                        success: false,
                        showTime: 6
                    };
                    g.addAndEditWin.remove();
                    EUI.ProcessStatus(status);
                }
            }
        );

    },
    message: function (msg) {
        var g = this;
        var message = EUI.MessageBox({
            border: true,
            title: g.lang.hintText,
            showClose: true,
            msg: msg,
            buttons: [{
                title: g.lang.okText,
                selected:true,
                handler: function () {
                    message.remove();
                }
            }]
        });
    }
})
;