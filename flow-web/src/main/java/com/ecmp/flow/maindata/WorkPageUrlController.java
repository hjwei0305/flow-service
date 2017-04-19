package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.vo.OperateResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
@RequestMapping(value = "/maindata/workPageUrl")
public class WorkPageUrlController {

    @RequestMapping()
    public String showWorkPageUrl() {
        return "maindata/WorkPageUrlView";
    }
}
