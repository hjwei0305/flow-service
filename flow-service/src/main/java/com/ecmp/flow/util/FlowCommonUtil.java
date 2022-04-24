package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
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
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.GenericType;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, userIds);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【根据用户的id获取执行人】，接口返回错误信息:{}，接口url={}，参数值:{}", result.getMessage(), url, JsonUtils.toJson(userIds));
                throw new FlowException(ContextUtil.getMessage("10285", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据用户的id获取执行人】，调用异常:{}，接口url={}，参数值:{}", e.getMessage(), url, JsonUtils.toJson(userIds), e);
                throw new FlowException(ContextUtil.getMessage("10286", e.getMessage()));
            }else{
                throw e;
            }
        }

        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【根据用户的id获取执行人】，返回信息为空，接口url={}，参数值:{}", url, JsonUtils.toJson(userIds));
            throw new FlowException(ContextUtil.getMessage("10287"));
        }
        return executors.get(0);
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
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, userIds);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【根据用户的id列表获取执行人】，接口返回错误信息:{}，接口url={}，参数值:{}", result.getMessage(), url, JsonUtils.toJson(userIds));
                throw new FlowException(ContextUtil.getMessage("10288", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据用户的id列表获取执行人】，调用异常:{}，接口url={}，参数值:{}", e.getMessage(), url, JsonUtils.toJson(userIds), e);
                throw new FlowException(ContextUtil.getMessage("10289", e.getMessage()));
            }else{
                throw e;
            }
        }
        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【根据用户的id列表获取执行人】，返回信息为空，接口url={}，参数值:{}", url, JsonUtils.toJson(userIds));
            throw new FlowException(ContextUtil.getMessage("10290"));
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
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【根据岗位的id列表获取执行人】，接口返回错误信息:{}，接口url={}，参数值:{}", result.getMessage(), url, JsonUtils.toJson(vo));
                throw new FlowException(ContextUtil.getMessage("10291", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据岗位的id列表获取执行人】，调用异常:{}，接口url={}，参数值:{}", e.getMessage(), url, JsonUtils.toJson(vo), e);
                throw new FlowException(ContextUtil.getMessage("10292", e.getMessage()));
            }else{
                throw e;
            }
        }
        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【根据岗位的id列表获取执行人】，返回信息为空，接口url={}，参数值:{}", url, JsonUtils.toJson(vo));
            throw new FlowException(ContextUtil.getMessage("10293"));
        } else {
            LogUtil.bizLog("开始调用【根据岗位的id列表获取执行人】，接口url={}，参数值:{},返回信息【{}】", url, JsonUtils.toJson(vo), JsonUtils.toJson(executors));
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
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【根据岗位类别的id列表获取执行人】，接口返回错误信息:{}，接口url={}，参数值:{}", result.getMessage(), url, JsonUtils.toJson(vo));
                throw new FlowException(ContextUtil.getMessage("10294", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据岗位类别的id列表获取执行人】，调用异常:{}，接口url={}，参数值:{}", e.getMessage(), url, JsonUtils.toJson(vo), e);
                throw new FlowException(ContextUtil.getMessage("10295", e.getMessage()));
            }else{
                throw e;
            }
        }
        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【根据岗位类别的id列表获取执行人】，返回信息为空，接口url={}，参数值:{}", url, JsonUtils.toJson(vo));
            throw new FlowException(ContextUtil.getMessage("10296"));
        } else {
            LogUtil.bizLog("开始调用【根据岗位类别的id列表获取执行人】，接口url={}，参数值:{},返回信息【{}】", url, JsonUtils.toJson(vo), JsonUtils.toJson(executors));
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
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【根据岗位集合、组织维度集合和组织机构获取执行人】，接口返回错误信息:{}，接口url={}，参数值:{}", result.getMessage(), url, JsonUtils.toJson(vo));
                throw new FlowException(ContextUtil.getMessage("10297", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据岗位集合、组织维度集合和组织机构获取执行人】，调用异常:{}，接口url={}，参数值:{}", e.getMessage(), url, JsonUtils.toJson(vo), e);
                throw new FlowException(ContextUtil.getMessage("10298", e.getMessage()));
            }else{
                throw e;
            }
        }
        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【根据岗位集合、组织维度集合和组织机构获取执行人】，返回信息为空，接口url={}，参数值:{}", url, JsonUtils.toJson(vo));
            throw new FlowException(ContextUtil.getMessage("10299"));
        } else {
            LogUtil.bizLog("开始调用【根据岗位集合、组织维度集合和组织机构获取执行人】，接口url={}，参数值:{},返回信息【{}】", url, JsonUtils.toJson(vo), JsonUtils.toJson(executors));
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
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, vo);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【根据岗位类别集合和组织机构集合获取执行人】，接口返回错误信息:{}，接口url={}，参数值:{}", result.getMessage(), url, JsonUtils.toJson(vo));
                throw new FlowException(ContextUtil.getMessage("10300", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据岗位类别集合和组织机构集合获取执行人】，调用异常:{}，接口url={}，参数值:{}", e.getMessage(), url, JsonUtils.toJson(vo), e);
                throw new FlowException(ContextUtil.getMessage("10301", e.getMessage()));
            }else{
                throw e;
            }
        }
        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【根据岗位类别集合和组织机构集合获取执行人】，返回信息为空，接口url={}，参数值:{}", url, JsonUtils.toJson(vo));
            throw new FlowException(ContextUtil.getMessage("10302"));
        } else {
            LogUtil.bizLog("开始调用【根据岗位类别集合和组织机构集合获取执行人】，接口url={}，参数值:{},返回信息【{}】", url, JsonUtils.toJson(vo), JsonUtils.toJson(executors));
        }
        return executors;
    }


    public List<Executor> getExecutorsBySelfDef(String appModuleCode, String selfName, String path, FlowInvokeParams flowInvokeParams) {
        String url;
        if (PageUrlUtil.isFullPath(path)) {
            url = path;
        } else if (PageUrlUtil.isAppModelUrl(path)) {
            url = PageUrlUtil.buildUrl(PageUrlUtil.getBaseApiUrl(), path);
        } else {
            url = PageUrlUtil.buildUrl(Constants.getConfigValueByApi(appModuleCode), path);
        }
        ResponseData<List<Executor>> result;
        List<Executor> executors;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<List<Executor>>>() {
            }, flowInvokeParams);
            if (result.successful()) {
                executors = result.getData();
            } else {
                LogUtil.error("开始调用【自定义执行人-{}】，接口返回错误信息:{}，接口url={}，参数值:{}", selfName, result.getMessage(), url, JsonUtils.toJson(flowInvokeParams));
                throw new FlowException(ContextUtil.getMessage("10303", selfName, result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【自定义执行人-{}】，调用异常:{}，接口url={}，参数值:{}", selfName, e.getMessage(), url, JsonUtils.toJson(flowInvokeParams), e);
                throw new FlowException(ContextUtil.getMessage("10304", selfName, e.getMessage()));
            } else {
                throw e;
            }
        }

        if (CollectionUtils.isEmpty(executors)) {
            LogUtil.error("开始调用【自定义执行人-{}】，返回信息为空，接口url={}，参数值:{}", selfName, url, JsonUtils.toJson(flowInvokeParams));
            throw new FlowException(ContextUtil.getMessage("10305", selfName));
        } else {
            LogUtil.bizLog("开始调用【自定义执行人-{}】，接口url={}，参数值:{},返回信息【{}】", selfName, url, JsonUtils.toJson(flowInvokeParams), JsonUtils.toJson(executors));
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
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Organization>>>() {
            }, null);
            if (result.successful()) {
                String tenantCode = ContextUtil.getTenantCode();
                try {
                    redisTemplate.opsForValue().set(Constants.REDIS_KEY_PREFIX + "getBasicAllOrgs:" + tenantCode, result.getData(), 60 * 60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LogUtil.error("保存全部组织机构列表到缓存失败：", e.getMessage(), e);
                }
                return result.getData();
            } else {
                LogUtil.error("开始调用【获取所有组织机构树】，接口返回错误信息:{}，接口url={}，参数为空", result.getMessage(), url);
                throw new FlowException(ContextUtil.getMessage("10306", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取所有组织机构树】，调用异常:{}，接口url={}，参数为空", e.getMessage(), url, e);
                throw new FlowException(ContextUtil.getMessage("10307", e.getMessage()));
            } else {
                throw e;
            }
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
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Organization>>>() {
            }, null);
            if (result.successful()) {
                String tenantCode = ContextUtil.getTenantCode();
                try {
                    redisTemplate.opsForValue().set(Constants.REDIS_KEY_PREFIX + "getBasicAllOrgByPower:" + tenantCode, result.getData(), 60 * 60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LogUtil.error("保存有权限的组织机构列表到缓存失败：", e.getMessage(), e);
                }
                return result.getData();
            } else {
                LogUtil.error("开始调用【获取所有有权限的组织机构树】，接口返回错误信息:{}，接口url={}，参数为空", result.getMessage(), url);
                throw new FlowException(ContextUtil.getMessage("10308", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取所有有权限的组织机构树】，调用异常:{}，接口url={}，参数为空", e.getMessage(), url, e);
                throw new FlowException(ContextUtil.getMessage("10309", e.getMessage()));
            }else{
                throw e;
            }
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
        ResponseData<List<Organization>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Organization>>>() {
            }, params);
            if (result.successful()) {
                return result.getData();
            } else {
                LogUtil.error("开始调用【获取指定节点的父组织机构列表】，接口返回错误信息:{}，接口url={}，参数：{}", result.getMessage(), url, JsonUtils.toJson(params));
                throw new FlowException(ContextUtil.getMessage("10310", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取指定节点的父组织机构列表】，调用异常:{}，接口url={}，参数：{}", e.getMessage(), url, JsonUtils.toJson(params), e);
                throw new FlowException(ContextUtil.getMessage("10311", e.getMessage()));
            }else{
                throw e;
            }
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
        ResponseData<List<Employee>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<Employee>>>() {
            }, params);
            if (result.successful()) {
                return result.getData();
            } else {
                LogUtil.error("开始调用【根据组织机构ID获取员工集合】，接口返回错误信息:{}，接口url={}，参数：{}", result.getMessage(), url, JsonUtils.toJson(params));
                throw new FlowException(ContextUtil.getMessage("10312", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据组织机构ID获取员工集合】，调用异常:{}，接口url={}，参数：{}", e.getMessage(), url, JsonUtils.toJson(params), e);
                throw new FlowException(ContextUtil.getMessage("10313", e.getMessage()));
            }else{
                throw e;
            }
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
        ResponseData<PageResult<Employee>> result;
        try {
            result = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData<PageResult<Employee>>>() {
            }, userQueryParamVo);
            if (result.successful()) {
                return result.getData();
            } else {
                LogUtil.error("开始调用【根据组织机构ID获取员工集合】，接口返回错误信息:{}，接口url={}，参数：{}", result.getMessage(), url, JsonUtils.toJson(userQueryParamVo));
                throw new FlowException(ContextUtil.getMessage("10312", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【根据组织机构ID获取员工集合】，调用异常:{}，接口url={}，参数：{}", e.getMessage(), url, JsonUtils.toJson(userQueryParamVo), e);
                throw new FlowException(ContextUtil.getMessage("10313", e.getMessage()));
            }else{
                throw e;
            }
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
                LogUtil.error("开始调用【获取所有岗位】，接口返回错误信息:{}，接口url={}，参数：{}", result.getMessage(), url, JsonUtils.toJson(search));
                throw new FlowException(ContextUtil.getMessage("10314", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取所有岗位】，调用异常:{}，接口url={}，参数：{}", e.getMessage(), url, JsonUtils.toJson(search), e);
                throw new FlowException(ContextUtil.getMessage("10315", e.getMessage()));
            }else{
                throw e;
            }
        }
    }


    //----------------------------------------------获取岗位类别--------------------------------//
    public List<PositionCategory> getBasicPositioncategoryFindall() {
        String url = Constants.getBasicPositioncategoryFindallUrl();
        ResponseData<List<PositionCategory>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<PositionCategory>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                LogUtil.error("开始调用【获取所有岗位类别】，接口返回错误信息:{}，接口url={}，参数值为空", result.getMessage(), url);
                throw new FlowException(ContextUtil.getMessage("10316", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取所有岗位类别】，调用异常:{}，接口url={}，参数值为空", e.getMessage(), url, e);
                throw new FlowException(ContextUtil.getMessage("10317", e.getMessage()));
            }else{
                throw e;
            }
        }
    }

    //----------------------------------------------获取组织维度--------------------------------//
    public List<OrganizationDimension> getBasicOrgDimension() {
        String url = Constants.getBasicOrgDimensionUrl();
        ResponseData<List<OrganizationDimension>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<OrganizationDimension>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                LogUtil.error("开始调用【获取所有组织维度】，接口返回错误信息:{}，接口url={}，参数值为空", result.getMessage(), url);
                throw new FlowException(ContextUtil.getMessage("10318", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取所有组织维度】，调用异常:{}，接口url={}，参数值为空", e.getMessage(), url, e);
                throw new FlowException(ContextUtil.getMessage("10319", e.getMessage()));
            }else{
                throw e;
            }
        }
    }


    /**
     * 获取当前用户拥有权限的应用模块
     *
     * @return 拥有权限的应用模块集合
     */
    public List<com.ecmp.flow.basic.vo.AppModule> getBasicTenantAppModule() {
        String url = Constants.getBasicTenantAppModuleUrl();
        ResponseData<List<com.ecmp.flow.basic.vo.AppModule>> result;
        try {
            result = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData<List<com.ecmp.flow.basic.vo.AppModule>>>() {
            }, null);
            if (result.successful()) {
                return result.getData();
            } else {
                LogUtil.error("开始调用【获取当前用户拥有权限的应用模块】，接口返回错误信息:{}，接口url={}，参数值为空", result.getMessage(), url);
                throw new FlowException(ContextUtil.getMessage("10320", result.getMessage()));
            }
        } catch (Exception e) {
            if (e.getClass() != FlowException.class) {
                LogUtil.error("开始调用【获取当前用户拥有权限的应用模块】，调用异常:{}，接口url={}，参数值为空", e.getMessage(), url, e);
                throw new FlowException(ContextUtil.getMessage("10321", e.getMessage()));
            }else{
                throw e;
            }
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
