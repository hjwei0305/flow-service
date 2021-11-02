package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.Search;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dto.RollBackParam;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.*;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-10-11 14:59
 */
public class FlowTaskServiceTest extends BaseContextTestCase {
    @Autowired
    private FlowTaskService service;
    @Autowired
    private FlowDefinationService flowDefinationService;



    @Test
    public void getCanReturnNodeInfos(){
        ResponseData<List<FlowNodeVO>> responseData =   service.getCanReturnNodeInfos("7F020E25-3B7B-11EC-BB4A-0242C0A84611");
        System.out.println(ApiJsonUtils.toJson(responseData));
    }


    @Test
    public void findNextNodes() {
        try {
            List<NodeInfo> nodeInfoList = service.findNextNodes("112A2D0B-A887-11EB-A31B-983B8F805BAB");
            System.out.println(ApiJsonUtils.toJson(nodeInfoList));
        } catch (Exception e) {
            System.out.println("出错！");
        }
    }


    @Test
    public void getAllCanAddNodeInfoList() {
        try {
            //加签列表
            List<CanAddOrDelNodeInfo> list = service.getAllCanAddNodeInfoList();
            //减签列表
//            List<CanAddOrDelNodeInfo> list = service.getAllCanDelNodeInfoList();
            System.out.println(ApiJsonUtils.toJson(list));
        } catch (Exception e) {
        }
    }

    @Test
    public void haveReadTaskByTaskId() {
        String taskId = "4FBE1BFA-6683-11EB-8467-0242C0A84413";
        ResponseData responseData = service.haveReadTaskByTaskId(taskId);
        System.out.println(ApiJsonUtils.toJson(responseData));
    }

    @Test
    public void getApprovalHeaderVO() {
        String taskId = "4FBE1BFA-6683-11EB-8467-0242C0A84413";
        ApprovalHeaderVO vo = service.getApprovalHeaderVO(taskId);
        System.out.println(ApiJsonUtils.toJson(vo));
    }


    @Test
    public void rollBackToHis() {
        RollBackParam param = new RollBackParam();
        param.setId("7CD8A7AE-5EBF-11EB-8EFB-0242C0A84413");
        param.setOpinion("测试");
        ResponseData responseData = service.rollBackToHis(param);
        System.out.println(ApiJsonUtils.toJson(responseData));
    }

    @Test
    public void rollBackTo() {
        OperateResult operateResult = service.rollBackTo("7CD8A7AE-5EBF-11EB-8EFB-0242C0A84413", "测试");
        System.out.println(ApiJsonUtils.toJson(operateResult));
    }

    @Test
    public void taskReject() throws Exception {
        service.taskReject("2DE719E8-6519-11EB-9C0E-0242C0A84620", "111", null);
    }


    @Test
    public void addTaskAutoStatus() {
        FlowTask flowTask = service.findOne("42BB7CFF-DE2D-11EB-9E14-0242C0A8462A");
        List<FlowTask> checkFlowList = new ArrayList<>();
        checkFlowList.add(flowTask);
        service.addTaskAutoStatus(checkFlowList);
        System.out.println(ApiJsonUtils.toJson(checkFlowList));
    }

    @Test
    public void getBatchNextNodes() {
        List<String> list = new ArrayList<>();
        list.add("2515F2AE-DAE6-11EA-B55A-0242C0A84613");
        list.add("1F8C67E6-DAE6-11EA-B55A-0242C0A84613");
        service.getBatchNextNodes(list);
    }


    @Test
    public void listFlowTaskWithAllCount() {
        Search search = new Search();
        search.setFilters(null);
        search.setPageInfo(new PageInfo());
        search.setQuickSearchProperties(null);
        search.setQuickSearchValue(null);
        search.setSortOrders(null);
        ResponseData res = service.listFlowTaskWithAllCount(search, null);
        System.out.print(ApiJsonUtils.toJson(res));
    }

