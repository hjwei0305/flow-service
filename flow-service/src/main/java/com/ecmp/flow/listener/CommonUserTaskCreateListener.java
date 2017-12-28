package com.ecmp.flow.listener;

import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.FlowListenerTool;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import net.sf.json.JSONObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


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
//@Component(value="commonUserTaskCreateListener")
public class CommonUserTaskCreateListener implements ExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(CommonUserTaskCreateListener.class);
	public CommonUserTaskCreateListener(){
	}
    private static final long serialVersionUID = 1L;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowListenerTool flowListenerTool;

    @Transactional( propagation= Propagation.REQUIRED)
    public void notify(DelegateExecution delegateTask) {
        try {
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject event =null;
            if(currentNode.has(Constants.NODE_CONFIG)&&currentNode.getJSONObject(Constants.NODE_CONFIG).has("event")){
                event = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.EVENT);
            }
            if (event != null) {
                String beforeExcuteServiceId = (String) event.get(Constants.BEFORE_EXCUTE_SERVICE_ID);
                boolean async = true;//默认为异步
                if(event.has(Constants.BEFORE_ASYNC)){
                    Boolean beforeAsync = event.getBoolean(Constants.BEFORE_ASYNC);
                    if(beforeAsync != true){
                        async=false;
                    }
                }
                if (!StringUtils.isEmpty(beforeExcuteServiceId)) {
                    flowListenerTool.taskEventServiceCall(delegateTask, async, beforeExcuteServiceId ,businessId);
                }
            }
        }catch(Exception e){
            logger.error(e.getMessage());
            new Thread(new Runnable() {//模拟异步
                @Override
                public void run() {
                    LogUtil.addExceptionLog(e.getMessage());
                }
            }).start();
            throw new FlowException(e.getMessage());
        }
    }
}
