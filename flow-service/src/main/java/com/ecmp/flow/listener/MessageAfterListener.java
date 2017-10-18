package com.ecmp.flow.listener;

import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.notify.api.INotifyService;
import com.ecmp.notity.entity.EcmpMessage;
import com.ecmp.notity.entity.NotifyType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
@Component(value="messageAfterListener")
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
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private TaskService taskService;

    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void notify(DelegateExecution execution) throws Exception{
        try {
//            ExecutorService pool = Executors.newSingleThreadExecutor();
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
//            new Thread(messageSendThread).start();
//            pool.submit(messageSendThread);
//            //关闭线程池
//            System.out.println(pool.isShutdown());
//            pool.shutdown();
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }
}
