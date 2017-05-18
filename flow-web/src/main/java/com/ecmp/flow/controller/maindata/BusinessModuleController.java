package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.api.IBusinessWorkPageUrlService;
import com.ecmp.flow.api.IWorkPageUrlService;
import com.ecmp.flow.api.common.api.IConditionServer;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
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
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/businessModel")
public class BusinessModuleController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/BusinessModelView";
    }

    /**
     * 查询业务实体
     *
     * @param request
     * @return 业务实体清单
     * @throws ParseException
     */
    @RequestMapping(value = "listBusinessModel")
    @ResponseBody
    public String listBusinessModel(ServletRequest request) throws ParseException {
        Search search = SearchUtil.genSearch(request);
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        PageResult<BusinessModel> businessModelPageResult = proxy.findByPage(search);
        return JsonUtil.serialize(businessModelPageResult);
    }

    /**
     * 根据id删除业务实体
     *
     * @param id
     * @return 操作结果
     * @throws
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查询应用模块
     * @return 应用模块清单
     * @throws
     */
    @RequestMapping(value = "listAllAppModule")
    @ResponseBody
    public String listAllAppModule() {
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        List<com.ecmp.basic.entity.AppModule> appModuleList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, appModuleList);
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 保存业务实体
     * @param businessModel
     * @return 保存后的业务实体
     * @throws
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(BusinessModel businessModel) {
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        OperateResultWithData<BusinessModel> result = proxy.save(businessModel);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查看对应业务实体已选中的工作界面
     * @param appModuleId
     * @param businessModelId
     * @return
     * @throws
     */
    @RequestMapping(value = "listAllSelectEdByAppModuleId")
    @ResponseBody
    public List<WorkPageUrl> listAllSelectEdByAppModuleId(@RequestParam(value = "appModuleId") String appModuleId, @RequestParam(value = "businessModelId") String businessModelId) {
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        List<WorkPageUrl> workPageUrlList = proxy.findSelectEdByAppModuleId(appModuleId, businessModelId);
        return workPageUrlList;
    }

    /**
     * 查看对应业务实体未选中的工作界面
     * @param appModuleId     应该模块id
     * @param businessModelId 业务实体id
     * @return
     * @throws
     */
    @RequestMapping(value = "listAllNotSelectEdByAppModuleId")
    @ResponseBody
    public List<WorkPageUrl> listAllNotSelectEdByAppModuleId(@RequestParam(value = "appModuleId") String appModuleId, @RequestParam(value = "businessModelId") String businessModelId) {
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        List<WorkPageUrl> workPageUrlList = proxy.findNotSelectEdByAppModuleId(appModuleId, businessModelId);
        return workPageUrlList;
    }

    /**
     * 保存设置的工作界面
     * @param id    业务实体id
     * @param selectWorkPageIds 选中的工作界面所有di
     * @return
     * @throws
     */

    @RequestMapping(value = "saveSetWorkPage")
    @ResponseBody
    public String saveSetWorkPage(String id, String selectWorkPageIds) {
        IBusinessWorkPageUrlService proxy = ApiClient.createProxy(IBusinessWorkPageUrlService.class);
        proxy.saveBusinessWorkPageUrlByIds(id, selectWorkPageIds);
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG);
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查询条件属性
     * @param conditonPojoClassName
     * @return
     * @throws ClassNotFoundException
     */
    @RequestMapping(value = "getPropertiesForConditionPojo")
    @ResponseBody
    public String getPropertiesForConditionPojo(String conditonPojoClassName) throws  ClassNotFoundException {
        IConditionServer proxy = ApiClient.createProxy(IConditionServer.class);
        Map<String, String> result = proxy.getPropertiesForConditionPojo(conditonPojoClassName);
        return JsonUtil.serialize(result);
    }

}
