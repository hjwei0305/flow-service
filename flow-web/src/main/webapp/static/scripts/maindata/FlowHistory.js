/**
 * 流程历史页面
 */
if(!window.Flow){
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
    designInstanceId:null,
    designFlowDefinationId:null,
    versionCode:null,
    initComponent: function () {
        var g = this;
        g.getData();
    },
    getData: function () {
        var g = this;
        var myMask = EUI.LoadMask({
            msg: "正在加载，请稍候..."
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
                name: item.flowName + "," + item.creatorName + "," + item.createdDate + "发起",
                instanceId: item.id,
                data:data[i]
            };
            instanceData.push(instanceItem);
            if(this.instanceId == item.id){
                this.defaultData = instanceItem;
                this.designInstanceId = instanceItem.id;
                this.designFlowDefinationId = instanceItem.data.flowInstance.flowDefVersion.flowDefination.id
                this.versionCode = instanceItem.data.flowInstance.flowDefVersion.versionCode
            }
        }
        this.instanceData = instanceData;
        if(!this.instanceId ){
            this.defaultData = instanceData[0];
            this.designInstanceId = instanceData[0].id;
            this.designFlowDefinationId = instanceData[0].data.flowInstance.flowDefVersion.flowDefination.id;
            this.versionCode = instanceData[0].data.flowInstance.flowDefVersion.versionCode;
        }
    },
    showWind: function () {
        var g = this;
        g.win = EUI.Window({
            title: "流程信息",
            width: 650,
            height: 512,
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
                title: "<span style='font-weight: bold'>" + "启动历史" + "</span>",
                width: 418,
                field: ["id"],
                labelWidth: 80,
                name: "name",
                id: "flowInstanceId",
                listWidth: 200,
                reader: {
                    name: "name",
                    field: ["id"]
                },
                data: this.instanceData,
                afterSelect: function (data) {
                    console.log(data.data.id);
                    this.designInstanceId = data.data.id;
                    this.designFlowDefinationId = data.data.data.flowInstance.flowDefVersion.flowDefination.id;
                    this.versionCode  = data.data.data.flowInstance.flowDefVersion.versionCode;
                    $(".statuscenter-info").html("");
                    $(".flow-historyprogress").html("");
                    g.showFlowHistoryData(data.data.data.flowHistoryList);
                    g.showFlowStatusData(data.data.data.flowTaskList);
                }
            },{
                xtype: "Button",
                title: "查看流程图",
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
            html: g.getTopHtml() + g.getCenterHtml()
        }
    },
    getTopHtml: function () {
        return '<div class="top">' +
            '				<div class="top-left navbar flowselect">' +
            '					<div class="flow-tabicon flow-statusimg"></div>' +
            '					<div class="flow-stutsfield text">' +
            '						当前处理状态' +
            '					</div>' +
            '				</div>' +
            '				<div class="flow-line"></div>' +
            '				<div class="top-center navbar">' +
            '					<div class="flow-tabicon flow-historyimg"></div>' +
            '					<div class="flow-historyfield text">' +
            '						流程处理历史' +
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
            '					<div class="statuscenter-info">' +
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
            '					</div>' +
            '				</div>';
    },

    //拼接流程历史头部数据的html
    showFlowHistoryTopData: function (data) {
        var g = this;
        var html = "";
        html = '<div class="flow-startimg"></div>' +
            '							<div class="flow-startfield">流程启动</div>' +
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
                '								<div class="flow-historyright">处理人：' + item.executorName + ' (' + item.actEndTime+ ')</div>' +
                '							</div>' +
                '							<div class="flow-usetime">耗时：' + g.changeLongToString(item.actDurationInMillis) + '</div>' +
                '							<div class="flow-remark">处理摘要：' + item.depict + '</div>' +
                '							 <div class="clear"></div> ' +
                '						</div>';
        }
        $(".flow-historyprogress").append(html);
    },
    //拼接流程状态数据的html
    showFlowStatusData: function (data) {
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            html += '<div class="flow-progress">' +
                '						<div class="flow-progresstitle">' + item.taskName + '</div>' +
                '						<div class="flow-progressinfo">' +
                '							<div class="flow-progressinfoleft">等待处理人：' + item.executorName + '</div>' +
                '							<div class="flow-progressline"></div>' +
                '							<div class="flow-progressinforight">任务到达时间：' + item.createdDate + '</div>' +
                '						</div>' +
                '					</div>';
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
            strVar += day + "天";
        }
        if (hour > 0) {
            strVar += hour + "小时";
        }
        if (minute > 0) {
            strVar += minute + "分";
        }
        if (second > 0) {
            strVar += second + "秒";
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
            title: "流程图",
            url: _ctxPath + "/design/showLook?id="+ this.designFlowDefinationId+ "&instanceId="+this.designInstanceId+"&versionCode="+this.versionCode,
            id : this.designInstanceId
        };
        g.addTab(tab);
    },
    addTab: function (tab) {
        window.open(tab.url);
    }
});