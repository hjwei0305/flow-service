if(!window.Flow) {
    window.Flow = {};
    EUI.ns("Flow.flow");
}
var common_lang = {
    operateText: "操作",
    tiShiText: "提示",
    ifDelMsgText: "确定删除吗？",
    sureText: "确定",
    nowDelMsgText: "正在删除,请稍后....",
    cancelText: "取消",
    saveText: "保存",
    nowSaveMsgText: "正在保存，请稍候...",
    codeText: "代码",
    nameText: "名称",
    depictText: "描述",
    searchNameText: "请输入名称进行搜索",
    InputSearchNameText: "请输入搜索名称",

    addText: "新增",
    hintText: "提示",
    paramsText: "参数为空!",
    addHintMessageText: "您确定要切换操作吗？未保存的数据可能会丢失!",
    okText: "确定",
    operateText: "操作",
    cancelText: "取消",
    saveText: "保存",
    modifyText: "修改",
    appendText: "添加",
    saveMaskMessageText: "正在保存，请稍候...",
    deleteText: "删除",
    deleteHintMessageText: "您确定要删除吗？",
    deleteMaskMessageText: "正在删除，请稍候...",
    queryMaskMessageText: "正在努力获取数据，请稍候...",
    submitText: "提交",
    finishText: "完成",
    editText: "编辑",
    searchByNameMsgText: "请输入名称进行搜索"
};

if (EUI.BusinessModelView) {
    EUI.apply(EUI.BusinessModelView.prototype.lang, {
        modelText: "应用模块",
        addResourceText: "新增",
        classPathText: "类全路径",
        conditonBeanText: "转换对象",
        belongToAppModuleText: "所属应用模块",
        updateBusinessModelText: "修改业务模型",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputClassPathMsgText: "请输入类全路径",
        inputConditonBeanText: "请输入转换对象",
        inputDepictMsgText: "请输入描述",
        inputWorkPageText: "请输入工作界面",
        searchNameText: "请输入名称进行搜索",
        chooseBelongToAppModuleText: "请选择所属应用模块",
        addNewBusinessModelText: "新增业务模型",
        urlViewAddressText: "URL地址",
        conditionPropertyText: "条件属性",
        propertyText: "属性",
        workPageSetText: "工作界面配置",
        typeText: "类型",
        fieldNameText: "字段名",
        noteText: "注解",
        chooseAppModelText: "请选择应用模块",
        appModelIdText: "应用模块ID",
        serviceUrlText: "服务地址管理",
        addServiceUrlText: "新增服务地址",
        businessModelIdText: "业务实体ID",
        updateServiceUrlText: "修改服务地址管理"
    }, common_lang);
}

if (EUI.FlowServiceUrlView) {
    EUI.apply(EUI.FlowServiceUrlView.prototype.lang, {
        addResourceText: "新增",
        updateFlowServiceUrlText: "修改服务地址管理",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputUrlMsgText: "请输入URL",
        inputDepictMsgText: "请输入描述",
        addNewFlowServiceUrlText: "新增服务地址管理"
    }, common_lang);
}

if (EUI.CustomExecutorView) {
    EUI.apply(EUI.CustomExecutorView.prototype.lang, {
        addResourceText: "新增",
        updateFlowServiceUrlText: "修改服务地址管理",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputUrlMsgText: "请输入URL",
        inputDepictMsgText: "请输入描述",
        addNewFlowServiceUrlText: "新增服务地址管理"
    }, common_lang);
}

if (EUI.FlowTypeView) {
    EUI.apply(EUI.FlowTypeView.prototype.lang, {
        addResourceText: "新增",
        belongToBusinessModelText: "所属业务实体模型",
        updateFlowTypeText: "修改流程类型",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputDepictMsgText: "请输入描述",
        chooseBelongToBusinessModelText: "请选择所属业务实体模型",
        belongToBusinessText: "所属业务实体",
        addNewFlowTypeText: "新增流程类型"
    }, common_lang);
}

if (EUI.FlowInstanceView) {
    EUI.apply(EUI.FlowInstanceView.prototype.lang, {
        addResourceText: "新增",
        belongToBusinessModelText: "所属业务实体模型",
        updateFlowTypeText: "修改流程类型",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputDepictMsgText: "请输入描述",
        chooseBelongToBusinessModelText: "请选择所属业务实体模型",
        belongToBusinessText: "所属业务实体",
        addNewFlowTypeText: "新增流程类型"
    }, common_lang);
}

