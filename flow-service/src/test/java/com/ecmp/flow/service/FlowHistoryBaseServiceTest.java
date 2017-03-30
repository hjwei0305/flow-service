package com.ecmp.flow.service;

import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowHistory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
        flowHistory.setFlowName("流程name2");
        flowHistory.setFlowTaskName("流程任务名2");
        flowHistory.setFlowRunId("流程运行id2");
        flowHistory.setFlowInstanceId("流程实例id2");
        flowHistory.setFlowDefId("流程定义id2");
        flowHistoryService.save(flowHistory);
    }

    @Test
    public void findAll()  {
        List<FlowHistory> flowHistoryList = flowHistoryService.findAll();
        System.out.println(flowHistoryList);
    }

    @Test
    public void delete() {
        List<FlowHistory> flowHistoryList = flowHistoryService.findAll();
        if(flowHistoryList !=null && flowHistoryList.size()>0){
            FlowHistory flowHistory = flowHistoryList.get(0);
            flowHistoryService.delete(flowHistory);
        }
    }

}