    @Test
    public void getTaskFormUrlXiangDuiByTaskId() {
        String taskId = "A8C69533-6999-11E9-BAC1-0242C0A84403";
        ResponseData res = service.getTaskFormUrlXiangDuiByTaskId(taskId);
        System.out.print(res.getData());
    }

    @Test
    public void findCountByExecutorId() {
        String userId = "7363AEB8-BC78-11E8-8A20-0242C0A8440D";
        Search search = new Search();
        search.setQuickSearchValue("");
        search.setQuickSearchProperties(Arrays.asList("flowName", "taskName", "flowInstance.businessCode", "flowInstance.businessModelRemark", "creatorName"));
        int count = service.findCountByExecutorId(userId, search);
        System.out.println("用户待办数量：" + count);
    }

    @Test
    public void findTaskById() {
        String id = "174C72AC-CD4A-11E8-A2BA-0242C0A84402";
        FlowTask flowTask = service.findTaskById(id);
        Assert.assertNotNull(flowTask);
        System.out.println(ApiJsonUtils.toJson(flowTask));
    }

    @Test
    public void findTasksByBusinessId() {
        String id = "A966DAE3-F8FB-11E8-A118-0242C0A84405";
        ResponseData responseData = service.findTasksByBusinessId(id, true);
        Assert.assertNotNull(responseData);
        System.out.println(ApiJsonUtils.toJson(responseData));
    }

    @Test
    public void findByBusinessModelIdWithAllCountOfPhone() {
//        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
//        FlowTaskPageResultVO<FlowTask> responseData =
//                proxy.findByBusinessModelIdWithAllCountOfPhone("","","",1,15,"");
        ResponseData aaa = flowDefinationService.resetPosition("");
        Assert.assertNotNull(aaa);
        System.out.println(ApiJsonUtils.toJson(aaa));
    }


    @Test
    public void getSelectedNodesInfo() {
        String taskId = "8A5C5D6E-1FD7-11EA-84D4-0242C0A84516";
        String apprvod = "true";
        try {
            OperateResultWithData res = service.getSelectedNodesInfo(taskId, apprvod, null, true);
            System.out.print(ApiJsonUtils.toJson(res));
        } catch (Exception e) {
        }
    }


    @Test
    public void completeBatch() {
        FlowTaskBatchCompleteVO param = new FlowTaskBatchCompleteVO();
        param.setTaskIdList(Collections.singletonList("A86CC83D-3647-11E9-AA0C-0242C0A8441B"));
        param.setOpinion("同意");
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        param.setVariables(variables);
        OperateResultWithData<Integer> result = service.completeBatch(param);
        System.out.println(JsonUtils.toJson(result));
        Assert.assertTrue(result.successful());
    }

    @Test
    public void complete() throws Exception {
        FlowTaskCompleteVO completeVO = new FlowTaskCompleteVO();
        completeVO.setTaskId("8A5C5D6E-1FD7-11EA-84D4-0242C0A84516");
        completeVO.setOpinion("同意");
        Map<String, Object> vars = new HashMap<>();
        vars.put("manageSolidifyFlow", false);
        vars.put("approved", null);
        vars.put("loadOverTime", 1559807544731L);
        vars.put("allowChooseInstancyMap", "{\"PoolTask_5\": null}");
        vars.put("selectedNodesUserMap", "{\"PoolTask_5\": [\"\"]}");
        completeVO.setVariables(vars);
        OperateResultWithData<FlowStatus> result = service.complete(completeVO);
        System.out.println(JsonUtils.toJson(result));
        Assert.assertTrue(result.successful());
    }


