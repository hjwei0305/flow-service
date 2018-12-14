package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.FlowTaskPageResultVO;
import com.ecmp.vo.ResponseData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

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

    @Test
    public void findCountByExecutorId(){
        String userId = "7363AEB8-BC78-11E8-8A20-0242C0A8440D";
        Search search = new Search();
        search.setQuickSearchValue("");
        search.setQuickSearchProperties(Arrays.asList("flowName","taskName","flowInstance.businessCode", "flowInstance.businessModelRemark", "creatorName"));
        int count = service.findCountByExecutorId(userId, search);
        System.out.println("用户待办数量："+count);
    }

    @Test
    public void findTaskById(){
        String id = "174C72AC-CD4A-11E8-A2BA-0242C0A84402";
        FlowTask flowTask = service.findTaskById(id);
        Assert.assertNotNull(flowTask);
        System.out.println(ApiJsonUtils.toJson(flowTask));
    }

    @Test
    public void findTasksByBusinessId(){
        String id = "A966DAE3-F8FB-11E8-A118-0242C0A84405";
        ResponseData responseData = service.findTasksByBusinessId(id);
        Assert.assertNotNull(responseData);
        System.out.println(ApiJsonUtils.toJson(responseData));
    }

    @Test
    public void findByBusinessModelIdWithAllCountOfPhone(){
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        FlowTaskPageResultVO<FlowTask> responseData =
                proxy.findByBusinessModelIdWithAllCountOfPhone("","","",1,15,"");
        Assert.assertNotNull(responseData);
        System.out.println(ApiJsonUtils.toJson(responseData));
    }





}
