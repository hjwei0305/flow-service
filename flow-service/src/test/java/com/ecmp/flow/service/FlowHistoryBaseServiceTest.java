package com.ecmp.flow.service;

import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowHistory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by xxxlimit on 2017/3/30.
 */
public class FlowHistoryBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowHistoryService flowHistoryService;

    @Test
    public void save()  {
        FlowHistory flowHistory = new FlowHistory();
        flowHistory.setFlowName("流程name");
        flowHistory.setFlowTaskName("流程任务名");
        flowHistory.setFlowRunId("流程运行id");
        flowHistory.setFlowInstanceId("流程实例id");
        flowHistory.setFlowDefId("流程定义id");
        flowHistoryService.save(flowHistory);
    }

    @Test
    public void findAll()  {
    }

    @Test
    public void delete() {
    }

}