    @Test
    public void completeTaskBatch() {

        FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO1 = new FlowTaskBatchCompleteWebVO();
        List<String> list1 = new ArrayList<>();
        list1.add("0A456A0D-3346-11EA-AC69-0242C0A8440A");
        flowTaskBatchCompleteWebVO1.setTaskIdList(list1);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList1 = new ArrayList<>();
        FlowTaskCompleteWebVO flowTaskCompleteWebVO1 = new FlowTaskCompleteWebVO();
        flowTaskCompleteWebVO1.setSolidifyFlow(null);
        flowTaskCompleteWebVO1.setCallActivityPath(null);
        flowTaskCompleteWebVO1.setUserVarName("UserTask_65_Approve");
        flowTaskCompleteWebVO1.setNodeId("UserTask_65");
        flowTaskCompleteWebVO1.setInstancyStatus(false);
        flowTaskCompleteWebVO1.setFlowTaskType("approve");
        flowTaskCompleteWebVO1.setUserIds("1AE28F00-2FFC-11E9-AC2E-0242C0A84417");
        flowTaskCompleteList1.add(flowTaskCompleteWebVO1);
        flowTaskBatchCompleteWebVO1.setFlowTaskCompleteList(flowTaskCompleteList1);
        flowTaskBatchCompleteWebVO1.setSolidifyFlow(false);


        FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO2 = new FlowTaskBatchCompleteWebVO();
        List<String> list2 = new ArrayList<>();
        list2.add("E21C5985-3345-11EA-AC69-0242C0A8440A");
        flowTaskBatchCompleteWebVO2.setTaskIdList(list2);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList2 = new ArrayList<>();
        FlowTaskCompleteWebVO flowTaskCompleteWebVO2 = new FlowTaskCompleteWebVO();
        flowTaskCompleteWebVO2.setSolidifyFlow(null);
        flowTaskCompleteWebVO2.setCallActivityPath(null);
        flowTaskCompleteWebVO2.setUserVarName("UserTask_65_Approve");
        flowTaskCompleteWebVO2.setNodeId("UserTask_65");
        flowTaskCompleteWebVO2.setInstancyStatus(false);
        flowTaskCompleteWebVO2.setFlowTaskType("approve");
        flowTaskCompleteWebVO2.setUserIds("1AE28F00-2FFC-11E9-AC2E-0242C0A84417");
        flowTaskCompleteList2.add(flowTaskCompleteWebVO2);
        flowTaskBatchCompleteWebVO2.setFlowTaskCompleteList(flowTaskCompleteList2);
        flowTaskBatchCompleteWebVO2.setSolidifyFlow(false);


        FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO3 = new FlowTaskBatchCompleteWebVO();
        List<String> list3 = new ArrayList<>();
        list3.add("F57E5B28-3345-11EA-AC69-0242C0A8440A");
        flowTaskBatchCompleteWebVO3.setTaskIdList(list3);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList3 = new ArrayList<>();
        FlowTaskCompleteWebVO flowTaskCompleteWebVO3 = new FlowTaskCompleteWebVO();
        flowTaskCompleteWebVO3.setSolidifyFlow(null);
        flowTaskCompleteWebVO3.setCallActivityPath(null);
        flowTaskCompleteWebVO3.setUserVarName("UserTask_65_Approve");
        flowTaskCompleteWebVO3.setNodeId("UserTask_65");
        flowTaskCompleteWebVO3.setInstancyStatus(false);
        flowTaskCompleteWebVO3.setFlowTaskType("approve");
        flowTaskCompleteWebVO3.setUserIds(null);
        flowTaskCompleteList3.add(flowTaskCompleteWebVO3);
        flowTaskBatchCompleteWebVO3.setFlowTaskCompleteList(flowTaskCompleteList3);
        flowTaskBatchCompleteWebVO3.setSolidifyFlow(true);

        List<FlowTaskBatchCompleteWebVO> flowTaskBatchCompleteWebVOList = new ArrayList<>();
        flowTaskBatchCompleteWebVOList.add(flowTaskBatchCompleteWebVO1);
        flowTaskBatchCompleteWebVOList.add(flowTaskBatchCompleteWebVO2);
        flowTaskBatchCompleteWebVOList.add(flowTaskBatchCompleteWebVO3);

        service.completeTaskBatch(flowTaskBatchCompleteWebVOList);

    }

}
