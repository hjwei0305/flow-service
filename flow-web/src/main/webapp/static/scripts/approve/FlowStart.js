/**
 * 采购页面
 */
window.Flow = {};
EUI.ns("Flow.flow");
Flow.FlowStart = function (options) {
    return new Flow.flow.FlowStart(options);
};
Flow.flow.FlowStart = EUI.extend(EUI.CustomUI, {
    data:null,
    businessKey:null,
    typeId:null,
    initComponent: function () {
        var g = this;
        var item = [];
       if(this.data.flowTypeList.length ==1){
           item = [this.initWindContainer()]
         }else{
           item = [this.initWindTbar(g.data), this.initWindContainer()]
       }
        g.win = EUI.Window({
            title: "流程信息",
            id:"flowStratWind",
            width: 600,
            isOverFlow: false,
            padding: 0,
            items: item,
            buttons: [{
                title:"提交",
                selected: true,
                handler: function () {
                    g.submit();
                }
            }, {
                title: "取消",
                handler: function () {
                    g.win.remove();
                }
            }]
        });
        this.addEvents();
        g.showChooseUser();
    },
    addEvents: function () {
        var g = this;
        $(".flow-user-item").live("click", function () {
            var type = $(this).attr("type");
            if (type != "countersign") {
                $(".flow-user-item").removeClass("select");
                $(this).addClass("select");
            }
        });

        // $(".submit").bind("click", function () {
        //     g.submit();
        // });
    },
    showChooseFlowTypeAndExecutorWind:function(flowTypeList){
        var g = this;
        window = EUI.Window({
            title: "选择流程类型",
            width: 600,
            layout: "border",
            padding: 0,
            items: [this.initWindTbar(flowTypeList), this.initWindContainer()]
        });
    },
    initWindTbar: function (data) {
        var g = this;
        g.typeId =data.flowTypeList[0].id;
        var flowTypeList = data.flowTypeList;
        return {
            xtype: "ToolBar",
            region: "north",
            height: 40,
            padding: 8,
            isOverFlow: false,
            border: false,
            items: [{
                xtype: "ComboBox",
                field : ["id"],
                width:250,
                labelWidth:100,
                name : "name",
                id : "flowTypeId",
                title: "<span style='font-weight: bold'>" + "流程类型"+ "</span>",
                async : true,
                store : null,
                listWidth : 200,
                reader: {
                    name: "name",
                    field: ["id"]
                },
                data : flowTypeList,
                value:flowTypeList[0].name,
                submitValue : {
                    id : flowTypeList[0].id
                },
                afterSelect: function (data) {
                    var myMask = EUI.LoadMask({
                        msg: "正在加载，请稍候..."
                    });
                    g.typeId = data.data.id;
                    EUI.Store({
                        url: _ctxPath + "/defaultBusinessModel2/startFlow",
                        params: {
                            businessKey:g.businessKey,
                            businessModelCode:'com.ecmp.flow.entity.DefaultBusinessModel2',
                            typeId:g.typeId
                        },
                        success: function (result) {
                            g.data = result.data;
                           g.showChooseUser();
                            myMask.hide();
                        }, failure: function (result) {
                         myMask.hide();
                        }
                    });
                }
            }]
        };
    },
    initWindContainer: function () {
        var g = this;
        return {
            xtype: "Container",
            region: "center",
            height:400,
            border:true,
            style: {
                "border-radius": "3px"
            },
            html:g.getContainerHtml()
        };
    },
    getContainerHtml:function () {
        return    '<div class="chooseExecutor"></div>';
    },
    showChooseUser: function () {
        var  data= this.data.nodeInfoList;
        $(".flow-approve").hide();
        $(".flow-chooseuser").show();
        $(".flow-node-box").remove();
        var html = "";
        for (var i = 0; i < data.length; i++) {
            var node = data[i];
            var nodeType = "普通任务";
            var iconCss = "choose-radio";
            if (node.flowTaskType == "singleSign") {
                nodeType = "单签任务";
                iconCss = "choose-checkbox";
            } else if (node.flowTaskType == "countersign") {
                nodeType = "会签任务";
                iconCss = "choose-checkbox";
            }
            var nodeHtml = '<div class="flow-node-box" index="' + i + '">' +
                '<div class="flow-excutor-title">' + node.name + '-[' + nodeType +
                ']</div><div class="flow-excutor-content">';
            for (var j = 0; j < node.executorSet.length; j++) {
                var item = node.executorSet[j];
                nodeHtml += '<div class="flow-user-item" type="' + node.flowTaskType + '" id="' + item.id + '">' +
                    '<div class="choose-icon ' + iconCss + '"></div>' +
                    '<div class="excutor-item-title">姓名：' + item.name + '，岗位：' + item.positionName +
                    '，组织机构：' + item.organizationName + '，编号：' + item.code + '</div>' +
                    '</div>';
            }
            nodeHtml += "</div></div>";
            html += nodeHtml;
        }
       $(".chooseExecutor").after(html);
    },
    getSelectedUser: function () {
        var users = [];
        var nodeDoms = $(".flow-node-box");
        for (var i = 0; i < nodeDoms.length; i++) {
            var nodeDom = $(nodeDoms[i]);
            var index = nodeDom.attr("index");
            var data = this.data.nodeInfoList[index];
            var node = {
                nodeId: data.id,
                userVarName: data.userVarName,
                flowTaskType: data.flowTaskType
            };
            var itemDoms = $(".select", nodeDom);
            var ids = "";
            for (var j = 0; j < itemDoms.length; j++) {
                if (j > 0) {
                    ids += ",";
                }
                ids += $(itemDoms[j]).attr("id");
            }
            node.userIds = ids;
            users.push(node);
        }
        return users;
    },
    checkUserValid: function () {
        var nodeDoms = $(".flow-node-box");
        for (var i = 0; i < nodeDoms.length; i++) {
            var nodeDom = $(nodeDoms[i]);
            var index = nodeDom.attr("index");
            var data = this.data.nodeInfoList[index];
            var itemDoms = $(".select", nodeDom);
            if (itemDoms.length == 0) {
                EUI.ProcessStatus({
                    success: false,
                    msg: "请选择[" + data.name + "]的执行人"
                });
                return false;
            }
        }
        return true;
    },
    submit: function () {
        var g = this;
        if(!g.checkUserValid()){
            return;
        }
        var mask = EUI.LoadMask({
            msg: "正在保存，请稍候..."
        });
        EUI.Store({
            url: _ctxPath + "/defaultBusinessModel2/startFlow",
            params: {
                businessKey:g.businessKey,
                businessModelCode:'com.ecmp.flow.entity.DefaultBusinessModel2',
                typeId:g.typeId,
                opinion: null,
                taskList: JSON.stringify(this.getSelectedUser())
            },
            success: function (status) {
                mask.hide();
                if (status.success) {
                    g.win.close();
                } else {
                    EUI.ProcessStatus(status);
                }
            },
            failure: function (response) {
                mask.hide();
                EUI.ProcessStatus(response);
            }
        });
    }
});