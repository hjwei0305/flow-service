package com.ecmp.flow.maindata;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 应用模块控制器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/30 16:10      陈飞(Vision.Mac)            新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/maindata/module")
public class AppModuleController {

    @RequestMapping(method = RequestMethod.GET)
    public String show() {
        return "maindata/AppModuleView";
    }
}
