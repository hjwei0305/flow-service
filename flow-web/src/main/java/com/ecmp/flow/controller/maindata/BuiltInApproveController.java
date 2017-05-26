package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.*;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 *
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * 1.0.00      2017/5/26 9:32      谭军（tanjun）                    增加启动流程，完成任务
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/builtInApprove")
public class BuiltInApproveController extends FlowBaseController<IDefaultBusinessModelService,DefaultBusinessModel>{


    public BuiltInApproveController(){
        super(IDefaultBusinessModelService.class);
    }
    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/BuiltInApproveView";
    }

    @RequestMapping(value = "show2", method = RequestMethod.GET)
    public String show2() {
        return "maindata/LookApproveBillView";
    }
}

