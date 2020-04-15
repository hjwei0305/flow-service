package com.ecmp.flow.activiti.ext;

import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.util.ConditionUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.util.JsonUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：扩展网关判断
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 11:19      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ExclusiveGatewayActivityBehaviorExt extends ExclusiveGatewayActivityBehavior {

    protected static Logger log =  LoggerFactory.getLogger(ExclusiveGatewayActivityBehaviorExt.class);


    private RepositoryService repositoryService;

    @Override
    protected void leave(ActivityExecution execution) {
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
        List<PvmTransition> outSequenceList = execution.getActivity().getOutgoingTransitions();
        if (outSequenceList != null && outSequenceList.size() > 0) {
            for (PvmTransition pv : outSequenceList) {
                String conditionText = (String) pv.getProperty(Constants.CONDITION_TEXT);
                if(conditionText != null && conditionText.startsWith("#{")){
                    String conditionFinal = conditionText.substring(conditionText.indexOf("#{")+2, conditionText.lastIndexOf("}"));
                    Map<String, Object> map = execution.getVariables();
                    Boolean boo = ConditionUtil.groovyTest(conditionFinal, map);
                    if(boo == null){
                        throw  new FlowException("验证表达式失败！表达式：【"+conditionFinal+"】,带入参数：【"+ JsonUtils.toJson(map)+"】");
                    }else if(boo){
                        execution.take(pv);
                        return;
                    }
                }
            }
        }
        // 执行父类的写法，以使其还是可以支持旧式的在跳出线上写条件的做法
        super.leave(execution);
    }

}
