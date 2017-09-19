package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/lookApproveBill")
@IgnoreCheckAuth
public class LookApproveBillController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/LookApproveBillView";
    }

    @RequestMapping(value = "look", method = RequestMethod.GET)
    public String look() {
        return "maindata/LookApproveView";
    }


    @RequestMapping(value = "showApprove", method = RequestMethod.GET)
    public String showApprove() {
        return "maindata/ReadyOnlyApproveView";
    }

    @RequestMapping(value = "getApproveBill")
    @ResponseBody
    public OperateStatus getApproveBill(String id) throws JsonProcessingException {
       // id="0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7";
        IDefaultBusinessModelService proxy = ApiClient.createProxy(IDefaultBusinessModelService.class);
        DefaultBusinessModel result = proxy.findOne(id);
        OperateStatus status = new OperateStatus(true,OperateStatus.COMMON_SUCCESS_MSG,result);
        return status;
    }
}

