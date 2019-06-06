package com.ecmp.flow.service;

import com.ecmp.flow.entity.FlowTask;
import com.ecmp.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-05-30 17:51
 */
public class FlowInstanceServiceTest extends BaseContextTestCase{
    @Autowired
    private FlowInstanceService service;
    @Test
    public void findTaskByBusinessIdAndActTaskKey() {
        String id = "BC808F8B-81BB-11E9-9D74-0242C0A84410";
        String flowTaskId = "PoolTask_6";
        FlowTask task = service.findTaskByBusinessIdAndActTaskKey(id, flowTaskId);
        Assert.assertNotNull(task);
        System.out.println(JsonUtils.toJson(task));
    }
}