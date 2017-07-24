package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IDefaultBusinessModel2Service;
import com.ecmp.flow.api.IDefaultBusinessModel3Service;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.common.web.controller.FlowBaseController;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.flow.entity.DefaultBusinessModel3;
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
@RequestMapping(value = "/defaultBusinessModel3")
public class DefaultBusinessModel3Controller extends FlowBaseController<IDefaultBusinessModel3Service,DefaultBusinessModel3> {

    public DefaultBusinessModel3Controller(){
        super(IDefaultBusinessModel3Service.class);
    }

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/DefaultBusinessModel3View";
    }

    @RequestMapping(value = "showBill", method = RequestMethod.GET)
    public String showBill() {
        return "maindata/LookApproveBillView3";
    }


    @RequestMapping(value = "approve", method = RequestMethod.GET)
    public String showApprove(){
        return "approve/ApproveView3";
    }


    /**
     * 销售申请审批界面(查看)
     *
     * @return
     */
    @RequestMapping(value = "look", method = RequestMethod.GET)
    public String look() {
        return "approve/ApproveView3";
    }

    /**
     * 销售申请表单查看
     *
     * @return
     */
    @RequestMapping(value = "orderLook", method = RequestMethod.GET)
    public String orderLook() {
        return "maindata/ReadyOnlyApproveView3";
    }

    /**
     * 销售申请审批界面(编辑)
     *
     * @return
     */
    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit() {
        return "approve/ApproveEditView3";
    }

    /**
     * 销售申请表单编辑
     *
     * @return
     */
    @RequestMapping(value = "orderEdit", method = RequestMethod.GET)
    public String orderEdit() {
        return "maindata/DefauleOrderEditView3";
    }


    @RequestMapping(value = "getApproveBill3")
    @ResponseBody
    public String getApproveBill3(String id) throws JsonProcessingException {
        // id="0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7";
        IDefaultBusinessModel3Service proxy = ApiClient.createProxy(IDefaultBusinessModel3Service.class);
        DefaultBusinessModel3 result = proxy.findOne(id);
        OperateStatus status = new OperateStatus(true,OperateStatus.COMMON_SUCCESS_MSG,result);
        return JsonUtil.serialize(status,JsonUtil.DATE_TIME);
    }
}

