package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.basic.vo.*;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.DateUtils;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.GenericType;
import java.io.Serializable;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/1/30 14:20      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component
public class FlowCommonUtil implements Serializable {

    public FlowCommonUtil() {
        clearAllCache();
    }

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    //注入缓存模板
    @Autowired(required = false)
    protected RedisTemplate<String, Object> redisTemplate;


    public String getErrorLogString(String name) {
        return "调用" + name + "接口异常,详情请查看日志!";
    }

    public String getNulString(String name) {
        return "调用" + name + "接口,返回为空,详情请查看日志!";
    }


    //------------------------------------------------获取执行人--------------------------------//

    /**
     * 根据用户的id获取执行人
     * 1.剔除冻结的用户  2.如果有员工信息，赋值组织机构和岗位信息
     *
     * @param userId 用户id
     * @return 流程执行人
     */
    public Executor getBasicUserExecutor(String userId) {
        List<String> userIds = Arrays.asList((userId));
        String url = Constants.getBasicUserGetExecutorsbyUseridsUrl();
        String messageLog = "开始调用【根据用户的id获取执行人】，接口url=" + url + ",参数值" + JsonUtils.toJson(userIds);
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, userIds);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【根据id获取执行人】"));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString("【根据id获取执行人】"), e);
        }

        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "-返回信息为空！");
            throw new FlowException(getNulString("【根据id获取执行人】"));
        }
        Executor executor = null;
        if (executors != null && !executors.isEmpty()) {
            executor = executors.get(0);
        }
        return executor;
    }

    /**
     * 根据用户的id列表获取执行人
     * 1.剔除冻结的用户  2.如果有员工信息，赋值组织机构和岗位信息
     *
     * @param userIds 用户ID列表
     * @return 流程执行人集合
     */
    public List<Executor> getBasicUserExecutors(List<String> userIds) {
        String url = Constants.getBasicUserGetExecutorsbyUseridsUrl();
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        String messageLog = "开始调用【根据用户的id列表获取执行人】，接口url=" + url + ",参数值" + JsonUtils.toJson(userIds);
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, userIds);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【根据id列表获取执行人】"));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString("【根据id列表获取执行人】"), e);
        }
        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "-返回信息为空！");
            throw new FlowException(getNulString("【根据id列表获取执行人】"));
        }
        return executors;
    }

    /**
     * 通过岗位ids和单据所属组织机构id来获取执行人
     *
     * @param positionIds 岗位id集合
     * @param orgId       组织机构id（应项目组要求添加，是否使用这个参数根据项目而定）
     * @return 流程执行人集合
     */
    public List<Executor> getBasicExecutorsByPositionIds(List<String> positionIds, String orgId) {
        FindExecutorByPositionParamVo vo = new FindExecutorByPositionParamVo();
        vo.setPositionIds(positionIds);
        vo.setOrgId(orgId);
        String url = Constants.getBasicPositionGetexecutorsbypositionidsUrl();
        String messageLog = "开始调用【根据岗位的id列表获取执行人】，接口url=" + url + ",参数值" + JsonUtils.toJson(vo);
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【根据岗位列表获取执行人】"));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString("【根据岗位列表获取执行人】"), e);
        }
        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "返回信息为空！");
            throw new FlowException(getNulString("【根据岗位列表获取执行人】"));
        } else {
            LogUtil.bizLog(messageLog + ",返回信息【" + JsonUtils.toJson(executors) + "】");
        }
        return executors;
    }

    /**
     * 根据岗位类别id列集合获取执行人
     *
     * @param postCatIds 岗位类别id集合
     * @param orgId      组织机构id
     * @return 流程执行人集合
     */
    public List<Executor> getBasicExecutorsByPostCatIds(List<String> postCatIds, String orgId) {
        FindExecutorByPositionCateParamVo vo = new FindExecutorByPositionCateParamVo();
        vo.setPostCatIds(postCatIds);
        vo.setOrgId(orgId);
        String url = Constants.getBasicPositionGetexecutorsbyposcateidsUrl();
        String messageLog = "开始调用【根据岗位类别的id列表获取执行人】，接口url=" + url + ",参数值" + JsonUtils.toJson(vo);
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【根据岗位类别列表获取执行人】"));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString("【根据岗位类别列表获取执行人】"), e);
        }
        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "返回信息为空！");
            throw new FlowException(getNulString("【根据岗位类别列表获取执行人】"));
        } else {
            LogUtil.bizLog(messageLog + ",返回信息【" + JsonUtils.toJson(executors) + "】");
        }
        return executors;
    }

    /**
     * 通过岗位ids、组织维度ids和组织机构id来获取执行人
     *
     * @param positionIds 岗位ids
     * @param orgDimIds   组织维度ids
     * @param orgId       组织机构id
     * @return 流程执行人集合
     */
    public List<Executor> getExecutorsByPositionIdsAndorgDimIds(List<String> positionIds, List<String> orgDimIds, String orgId) {
        FindExecutorByPositionParamVo vo = new FindExecutorByPositionParamVo();
        vo.setPositionIds(positionIds);
        vo.setOrgId(orgId);
        vo.setOrgDimIds(orgDimIds);
        String url = Constants.getBasicPositionGetexecutorsbypositionidsUrl();
        String messageLog = "开始调用【根据岗位集合、组织维度集合和组织机构获取执行人】，接口url=" + url + ",参数值" + JsonUtils.toJson(vo);
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【根据岗位、组织维度和组织机构获取执行人】"));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString("【根据岗位、组织维度和组织机构获取执行人】"), e);
        }
        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "返回信息为空！");
            throw new FlowException(getNulString("【根据岗位、组织维度和组织机构获取执行人】"));
        } else {
            LogUtil.bizLog(messageLog + ",返回信息【" + JsonUtils.toJson(executors) + "】");
        }
        return executors;
    }

    /**
     * 通过岗位类别ids和组织机构ids获取执行人
     *
     * @param postCatIds 岗位类别ids
     * @param orgIds     组织机构ids
     * @return 流程执行人集合
     */
    public List<Executor> getExecutorsByPostCatIdsAndOrgs(List<String> postCatIds, List<String> orgIds) {
        ExecutorQueryParamVo vo = new ExecutorQueryParamVo();
        vo.setPostCatIds(postCatIds);
        vo.setOrgIds(orgIds);
        String url = Constants.getExecutorsByPostCatAndOrgUrl();
        String messageLog = "开始调用【根据岗位类别集合和组织机构集合获取执行人】，接口url=" + url + ",参数值" + JsonUtils.toJson(vo);
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【根据岗位类别和组织机构获取执行人】"));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString("【根据岗位类别和组织机构获取执行人】"), e);
        }
        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "返回信息为空！");
            throw new FlowException(getNulString("【根据岗位类别和组织机构获取执行人】"));
        } else {
            LogUtil.bizLog(messageLog + ",返回信息【" + JsonUtils.toJson(executors) + "】");
        }
        return executors;
    }


    public List<Executor> getExecutorsBySelfDef(String appModuleCode, String selfName, String path, FlowInvokeParams flowInvokeParams) {
        String messageLog = "调用【自定义执行人-" + selfName + "】";
        String url = PageUrlUtil.buildUrl(Constants.getConfigValueByApi(appModuleCode), path);
        String mes = "-接口地址：" + url + ",参数值:" + JsonUtils.toJson(flowInvokeParams);
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, flowInvokeParams);
            if (result.successful()) {
                executors = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【自定义执行人-" + selfName + "】"));
            }
        } catch (Exception e) {
            LogUtil.error(messageLog + mes + "-【调用异常】", e);
            throw new FlowException(messageLog + "-【调用异常】,详情请查看日志！", e);
        }

        if (executors.isEmpty()) {
            LogUtil.error(messageLog + "返回执行人为空！");
            throw new FlowException(getNulString("【自定义执行人-" + selfName + "】"));
        } else {
            LogUtil.bizLog(messageLog + ",返回信息【" + JsonUtils.toJson(executors) + "】");
        }
        return executors;
    }


    //------------------------------------------------获取组织机构--------------------------------//

    /**
     * 获取所有组织机构树（不包含冻结）
     *
     * @return
     */
    public List<Organization> getBasicAllOrgs() {
        String url = Constants.getBasicOrgListallorgsUrl();
        ResponseData<List<Organization>> result;
        String messageLog = "开始调用【获取所有组织机构树】，接口url=" + url + ",参数空";
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Organization>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }

    /**
     * 获取所有组织机构树（不包含冻结）
     *
     * @return
     */
    public List<Organization> getBasicAllOrgByPower() {
        String url = Constants.getBasicOrgListByPowerUrl();
        ResponseData<List<Organization>> result;
        String messageLog = "开始调用【获取所有有权限的组织机构树】，接口url=" + url + ",参数为空";
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Organization>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }


    /**
     * 获取指定节点的父组织机构列表
     *
     * @param nodeId 指定的组织机构id
     * @return 包含自己的所有父组织机构集合
     */
    public List<Organization> getParentOrganizations(String nodeId) {
        Map<String, Object> params = new HashMap();
        params.put("includeSelf", true); //默认包含本组织机构
        params.put("nodeId", nodeId);
        String url = Constants.getBasicOrgFindparentnodesUrl();
        String messageLog = "开始调用【获取指定节点的父组织机构列表】，接口url=" + url + ",参数值" + JsonUtils.toJson(params);
        ResponseData<List<Organization>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Organization>>>() {
            }, params);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }


    //----------------------------------------------获取员工--------------------------------//

    /**
     * 根据组织机构ID获取员工集合
     *
     * @param orgId 组织机构ID
     * @return 员工list集合
     */
    public List<Employee> getEmployeesByOrgId(String orgId) {
        Map<String, Object> params = new HashMap();
        if (Objects.isNull(orgId)) {
            orgId = StringUtils.EMPTY;
        }
        params.put("organizationId", orgId);
        String url = Constants.getBasicEmployeeFindbyorganizationidUrl();
        String messageLog = "开始调用【根据组织机构ID获取员工集合】，接口url=" + url + ",参数值" + JsonUtils.toJson(params);
        ResponseData<List<Employee>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Employee>>>() {
            }, params);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }

    /**
     * 根据组织机构获取员工（可以包含子节点）
     *
     * @param userQueryParamVo
     * @return
     */
    public PageResult<Employee> getEmployeesByOrgIdAndQueryParam(UserQueryParamVo userQueryParamVo) {
        String url = Constants.getBasicEmployeeFindByUserQueryParam();
        String messageLog = "开始调用【根据组织机构ID获取员工集合】，接口url=" + url + ",参数值" + JsonUtils.toJson(userQueryParamVo);
        ResponseData<PageResult<Employee>> result;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<PageResult<Employee>>>() {
            }, userQueryParamVo);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }


    //----------------------------------------------获取岗位--------------------------------//
    public PageResult<Position> getBasicPositionFindbypage(Search search) {
        String url = Constants.getBasicPositionFindbypageUrl();
        String messageLog = "开始调用【获取所有岗位】，接口url=" + url + ",参数值" + JsonUtils.toJson(search);
        ResponseData<PageResult<Position>> result;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<PageResult<Position>>>() {
            }, search);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }


    //----------------------------------------------获取岗位类别--------------------------------//
    public List<PositionCategory> getBasicPositioncategoryFindall() {
        String url = Constants.getBasicPositioncategoryFindallUrl();
        String messageLog = "开始调用【获取所有岗位类别】，接口url=" + url + ",参数值为空";
        ResponseData<List<PositionCategory>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<PositionCategory>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }

    //----------------------------------------------获取组织维度--------------------------------//
    public List<OrganizationDimension> getBasicOrgDimension() {
        String url = Constants.getBasicOrgDimensionUrl();
        String messageLog = "开始调用【获取所有组织维度】，接口url=" + url + ",参数值为空";
        ResponseData<List<OrganizationDimension>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<OrganizationDimension>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }


    /**
     * 获取当前用户拥有权限的应用模块
     *
     * @return 拥有权限的应用模块集合
     */
    public List<com.ecmp.flow.basic.vo.AppModule> getBasicTenantAppModule() {
        String url = Constants.getBasicTenantAppModuleUrl();
        String messageLog = "开始调用【获取当前用户拥有权限的应用模块】，接口url=" + url + ",不需要参数值";
        ResponseData<List<com.ecmp.flow.basic.vo.AppModule>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<com.ecmp.flow.basic.vo.AppModule>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString(url));
            }
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog, e);
            throw new FlowException(getErrorLogString(url), e);
        }
    }


    //    @Cacheable(value = "FLowGetDefinitionJSON", key = "'FLowGetDefinitionJSON_' + #flowDefVersion.id")
    public Definition flowDefinition(FlowDefVersion flowDefVersion) {
        String defObjStr = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        return definition;
    }


    //    @Cacheable(value = "FLowGetLastFlowDefVersion", key = "'FLowGetLastFlowDefVersion_' + #versionId")
    public FlowDefVersion getLastFlowDefVersion(String versionId) {
        FlowDefVersion flowDefVersion = flowDefVersionDao.findByIdNoF(versionId);
        return flowDefVersion;
    }

    private void clearAllCache() {
        clearCache("FLowGetDefinitionJSON_*");
        clearCache("FLowGetBasicExecutor_*");
        clearCache("FLowGetBasicExecutors*");
        clearCache("FLowGetLastFlowDefVersion_*");
        clearCache("FLowOrgParentCodes_*");

    }


    private void clearCache(String pattern) {
//        String pattern = "FLowGetLastFlowDefVersion_*";
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }
}
