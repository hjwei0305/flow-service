package com.ecmp.flow.util;

import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.juel.SimpleContext;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：条件表达式基础工具类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/11 10:04      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */

public class ConditionUtil {
	  public static Object uelResult(String uelExpressStr,Map<String,Object> map) throws NoSuchMethodException, SecurityException{
       ExpressionFactoryImpl factory = new ExpressionFactoryImpl();    
       //创建上下文对象context  
       ELContext context = new SimpleContext();    
       ValueExpression e = factory.createValueExpression(context, uelExpressStr, Object.class);    
       for(Map.Entry<String,Object> entry : map.entrySet()){        	
           factory.createValueExpression(context, "${"+entry.getKey()+"}", entry.getValue().getClass()).setValue(context, entry.getValue());  
       }   
       // get value for our expression  
       return e.getValue(context);
      // System.out.println(e.getValue(context)); // --> 2  
   }
    public static boolean groovyTest(String condition,Map<String, Object> map){

        boolean result = false;

        Binding bind = new Binding();
        for (Map.Entry<String, Object> pv : map.entrySet()) {
            bind.setVariable(pv.getKey(), pv.getValue());
        }
        GroovyShell shell = new GroovyShell(bind);
        try {
            Object obj = shell.evaluate(condition);
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        result = true;
        return result;
    }
}