if (EUI.FlowDefinationView) {
    EUI.apply(EUI.FlowDefinationView.prototype.lang, {
        addResourceText: "新增",
        belongToBusinessModelText: "所属业务实体模型",
        updateFlowTypeText: "修改流程类型",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputDepictMsgText: "请输入描述",
        chooseBelongToBusinessModelText: "请选择所属业务实体模型",
        belongToBusinessText: "所属业务实体",
        addNewFlowTypeText: "新增流程类型"
    }, common_lang);
}

if (EUI.WorkPageUrlView) {
    EUI.apply(EUI.WorkPageUrlView.prototype.lang, {
        modelText: "应用模块",
        inputModelText: "请选择应用模块",
        modelValueText: "全部模块",
        addBtnText: "新增",
        urlViewAddressText: "URL地址",
        appModelIdText: "应用模块ID",
        updateWorkPageUrlText: "修改工作页面",
        inputCodeMsgText: "请输入代码",
        inputNameMsgText: "请输入名称",
        inputUrlViewAddressMsgText: "请输入URL界面地址",
        inputDepictMsgText: "请输入描述",
        addNewWorkPageUrlText: "新增工作页面"
    }, common_lang);
}

if (EUI.FlowDefinationView) {
    EUI.apply(EUI.FlowDefinationView.prototype.lang, {
        moveText: "移动",
        codeText: "编号",
        nameText: "名称",
        frozenText: '是否冻结',
        rankText: '排序',
        refreshTest: "刷新",
        modifyRootText: "禁止修改根节点！",
        addHintMessageText: "请选择一个组织结构节点!",
        createNodeText: "创建节点",
        updateRootText: "禁止修改根节点!",
        moveHintMessageText: "请选择您要移动的节点！",
        rootText: "根节点",
        queryMaskMessageText: "正在努力获取数据，请稍候...",
        closeText: "关闭",
        searchDisplayText: "请输入代码或名称查询",
        processMaskMessageText: "正在处理，请稍候...",
        operateHintMessage: "请选择一条要操作的行项目!"
    }, common_lang);
}

if (EUI.WorkFlowView) {
    EUI.apply(EUI.WorkFlowView.prototype.lang, {
        eventTitleText: "事件",
        taskTitleText: "任务",
        gatewayTitleText: "网关",
        noConnectLineText: "{0}节点没有进行连线",
        startEventText: "开始",
        endEventText: "结束",
        normalTaskText: "普通任务",
        singleSignTaskText: "单签任务",
        counterSignTaskText: "会签任务",
        userTaskText: "审批任务",
        serviceTaskText: "服务任务",
        scriptTaskText: "脚本任务",
        emailTaskText: "邮件任务",
        manualTaskText: "手工任务",
        exclusiveGatewayText: "系统排他网关",
        manualExclusiveGatewayText: "人工排他网关",
        parallelGatewayText: "并行网关",
        inclusiveGatewayText: "包容网关",
        eventGatewayText: "事件网关",
        saveText: "保存",
        resetText: "清空",
        deployText: "发布"
    }, common_lang);
}

