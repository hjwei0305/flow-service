package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IDefaultBusinessModel2Service;
import com.ecmp.flow.api.IDefaultBusinessModel3Service;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.common.web.controller.FlowBaseController;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.flow.entity.DefaultBusinessModel3;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.util.HashMap;
import java.util.Map;

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
@RequestMapping(value = "/defaultBusinessModel2")
public class DefaultBusinessModel2Controller extends FlowBaseController<IDefaultBusinessModel2Service,DefaultBusinessModel2> {

    public DefaultBusinessModel2Controller(){
        super(IDefaultBusinessModel2Service.class);
    }

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/DefaultBusinessModel2View";
    }

    @RequestMapping(value = "showBill", method = RequestMethod.GET)
    public String showBill() {
        return "maindata/LookApproveBillView2";
    }



    @RequestMapping(value = "approve", method = RequestMethod.GET)
    public String showApprove(){
        return "approve/ApproveView2";
    }


    /**
     * 采购申请审批界面(查看)
     *
     * @return
     */
    @RequestMapping(value = "look", method = RequestMethod.GET)
    public String look() {
        return "approve/ApproveView2";
    }

    /**
     * 采购申请表单查看
     *
     * @return
     */
    @RequestMapping(value = "orderLook", method = RequestMethod.GET)
    public String orderLook() {
        return "maindata/ReadyOnlyApproveView2";
    }

    /**
     * 采购申请审批界面(编辑)
     *
     * @return
     */
    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit() {
        return "approve/ApproveEditView2";
    }

    /**
     * 采购申请表单编辑
     *
     * @return
     */
    @RequestMapping(value = "orderEdit", method = RequestMethod.GET)
    public String orderEdit() {
        return "maindata/DefauleOrderEditView2";
    }

    @RequestMapping(value = "getApproveBill2")
    @ResponseBody
    public String getApproveBill2(String id) throws JsonProcessingException {
        // id="0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7";
        IDefaultBusinessModel2Service proxy = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
        DefaultBusinessModel2 result = proxy.findOne(id);
        OperateStatus status = new OperateStatus(true,OperateStatus.COMMON_SUCCESS_MSG,result);
        return JsonUtil.serialize(status,JsonUtil.DATE_TIME);
    }
//    @RequestMapping(value = "list")
//    @ResponseBody
//    public PageResult<DefaultBusinessModel2> list(ServletRequest request) {
//        Search search = SearchUtil.genSearch(request);
//        IDefaultBusinessModel2Service proxy = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
//        PageResult<DefaultBusinessModel2> defaultBusinessModel2PageResult = proxy.findByPage(search);
//        return  defaultBusinessModel2PageResult;
//    }
//
//    /**
//     * 删除默认业务实体
//     *
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "delete")
//    @ResponseBody
//    public String delete(String id) {
//        IDefaultBusinessModel2Service proxy = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
//        OperateResult result = proxy.delete(id);
//        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
//        return JsonUtil.serialize(operateStatus);
//    }
//
//    /**
//     * 保存默认业务实体
//     *
//     * @param defaultBusinessModel2
//     * @return
//     */
//    @RequestMapping(value = "save")
//    @ResponseBody
//    public String save(DefaultBusinessModel2 defaultBusinessModel2) {
//        IDefaultBusinessModel2Service proxy = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
//        defaultBusinessModel2.setFlowStatus(FlowStatus.INIT);
//        OperateResultWithData<DefaultBusinessModel2> result = proxy.save(defaultBusinessModel2);
//        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
//        return JsonUtil.serialize(operateStatus);
//    }
//
//    /**
//     * 通过流程定义key启动流程
//     *
//     * @param businessModelCode
//     * @return 操作结果
//     */
//    @RequestMapping(value = "startFlow")
//    @ResponseBody
//    public String startFlow(String businessModelCode, String businessKey) {
//        IDefaultBusinessModel2Service proxy = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
//        OperateStatus operateStatus = null;
//        DefaultBusinessModel2 defaultBusinessModel2 = proxy.findOne(businessKey);
//        if (defaultBusinessModel2 != null) {
//            defaultBusinessModel2.setFlowStatus(FlowStatus.INPROCESS);
//            String startUserId = "admin";
//            String startUserIdContext = ContextUtil.getSessionUser().getUserId();
//            if (!StringUtils.isEmpty(startUserIdContext)) {
//                startUserId = startUserIdContext;
//            }
//            IFlowDefinationService proxy2 = ApiClient.createProxy(IFlowDefinationService.class);
//            Map<String, Object> variables = new HashMap<String, Object>();//UserTask_1_Normal
//            variables.put("UserTask_1_Normal", startUserId);
//            FlowInstance result = proxy2.startByBusinessModelCode(businessModelCode, startUserId, businessKey, variables);
//            if (result != null) {
//                proxy.save(defaultBusinessModel2);
//                operateStatus = new OperateStatus(true, "启动流程：" + result.getFlowName() + ",成功");
//            } else {
//                new OperateStatus(false, "启动流程失败");
//            }
//        } else {
//            operateStatus = new OperateStatus(false, "业务对象不存在");
//        }
//        return JsonUtil.serialize(operateStatus);
//    }


}

