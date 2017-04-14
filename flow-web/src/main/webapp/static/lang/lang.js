var common_lang = {};
if (EUI.AppModuleView) {
    EUI.apply(EUI.AppModuleView.prototype.lang, {
        addResourceText: "新增资源",
        operateText: "操作",
        codeText: "代码",
        nameText: "名称",
        depictText: "描述",
        tiShiText: "提示",
        ifDelMsgText: "确定删除吗？",
        sureText: "确定",
        nowDelMsgText: "正在删除,请稍后....",
        cancelText: "取消",
        updateAppModuleText: "修改实体模型",
        saveText: "保存",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputDepictMsgText: "请输入描述",
        addNewAppModuleText: "新增实体模型",
        nowSaveMsgText: "正在保存，请稍候...",
    }, common_lang);
}

if (EUI.BusinessModelView) {
    EUI.apply(EUI.BusinessModelView.prototype.lang, {
        addResourceText: "新增资源",
        operateText: "操作",
        codeText: "代码",
        nameText: "名称",
        classPathText: "类全路径",
        depictText: "描述",
        belongToAppModuleText: "所属应用模块",
        tiShiText: "提示",
        ifDelMsgText: "确定删除吗？",
        sureText: "确定",
        nowDelMsgText: "正在删除,请稍后....",
        cancelText: "取消",
        updateBusinessModelText: "修改业务模型",
        saveText: "保存",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputClassPathMsgText: "请输入类全路径",
        inputDepictMsgText: "请输入描述",
        chooseBelongToAppModuleText: "请选择所属应用模块",
        addNewBusinessModelText: "新增业务模型",
        nowSaveMsgText: "正在保存，请稍候...",
    });
}

if (EUI.FlowServiceUrlView) {
    EUI.apply(EUI.FlowServiceUrlView.prototype.lang, {
        addResourceText: "新增资源",
        operateText: "操作",
        codeText: "代码",
        nameText: "名称",
        urlText: "URL",
        depictText: "描述",
        tiShiText: "提示",
        ifDelMsgText: "确定删除吗？",
        sureText: "确定",
        nowDelMsgText: "正在删除,请稍后....",
        cancelText: "取消",
        updateFlowServiceUrlText: "修改服务地址管理",
        saveText: "保存",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputUrlMsgText: "请输入URL",
        inputDepictMsgText: "请输入描述",
        addNewFlowServiceUrlText: "新增服务地址管理",
        nowSaveMsgText: "正在保存，请稍候...",
    });
}

if (EUI.FlowTypeView) {
    EUI.apply(EUI.FlowTypeView.prototype.lang, {
        addResourceText: "新增资源",
        operateText: "操作",
        codeText: "代码",
        nameText: "名称",
        depictText: "描述",
        belongToBusinessModelText: "所属业务实体模型",
        tiShiText: "提示",
        ifDelMsgText: "确定删除吗？",
        sureText: "确定",
        nowDelMsgText: "正在删除,请稍后....",
        cancelText: "取消",
        updateFlowTypeText: "修改流程类型",
        saveText: "保存",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputDepictMsgText: "请输入描述",
        chooseBelongToBusinessModelText: "请选择所属业务实体模型",
        belongToBusinessText: "所属业务实体",
        addNewFlowTypeText: "新增流程类型",
        nowSaveMsgText: "正在保存，请稍候...",
    });
}
if (EUI.WorkFlowView) {
    EUI.apply(EUI.WorkFlowView.prototype.lang, {
        eventTitleText: "事件",
        TaskTextTitleText: "任务",
        GatewayTextTitleText: "网关",
        noConnectLineText: "{0}节点没有进行连线",
        startEventText: "开始",
        endEventText: "结束",
        userTaskText: "审批任务",
        serviceTaskText: "服务任务",
        scriptTaskText: "脚本任务",
        emailTaskText: "邮件任务",
        manualTaskText: "手工任务",
        exclusiveGatewayText: "排他网关",
        parallelGatewayText: "并行网关",
        inclusiveGatewayText: "包容网关",
        eventGatewayText: "事件网关"
    }, common_lang);
}