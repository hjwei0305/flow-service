package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
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

    /**
     * 根据用户的id获取执行人
     * 1.剔除冻结的用户
     * 2.如果有员工信息，赋值组织机构和岗位信息
     * @param userId  用户id
     * @return 流程执行人
     */
    public Executor getBasicUserExecutor(String userId) {
        List<String> userIds = Arrays.asList((userId));
        Map<String,Object> params = new HashMap();
        params.put("userIds",userIds);
        String url = Constants.getBasicUserGetExecutorsbyUseridsUrl();
        String messageLog = "开始调用【根据用户的id获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        List<Executor> users ;
        try{
            users= ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url));
        }
        Executor executor = null;
        if(users!=null && !users.isEmpty()){
            executor = users.get(0);
        }
        return executor;
    }

    /**
     * 根据用户的id列表获取执行人
     * 1.剔除冻结的用户
     * 2.如果有员工信息，赋值组织机构和岗位信息
     * @param userIds  用户ID列表
     * @return  流程执行人集合
     */
    public List<Executor> getBasicUserExecutors(List<String> userIds) {
        Map<String,Object> params = new HashMap();
        params.put("userIds",userIds);
        String url = Constants.getBasicUserGetExecutorsbyUseridsUrl();
        List<Executor> users;
        String messageLog = "开始调用【根据用户的id列表获取执行人】，接口url="+url+",参数值"+ JsonUtils.toJson(params);
        try{
            users   = ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            throw  new FlowException(getErrorLogString(url));
        }
        return users;
    }

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
            throw  new FlowException(getErrorLogString(url));
        }
        return result;
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
