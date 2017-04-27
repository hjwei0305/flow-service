package com.ecmp.flow.util;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：条件表达式工具类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/27 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ExpressionUtil {
    public static boolean validate(String clientApiBaseUrl,String clientClassName,String expression){
        boolean result = true;
        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());

        Map<String,Object> pvs = WebClient.create(clientApiBaseUrl, providerList)
                .path("/condition/propertiesAndValues/{conditonPojoClassName}",clientClassName)
//                .query("conditonPojoClassName",clientClassName)
                .accept(MediaType.APPLICATION_JSON)
                .get(Map.class);
        Binding bind = new Binding();
        for (Map.Entry<String,Object>  pv: pvs.entrySet()) {
            bind.setVariable(pv.getKey(), pv.getValue());
        }
        GroovyShell shell = new GroovyShell(bind);
        try {
            Object obj = shell.evaluate(expression);
        }catch (Exception e){
            result=false;
            e.printStackTrace();
        }
        return result;
    }
    public static boolean result(String clientApiBaseUrl,String clientClassName,String expression,String businessId){
        boolean result = true;
        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());

        LinkedHashMap<String,Object> pvs = WebClient.create(clientApiBaseUrl, providerList)
                .path("/properties")
                .accept(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        Binding bind = new Binding();
        for (Map.Entry<String,Object>  pv: pvs.entrySet()) {
            bind.setVariable(pv.getKey(), pv.getValue());

        }
        GroovyShell shell = new GroovyShell(bind);
        try {
            Object obj = shell.evaluate(expression);
        }catch (Exception e){
            e.printStackTrace();
            result=false;
        }
        return result;
    }
}
