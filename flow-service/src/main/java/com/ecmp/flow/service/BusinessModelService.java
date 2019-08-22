package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.basic.vo.AppModule;
import com.ecmp.flow.dao.BusinessModelDao;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final Logger logger = LoggerFactory.getLogger(BusinessModel.class);

    @Autowired
    private BusinessModelDao businessModelDao;
    @Autowired
    private FlowCommonUtil flowCommonUtil;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowTypeService flowTypeService;
    @Autowired
    private FlowTaskPushService flowTaskPushService;
    @Autowired
    private FlowHistoryService flowHistoryService;
    @Autowired
    private FlowTaskTool flowTaskTool;


    protected BaseEntityDao<BusinessModel> getDao() {
        return this.businessModelDao;
    }

    @Override
    public ResponseData getPropertiesByTaskIdOfModile(String taskId, String typeId, String id) {
        ResponseData responseData = new ResponseData();
        Boolean boo = true;  //是否为待办
        Boolean canMobile = true; //移动端是否可以审批
        Boolean canCancel = false; //如果是已办，是否可以撤回
        String historyId = "";//撤回需要的历史ID
        if (StringUtils.isNotEmpty(taskId) && StringUtils.isNotEmpty(typeId)) {
            //能查询到就是待办，查不到就是已处理
            FlowTask flowTask = flowTaskService.findOne(taskId);
            if (flowTask == null) {
                boo = false;
                List<FlowHistory> historylist =flowHistoryService.findListByProperty("oldTaskId",taskId);
                if(historylist != null && historylist.size() > 0){
                    FlowHistory a =  historylist.get(0);
                    historyId = a.getId();
                    if (a != null && a.getCanCancel() != null && a.getCanCancel() == true &&
                            a.getTaskStatus() != null && "COMPLETED".equalsIgnoreCase(a.getTaskStatus()) &&
                            a.getFlowInstance() != null && a.getFlowInstance().isEnded() != null &&
                            a.getFlowInstance().isEnded() == false) {
                        Boolean canCel = flowTaskTool.checkoutTaskRollBack(a);
                        if (canCel) {
                            canCancel = true;
                        }
                    }
                }

            } else {
                canMobile = flowTask.getCanMobile() == null ? false : flowTask.getCanMobile();
            }
            FlowType flowType = flowTypeService.findOne(typeId);
            if (flowType == null) {
                responseData.setSuccess(false);
                responseData.setMessage("流程类型不存在！");
                return responseData;
            }


            String businessDetailServiceUrl = "";
            String apiBaseAddress = "";
            String businessModelCode = "";

            try {
                businessDetailServiceUrl = flowType.getBusinessDetailServiceUrl();
                if (StringUtils.isEmpty(businessDetailServiceUrl)) {
                    businessDetailServiceUrl = flowType.getBusinessModel().getBusinessDetailServiceUrl();
                }
                businessModelCode = flowType.getBusinessModel().getClassName();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                responseData.setSuccess(false);
                responseData.setMessage("获取业务实体数据失败！");
                return responseData;
            }

            try {
                String apiBaseAddressConfig = flowType.getBusinessModel().getAppModule().getApiBaseAddress();
                apiBaseAddress = ContextUtil.getGlobalProperty(apiBaseAddressConfig);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                responseData.setSuccess(false);
                responseData.setMessage("获取模块Api基地址失败！");
                return responseData;
            }
            String url = apiBaseAddress + businessDetailServiceUrl;
            return this.getPropertiesByUrlOfModile(url, businessModelCode, id, boo, canMobile, canCancel, historyId);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("参数不能为空！");
        }
        return responseData;
    }


    public ResponseData getPropertiesByUrlOfModile(String url, String businessModelCode, String id, Boolean flowTaskIsInit, Boolean canMobile, Boolean canCancel, String historyId) {
        ResponseData responseData = new ResponseData();
        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(businessModelCode) && StringUtils.isNotEmpty(id)) {
            Map<String, Object> params = new HashMap();
            params.put("businessModelCode", businessModelCode);
            params.put("id", id);
            String messageLog = "开始调用‘表单明细’接口（移动端），接口url=" + url + ",参数值" + JsonUtils.toJson(params);
            try {
                Map<String, Object> properties = ApiClient.getEntityViaProxy(url, new GenericType<Map<String, Object>>() {
                }, params);
                properties.put("flowTaskIsInit", flowTaskIsInit); //添加是否是待办参数，true为待办
                properties.put("flowTaskCanMobile", canMobile);//添加移动端是都可以查看
                if (!flowTaskIsInit) {
                    properties.put("canCancel", canCancel);//如果是已办，是否可以撤回，true为可以
                    properties.put("historyId", historyId);//撤回需要的历史ID
                }
                responseData.setData(properties);
            } catch (Exception e) {
                messageLog += "表单明细接口调用异常：" + e.getMessage();
                LogUtil.error(messageLog, e);
                responseData.setSuccess(false);
                responseData.setMessage("接口调用异常，请查看日志！");
            }
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("参数不能为空！");
        }
        return responseData;
    }


    @Override
    public ResponseData getProperties(String businessModelCode) throws ClassNotFoundException {
        ResponseData responseData = new ResponseData();
        BusinessModel businessModel = this.findByClassName(businessModelCode);
        if (businessModel != null) {
            Map<String, String> result = ExpressionUtil.getPropertiesDecMap(businessModel);
            if (result != null) {
                responseData.setData(result);
            } else {
                responseData.setSuccess(false);
                responseData.setMessage("调用接口异常，请查看日志！");
            }
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("获取业务实体失败！");
        }
        return responseData;
    }

    @Override
    public List<ConditionVo> getPropertiesForConditionPojo(String businessModelCode) throws ClassNotFoundException {
        ResponseData responseData = this.getProperties(businessModelCode);
        List<ConditionVo> list = new ArrayList<ConditionVo>();
        if (responseData.getSuccess() && responseData.getData() != null) {
            Map<String, String> result = (Map<String, String>) responseData.getData();
            if (result.size() > 0) {
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
        OperateResultWithData<BusinessModel> resultWithData = null;
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
            logger.error(e.getMessage(), e);
        }
        clearFlowDefVersion();
        return resultWithData;
    }

    private void clearFlowDefVersion() {
        String pattern = "FLowGetLastFlowDefVersion_*";
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    public PageResult<BusinessModel> findByPage(Search searchConfig) {
        List<AppModule> appModuleList = null;
        List<String> appModuleCodeList = null;
        appModuleList = flowCommonUtil.getBasicTenantAppModule();
        if (appModuleList != null && !appModuleList.isEmpty()) {
            appModuleCodeList = new ArrayList<String>();
            for (AppModule appModule : appModuleList) {
                appModuleCodeList.add(appModule.getCode());
            }
        }
        if (appModuleCodeList != null && !appModuleCodeList.isEmpty()) {
            SearchFilter searchFilter = new SearchFilter("appModule.code", appModuleCodeList, SearchFilter.Operator.IN);
            searchConfig.addFilter(searchFilter);
        }
        PageResult<BusinessModel> result = businessModelDao.findByPage(searchConfig);
        return result;
    }

    public List<BusinessModel> findAllByAuth() {
        List<BusinessModel> result = null;
        List<AppModule> appModuleList = null;
        List<String> appModuleCodeList = null;
        try {
            appModuleList = flowCommonUtil.getBasicTenantAppModule();
            if (appModuleList != null && !appModuleList.isEmpty()) {
                appModuleCodeList = new ArrayList<String>();
                for (AppModule appModule : appModuleList) {
                    appModuleCodeList.add(appModule.getCode());
                }
            }
            if (appModuleCodeList != null && !appModuleCodeList.isEmpty()) {
                result = businessModelDao.findByAppModuleCodes(appModuleCodeList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = businessModelDao.findAll();
        }
        return result;
    }
}
