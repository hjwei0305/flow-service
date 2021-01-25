package com.ecmp.flow.service;

import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.util.FlowTaskTool;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class FlowHistoryServiceTest extends BaseContextTestCase {

    @Autowired
    private FlowTaskTool flowTaskTool;
    @Autowired
    private FlowHistoryService flowHistoryService;


    @Test
    public void checkoutTaskRollBack() {
        FlowHistory history = flowHistoryService.findOne("2A1BD648-5C92-11EB-8019-0242C0A84413");
        Boolean boo = flowTaskTool.checkoutTaskRollBack(history);
        System.out.println(boo);
    }


}
