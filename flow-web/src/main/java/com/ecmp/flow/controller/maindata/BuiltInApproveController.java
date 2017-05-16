package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.api.IFlowTypeService;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.List;

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
@RequestMapping(value = "/builtInApprove")
public class BuiltInApproveController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/BuiltInApproveView";
    }

    /**
     * 查询默认业务实体
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "listDefBusinessModel")
    @ResponseBody
    public String listDefBusinessModel(ServletRequest request) {
        Search search = SearchUtil.genSearch(request);
        IDefaultBusinessModelService proxy = ApiClient.createProxy(IDefaultBusinessModelService.class);
        PageResult<DefaultBusinessModel> defaultBusinessModelPageResult = proxy.findByPage(search);
        return JsonUtil.serialize(defaultBusinessModelPageResult);
    }

    /**
     * 删除默认业务实体
     * @param id
     * @return
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IDefaultBusinessModelService proxy = ApiClient.createProxy(IDefaultBusinessModelService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 保存默认业务实体
     * @param defaultBusinessModel
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(DefaultBusinessModel defaultBusinessModel) {
        IDefaultBusinessModelService proxy = ApiClient.createProxy(IDefaultBusinessModelService.class);
        OperateResultWithData<DefaultBusinessModel> result = proxy.save(defaultBusinessModel);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }
}
