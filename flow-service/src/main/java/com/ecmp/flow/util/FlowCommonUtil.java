package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.bpmn.Definition;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;

import javax.ws.rs.core.GenericType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
//@Component
public class FlowCommonUtil implements Serializable {

    public FlowCommonUtil(){
        clearAllCache();
    }

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    //注入缓存模板
    @Autowired(required = false)
    protected RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "FLowGetDefinitionJSON", key = "'FLowGetDefinitionJSON_' + #flowDefVersion.id")
    public Definition flowDefinition(FlowDefVersion flowDefVersion ){
        String defObjStr = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        return definition;
    }

    @Cacheable(value = "FLowGetBasicExecutor", key = "'FLowGetBasicExecutor_' + #userId")
    public Executor getBasicExecutor(String userId) {
        Map<String,Object> params = new HashMap();
        params.put("employeeIds",java.util.Arrays.asList(userId));
        String url = com.ecmp.flow.common.util.Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
        List<Executor> employees= ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        Executor executor = null;
        if(employees!=null && !employees.isEmpty()){
            executor = employees.get(0);
        }
        return executor;
    }

    @Cacheable(value = "FLowGetBasicExecutors")
    public List<Executor> getBasicExecutors(List<String> userIds) {
        Map<String,Object> params = new HashMap();
        params.put("employeeIds",userIds);
        String url = com.ecmp.flow.common.util.Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
        List<Executor> employees= ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        return employees;
    }

    @Cacheable(value = "FLowGetLastFlowDefVersion", key = "'FLowGetLastFlowDefVersion_' + #versionId")
    public FlowDefVersion getLastFlowDefVersion(String versionId ){
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(versionId);
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
