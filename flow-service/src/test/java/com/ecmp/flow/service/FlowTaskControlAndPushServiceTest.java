package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.dao.FlowTaskControlAndPushDao;
import com.ecmp.flow.dao.FlowTaskPushControlDao;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import com.ecmp.flow.vo.CleaningPushHistoryVO;
import com.ecmp.vo.OperateResult;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
    public void findbypage(){
        //页面查询
//        Search search = new Search();
//        search.addFilter(new SearchFilter("flowTypeId", "B2FC0C5F-5E87-11EA-AEE3-0242C0A8460D"));
//        search.addFilter(new SearchFilter("businessCode", "M9N8F0"));
//        List<SearchOrder> listOrder = new ArrayList<>();
//        listOrder.add(new SearchOrder("pushStartDate", SearchOrder.Direction.DESC));
//        search.setSortOrders(listOrder);
//        PageResult result = controlService.findByPage(search);
//        System.out.println(ApiJsonUtils.toJson(result));

        //后台查询
        Search search = new Search();
        search.addFilter(new SearchFilter("flowInstanceId", "16C62A5C-6156-11EC-B0A5-0242C0A84625"));
        search.addFilter(new SearchFilter("flowActTaskDefKey", "UserTask_113"));
        search.addFilter(new SearchFilter("pushType", "basic"));
        search.addFilter(new SearchFilter("pushStatus", "new"));
        List<FlowTaskPushControl> list =  controlService.findByFilters(search);
        System.out.println(ApiJsonUtils.toJson(list));
    }



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
        vo.setFlowTypeId("B2FC0C5F-5E87-11EA-AEE3-0242C0A8460D");
        vo.setRecentDate(11);
        controlService.cleaningPushHistoryData(vo);

        try{
            Thread.sleep(1000 * 60 * 5);
        }catch (Exception e){
        }

    }


}
