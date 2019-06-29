package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.basic.vo.Employee;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    public FlowCommonUtil(){
        clearAllCache();
    }

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    //注入缓存模板
    @Autowired(required = false)
    protected RedisTemplate<String, Object> redisTemplate;


    public  String  getErrorLogString(String url){
        return  "【调用接口异常："+url+",详情请查看日志】";
    }


    //------------------------------------------------获取执行人--------------------------------//
    /**
     * 根据用户的id获取执行人
     * 1.剔除冻结的用户  2.如果有员工信息，赋值组织机构和岗位信息
     * @param userId  用户id
     * @return 流程执行人
     */
    public Executor getBasicUserExecutor(String userId) {
        List<String> userIds = Arrays.asList((userId));
        Map<String,Object> params = new HashMap();
        params.put("userIds",userIds);
        String url = Constants.getBasicUserGetExecutorsbyUseridsUrl();
        String messageLog = "开始调用【根据用户的id获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Executor> executors ;
        try{
            executors= ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        Executor executor = null;
        if(executors!=null && !executors.isEmpty()){
            executor = executors.get(0);
        }
        return executor;
    }
    /**
     * 根据用户的id列表获取执行人
     * 1.剔除冻结的用户  2.如果有员工信息，赋值组织机构和岗位信息
     * @param userIds  用户ID列表
     * @return  流程执行人集合
     */
    public List<Executor> getBasicUserExecutors(List<String> userIds) {
        Map<String,Object> params = new HashMap();
        params.put("userIds",userIds);
        String url = Constants.getBasicUserGetExecutorsbyUseridsUrl();
        List<Executor> executors;
        String messageLog = "开始调用【根据用户的id列表获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        try{
            executors   = ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return executors;
    }
    /**
     * 根据岗位id列集合获取执行人
     * @param positionIds  岗位id集合
     * @param orgId  组织机构id（平台basic接口没用这个参数，项目需要的，直接在basic接口添加参数使用）
     * @return  流程执行人集合
     */
    public List<Executor> getBasicExecutorsByPositionIds(List<String> positionIds,String orgId){
        Map<String, Object> params = new HashMap();
        params.put("positionIds", positionIds);
        params.put("orgId", orgId);
        String url = Constants.getBasicPositionGetexecutorsbypositionidsUrl();
        String messageLog = "开始调用【根据岗位的id列表获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Executor> executors;
        try{
            executors = ApiClient.getEntityViaProxy(url, new GenericType<List<Executor>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return executors;
    }
    /**
     * 根据岗位类别id列集合获取执行人
     * @param postCatIds  岗位类别id集合
     * @param orgId  组织机构id
     * @return  流程执行人集合
     */
    public List<Executor> getBasicExecutorsByPostCatIds(List<String> postCatIds,String orgId){
        Map<String, Object> params = new HashMap();
        params.put("postCatIds", postCatIds);
        params.put("orgId", orgId);
        String url = Constants.getBasicPositionGetexecutorsbyposcateidsUrl();
        String messageLog = "开始调用【根据岗位类别的id列表获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Executor> executors;
        try{
            executors = ApiClient.getEntityViaProxy(url, new GenericType<List<Executor>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return executors;
    }

    /**
     * 通过岗位ids、组织维度ids和组织机构id来获取执行人
     * @param positionIds  岗位ids
     * @param orgDimIds  组织维度ids
     * @param orgId   组织机构id
     * @return    流程执行人集合
     */
    public  List<Executor>  getExecutorsByPositionIdsAndorgDimIds(List<String> positionIds,List<String> orgDimIds,String orgId){
        Map<String, Object> params = new HashMap();
        params.put("orgId", orgId);
        params.put("orgDimIds", orgDimIds);
        params.put("positionIds", positionIds);
        String url = Constants.getBasicPositionGetExecutorsUrl();
        String messageLog = "开始调用【根据岗位集合、组织维度集合和组织机构获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Executor> executors;
        try{
            executors = ApiClient.getEntityViaProxy(url, new GenericType<List<Executor>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return executors;
    }

    /**
     * 通过岗位类别ids和组织机构ids获取执行人
     * @param postCatIds  岗位类别ids
     * @param orgIds    组织机构ids
     * @return    流程执行人集合
     */
    public List<Executor>  getExecutorsByPostCatIdsAndOrgs(List<String> postCatIds,List<String> orgIds){
        Map<String, Object> params = new HashMap();
        params.put("postCatIds", postCatIds);
        params.put("orgIds", orgIds);
        String url = Constants.getExecutorsByPostCatAndOrgUrl();
        String messageLog = "开始调用【根据岗位类别集合和组织机构集合获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Executor> executors;
        try{
            executors = ApiClient.getEntityViaProxy(url, new GenericType<List<Executor>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return executors;
    }











    //------------------------------------------------获取组织机构--------------------------------//
    /**
     * 获取所有组织机构树（不包含冻结）
     * @return
     */
    public  List<Organization> getBasicAllOrgs(){
        String url = Constants.getBasicOrgListallorgsUrl();
        List<Organization> result;
        String messageLog = "开始调用【获取所有组织机构树】，接口url="+url+",参数值为null";
        try{
            result = ApiClient.getEntityViaProxy(url, new GenericType<List<Organization>>() {}, null);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return result;
    }
    /**
     * 获取指定节点的父组织机构列表
     * @param nodeId  指定的组织机构id
     * @return  包含自己的所有父组织机构集合
     */
    public List<Organization> getParentOrganizations(String nodeId) {
        Map<String, Object> params = new HashMap();
        params.put("includeSelf", true); //默认包含本组织机构
        params.put("nodeId", nodeId);
        String url = Constants.getBasicOrgFindparentnodesUrl();
        String messageLog = "开始调用【获取指定节点的父组织机构列表】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Organization> organizationsList;
        try{
            organizationsList = ApiClient.getEntityViaProxy(url, new GenericType<List<Organization>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return organizationsList;
    }


    //----------------------------------------------获取员工--------------------------------//
    /**
     * 根据组织机构ID获取员工集合
     * @param orgId  组织机构ID
     * @return   员工list集合
     */
    public List<Employee> getEmployeesByOrgId(String orgId){
        Map<String,Object> params = new HashMap();
        params.put("organizationId",orgId);
        String url = Constants.getBasicEmployeeFindbyorganizationidUrl();
        String messageLog = "开始调用【根据组织机构ID获取员工集合】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Employee> employeeList;
        try{
            employeeList = ApiClient.getEntityViaProxy(url, new GenericType<List<Employee>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
        return employeeList;
    }

    /**
     * 获取当前用户拥有权限的应用模块
     * @return  拥有权限的应用模块集合
     */
    public List<com.ecmp.flow.basic.vo.AppModule> getBasicTenantAppModule(){
        String url = Constants.getBasicTenantAppModuleUrl();
        String messageLog = "开始调用【获取当前用户拥有权限的应用模块】，接口url="+url+",不需要参数值";
        List<com.ecmp.flow.basic.vo.AppModule> appModuleList;
        try{
            appModuleList = ApiClient.getEntityViaProxy(url, new GenericType<List<com.ecmp.flow.basic.vo.AppModule>>() {}, null);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url), e);
        }
       return  appModuleList;
    }







//    @Cacheable(value = "FLowGetDefinitionJSON", key = "'FLowGetDefinitionJSON_' + #flowDefVersion.id")
    public Definition flowDefinition(FlowDefVersion flowDefVersion ){
        String defObjStr = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        return definition;
    }


//    @Cacheable(value = "FLowGetLastFlowDefVersion", key = "'FLowGetLastFlowDefVersion_' + #versionId")
    public FlowDefVersion getLastFlowDefVersion(String versionId ){
        FlowDefVersion flowDefVersion = flowDefVersionDao.findByIdNoF(versionId);
        return flowDefVersion;
    }

    private void clearAllCache(){
        clearCache( "FLowGetDefinitionJSON_*");
        clearCache( "FLowGetBasicExecutor_*");
        clearCache( "FLowGetBasicExecutors*");
        clearCache( "FLowGetLastFlowDefVersion_*");
        clearCache( "FLowOrgParentCodes_*");

    }


    private void clearCache(String pattern){
//        String pattern = "FLowGetLastFlowDefVersion_*";
        if(redisTemplate!=null){
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys!=null&&!keys.isEmpty()){
                redisTemplate.delete(keys);
            }
        }
    }
}
