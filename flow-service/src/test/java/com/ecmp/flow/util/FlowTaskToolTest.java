package com.ecmp.flow.util;

import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.BaseContextTestCase;
import com.ecmp.flow.service.FlowTaskService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-02-21 17:39
 */
public class FlowTaskToolTest extends BaseContextTestCase {
    @Autowired
    private FlowTaskTool tool;
    @Autowired
    private FlowTaskService flowTaskService;

    @Test
    public void checkNextNodesCanAprool() {
        String taskId = "8D0D0466-34EB-11E9-BE62-0242C0A8441B";
        FlowTask flowTask = flowTaskService.findTaskById(taskId);
        Boolean canBatch = tool.checkNextNodesCanAprool(flowTask, null);
        Assert.assertTrue(canBatch);
    }
}