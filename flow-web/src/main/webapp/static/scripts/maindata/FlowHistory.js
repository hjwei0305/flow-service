/**
 * 流程历史页面
 */
if (!window.Flow) {
    window.Flow = {};
    EUI.ns("Flow.flow");
}
Flow.FlowHistory = function (options) {
    return new Flow.flow.FlowHistory(options);
};
Flow.flow.FlowHistory = EUI.extend(EUI.CustomUI, {
    // width: null,
    // height: null,
    instanceId: null,
    businessId: null,
    defaultData: null,
    instanceData: null,
    designInstanceId: null,
    designFlowDefinationId: null,
    versionCode: null,
    isManuallyEnd: false,
    initComponent: function () {
        var g = this;
        g.getData();
    },
    getData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: g.lang.queryMaskMessageText
        });
        EUI.Store({
            url: _ctxPath + "/flowHistoryInfo/getFlowHistoryInfo",
            params: {
                businessId: g.businessId
            },
            success: function (result) {
                var flag = true;
                g.initData(result);
                g.showWind();
                g.showFlowHistoryTopData(g.defaultData.data.flowInstance);
                g.showFlowHistoryData(g.defaultData.data.flowHistoryList);
                g.showFlowStatusData(g.defaultData.data.flowTaskList);
                myMask.hide();
            }, failure: function (result) {
                myMask.hide();
            }
        });
    },
    initData: function (data) {
        var instanceData = [];
        for (var i = 0; i < data.length; i++) {
            var item = data[i].flowInstance;
            var instanceItem = {
                id: item.id,
                name: item.flowName + "," + item.creatorName + "," + item.createdDate + this.lang.startText,
                instanceId: item.id,
                data: data[i]
            };
            instanceData.push(instanceItem);
            if (this.instanceId == item.id) {
                this.defaultData = instanceItem;
                this.designInstanceId = instanceItem.id;
                this.designFlowDefinationId = instanceItem.data.flowInstance.flowDefVersion.flowDefination.id
                this.versionCode = instanceItem.data.flowInstance.flowDefVersion.versionCode
            }
        }
        this.instanceData = instanceData;
        if (!this.instanceId) {
            this.defaultData = instanceData[0];
            this.designInstanceId = instanceData[0].id;
            this.designFlowDefinationId = instanceData[0].data.flowInstance.flowDefVersion.flowDefination.id;
            this.versionCode = instanceData[0].data.flowInstance.flowDefVersion.versionCode;
        }
    },
    showWind: function () {
        var g = this;
        g.win = EUI.Window({
            title: g.lang.flowInfoText,
            width: 615,
            height: 523,
            padding: 0,
            xtype: "Container",
            layout: "border",
            border: false,
            items: [this.initTop(), this.initCenter()]
        });
        EUI.getCmp("flowInstanceId").loadData(this.defaultData);
        g.topEvent();
    },
    initTop: function () {
        var g = this;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 50,
            padding: 8,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "ComboBox",
                title: "<span style='font-weight: bold'>" + g.lang.launchHistoryText + "</span>",
                width: 365,
                field: ["id"],
                labelWidth: 80,
                name: "name",
                id: "flowInstanceId",
                reader: {
                    name: "name",
                    field: ["id"]
                },
                data: this.instanceData,
                afterSelect: function (data) {
                    g.designInstanceId = data.data.id;
                    g.designFlowDefinationId = data.data.data.flowInstance.flowDefVersion.flowDefination.id;
                    g.versionCode = data.data.data.flowInstance.flowDefVersion.versionCode;
                    $(".statuscenter-info").html("").removeClass("text-center");
                    $(".flow-historyprogress").html("");
                    $(".flow-end").css("display", "none");
                    g.showFlowHistoryData(data.data.data.flowHistoryList);
                    g.showFlowStatusData(data.data.data.flowTaskList);
                }
            }, {
                xtype: "Button",
                title: g.lang.showFlowDiagramText,
                iconCss:"ecmp-common-view",
                handler: function () {
                    $(".toptop-right").addClass("flowselect");
                    g.showDesgin()
                }
            }]
        };
    },
    initCenter: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            border: true,
            isOverFlow: false,
            html: g.getTopHtml() + g.getCenterHtml()
        }
    },
    getTopHtml: function () {
        return '<div class="top">' +
            '				<div class="top-left navbar flowselect">' +
            '					<div class="flow-tabicon flow-statusimg"></div>' +
            '					<div class="flow-stutsfield text">' +
            this.lang.processStatusText +
            '					</div>' +
            '				</div>' +
            '				<div class="flow-line"></div>' +
            '				<div class="top-center navbar">' +
            '					<div class="flow-tabicon flow-historyimg"></div>' +
            '					<div class="flow-historyfield text">' +
            this.lang.flowProcessHistoryText +
            '					</div>' +
            '				</div>' +
            '			</div>';

    },
    getCenterHtml: function () {
        var g = this;
        return g.getFlowStatusHtml() + g.getFlowHistoryHtml()
    },
    getFlowStatusHtml: function () {
        return '<div class="flow-statuscenter" style="display: block;">' +
            '					<div class="statuscenter-info ">' +
            '					</div>' +
            '				</div>';
    },
    getFlowHistoryHtml: function () {
        return '<div class="flow-hsitorycenter" style="display: none;">' +
            '					<div class="historycenter-info">' +
            '						<div class="flow-start">' +
            '						</div>' +
            '						<div class="flow-historyprogress">' +
            '						</div>' +
            '                       <div class="flow-end" style="display: none;">' +
            '							<div class="flow-endImg ecmp-flow-end"></div>' +
            '							<div class="flow-endfield">' + this.lang.flowEndText + '</div>' +
            '							<div class="flow-endright">' +
            '							</div>' +
            '						</div>';
        '					</div>' +
        '				</div>';
    },

    //拼接流程历史头部数据的html
    showFlowHistoryTopData: function (data) {
        var g = this;
        var html = "";
        html = '<div class="flow-startimg ecmp-flow-flag"></div>' +
            '							<div class="flow-startfield">' + g.lang.flowLaunchText + '</div>' +
            '							<div class="flow-startright">' +
            '								<div class="flow-startuser">' + data.creatorName + '</div>' +
            '								<div class="flow-startline"></div>' +
            '								<div class="flow-starttime">' + data.createdDate + '</div>' +
            '							</div>';
        $(".flow-start").html(html);
    },
    //拼接流程历史数据的html
    showFlowHistoryData: function (data) {
        var g = this;
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            html += '<div class="flow-historyinfoone">' +
                '							<div class="flow-historydot"></div>' +
                '							<div class="flow-historyinfotop">' +
                '								<div class="flow-historystatus">' + item.flowTaskName + '</div>' +
                '								<div class="flow-historyright">' + g.lang.processorText + item.executorName + ' (' + item.actEndTime + ')</div>' +
                '							</div>' +
                '							<div class="flow-usetime">' + g.lang.timeCunsumingText + g.changeLongToString(item.actDurationInMillis) + '</div>' +
                '							<div class="flow-remark">' + g.lang.handleAbstractText + (item.depict || g.lang.noneText) + '</div>' +
                '							 <div class="clear"></div> ' +
                '						</div>';
        }
        $(".flow-historyprogress").append(html);
        if (typeof(data[0]) == "undefined") {
            return;
        } else {
            if (data[0].flowInstance.ended == true) {
                if (data[0].flowInstance.manuallyEnd == true) {
                    g.isManuallyEnd = true;
                    $(".flow-end").css("display", "block");
                    $(".flow-endright").html(data[0].flowInstance.endDate);
                } else {
                    $(".flow-end").css("display", "block");
                    $(".flow-endright").html(data[0].flowInstance.endDate);
                }
            }
        }
    },
    //拼接流程状态数据的html
    showFlowStatusData: function (data) {
        var g = this;
        var html = "";
        if (data.length == 0) {
            if (g.isManuallyEnd) {
                html = "流程已被发起人终止";
            } else {
                html = g.lang.flowFinishedText;
            }
            $(".statuscenter-info").addClass("text-center")
        } else {
            for (var i = 0; i < data.length; i++) {
                var item = data[i];
                html += '<div class="flow-progress">' +
                    '						<div class="flow-progresstitle">' + item.taskName + '</div>' +
                    '						<div class="flow-progressinfo">' +
                    '							<div class="flow-progressinfoleft">' + g.lang.waitProcessorText + item.executorName + '</div>' +
                    '							<div class="flow-progressline"></div>' +
                    '							<div class="flow-progressinforight">' + g.lang.taskArrivalTimeText + item.createdDate + '</div>' +
                    '						</div>' +
                    '					</div>';
            }
        }
        $(".statuscenter-info").append(html)
    },
    changeLongToString: function (value) {
        var strVar = '';
        var day = Math.floor(value / (60 * 60 * 1000 * 24));
        var hour = Math.floor((value - day * 60 * 60 * 1000 * 24) / (60 * 60 * 1000));
        var minute = Math.floor((value - day * 60 * 60 * 1000 * 24 - hour * 60 * 60 * 1000) / (60 * 1000));
        var second = Math.floor((value - day * 60 * 60 * 1000 * 24 - hour * 60 * 60 * 1000 - minute * 60 * 1000) / 1000);
        if (day > 0) {
            strVar += day + this.lang.dayText;
        }
        if (hour > 0) {
            strVar += hour + this.lang.hourText;
        }
        if (minute > 0) {
            strVar += minute + this.lang.minuteText;
        }
        if (second > 0) {
            strVar += second + this.lang.secondText;
        }
        return strVar;
    },
    topEvent: function () {
        var g = this;
        $(".navbar").click(function () {
            $(this).addClass("flowselect").siblings().removeClass("flowselect");
            $(".toptop-right").removeClass("flowselect");
        });
        $(".top-left").click(function () {
            $(".flow-statuscenter").css("display", "block");
            $(".flow-hsitorycenter").css("display", "none");
        });
        $(".top-center").click(function () {
            $(".flow-statuscenter").css("display", "none");
            $(".flow-hsitorycenter").css("display", "block");
        });
    },
    showDesgin: function () {
        var g = this;
        var tab = {
            title: g.lang.flowDiagramText,
            url: _ctxPath + "/design/showLook?id=" + this.designFlowDefinationId + "&instanceId=" + this.designInstanceId + "&versionCode=" + this.versionCode,
            id: this.designInstanceId
        };
        g.addTab(tab);
    },
    addTab: function (tab) {
        window.open(tab.url);
    }
});