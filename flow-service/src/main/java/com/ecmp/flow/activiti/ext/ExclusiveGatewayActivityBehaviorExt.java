package com.ecmp.flow.activiti.ext;

import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.util.ConditionUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.util.JsonUtils;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


public class ExclusiveGatewayActivityBehaviorExt extends ExclusiveGatewayActivityBehavior {

    @Override
    protected void leave(ActivityExecution execution) {
        List<PvmTransition> outSequenceList = execution.getActivity().getOutgoingTransitions();
        if (!CollectionUtils.isEmpty(outSequenceList)) {
            Map<String, Object> map = execution.getVariables();
            for (PvmTransition pv : outSequenceList) {
                String conditionText = (String) pv.getProperty(Constants.CONDITION_TEXT);
                if(conditionText != null && conditionText.startsWith("#{")){
                    String conditionFinal = conditionText.substring(conditionText.indexOf("#{")+2, conditionText.lastIndexOf("}"));
                    Boolean boo = ConditionUtil.groovyTest(conditionFinal, map);
                    if(boo == null){
                        throw  new FlowException(ContextUtil.getMessage("10090", conditionFinal, JsonUtils.toJson(map)));
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