if (EUI.LookWorkFlowView) {
    EUI.apply(EUI.LookWorkFlowView.prototype.lang, {
        eventTitleText: "事件",
        taskTitleText: "任务",
        gatewayTitleText: "网关",
        noConnectLineText: "{0}节点没有进行连线",
        startEventText: "开始",
        endEventText: "结束",
        normalTaskText: "普通任务",
        singleSignTaskText: "单签任务",
        counterSignTaskText: "会签任务",
        userTaskText: "审批任务",
        serviceTaskText: "服务任务",
        scriptTaskText: "脚本任务",
        emailTaskText: "邮件任务",
        manualTaskText: "手工任务",
        exclusiveGatewayText: "系统排他网关",
        manualExclusiveGatewayText: "人工排他网关",
        parallelGatewayText: "并行网关",
        inclusiveGatewayText: "包容网关",
        eventGatewayText: "事件网关",
        saveText: "保存",
        resetText: "清空",
        deployText: "发布"
    }, common_lang);
}

 if(Flow.flow.FlowStart){
     EUI.apply(Flow.flow.FlowStart.prototype.lang, {
         launchMaskMsgText:"正在启动，请稍候",
         notFoundFlowDefinitionText:"找不到流程定义",
         notFoundFlowTypeText:"找不到流程类型",
         launchFlowText:"流程启动",
         chooseFlowTypeText:"选择流程类型",
         flowTypeText:"流程类型",
         generalTaskText:"普通任务",
         singleSignTaskText:"单签任务",
         counterSignTaskText:"会签任务",
         nameText:"姓名：",
         number2Text:"，编号：",
         jobText:"，岗位：",
         organizationText:"，组织机构：",
         chooseMsgText:"请选择[",
         executorMsgText:"]的执行人",
         launchSuccessText:"启动成功"
     },common_lang);
 }

 if(Flow.flow.FlowApprove){
     EUI.apply(Flow.flow.FlowApprove.prototype.lang, {
         businessUnitText:"业务单号：",
         docMarkerText:"制单人：",
         preExecutorText:"上一步执行人：",
         preApprovalOpinionsText:"上一步审批意见：",
         decisionText:"决策：",
         handlingSuggestionText:"处理意见",
         nextStepText:"下一步",
         formDetailText:"表单明细",
         collectText:"收起",
         chooseNextExecutorText:"选择下一步执行人",
         previousStepText:"上一步",
         spreadText:"展开",
         chooseNextExecuteNodeText:"请选择下一步执行节点",
         operationHintText:"操作提示",
         stopFlowMsgText:"当前操作流程将会结束，是否继续？",
         generalTaskText:"普通任务",
         singleSignTaskText:"单签任务",
         counterSignTaskText:"会签任务",
         nameText:"姓名：",
         number2Text:"，编号：",
         jobText:"，岗位：",
         organizationText:"，组织机构：",
         chooseMsgText:"请选择[",
         executorMsgText:"]的执行人",
         seachByIdOrNameText:"请输入用户名称或编号进行搜索",
         organization2Text:"组织机构",
         userNumberText:"员工编号",
         userNameText:"用户名称",
         userIDText:"用户ID",
         freezeText:"(已冻结)",
         arbitraryExecutorText:"任意执行人",
         chooseText:"选择",
         chooseArbitraryExecutorText:"选择任意执行人",
         chooseArbitraryExecutorMsgText:"选择任意执行人【请双击进行选择】"

     },common_lang);
 }

if(EUI.BusinessModelView){
    EUI.apply(EUI.BusinessModelView.prototype.lang, {
        showConditionPropertiesText:"查看条件属性",
        configWorkSpaceText:"配置工作界面",
        configServerLocationText:"配置服务地址",
        applyModuleCodeText:"应用模块Code",
        dataAccessObjectText:"数据访问对象",
        formURLText:"表单URL"

    },common_lang);
}

if(EUI.CustomExecutorView){
    EUI.apply(EUI.CustomExecutorView.prototype.lang, {
        businessEntityText:"业务实体",
        allocationExectorText:"分配执行人",
        userIDText:"用户ID",
        userNameText:"用户名称",
        userNumberText:"员工编号",
        organizationText:"组织机构",
        customExecutorConfigText:"自定义执行人配置"
    },common_lang);
}

if(EUI.FlowDefinationView){
    EUI.apply(EUI.FlowDefinationView.prototype.lang, {
        flowDefinitionVersionText: "流程定义版本",
        definitionIDText: "定义ID",
        definitionKEYText: "定义KEY",
        deployIDText: "部署ID",
        versionText: "版本号",
        priorityText: "优先级",
        editFlowDefinitionText: "编辑流程定义:",
        addFlowDefinitionText: "新增流程定义",
        FreezeText: "(已冻结)",
        chooseOrganizationMsgText: "请选择组织机构",
        latestVersionIDText: "最新版本ID",
        flowTypeText: "流程类型",
        businessEntityIDText: "业务实体ID",
        launchConditionUELText: "启动条件UEL",
        organizationIDText: "组织机构ID",
        organizationCodeText: "组织机构code ",
        flowDefinitionStatusText : "流程定义状态",
        unReleasedText: "未发布",
        activeText: "激活",
        frozenText: "冻结"
    },common_lang);
}

 if(Flow.flow.FlowHistory){
     EUI.apply(Flow.flow.FlowHistory.prototype.lang, {
         queryMaskMessageText: "正在加载，请稍候...",
         startText: "发起",
         flowInfoText: "流程信息",
         launchHistoryText: "启动历史",
         showFlowDiagramText: "查看流程图",
         processStatusText: "当前处理状态",
         flowProcessHistoryText: "流程处理历史",
         flowEndText: "流程结束",
         flowLaunchText: "流程启动",
         processorText: "处理人：",
         timeCunsumingText: "耗时：",
         handleAbstractText: "处理摘要：",
         noneText: "无",
         flowFinishedText: "流程已处理完成",
         waitProcessorText: "等待处理人：",
         taskArrivalTimeText: "任务到达时间：",
         dayText: "天",
         hourText: "小时",
         minuteText: "分",
         secondText: "秒",
         flowDiagramText: "流程图"

     },common_lang);
 }

