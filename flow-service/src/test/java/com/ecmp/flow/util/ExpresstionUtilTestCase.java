package com.ecmp.flow.util;

import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.Process;
import net.sf.json.JSONObject;

import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;

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
public class ExpresstionUtilTestCase {

    public static void main(String[] args) throws JAXBException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String clientApiBaseUrl = "http://localhost:8080/flow/condition/propertiesAndValues";
        String clientClassName = "com.ecmp.flow.vo.conditon.AppModuleCondition";
        String expression = "return id+code";
       System.out.println( ExpressionUtil.validate(clientApiBaseUrl,clientClassName,expression));
    }
}
