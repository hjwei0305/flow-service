package com.ecmp.flow.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;



/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用会签任务监听器，当会签任务完成时统计投票数量
 *   1代表同意、-1代表不同意、0代表弃权
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 11:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="commonCounterSignCompleteListener")
public class CommonCounterSignCompleteListener implements TaskListener{
	public CommonCounterSignCompleteListener(){
		System.out.println("commonCounterSignCompleteListener-------------------------");
	}
    private static final long serialVersionUID = 1L;

    public void notify(DelegateTask delegateTask) {
        Integer agreeCounter = (Integer) delegateTask.getVariable("counterSignValue");
        if(agreeCounter==null) {
            agreeCounter = 0;
        }
        String approved = (String) delegateTask.getVariable("approved");
        Integer value = 0;//默认弃权
        if("true".equalsIgnoreCase(approved)){
            value = 1;
        }else if("false".equalsIgnoreCase(approved)){
            value = -1;
        }

        delegateTask.setVariable("counterSignValue", agreeCounter + value);

    }
}