if(EUI.FlowHistoryView){
    EUI.apply(EUI.FlowHistoryView.prototype.lang, {
        searchByTaskNameText: "请输入任务名进行搜索",
        reverseText: "撤销",
        taskNameText: "任务名",
        flowInstanceText: "流程实例",
        taskFormURLText: "任务表单URL",
        taskStatusText: "任务状态",
        doneText: "已办",
        reversedText: "已撤销",
        agentStatusText: "代理状态",
        processorNameText: "执行人名称",
        processorAccountText: "执行人账号",
        taskBeginTimeText: "任务开始时间",
        taskEndTimeText: "任务结束时间",
        taskProcessTimeText: "任务执行时长",
        lastUpdateTimeText: "最后更新时间",
        dayText: "天",
        hourText: "小时",
        minuteText: "分",
        secondText: "秒",
        reverseTaskMsgText: "确定撤销当前任务吗？",
        processingText: "正在执行"

    },common_lang);
}

if(EUI.FlowInstanceView){
    EUI.apply(EUI.FlowInstanceView.prototype.lang, {
        flowDefinitionVersionText: "流程定义版本",
        showDoneText: "查看已办",
        flowNameText: "流程名称",
        startTimeText: "开始时间",
        endTimeText: "结束时间",
        whetherSuspendText: "是否挂起",
        noText: "否",
        yesText: "是",
        whetherDoneText: "是否已经结束",
        taskDoneText: "已办任务",
        taskNameText: "任务名",
        flowInstanceText: "流程实例",
        taskFormURLText: "任务表单URL",
        taskStatusText: "任务状态",
        doneText: "已办",
        reversedText: "已撤销",
        agentStatusText: "代理状态",
        processorNameText: "执行人名称",
        processorAccountText: "执行人账号",
        taskBeginTimeText: "任务开始时间",
        taskEndTimeText: "任务结束时间",
        taskProcessTimeText: "任务执行时长",
        lastUpdateTimeText: "最后更新时间"

    },common_lang);
}

if(EUI.FlowServiceUrlView){
    EUI.apply(EUI.FlowServiceUrlView.prototype.lang, {
        businessEntityText: "业务实体",
        totalText: "全部",
        searchByNameMsgText: "请输入名称进行搜索",
        businessEntityModelText: "所属业务实体模型"

    },common_lang);
}

if(EUI.FlowTaskView){
    EUI.apply(EUI.FlowTaskView.prototype.lang, {
        searchByNameMsgText: "请输入任务名进行搜索",
        passText: "通过",
        rejectText: "驳回",
        taskNameText: "任务名",
        taskFormURLText: "任务表单URL",
        taskStatusText: "任务状态",
        todoText: "待办",
        reversedText: "已撤销",
        doneText: "已办",
        agentStatusText: "代理状态",
        processorNameText: "执行人名称",
        processorAccountText: "执行人账号",
        candidateAccountText: "候选人账号",
        createTimeText: "创建时间",
        passCurrentTaskMsgText: "确定通过当前任务吗？",
        rejectCurrentTaskMsgText: "确定驳回当前任务吗？",
        processingText: "正在执行"


    },common_lang);
}

if(EUI.FlowTypeView){
    EUI.apply(EUI.FlowTypeView.prototype.lang, {
        businessEntityText:"业务实体",
        totalText:"全部"
    },common_lang);
}

if(EUI.WorkPageUrlView){
    EUI.apply(EUI.WorkPageUrlView.prototype.lang, {
    },common_lang);
}