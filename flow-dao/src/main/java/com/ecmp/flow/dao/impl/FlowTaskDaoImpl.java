package com.ecmp.flow.dao.impl;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.dao.AppModuleDao;
import com.ecmp.flow.dao.CustomFlowTaskDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.WorkPageUrl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collection;
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
 * 1.0.00      2017/5/9 20:26      马超(Vision.Mac)                新建
 * <br>
 * *************************************************************************************************
 */
public class FlowTaskDaoImpl extends BaseEntityDaoImpl<FlowTask> implements CustomFlowTaskDao {
    public FlowTaskDaoImpl(EntityManager entityManager) {
        super(FlowTask.class, entityManager);
    }
    @Autowired
    private AppModuleDao appModuleDao;

    String hqlQueryOrder="   order by ft.priority desc,ft.createdDate asc ";


    public PageResult<FlowTask> findByPageByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();

        String hqlCount ="select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where  (ft.trustState !=1  or ft.trustState is null )";
        String hqlQuery = "select ft          from com.ecmp.flow.entity.FlowTask ft where  (ft.trustState !=1  or ft.trustState is null )";

        if(StringUtils.isNotEmpty(flowTypeId)&&!"".equals(flowTypeId)){
            hqlCount += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.id  = '"+flowTypeId+"' ))";
            hqlQuery += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.id  = '"+flowTypeId+"' ))";
        }else if(StringUtils.isNotEmpty(businessModelId)&&!"".equals(businessModelId)){
            hqlCount += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = '"+businessModelId+"' ) )";
            hqlQuery += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = '"+businessModelId+"' ) )";
        }else if(StringUtils.isNotEmpty(appModuleId)&&!"".equals(appModuleId)){
            hqlCount += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.id='"+appModuleId+"'   )) )";
            hqlQuery += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.id='"+appModuleId+"'   )) )";
        }

        if(StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties!=null && !quickSearchProperties.isEmpty()){
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for(String s:quickSearchProperties){
                if(first){
                    extraHql.append("  ft."+s+" like '%"+quickSearchValue+"%'");
                    first = false;
                }else {
                    extraHql.append(" or  ft."+s+" like '%"+quickSearchValue+"%'");
                }
            }
            extraHql.append(" )");
            hqlCount+=extraHql.toString();
            hqlQuery+=extraHql.toString();
        }
        hqlQuery+=hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowTask>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());

        return pageResult;
    }


    public PageResult<FlowTask> findByPageByBusinessModelId(String businessModelId,String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and (ft.trustState !=1  or ft.trustState is null ) and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and (ft.trustState !=1  or ft.trustState is null ) and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id  in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )";
        if(StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties!=null && !quickSearchProperties.isEmpty()){
               StringBuffer extraHql = new StringBuffer(" and (");
               boolean first = true;
               for(String s:quickSearchProperties){
                   if(first){
                       extraHql.append("  ft."+s+" like '%"+quickSearchValue+"%'");
                       first = false;
                   }else {
                       extraHql.append(" or  ft."+s+" like '%"+quickSearchValue+"%'");
                   }
               }
            extraHql.append(" )");
            hqlCount+=extraHql.toString();
            hqlQuery+=extraHql.toString();
        }
        hqlQuery+=hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        queryTotal.setParameter("executorId",executorId);
        queryTotal.setParameter("businessModelId",businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId",executorId);
        query.setParameter("businessModelId",businessModelId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowTask>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());

        return pageResult;
    }

    private List<FlowTask> initFlowTaskAppModule(List<FlowTask>  result ){
        if(result!=null && !result.isEmpty()){
               for(FlowTask flowTask:result){
                   String apiBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                   String apiBaseAddress =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                   if(StringUtils.isNotEmpty(apiBaseAddress)){
                       flowTask.setApiBaseAddressAbsolute(apiBaseAddress);
                       String[]  tempApiBaseAddress = apiBaseAddress.split("/");
                       if(tempApiBaseAddress!=null && tempApiBaseAddress.length>0){
                           apiBaseAddress = tempApiBaseAddress[tempApiBaseAddress.length-1];
                           flowTask.setApiBaseAddress("/"+apiBaseAddress+"/");
                       }
                   }
                   String webBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                   String webBaseAddress =  ContextUtil.getGlobalProperty(webBaseAddressConfig);

                   if(StringUtils.isNotEmpty(webBaseAddress)){
                       flowTask.setWebBaseAddressAbsolute(webBaseAddress);
                       String[]  tempWebBaseAddress = webBaseAddress.split("/");
                       if(tempWebBaseAddress!=null && tempWebBaseAddress.length>0){
                           webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length-1];
                           flowTask.setWebBaseAddress("/"+webBaseAddress+"/");
                       }
                   }
                   WorkPageUrl workPageUrl = flowTask.getWorkPageUrl();
                   String completeTaskServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getCompleteTaskServiceUrl();
                   String businessDetailServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
                   if(StringUtils.isEmpty(completeTaskServiceUrl)){
                       completeTaskServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getCompleteTaskServiceUrl();
                   }
                   if(StringUtils.isEmpty(businessDetailServiceUrl)){
                       businessDetailServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
                   }
                   flowTask.setCompleteTaskServiceUrl(completeTaskServiceUrl);
                   flowTask.setBusinessDetailServiceUrl(businessDetailServiceUrl);
                   if(workPageUrl!=null){
                       flowTask.setTaskFormUrl(flowTask.getWebBaseAddressAbsolute()+"/"+workPageUrl.getUrl());
                       String taskFormUrlXiangDui = "/"+webBaseAddress+"/"+workPageUrl.getUrl();
                       taskFormUrlXiangDui =  taskFormUrlXiangDui.replaceAll("\\//","/");
                       flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
                       String appModuleId = workPageUrl.getAppModuleId();
                       AppModule appModule = appModuleDao.findOne(appModuleId);
                       if(appModule!=null && appModule!=flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule()){
                           webBaseAddressConfig = appModule.getWebBaseAddress();
                           webBaseAddress =  ContextUtil.getGlobalProperty(webBaseAddressConfig);
                           flowTask.setTaskFormUrl(webBaseAddress+"/"+workPageUrl.getUrl());
                           webBaseAddress =  webBaseAddress.substring(webBaseAddress.indexOf("://")+3);
                           webBaseAddress = webBaseAddress.substring(webBaseAddress.indexOf("/"));
                           taskFormUrlXiangDui = "/"+webBaseAddress+"/"+workPageUrl.getUrl();
                           taskFormUrlXiangDui =  taskFormUrlXiangDui.replaceAll("\\//","/");
                           flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
                       }
                   }
               }
       }
       return result;
    }

    public PageResult<FlowTask> findByPage(String executorId, Search searchConfig){
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId  and (ft.trustState !=1  or ft.trustState is null ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId  and (ft.trustState !=1  or ft.trustState is null ) ";
        if(StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties!=null && !quickSearchProperties.isEmpty()){
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for(String s:quickSearchProperties){
                if(first){
                    extraHql.append("  ft."+s+" like '%"+quickSearchValue+"%'");
                    first = false;
                }else {
                    extraHql.append(" or  ft."+s+" like '%"+quickSearchValue+"%'");
                }
            }
            extraHql.append(" )");
            hqlCount+=extraHql.toString();
            hqlQuery+=extraHql.toString();
        }
        hqlQuery+=hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        queryTotal.setParameter("executorId",executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId",executorId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowTask>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());
        return pageResult;
    }

    public PageResult<FlowTask> findByPageCanBatchApproval(String executorId, Search searchConfig){
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true ";
        if(StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties!=null && !quickSearchProperties.isEmpty()){
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for(String s:quickSearchProperties){
                if(first){
                    extraHql.append("  ft."+s+" like '%"+quickSearchValue+"%'");
                    first = false;
                }else {
                    extraHql.append(" or  ft."+s+" like '%"+quickSearchValue+"%'");
                }
            }
            extraHql.append(" )");
            hqlCount+=extraHql.toString();
            hqlQuery+=extraHql.toString();
        }
        hqlQuery+=hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        queryTotal.setParameter("executorId",executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId",executorId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowTask>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());

        return pageResult;
    }


    public Long findCountByExecutorId(String executorId, Search searchConfig){
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId  and  (ft.trustState !=1  or ft.trustState is null ) ";
        if(StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties!=null && !quickSearchProperties.isEmpty()){
            StringBuffer extraHql = new StringBuffer("and (");
            boolean first = true;
            for(String s:quickSearchProperties){
                if(first){
                    extraHql.append("  ft."+s+" like '%"+quickSearchValue+"%'");
                    first = false;
                }else {
                    extraHql.append(" or  ft."+s+" like '%"+quickSearchValue+"%'");
                }
            }
            extraHql.append(" )");
            hqlCount+=extraHql.toString();
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        queryTotal.setParameter("executorId",executorId);
        Long total = queryTotal.getSingleResult();
        return total;
    }

    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(String businessModelId ,String executorId, Search searchConfig){
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )";
        if(StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties!=null && !quickSearchProperties.isEmpty()){
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for(String s:quickSearchProperties){
                if(first){
                    extraHql.append("  ft."+s+" like '%"+quickSearchValue+"%'");
                    first = false;
                }else {
                    extraHql.append(" or  ft."+s+" like '%"+quickSearchValue+"%'");
                }
            }
            extraHql.append(" )");
            hqlCount+=extraHql.toString();
            hqlQuery+=extraHql.toString();
        }
        hqlQuery+=hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        queryTotal.setParameter("executorId",executorId);
        queryTotal.setParameter("businessModelId",businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId",executorId);
        query.setParameter("businessModelId",businessModelId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowTask>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());

        return pageResult;
    }
}
