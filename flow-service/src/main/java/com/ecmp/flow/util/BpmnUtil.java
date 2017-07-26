package com.ecmp.flow.util;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/28 10:57      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class BpmnUtil {
    public static String getCurrentNodeParamName(net.sf.json.JSONObject currentNode ){
        String result = null;
        String flowTaskType =  currentNode.get("nodeType")+"";
        String id =  currentNode.get("id")+"";
        if("Normal".equalsIgnoreCase(flowTaskType)){
            result = id+"_Normal";
        }else if("SingleSign".equalsIgnoreCase(flowTaskType)){
            result = id+"_SingleSign";
        }else if("CounterSign".equalsIgnoreCase(flowTaskType)|| "ParallelTask".equalsIgnoreCase(flowTaskType)
                || "SerialTask".equals(flowTaskType)){
            result = id+"_CounterSign";
        }else if("Approve".equalsIgnoreCase(flowTaskType)){
            result = id+"_Approve";
        }
        return result;
    }
}
