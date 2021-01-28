package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.basic.vo.AppModule;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.BusinessModelDao;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.util.FlowTaskTool;
import com.ecmp.flow.vo.ConditionVo;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.GenericType;
import java.sql.SQLException;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class BusinessModelService extends BaseEntityService<BusinessModel> implements IBusinessModelService {


    @Autowired
    private BusinessModelDao businessModelDao;
    @Autowired
    private FlowCommonUtil flowCommonUtil;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTypeService flowTypeService;
    @Autowired
    private FlowHistoryService flowHistoryService;
    @Autowired
    private FlowTaskTool flowTaskTool;
    @Autowired
    private DisagreeReasonService disagreeReasonService;


    protected BaseEntityDao<BusinessModel> getDao() {
        return this.businessModelDao;
    }


    @Override
    public ResponseData getPropertiesByInstanceIdOfModile(String instanceId, String typeId, String id) {
        Boolean taskBoo = true;  //是否为待办
        Boolean canMobile = true; //移动端是否可以审批
        Boolean canCancel = false; //如果是已办，是否可以撤回
        String historyId = "";//撤回需要的历史ID
        String flowTaskId = "";//待办ID
        List<DisagreeReason> disagreeReasonList = null;
        String userId = ContextUtil.getUserId();
        Boolean isFlowtask = false;  //是否在待办中有数据
        if (StringUtils.isNotEmpty(instanceId) && StringUtils.isNotEmpty(typeId)) {
            List<FlowTask> flowTaskList = flowTaskService.findByInstanceId(instanceId);
            if (!CollectionUtils.isEmpty(flowTaskList)) {
                FlowTask flowTask = flowTaskList.stream().filter(a -> userId.equals(a.getExecutorId())).findFirst().orElse(null);
                if (flowTask != null) { //TODO:如果待办中有当前用户的，默认就是查询的待办(实际可能是从已办过来的)
                    isFlowtask = true;
                    flowTaskId = flowTask.getId();
                    canMobile = flowTask.getCanMobile() == null ? false : flowTask.getCanMobile();
                    //添加不同意原因
                    String defJson = flowTask.getTaskJsonDef();
                    JSONObject defObj = JSONObject.fromObject(defJson);
                    JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");
                    if (normalInfo.has("chooseDisagreeReason") && normalInfo.getBoolean("chooseDisagreeReason")) {
                        String flowTypeId = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId();
                        disagreeReasonList = disagreeReasonService.getDisReasonListByTypeId(flowTypeId);
                    }
                }
            }

            if (!isFlowtask) {//TODO:待办里面没有，查询不到，默认取已办最后一条数据
                taskBoo = false;
                Search search = new Search();
                search.addFilter(new SearchFilter("flowInstance.id", instanceId));
                search.addFilter(new SearchFilter("executorId", userId));
                SearchOrder searchOrder = new SearchOrder();
                search.addSortOrder(searchOrder.desc("lastEditedDate"));
                List<FlowHistory> historylist = flowHistoryService.findByFilters(search);
                if (!CollectionUtils.isEmpty(historylist)) {
                    FlowHistory flowHistory = historylist.get(0);
                    historyId = flowHistory.getId();
                    //动态设置撤回按钮是否显示
                    flowHistoryService.dynamicallySetTheRecallButtonByOne(flowHistory);
                    if (BooleanUtils.isTrue(flowHistory.getCanCancel())) {
                        canCancel = true;
                    }
                }
            }

            FlowType flowType = flowTypeService.findOne(typeId);
            if (flowType == null) {
                return ResponseData.operationFailure("流程类型不存在！");
            }
            String businessDetailServiceUrl;
            String apiBaseAddress;
            String businessModelCode;

            try {
                businessDetailServiceUrl = flowType.getBusinessDetailServiceUrl();
                if (StringUtils.isEmpty(businessDetailServiceUrl)) {
                    businessDetailServiceUrl = flowType.getBusinessModel().getBusinessDetailServiceUrl();
                }
                businessModelCode = flowType.getBusinessModel().getClassName();
            } catch (Exception e) {
                LogUtil.error("获取业务实体数据失败！" + e.getMessage(), e);
                return ResponseData.operationFailure("获取业务实体数据失败！");
            }

            try {
                String apiBaseAddressConfig = flowType.getBusinessModel().getAppModule().getApiBaseAddress();
                apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
            } catch (Exception e) {
                LogUtil.error("获取模块Api基地址失败！" + e.getMessage(), e);
                return ResponseData.operationFailure("获取模块Api基地址失败！");
            }

            //请求业务系统
            String url = PageUrlUtil.buildUrl(apiBaseAddress, businessDetailServiceUrl);
            ResponseData responseData = getPropertiesOfModile(url, businessModelCode, id);
            if (responseData.getSuccess()) {
                Map<String, Object> properties = (Map<String, Object>) responseData.getData();
                properties.put("flowTaskIsInit", taskBoo); //添加是否是待办参数，true为待办
                properties.put("flowTaskCanMobile", canMobile);//添加移动端是都可以查看
                if (!taskBoo) {
                    properties.put("canCancel", canCancel);//如果是已办，是否可以撤回，true为可以
                    properties.put("historyId", historyId);//撤回需要的历史ID
                }
                properties.put("flowTaskId", flowTaskId); //待办ID
                if (!CollectionUtils.isEmpty(disagreeReasonList)) {
                    properties.put("disagreeReasonList", disagreeReasonList); //不同意原因
                }
                return ResponseData.operationSuccessWithData(properties);
            } else {
                return responseData;
            }
        } else {
            return ResponseData.operationFailure("参数不能为空！");
        }
    }

    @Override
    public ResponseData getPropertiesByTaskIdOfModile(String taskId, String typeId, String id) {
        Boolean taskBoo = true;  //是否为待办
        Boolean canMobile = true; //移动端是否可以查看
        Boolean canCancel = false; //如果是已办，是否可以撤回
        String historyId = "";//撤回需要的历史ID
        List<DisagreeReason> disagreeReasonList = null;
        if (StringUtils.isNotEmpty(taskId) && StringUtils.isNotEmpty(typeId)) {
            //能查询到就是待办，查不到就是已处理
            FlowTask flowTask = flowTaskService.findOne(taskId);
            if (flowTask == null) {
                taskBoo = false;
                List<FlowHistory> historylist = flowHistoryService.findListByProperty("oldTaskId", taskId);
                if (!CollectionUtils.isEmpty(historylist)) {
                    FlowHistory flowHistory = historylist.get(0);
                    historyId = flowHistory.getId();
                    //动态设置撤回按钮是否显示
                    flowHistoryService.dynamicallySetTheRecallButtonByOne(flowHistory);
                    if (BooleanUtils.isTrue(flowHistory.getCanCancel())) {
                        canCancel = true;
                    }
                }
            } else {
                canMobile = flowTask.getCanMobile() == null ? false : flowTask.getCanMobile();
                //添加不同意原因
                String defJson = flowTask.getTaskJsonDef();
                JSONObject defObj = JSONObject.fromObject(defJson);
                JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");
                if (normalInfo.has("chooseDisagreeReason") && normalInfo.getBoolean("chooseDisagreeReason")) {
                    String flowTypeId = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId();
                    disagreeReasonList = disagreeReasonService.getDisReasonListByTypeId(flowTypeId);
                }
            }
            FlowType flowType = flowTypeService.findOne(typeId);
            if (flowType == null) {
                return ResponseData.operationFailure("流程类型不存在！");
            }


            String businessDetailServiceUrl;
            String apiBaseAddress;
            String businessModelCode;

            try {
                businessDetailServiceUrl = flowType.getBusinessDetailServiceUrl();
                if (StringUtils.isEmpty(businessDetailServiceUrl)) {
                    businessDetailServiceUrl = flowType.getBusinessModel().getBusinessDetailServiceUrl();
                }
                businessModelCode = flowType.getBusinessModel().getClassName();
            } catch (Exception e) {
                LogUtil.error("获取业务实体数据失败！" + e.getMessage(), e);
                return ResponseData.operationFailure("获取业务实体数据失败！");
            }

            try {
                String apiBaseAddressConfig = flowType.getBusinessModel().getAppModule().getApiBaseAddress();
                apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
            } catch (Exception e) {
                LogUtil.error("获取模块Api基地址失败！" + e.getMessage(), e);
                return ResponseData.operationFailure("获取模块Api基地址失败！");
            }
            //请求业务系统
            String url = PageUrlUtil.buildUrl(apiBaseAddress, businessDetailServiceUrl);
            ResponseData responseData = getPropertiesOfModile(url, businessModelCode, id);
            if (responseData.getSuccess()) {
                Map<String, Object> properties = (Map<String, Object>) responseData.getData();
                properties.put("flowTaskIsInit", taskBoo); //添加是否是待办参数，true为待办
                properties.put("flowTaskCanMobile", canMobile);//添加移动端是都可以查看
                if (!taskBoo) {
                    properties.put("canCancel", canCancel);//如果是已办，是否可以撤回，true为可以
                    properties.put("historyId", historyId);//撤回需要的历史ID
                }
                if (!CollectionUtils.isEmpty(disagreeReasonList)) {
                    properties.put("disagreeReasonList", disagreeReasonList); //不同意原因
                }
                return ResponseData.operationSuccessWithData(properties);
            } else {
                return responseData;
            }
        } else {
            return ResponseData.operationFailure("参数不能为空！");
        }
    }


    public ResponseData getPropertiesOfModile(String url, String businessModelCode, String id) {
        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(businessModelCode) && StringUtils.isNotEmpty(id)) {
            Map<String, Object> params = new HashMap();
            params.put("businessModelCode", businessModelCode);
            params.put("id", id);
            String messageLog = "开始调用‘表单明细’接口（移动端），接口url=" + url + ",参数值" + JsonUtils.toJson(params);
            try {
                Map<String, Object> properties = ApiClient.getEntityViaProxy(url, new GenericType<Map<String, Object>>() {
                }, params);
                return ResponseData.operationSuccessWithData(properties);
            } catch (Exception e) {
                messageLog += "表单明细接口调用异常：" + e.getMessage();
                LogUtil.error(messageLog, e);
                return ResponseData.operationFailure("接口调用异常，请查看日志！");
            }
        } else {
            return ResponseData.operationFailure("参数不能为空！");
        }
    }


    @Override
    public ResponseData getProperties(String businessModelCode) {
        BusinessModel businessModel = this.findByClassName(businessModelCode);
        if (businessModel != null) {
            Map<String, String> result = ExpressionUtil.getPropertiesDecMap(businessModel);
            if (result != null) {
                return ResponseData.operationSuccessWithData(result);
            } else {
                return ResponseData.operationFailure("调用接口异常，请查看日志！");
            }
        } else {
            return ResponseData.operationFailure("获取业务实体失败！");
        }
    }

    @Override
    public ResponseData getPropertiesRemark(String businessModelCode) {
        BusinessModel businessModel = this.findByClassName(businessModelCode);
        if (businessModel != null) {
            Map<String, String> result = ExpressionUtil.getPropertiesRemark(businessModel);
            if (result != null) {
                return ResponseData.operationSuccessWithData(result);
            } else {
                return ResponseData.operationFailure("调用【获取条件属性的备注说明】接口异常，请查看日志！");
            }
        } else {
            return ResponseData.operationFailure("获取业务实体失败！");
        }
    }


    @Override
    public List<ConditionVo> getPropertiesForConditionPojo(String businessModelCode) {
        ResponseData responseData = this.getProperties(businessModelCode);
        List<ConditionVo> list = new ArrayList<>();
        if (responseData.getSuccess() && responseData.getData() != null) {
            Map<String, String> result = (Map<String, String>) responseData.getData();
            if (!CollectionUtils.isEmpty(result)) {
                result.forEach((key, value) -> {
                    ConditionVo bean = new ConditionVo();
                    bean.setCode(key);
                    bean.setName(value);
                    list.add(bean);
                });
            }
        }
        return list;
    }

    @Override
    public List<BusinessModel> findByAppModuleId(String appModuleId) {
        return businessModelDao.findByAppModuleId(appModuleId);
    }

    @Override
    public BusinessModel findByClassName(String className) {
        return businessModelDao.findByClassName(className);
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    public OperateResult delete(String id) {
        OperateResult operateResult = preDelete(id);
        if (Objects.isNull(operateResult) || operateResult.successful()) {
            BusinessModel entity = findOne(id);
            if (entity != null) {
                try {
                    getDao().delete(entity);
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    e.printStackTrace();
                    SQLException sqlException = (SQLException) e.getCause().getCause();
                    if (sqlException != null && "23000".equals(sqlException.getSQLState())) {
                        return OperateResult.operationFailure("10027");
                    } else {
                        throw e;
                    }
                }
                // 业务实体删除成功！
                return OperateResult.operationSuccess("10057");
            } else {
                // 业务实体{0}不存在！
                return OperateResult.operationWarning("10058", id);
            }
        }
        clearFlowDefVersion();
        return operateResult;
    }

    public OperateResultWithData<BusinessModel> save(BusinessModel businessModel) {
        OperateResultWithData<BusinessModel> resultWithData;
        try {
            resultWithData = super.save(businessModel);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            cause = cause.getCause();
            SQLException sqlException = (SQLException) cause;
            if (sqlException != null && sqlException.getSQLState().equals("23000")) {
                resultWithData = OperateResultWithData.operationFailure("10037");//类全路径重复，请检查！
            } else {
                resultWithData = OperateResultWithData.operationFailure(e.getMessage());
            }
            LogUtil.error(e.getMessage(), e);
        }
        clearFlowDefVersion();
        return resultWithData;
    }

    private void clearFlowDefVersion() {
        String pattern = "FLowGetLastFlowDefVersion_*";
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        }
    }

    public PageResult<BusinessModel> findByPage(Search searchConfig) {
        List<AppModule> appModuleList;
        List<String> appModuleCodeList = null;
        appModuleList = flowCommonUtil.getBasicTenantAppModule();
        if (!CollectionUtils.isEmpty(appModuleList)) {
            appModuleCodeList = new ArrayList<>();
            for (AppModule appModule : appModuleList) {
                appModuleCodeList.add(appModule.getCode());
            }
        }
        if (!CollectionUtils.isEmpty(appModuleCodeList)) {
            SearchFilter searchFilter = new SearchFilter("appModule.code", appModuleCodeList, SearchFilter.Operator.IN);
            searchConfig.addFilter(searchFilter);
        }
        PageResult<BusinessModel> result = businessModelDao.findByPage(searchConfig);
        return result;
    }

    public List<BusinessModel> findAllByAuth() {
        List<BusinessModel> result = null;
        List<AppModule> appModuleList;
        List<String> appModuleCodeList = null;
        try {
            appModuleList = flowCommonUtil.getBasicTenantAppModule();
            if (!CollectionUtils.isEmpty(appModuleList)) {
                appModuleCodeList = new ArrayList<>();
                for (AppModule appModule : appModuleList) {
                    appModuleCodeList.add(appModule.getCode());
                }
            }
            if (!CollectionUtils.isEmpty(appModuleCodeList)) {
                result = businessModelDao.findByAppModuleCodes(appModuleCodeList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = businessModelDao.findAll();
        }

        //添加应用模块代码和名称（方便前端进行本地全字段搜索功能）
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(k -> {
                k.setAppModuleCode(k.getAppModule().getCode());
                k.setAppModuleName(k.getAppModule().getName());
            });
        }

        return result;
    }
}
