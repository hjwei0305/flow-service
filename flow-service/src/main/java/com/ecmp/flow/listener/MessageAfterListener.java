package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
//@Component(value="messageAfterListener")
public class MessageAfterListener implements Serializable, org.activiti.engine.delegate.ExecutionListener{

    private final Logger logger = LoggerFactory.getLogger(MessageAfterListener.class);
	public MessageAfterListener(){
	}
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


    @Override
    public void notify(DelegateExecution execution) throws Exception{
        try {
            String deleteReason = ((ExecutionEntity) execution).getDeleteReason();
            if(StringUtils.isNotEmpty(deleteReason)){
                return;
            }
            String eventType = "after";
            String contentTemplateCode = "EMAIL_TEMPLATE_AFTER_DOWORK";
            MessageSendThread messageSendThread = new MessageSendThread(eventType,execution,contentTemplateCode);
            messageSendThread.setFlowDefVersionDao(this.flowDefVersionDao);
            messageSendThread.setFlowTaskDao(this.flowTaskDao);
            messageSendThread.setHistoryService(this.historyService);
            messageSendThread.setFlowHistoryDao(this.flowHistoryDao);
            messageSendThread.setRuntimeService(runtimeService);
            messageSendThread.setTaskService(taskService);
            messageSendThread.run();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }
}
