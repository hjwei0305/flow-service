package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BusinessModelBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private BusinessModelService businessModelService;

    @Autowired
    private AppModuleService appModuleService;

    @Test
    public void save() {
        AppModule appModule = appModuleService.findOne("c0a8016c-5afe-1184-815a-fe518bf90000");
        BusinessModel businessModel = new BusinessModel();
        businessModel.setClassName("ecmp-flow-businessModel22_" + System.currentTimeMillis());
        businessModel.setName("业务实体模型测试-hasAppModule");
        businessModel.setAppModule(appModule);
        OperateResultWithData<BusinessModel> result  = businessModelService.save(businessModel);

        businessModel=result.getData();
        logger.debug("id = {}", businessModel.getId());
        logger.debug("create结果：{}", businessModel);
    }

    @Test
    public void update() {
        List<BusinessModel> businessModelList = businessModelService.findAll();
        if (businessModelList != null && businessModelList.size() > 0) {
            BusinessModel businessModel = businessModelList.get(0);
            logger.debug("update前：{}", businessModel);
            businessModel.setClassName("ecmp-flow-businessModel2_" + System.currentTimeMillis());
            businessModel.setName("业务实体模型测试2");
            businessModelService.save(businessModel);
            logger.debug("update后：{}", businessModel);
        } else {
            logger.warn("未能取到数据");
        }
    }

}
