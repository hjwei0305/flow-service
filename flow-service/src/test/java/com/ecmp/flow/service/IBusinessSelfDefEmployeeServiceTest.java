package com.ecmp.flow.service;


import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.api.IBusinessSelfDefEmployeeService;
import com.ecmp.flow.entity.BusinessSelfDefEmployee;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class IBusinessSelfDefEmployeeServiceTest extends BasicContextTestCase {

    @Autowired
    private BusinessSelfDefEmployeeService businessSelfDefEmployeeService;

    @Test
    public void save() {
        BusinessSelfDefEmployee businessSelfDefEmployee = new BusinessSelfDefEmployee();

        OperateResultWithData<BusinessSelfDefEmployee> result  = businessSelfDefEmployeeService.save(businessSelfDefEmployee);
        businessSelfDefEmployee=result.getData();
        logger.debug("id = {}", businessSelfDefEmployee.getId());
        logger.debug("create结果：{}", businessSelfDefEmployee);
    }

    @Test
    public void update() {
        List<BusinessSelfDefEmployee> businessSelfDefEmployeeList = businessSelfDefEmployeeService.findAll();
        if (businessSelfDefEmployeeList != null && businessSelfDefEmployeeList.size() > 0) {
            BusinessSelfDefEmployee businessSelfDefEmployee = businessSelfDefEmployeeList.get(0);
            logger.debug("update前：{}", businessSelfDefEmployee);

            businessSelfDefEmployeeService.save(businessSelfDefEmployee);
            logger.debug("update后：{}", businessSelfDefEmployee);
        } else {
            logger.warn("未能取到数据");
        }
    }
    @Test
    public void apiTest(){
        String baseAddress = "http://localhost:8080";

        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());

        IBusinessSelfDefEmployeeService businessSelfDefEmployeeService = JAXRSClientFactory.create(baseAddress, IBusinessSelfDefEmployeeService.class, providerList);
        List<BusinessSelfDefEmployee> result  = businessSelfDefEmployeeService.findAll();
        System.out.println(result.size());

//        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
//        List<BusinessSelfDefEmployee> result   = proxy.findAll();
//        System.out.println(result.size());
    }

}
