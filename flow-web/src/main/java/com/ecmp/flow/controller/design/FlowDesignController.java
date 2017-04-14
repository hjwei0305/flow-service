package com.ecmp.flow.controller.design;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程设计器控制器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/14 11:22      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/design")
public class FlowDesignController {

    @RequestMapping(method = RequestMethod.GET)
    public String show(){
        return "/design/WorkFlowView";
    }
}
