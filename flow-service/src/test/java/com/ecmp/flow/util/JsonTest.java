package com.ecmp.flow.util;

import net.sf.json.JSONObject;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/23 13:22      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class JsonTest {
    public static void main(String[] args) {
        String jsonStr = "\t\n" +
                "{\"flowTypeId\":\"c0a80168-5bcd-10b6-815b-cd6118490000\",\"process\":{\"id\":\"flow1495436521389\",\"name\":\"\",\"key\"\n" +
                ":\"\",\"isExecutable\":true,\"nodes\":{\"StartEvent_0\":{\"type\":\"StartEvent\",\"x\":175,\"y\":110,\"id\":\"StartEvent_0\"\n" +
                ",\"target\":[{\"targetId\":\"UserTask_1\",\"uel\":\"\"}],\"name\":\"开始\",\"nodeConfig\":{}},\"UserTask_1\":{\"type\":\"UserTask\"\n" +
                ",\"x\":350,\"y\":108,\"id\":\"UserTask_1\",\"target\":[{\"targetId\":\"UserTask_2\",\"uel\":\"\"}],\"name\":\"普通任务1\",\"nodeConfig\"\n" +
                ":{\"normal\":{\"name\":\"普通任务1\",\"executeTime\":\"\",\"workPageName\":\"\",\"workPageUrl\":\"\",\"allowTerminate\":false\n" +
                ",\"allowPreUndo\":false,\"allowReject\":false},\"executor\":{\"userType\":\"AnyOne\"},\"event\":{\"beforeExcuteService\"\n" +
                ":\"\",\"beforeExcuteServiceId\":\"\",\"afterExcuteService\":\"\",\"afterExcuteServiceId\":\"\"},\"notify\":null}},\"UserTask_2\"\n" +
                ":{\"type\":\"UserTask\",\"x\":643,\"y\":108,\"id\":\"UserTask_2\",\"target\":[{\"targetId\":\"EndEvent_3\",\"uel\":\"\"}],\"name\"\n" +
                ":\"普通任务2\",\"nodeConfig\":{\"normal\":{\"name\":\"普通任务2\",\"executeTime\":\"\",\"workPageName\":\"\",\"workPageUrl\":\"\",\"allowTerminate\"\n" +
                ":false,\"allowPreUndo\":false,\"allowReject\":false},\"executor\":{\"userType\":\"AnyOne\"},\"event\":{\"beforeExcuteService\"\n" +
                ":\"\",\"beforeExcuteServiceId\":\"\",\"afterExcuteService\":\"\",\"afterExcuteServiceId\":\"\"},\"notify\":null}},\"EndEvent_3\"\n" +
                ":{\"type\":\"EndEvent\",\"x\":945,\"y\":112,\"id\":\"EndEvent_3\",\"target\":[],\"name\":\"结束\",\"nodeConfig\":{}}}}}";
        JSONObject defObj = JSONObject.fromObject(jsonStr);
        JSONObject userTask2 = defObj.getJSONObject("process").getJSONObject("nodes").getJSONObject("UserTask_2");
        System.out.println(userTask2.get("nodeConfig"));
    }
}
