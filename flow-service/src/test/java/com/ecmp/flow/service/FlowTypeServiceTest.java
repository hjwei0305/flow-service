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

    @Test
    public  void getPropertiesByInstanceIdOfModile(){
        businessModelService.getPropertiesByInstanceIdOfModile(
                "B620C6D5-764E-11EB-A836-0242C0A84620",
                "2FE697CC-6202-11EB-A18B-0242C0A84620",
                "93423BD4-764E-11EB-843A-0242C0A8441B");
    }



}