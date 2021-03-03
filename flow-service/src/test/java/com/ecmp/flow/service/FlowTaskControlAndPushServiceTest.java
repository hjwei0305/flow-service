package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.flow.dao.FlowTaskControlAndPushDao;
import com.ecmp.flow.dao.FlowTaskPushControlDao;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.vo.CleaningPushHistoryVO;
import com.ecmp.vo.OperateResult;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class FlowTaskControlAndPushServiceTest extends BaseContextTestCase {

    @Autowired
    FlowTaskControlAndPushService service;
    @Autowired
    FlowTaskPushControlService controlService;
    @Autowired
    FlowTaskPushService pushService;

    @Test
    public void testAll() {
        String controlId = "02F8720B-312C-11EA-B7F4-0242C0A8440A";
        List<FlowTaskControlAndPush> list = service.getRelationsByParentId(controlId);
        List<String> pushIdList = list.stream().map(FlowTaskControlAndPush::getChild).map(FlowTaskPush::getId).collect(Collectors.toList());
        OperateResult operateResult = service.removeRelations(controlId, pushIdList);
        if (operateResult.successful()) {
            controlService.delete(controlId);
            for (String pushId : pushIdList) {
                if (!service.isExistByChild(pushId)) {
                    pushService.delete(pushId);
                }
            }
        }
        System.out.println(ApiJsonUtils.toJson(list));
    }

    @Test
    public void cleaningPushHistoryData() {
        CleaningPushHistoryVO vo = new CleaningPushHistoryVO();
        vo.setAppModuleId("42839AC3-5E7F-11EA-9017-0242C0A8460D");
        vo.setBusinessModelId("B0E334A1-5E86-11EA-AEE3-0242C0A8460D");
        vo.setFlowTypeId("B2FC0C5F-5E87-11EA-AEE3-0242C0A8460D");
        vo.setRecentDate(11);
        controlService.cleaningPushHistoryData(vo);

        try{
            Thread.sleep(1000 * 60 * 5);
        }catch (Exception e){
        }

    }


}
