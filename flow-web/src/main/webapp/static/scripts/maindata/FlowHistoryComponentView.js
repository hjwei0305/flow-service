/**
 * 显示页面
 */

EUI.FlowHistoryComponentView = EUI.extend(EUI.CustomUI, {
    // width: null,
    // height: null,
    instanceId: null,
    initComponent: function () {
        var g = this;
        g.win = EUI.Window({
            title: "流程信息",
            width: 600,
            height: 450,
            padding: 0,
            html: g.getTopHtml() + g.getCenterHtml()
        });
        var myMask = EUI.LoadMask({
            msg: "正在加载，请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/flowHistoryInfo/getFlowHistoryInfo",
            params: {
                instanceId: this.instanceId
            },
            success: function (result) {
                g.showFlowHistoryTopData(result);
                g.showFlowHistoryData(result);
                g.showFlowStatusData(result);
                myMask.hide();
            }, failure: function (result) {
                myMask.hide();
            }
        });
        g.topEvent();
    },
    getTopHtml: function () {
        return '<div class="top">' +
            '				<div class="top-left navbar select">' +
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
            '				<div class="flow-line"></div>' +
            '				<div class="top-right navbar">' +
            '					<div class="flow-tabicon flow-infoimg"></div>' +
            '					<div class="flow-infofield text">' +
            '						流程图' +
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
            '					<div class="center-info">' +
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
            '								<div class="flow-startuser">' + data.flowStarter + '</div>' +
            '								<div class="flow-startline"></div>' +
            '								<div class="flow-starttime">' + data.flowStartTime + '</div>' +
            '							</div>';
        $(".flow-start").html(html);
    },
    //拼接流程历史数据的html
    showFlowHistoryData: function (data) {
        var g = this;
        var html = "";
        for (var i = 0; i < data.flowHandleHistoryVOList.length; i++) {
            var item = data.flowHandleHistoryVOList[i];
            html += '<div class="flow-historyinfoone">' +
                '							<div class="flow-historydot"></div>' +
                '							<div class="flow-historyinfotop">' +
                '								<div class="flow-historystatus">' + item.flowHistoryTaskName + '</div>' +
                '								<div class="flow-historyright">处理人：' + item.flowHistoryTaskExecutorName + ' (' + item.flowHistoryTaskEndTime + ')</div>' +
                '							</div>' +
                '							<div class="flow-usetime">耗时：' + g.changeLongToString(item.flowHistoryTaskDurationInMillis) + '</div>' +
                '							<div class="flow-remark">处理摘要：' + item.flowHistoryTaskRemark + '</div>' +
                '							 <div class="clear"></div> ' +
                '						</div>';

        }
        $(".flow-historyprogress").append(html);
    },
    //拼接流程状态数据的html
    showFlowStatusData: function (data) {
        var html = "";
        for (var i = 0; i < data.flowHandleStatusVOList.length; i++) {
            var item = data.flowHandleStatusVOList[i];
            html += '<div class="flow-progress">' +
                '						<div class="flow-progresstitle">' + item.flowCurHandleStatusTaskName + '</div>' +
                '						<div class="flow-progressinfo">' +
                '							<div class="flow-progressinfoleft">等待处理人：' + item.flowWaitingPerson + '</div>' +
                '							<div class="flow-progressline"></div>' +
                '							<div class="flow-progressinforight">任务到达时间：' + item.flowTaskArriveTime + '</div>' +
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
        $(".navbar").click(function () {
            $(this).addClass("select").siblings().removeClass("select");
        });
        $(".top-left").click(function () {
            $(".flow-statuscenter").css("display", "block");
            $(".flow-hsitorycenter").css("display", "none");
        });
        $(".top-center").click(function () {
            $(".flow-statuscenter").css("display", "none");
            $(".flow-hsitorycenter").css("display", "block");
        });
        $(".top-right").click(function () {

        })
    }
});