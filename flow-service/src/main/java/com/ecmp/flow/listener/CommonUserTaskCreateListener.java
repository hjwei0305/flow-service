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
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


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
public class CommonUserTaskCreateListener implements ExecutionListener {

    public CommonUserTaskCreateListener() {
    }

    private static final long serialVersionUID = 1L;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowListenerTool flowListenerTool;

    @Transactional(propagation = Propagation.REQUIRED)
    public void notify(DelegateExecution delegateTask) {
        try {
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId = delegateTask.getProcessBusinessKey();
            Map<String, Object> tempV = delegateTask.getVariables();
            Boolean targetNodeBeforeEvent = (Boolean) tempV.get("targetNodeBeforeEvent");
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject event = null;
            if (currentNode.has(Constants.NODE_CONFIG) && currentNode.getJSONObject(Constants.NODE_CONFIG).has("event")) {
                event = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.EVENT);
            }
            if (event != null) {
                String flowTaskName = ""; //节点名称
                if (currentNode.has(Constants.NAME)) {
                    flowTaskName = currentNode.getString(Constants.NAME);
                }
                String beforeExcuteServiceId = null;//服务ID
                if (event.has(Constants.BEFORE_EXCUTE_SERVICE_ID)) {
                    beforeExcuteServiceId = event.getString(Constants.BEFORE_EXCUTE_SERVICE_ID);
                }
                String beforeExcuteService = "";//服务名称
                if (event.has(Constants.BEFORE_EXCUTE_SERVICE)) {
                    beforeExcuteService = "任务到达时 - " + event.getString(Constants.BEFORE_EXCUTE_SERVICE);
                }
                boolean async = true;//默认为异步
                if (event.has(Constants.BEFORE_ASYNC)) {
                    Boolean beforeAsync = event.getBoolean(Constants.BEFORE_ASYNC);
                    if (beforeAsync != true) {
                        async = false;
                    }
                }
                if (!StringUtils.isEmpty(beforeExcuteServiceId) && BooleanUtils.isNotFalse(targetNodeBeforeEvent)) {
                    flowListenerTool.taskEventServiceCall(delegateTask, async, flowTaskName, beforeExcuteServiceId, beforeExcuteService, businessId);
                }
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error(e.getMessage(), e);
            }
            throw new FlowException(e.getMessage());
        }
    }
}
