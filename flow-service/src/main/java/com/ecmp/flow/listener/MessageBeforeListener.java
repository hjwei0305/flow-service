package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowExecutorConfigDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.util.FlowTaskTool;
import com.ecmp.log.util.LogUtil;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用用户任务创建监听器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
//@Component(value="messageBeforeListener")
public class MessageBeforeListener implements Serializable, org.activiti.engine.delegate.ExecutionListener{

	public MessageBeforeListener(){}
    private static final long serialVersionUID = 1L;
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private FlowTaskTool flowTaskTool;

    @Override
    public void notify(DelegateExecution execution) throws Exception{
        try {
            String eventType = "before";
            String contentTemplateCode = "EMAIL_TEMPLATE_BEFORE_DOWORK";
            MessageSendThread messageSendThread = new MessageSendThread(eventType,execution,contentTemplateCode);
            messageSendThread.setFlowDefVersionDao(this.flowDefVersionDao);
            messageSendThread.setFlowTaskDao(this.flowTaskDao);
            messageSendThread.setHistoryService(this.historyService);
            messageSendThread.setFlowHistoryDao(this.flowHistoryDao);
            messageSendThread.setFlowExecutorConfigDao(this.flowExecutorConfigDao);
            messageSendThread.setRuntimeService(runtimeService);
            messageSendThread.setTaskService(taskService);
            messageSendThread.setFlowCommonUtil(flowCommonUtil);
            messageSendThread.setFlowTaskTool(flowTaskTool);
            messageSendThread.run();
        }catch (Exception e){
            LogUtil.error(e.getMessage(),e);
        }
    }
}
