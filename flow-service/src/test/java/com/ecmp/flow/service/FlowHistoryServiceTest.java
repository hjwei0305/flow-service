package com.ecmp.flow.service;

import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.util.FlowTaskTool;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


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

    @Test
    public void  initVirtualTask(){
        try{
            List<String> idList = new ArrayList<>();
            idList.add("B54E8964-D14D-11E8-A64B-0242C0A8441B");
            idList.add("394DE15B-F6FF-11EA-8F02-0242C0A8460D");
            flowTaskTool.initVirtualTask("da9d17ab-a7f0-11eb-bbed-0242c0a8462a","UserTask_67","测试虚拟待办通知生产",idList);
        }catch (Exception e){
        }
    }


}
