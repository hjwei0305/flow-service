package com.ecmp.flow.controller.maindata;

import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.common.web.controller.FlowBaseController;
import com.ecmp.flow.entity.DefaultBusinessModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
public class BuiltInApproveController extends FlowBaseController<IDefaultBusinessModelService,DefaultBusinessModel> {


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

    @RequestMapping(value = "approve", method = RequestMethod.GET)
    public String showApprove(){
        return "approve/ApproveView";
    }
}

