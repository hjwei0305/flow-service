package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.flow.entity.TaskMakeOverPower;
import com.ecmp.vo.OperateResultWithData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

public class TaskMakeOverPowerServiceTest  extends BaseContextTestCase{


    @Autowired
    private TaskMakeOverPowerService service;


    @Test
    public void setUserAndsave() {
        TaskMakeOverPower bean = new TaskMakeOverPower();
        bean.setAppModuleId("42839AC3-5E7F-11EA-9017-0242C0A8460D");
        bean.setAppModuleName("业务流程");
        bean.setBusinessModelId("B0E334A1-5E86-11EA-AEE3-0242C0A8460D");
        bean.setBusinessModelName("业务申请");
        bean.setMakeOverPowerType("sameToSee");
        bean.setOpenStatus(true);
        bean.setPowerUserAccount("aa");
        bean.setPowerUserId("C0B5AD75-7BCF-11EA-AE3A-0242C0A84603");
        bean.setPowerUserName("ff");
        bean.setPowerUserOrgCode("10607");
        bean.setPowerUserOrgId("877035BF-A40C-11E7-A8B9-02420B99179E");
        bean.setPowerUserOrgName("四川虹信软件股份有限公司");
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try{
            bean.setPowerStartDate(sim.parse("2020-04-28 14:07:04"));
            bean.setPowerEndDate(sim.parse("2020-04-29 14:07:06"));
        }catch (Exception e){
        }
        OperateResultWithData<TaskMakeOverPower>  result = service.setUserAndsave(bean);
        System.out.println(ApiJsonUtils.toJson(result));
    }

}
