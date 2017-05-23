package com.ecmp.flow.util;

import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.Process;
import net.sf.json.JSONObject;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/19 15:47      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
public class ExpresstionUtilTestCase extends BasicContextTestCase {

    @Test
    public  void test() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        String clientApiBaseUrl = "http://localhost:8080/flow/condition/propertiesAndValues";
//        String clientClassName = "com.ecmp.flow.vo.conditon.AppModuleCondition";
//        String expression = "return id+code";
//       System.out.println( ExpressionUtil.validate(clientApiBaseUrl,clientClassName,expression));

                String clientApiBaseUrl = "http://localhost:8080/";
        String clientClassName = "com.ecmp.flow.vo.conditon.DefaultBusinessModelCondition";
        String expression = "return id+code";
        String businessModelId = "D1E8B6BB-361D-11E7-9617-3C970EA9E0F7";
        String businessId = "0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7";
       System.out.println( ExpressionUtil.validate(clientApiBaseUrl,clientClassName,expression));
       Map result = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
        System.out.println(result);

    }
}
