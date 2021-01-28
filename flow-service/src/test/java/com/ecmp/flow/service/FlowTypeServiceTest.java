package com.ecmp.flow.service;

import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.vo.OperateResultWithData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2018/6/14 9:51
 */
public class FlowTypeServiceTest extends BaseContextTestCase {

    @Autowired
    private BusinessModelService businessModelService;

    @Autowired
    private FlowTypeService flowTypeService;

    @Test
    public void save() {
        List<BusinessModel> businessModels = businessModelService.findAll();
        if (!CollectionUtils.isEmpty(businessModels)) {
            FlowType flowType = new FlowType();
            flowType.setBusinessModel(businessModels.get(0));
            flowType.setCode("test_mac");
            flowType.setName("测试");
            flowType.setDepict("测试");
            OperateResultWithData<FlowType> op = flowTypeService.save(flowType);
            System.out.println(op);
        }
    }
}