package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
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
@RequestMapping(value = "/maindata/businessModel")
public class BusinessModuleController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/BusinessModelView";
    }

    /**
     * 查询业务实体
     * @param request
     * @return 业务实体清单
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException {
        //  System.out.println("---------------------------------------------");
        Search search = SearchUtil.genSearch(request);
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        PageResult<BusinessModel> businessModelPageResult = proxy.findByPage(search);
        String businessModel = JsonUtil.serialize(businessModelPageResult);
        return businessModel;
    }

    /**
     * 根据id删除业务实体
     * @param id
     * @return 操作结果
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(id);
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查询应用模块
     * @return 应用模块清单
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "findAllAppModuleName")
    @ResponseBody
    public String findAllAppModuleName() throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        List<AppModule> appModuleList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, "ok", appModuleList);
        operateStatus.setData(appModuleList);
        String findAppModuleName = JsonUtil.serialize(operateStatus);
        System.out.println(findAppModuleName);
        return findAppModuleName;
    }

    /**
     * 保存业务实体
     * @param businessModel
     * @return 保存后的业务实体
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "update")
    @ResponseBody
    public String update(BusinessModel businessModel) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(businessModel);
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        OperateResultWithData<BusinessModel> result = proxy.save(businessModel);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }

}
