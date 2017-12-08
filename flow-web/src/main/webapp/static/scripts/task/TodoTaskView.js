// 待办部分
EUI.TodoTaskView = EUI.extend(EUI.CustomUI, {
    renderTo: null,
    params: {
        page: 1,
        rows: 5,
        modelId: null,
        S_createdDate: "ASC",
        S_priority: "DESC",
        Quick_value: null
    },
    initComponent: function () {
        var g = this;
        this.boxCmp = EUI.Container({
            renderTo: this.renderTo,
            layout: "border",
            items: [{
                xtype: "Container",
                height: 70,
                region: "north",
                padding: 0,
                itemspace: 0,
                border: false,
                items: [{
                    xtype: "Label",
                    content: "我的工作 > 待办工作"
                }, {
                    xtype: "ToolBar",
                    height: 36,
                    items: this.initToolBar(),
                    afterRender: function () {
                        EUI.container.Container.superclass.afterRender.call(this);
                        this.content.css("paddingTop", 12);
                    }
                }]
            }, {
                xtype: "Container",
                region: "center",
                padding: 0,
                html: '<div class="info-left todo-info"></div><div class="load-more"><span>获取更多</span></div>'
                + '<div class="empty-data"><div class="not-data-msg">------------您当前没有待办事项------------</div></div>'
            }]
        });
        this.dataDom = $(".todo-info", "#" + this.renderTo);
        this.emptyDom = $(".empty-data", "#" + this.renderTo);
        this.loadMoreDom = $(".load-more", "#" + this.renderTo);
        this.getData();
        this.addEvents();
    },
    initToolBar: function () {
        var g = this;
        return [{
            xtype: "Label",
            content: "<span>全部</span><span class='todo-count'>25</span>",
            customCss: "module-all",
            onClick: function () {
                if (!g.params.modelId) {
                    return;
                }
                g.params.modelId = null;
                g.refresh();
            }
        }, {
            xtype: "ComboBox",
            name: "businessModelName",
            displayText: "筛选",
            async: false,
            store: {
                url: _ctxPath + "/flowTask/listFlowTaskHeader"
            },
            afterSelect: function (data) {
                if (g.params.modelId == data.data.businessModeId) {
                    return;
                }
                g.params.page = 1;
                g.params.modelId = data.data.businessModeId;
                g.refresh();
            },
            afterClear: function () {
                g.params.page = 1;
                g.params.modelId = null;
                g.getData();
            }
        }, {
            xtype: "SearchBox",
            name: "Quick_value",
            onSearch: function (value) {
                if (g.params.Quick_value == value) {
                    return;
                }
                g.params.page = 1;
                g.params.Quick_value = value;
                g.getData();
            }
        }, {
            xtype: "Button",
            title: "批量审批",
            handler: function () {
                g.hide();
                $("body").append('<div id="batchlist"></div>');
                var listview = new EUI.BatchApproveListView({
                    returnBack: function () {
                        g.show();
                    }
                });
                $("body").trigger("updatenowview", [listview]);
            }
        }, "->", {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-eui-leaf' style='vertical-align: middle;color:#3671cf;'></i><span>已办工作</span>",
            onClick: function () {
                $("body").trigger("completetask");
            }
        }, {
            xtype: "Label",
            style: {
                cursor: "pointer"
            },
            content: "<i class='ecmp-sys-syslog' style='vertical-align: middle;color:#3671cf;'></i><span>我的单据</span>",
            onClick: function () {
                $("body").trigger("myorder");
            }
        }];
    },

    //待办内容部分的数据调用
    getData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载,请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowTask/listFlowTaskWithAllCount",
            params: this.params,
            success: function (result) {
                if (result.allTotal > 0) {
                    $(".todo-count").text(result.allTotal).show();
                } else {
                    $(".todo-count").text(0).hide();
                }
                if (result.records == 0) {
                    g.params.page = 1;
                    g.showEmptyWorkInfo();
                } else if (result.rows.length > 0) {
                    g.showContent(result);
                    g.showData(result.rows);
                } else {
                    EUI.ProcessStatus({
                        success: true,
                        msg: "没有更多数据"
                    });
                }
                if (result.rows.length == this.params.rows) {
                    g.params.page++;
                }
                myMask.hide();
            },
            failure: function (result) {
                myMask.hide();
                EUI.ProcessStatus(result);
            }
        });
    },
    //待办里面内容部分的循环
    showData: function (items) {
        var g = this;
        for (var j = 0; j < items.length; j++) {
            var rejectHtml = items[j].canReject ? '<div class="todo-btn reject-btn"><i class="ecmp-common-return reject-icon" title="驳回"></i><span>驳回</span></div>' : '';
            var nodeType = JSON.parse(items[j].taskJsonDef).nodeType;
            var claimTaskHtml = nodeType == "SingleSign" && !items[j].actClaimTime ? '<div class="todo-btn claim-btn"><i class="ecmp-common-claim claim-icon" title="签收"></i><span>签收</span></div>' : '';
            var flowInstanceCreatorId = items[j].flowInstance ? items[j].flowInstance.creatorId : "";
            var workRemark = null;
            if(items[j].flowInstance.businessModelRemark && items[j].flowInstance.businessModelRemark!='null'){
                workRemark =  items[j].flowInstance.businessCode + '-' + items[j].flowInstance.businessModelRemark;
            }else {
                workRemark =  items[j].flowInstance.businessCode;
            }
            var endFlowHtml = items[j].canSuspension && flowInstanceCreatorId == items[j].executorId ? '<div class="todo-btn endFlow-btn"><i class="ecmp-flow-end endFlow-icon icon-size" title="终止"></i><span>终止</span></div>' : '';var itemdom = $('<div class="info-item">' +
                '                 <div class="item">' +
                '                     <span class="flow-text">' + items[j].flowName + '_' + items[j].taskName + '</span>' +
                '                 </div>' +
                '                 <div class="item flow-digest">' +
                '                     <span class="digest">' + workRemark + '</span></span>' +
                '                 </div>' +
                '                 <div class="item operation">' +
                '                     <div class="end">'
                + claimTaskHtml +
                '                          <div class="todo-btn approve-btn"><i class="ecmp-eui-edit end-icon handle-icon-size" title="审批"></i><span>处理</span></div>'
                // +'                          <div class="todo-btn turn-to-do-btn"><i class="ecmp-flow-turn-to-do-c turn-to-do-icon handle-icon-size" title="转办"></i><span>转办</span></div>'
                + rejectHtml + endFlowHtml +
                '                          <div class="todo-btn look-approve-btn"><i class="ecmp-common-view look-icon look-approve" title="查看表单"></i><span>查看表单</span></div>' +
                '                          <div class="todo-btn flowInstance-btn"><i class="ecmp-flow-history time-icon flowInstance icon-size" title="流程历史"></i><span>流程历史</span></div>' +
                '                     </div>' +
                '                     <span class="item-right task-item-right">' +
                '                          <div class="userName">发起人：' + items[j].creatorName + '</div>' +
                '                          <div class="todo-date"><i class="ecmp-flow-history flow-time-icon time-icon-size" title="创建时间"></i><span>' + this.countDate(items[j].createdDate) + '</span></div>' +
                '                     </span>' +
                '                 </div>' +
                '</div>');
            itemdom.data(items[j]);
            this.dataDom.append(itemdom);
        }
        EUI.resize(this.boxCmp);
    }
    ,
    //计算时间几天前
    countDate: function (startTime) {
        if (!startTime) {
            return;
        }
        var g = this;
        var date = new Date();
        var endTime = date.getTime();
        startTime = startTime.replace(new RegExp("-", "gm"), "/");
        startTime = new Date(startTime).getTime();
        var time = endTime - startTime;
        if (time <= 60000) {//如果结束时间小于开始时间
            return "刚刚";
        } else {
            //计算出相差天数
            var days = Math.floor(time / (24 * 3600 * 1000));
            if (days > 0) {
                return days + '天前';
            }
            //计算出小时数
            var leave1 = time % (24 * 3600 * 1000);   //计算天数后剩余的毫秒数
            if (leave1 == 0) {//如果leave1=0就不需要在做计算，直接把0赋给hours
                return hours = 0;
            } else {
                var hours = Math.floor(leave1 / (3600 * 1000));
                if (hours > 0) {
                    return hours + '小时前';
                }
            }
            //计算相差分钟数
            var leave2 = leave1 % (3600 * 1000);        //计算小时数后剩余的毫秒数
            var minutes = Math.floor(leave2 / (60 * 1000));
            return minutes + '分钟前';
        }
    }
    ,
    show: function () {
        this.boxCmp.show();
        $("body").trigger("updatenowview", [this]);
    }
    ,
    hide: function () {
        this.boxCmp.hide();
    }
    ,
    showContent: function (result) {
        this.dataDom.show();
        if (this.params.page == 1) {
            this.dataDom.empty();
        } else {
            var index = (this.params.page - 1) * this.params.rows - 1;
            $(".info-item:gt(" + index + ")", this.dataDom).remove();
        }
        var loaded = result.rows.length + (this.params.page - 1) * this.params.rows;
        if (result.records > loaded) {
            this.loadMoreDom.show();
        } else {
            this.loadMoreDom.hide();
        }
        this.emptyDom.hide();
    },
    //当页面没有数据时的显示内容
    showEmptyWorkInfo: function () {
        this.emptyDom.show();
        this.dataDom.hide();
        this.loadMoreDom.hide();
        this.dataDom.empty();
        EUI.resize(this.boxCmp);
    }
    ,
    addEvents: function () {
        var g = this;
        $(".not-data-msg", "#" + this.renderTo).bind("click", function () {
            g.getData();
        });
        this.loadMoreDom.click(function () {
            g.getData();
        });
        g.approveViewWindow();
        g.lookApproveViewWindow();
        g.flowInstanceWindow();
        g.showRejectWindow();
        g.claimEvent();
        g.endFlowEvent();
        // g.showTurnToDoWindow();
    }
    ,
    //点击打开审批界面的新页签
    approveViewWindow: function () {
        var g = this;
        $(".approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var taskConfig = JSON.parse(data.taskJsonDef);
            var workPageUrl = data.taskFormUrlXiangDui;
            var joinStr = workPageUrl.indexOf("?") != -1 ? "&" : "?";
            var tab = {
                title: data.taskName,
                url: workPageUrl + joinStr + "id=" + data.flowInstance.businessId + "&taskId=" + data.id,
                id: data.id
            };
            g.addTab(tab);
        });
    }
    ,
    refresh: function (modelId) {
        this.params.page = 1;
        this.getData();
    }
    ,
    //点击打开查看表单界面的新页签
    lookApproveViewWindow: function () {
        var g = this;
        $(".look-approve-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var url = data.flowInstance.flowDefVersion.flowDefination.flowType.lookUrl;
            if(!url){
               url = data.flowInstance.flowDefVersion.flowDefination.flowType.businessModel.lookUrl;
            }
            var joinStr = url.indexOf("?") != -1 ? "&" : "?";
            var tab = {
                title: "查看表单",
                url: data.webBaseAddress + url + joinStr + "id=" + data.flowInstance.businessId,
                id: data.flowInstance.businessId
            };
            g.addTab(tab);
        });
    }
    ,
    //点击打开流程历史的新页签
    flowInstanceWindow: function () {
        var g = this;
        $(".flowInstance-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            EUI.FlowHistory({
                businessId: data.flowInstance.businessId,
                instanceId: data.flowInstance.id
            })
        });
    }
    ,
    //驳回
    showRejectWindow: function () {
        var g = this;
        $(".reject-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var win = EUI.Window({
                title: "驳回意见",
                height: 100,
                items: [{
                    xtype: "TextArea",
                    title: "驳回意见",
                    name: 'opinion',
                    id: "opinion",
                    labelWidth: 90,
                    width: 220,
                    height: 80,
                    allowBlank: false
                }],
                buttons: [{
                    title: "取消",
                    handler: function () {
                        win.remove();
                    }
                }, {
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var opinion = EUI.getCmp("opinion").getValue();
                        if (!opinion) {
                            EUI.ProcessStatus({
                                success: false,
                                msg: "请输入驳回意见"
                            });
                            return;
                        }

                        var myMask = EUI.LoadMask({
                            msg: "处理中，请稍后.."
                        });
                        EUI.Store({
                            url: _ctxPath + "/flowClient/rejectTask",
                            params: {
                                taskId: data.id,
                                opinion: opinion
                            },
                            success: function (result) {
                                myMask.hide();
                                win.close();
                                g.refresh();
                            },
                            failure: function (result) {
                                myMask.hide();
                                EUI.ProcessStatus(result);
                            }
                        })
                    }
                }]
            })
        });
    }
    ,
    //转办
    showTurnToDoWindow: function () {
        var g = this;
        var isChooseOneTitle = "选择转办人【请双击进行选择】";
        var saveBtnIsHidden = false;
        $(".turn-to-do-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            g.chooseAnyOneWind = EUI.Window({
                title: isChooseOneTitle,
                width: 720,
                layout: "border",
                height: 500,
                padding: 8,
                items: [g.initChooseUserWindTree(), g.InitChooseUserGrid(data.id)]
            });
        });
    }
    ,
    //签收事件
    claimEvent: function () {
        var g = this;
        $(".claim-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var message = EUI.MessageBox({
                border: true,
                title: "温馨提示",
                showClose: true,
                msg: "您确定要签收吗",
                buttons: [{
                    title: "取消",
                    handler: function () {
                        message.remove();
                    }
                }, {
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var myMask = EUI.LoadMask({
                            msg: "正在签收，请稍候..."
                        });
                        EUI.Store({
                            url: _ctxPath + "/flowClient/claimTask",
                            params: {taskId: data.id},
                            success: function (status) {
                                myMask.remove();
                                EUI.ProcessStatus(status);
                                g.refresh();
                            },
                            failure: function (status) {
                                myMask.hide();
                                EUI.ProcessStatus(status);
                            }
                        });
                        message.remove();
                    }
                }]
            });
        });
    },

    initChooseUserWindTopBar: function () {
        var g = this;
        return   ['->', {
            xtype: "SearchBox",
            width: 200,
            displayText: g.lang.searchDisplayText,
            onSearch: function (v) {
                EUI.getCmp("chooseAnyUserTree").search(v);
                g.selectedOrgId = null;
            },
            afterClear: function () {
                EUI.getCmp("chooseAnyUserTree").reset();
                g.selectedOrgId = null;
            }
        }];
    },
    initChooseUserWindTree: function () {
        var g = this;
        return {
            xtype: "TreePanel",
            width: 250,
            tbar: this.initChooseUserWindTopBar(),
            region: "west",
            id: "chooseAnyUserTree",
            url: _ctxPath + "/flowDefination/listAllOrgs",
            border: true,
            searchField: ["name"],
            showField: "name",
            style: {
                "background": "#fff"
            },
            onSelect: function (node) {
                g.selectedOrgId = node.id;
                //   EUI.getCmp("gridPanel").grid[0].p.postData={}
                EUI.getCmp("chooseUserGridPanel_start").setGridParams({
                    url: _ctxPath + "/customExecutor/listAllUser",
                    loadonce: true,
                    datatype: "json",
                    postData: {
                        organizationId: g.selectedOrgId
                    }
                }, true);
            },
            afterItemRender: function (nodeData) {
                if (nodeData.frozen) {
                    var nodeDom = $("#" + nodeData.id);
                    if (nodeDom.length === 0) {
                        return;
                    }
                    var itemCmp = $(nodeDom[0].children[0]);
                    itemCmp.addClass("ux-tree-freeze");
                    itemCmp.find(".ux-tree-title").text(itemCmp.find(".ux-tree-title").text() + "(已冻结)");
                }
            },
            afterShowTree: function (data) {
                this.setSelect(data[0].id);
            }
        }
    },
    initUserGridTbar: function(){
        var g =this;
        return  ['->', {
            xtype: "SearchBox",
            displayText: g.lang.searchDisplayText,
            onSearch: function (value) {
                EUI.getCmp("chooseUserGridPanel_start").localSearch(value);
            },
            afterClear: function () {
                EUI.getCmp("chooseUserGridPanel_start").restore();
            }
        }];
    },
    InitChooseUserGrid: function (taskId) {
        var g = this;
        var isShowMultiselect=false;

        return  {
            xtype: "GridPanel",
            tbar: this.initUserGridTbar(),
            region: "center",
            id: "chooseUserGridPanel_start",
            searchConfig: {
                searchCols: ["userName", "code"]
            },
            style: {"border-radius": "3px"},
            gridCfg: {
                loadonce: true,
                datatype: "local",
                multiselect: isShowMultiselect,
                colModel: [{
                    label: "用户ID",
                    name: "id",
                    index: "id",
                    hidden: true
                }, {
                    label: "用户名称",
                    name: "userName",
                    index: "userName",
                    width: 150,
                    align: "center"
                }, {
                    label: "员工编号",
                    name: "code",
                    index: "code",
                    width: 200
                }, {
                    label: "组织机构",
                    name: "organization.name",
                    index: "organization.name",
                    width: 150,
                    align: "center",
                    hidden: true
                }],
                ondblClickRow: function (rowid) {
                    // var $content2 = $(".flowstart-excutor-content2","div[index=" + currentChooseDivIndex + "]");
                    // var html = "";
                    // if(isShowMultiselect){
                    //     html = $content2.html();
                    // }
                    // var rowData = EUI.getCmp("chooseUserGridPanel_start").grid.jqGrid('getRowData', rowid);
                    // var selectedUser = [];
                    // $content2.children().each(function (index, domEle) {
                    //     selectedUser.push(domEle.id)
                    // });
                    // if (!g.itemIdIsInArray(rowData.id, selectedUser)) {
                    //     html += '<div class="flow-anyOneUser-item select" type="' + currentChooseTaskType + '" id="' + rowData.id + '">' +
                    //         '<div class="choose-icon choose-delete"></div>' +
                    //         '<div class="excutor-item-title">' +
                    //         String.format(g.lang.showUserInfo2Text, rowData["userName"], rowData["organization.name"], rowData.code) +
                    //         '</div>' +
                    //         '</div>';
                    //     $content2.html(html);
                    // }
                    g.chooseAnyOneWind.close();
                }
            }
        };
    },
    addChooseUsersInContainer: function (selectRow, currentChooseDivIndex, currentChooseTaskType) {
        var g = this;
        var $content2 = $(".flowstart-excutor-content2","div[index=" + currentChooseDivIndex + "]");
        var html = "";
        var selectedUser = [];
        $content2.children().each(function (index, domEle) {
            selectedUser.push(domEle.id)
        });
        for (var j = 0; j < selectRow.length; j++) {
            var item = selectRow[j];
            if (!g.itemIdIsInArray(item.id, selectedUser)) {
                html += '<div class="flow-anyOneUser-item select" type="' + currentChooseTaskType + '" id="' + item.id + '">' +
                    '<div class="choose-icon choose-delete"></div>' +
                    '<div class="excutor-item-title">' +
                    String.format(g.lang.showUserInfo2Text, item["userName"], item["organization.name"], item.code) +
                    '</div>' +
                    '</div>';
            }
        }
        $content2.append(html);
    },
    itemIdIsInArray: function (id, array) {
        for (var i = 0; i < array.length; i++) {
            if (id == array[i]) {
                return true;
            }
        }
        return false;
    }
    ,
    //终止事件
    endFlowEvent: function () {
        var g = this;
        $(".endFlow-btn", "#" + this.renderTo).live("click", function () {
            var itemdom = $(this).parents(".info-item");
            var data = itemdom.data();
            var message = EUI.MessageBox({
                border: true,
                title: "温馨提示",
                showClose: true,
                msg: "您确定要终止吗",
                buttons: [{
                    title: "取消",
                    handler: function () {
                        message.remove();
                    }
                }, {
                    title: "确定",
                    selected: true,
                    handler: function () {
                        var myMask = EUI.LoadMask({
                            msg: "正在终止，请稍候..."
                        });
                        EUI.Store({
                            url: _ctxPath + "/flowInstance/endFlowInstance/",
                            params: {id: data.flowInstance.id},
                            success: function (status) {
                                myMask.remove();
                                EUI.ProcessStatus(status);
                                g.refresh();
                            },
                            failure: function (status) {
                                myMask.hide();
                                EUI.ProcessStatus(status);
                            }
                        });
                        message.remove();
                    }
                }]
            });
        });
    }
    ,
    //在新的窗口打开（模拟新页签的打开方式）
    addTab: function (tab) {
        if (parent.homeView) {
            parent.homeView.addTab(tab);//获取到父窗口homeview，在其中新增页签
        } else {
            window.open(tab.url);
        }
    }
})
;