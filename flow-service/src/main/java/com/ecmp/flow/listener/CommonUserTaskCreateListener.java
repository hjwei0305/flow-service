package com.ecmp.flow.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;


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
@Component(value="commonUserTaskCreateListener")
public class CommonUserTaskCreateListener implements TaskListener{
	public CommonUserTaskCreateListener(){
		System.out.println("commonUserTaskCreateListener-------------------------");
	}
    private static final long serialVersionUID = 1L;

    public void notify(DelegateTask delegateTask) {


    }
}
