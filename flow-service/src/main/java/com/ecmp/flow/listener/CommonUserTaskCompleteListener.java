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
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用用户任务完成监听器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class CommonUserTaskCompleteListener implements ExecutionListener {

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowListenerTool flowListenerTool;

    public CommonUserTaskCompleteListener() {
    }

    private static final long serialVersionUID = 1L;

    public void notify(DelegateExecution delegateTask) {
        try {
            String deleteReason = ((ExecutionEntity) delegateTask).getDeleteReason();
            if (StringUtils.isNotEmpty(deleteReason)) {//流程结束不触发事件
                return;
            }
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId = delegateTask.getProcessBusinessKey();
            Map<String, Object> tempV = delegateTask.getVariables();
            Boolean currentNodeAfterEvent = (Boolean) tempV.get(actTaskDefKey + "currentNodeAfterEvent");
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
                String afterExcuteServiceId = null;//服务ID
                if (event.has(Constants.AFTER_EXCUTE_SERVICE_ID)) {
                    afterExcuteServiceId = event.getString(Constants.AFTER_EXCUTE_SERVICE_ID);
                }
                String afterExcuteService = "";//服务名称
                if (event.has(Constants.AFTER_EXCUTE_SERVICE)) {
                    afterExcuteService = "任务执行后 - " + event.getString(Constants.AFTER_EXCUTE_SERVICE);
                }
                boolean async = false;//默认为同步
                if (event.has(Constants.AFTER_ASYNC)) {
                    async = event.getBoolean(Constants.AFTER_ASYNC);
                    if (async != true) {
                        async = false;
                    }
                }
                if (!StringUtils.isEmpty(afterExcuteServiceId) && BooleanUtils.isNotFalse(currentNodeAfterEvent)) {
                    flowListenerTool.taskEventServiceCall(delegateTask, async, flowTaskName, afterExcuteServiceId, afterExcuteService, businessId);
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
