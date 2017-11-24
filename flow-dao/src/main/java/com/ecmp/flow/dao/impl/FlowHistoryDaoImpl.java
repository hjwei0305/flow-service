package com.ecmp.flow.dao.impl;

import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.dao.CustomFlowHistoryDao;
import com.ecmp.flow.dao.CustomFlowHistoryDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.WorkPageUrl;
import org.apache.commons.lang.StringUtils;

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
public class FlowHistoryDaoImpl extends BaseEntityDaoImpl<FlowHistory> implements CustomFlowHistoryDao {
    public FlowHistoryDaoImpl(EntityManager entityManager) {
        super(FlowHistory.class, entityManager);
    }

    public PageResult<FlowHistory> findByPageByBusinessModelId(String businessModelId,String executorAccount, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();

        TypedQuery<Integer> queryTotal = entityManager.createQuery("select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorAccount  = :executorAccount and ft.flowDefinitionId in(select fd.id from FlowDefination fd where fd.id in(select fType.id from FlowType fType where fType.id in( select bm.id from BusinessModel bm where bm.id = :businessModelId)) ) ", Integer.class);
        queryTotal.setParameter("executorAccount",executorAccount);
        queryTotal.setParameter("businessModelId",businessModelId);
        Integer total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorAccount  = :executorAccount and ft.flowDefinitionId in(select fd.id from FlowDefination fd where fd.id in(select fType.id from FlowType fType where fType.id in( select bm.id from BusinessModel bm where bm.id = :businessModelId)) ) order by ft.lastEditedDate desc", FlowHistory.class);
        query.setParameter("executorAccount",executorAccount);
        query.setParameter("businessModelId",businessModelId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowHistory>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(result.size());
        pageResult.setTotal(total);

        return pageResult;
    }
    public List<FlowHistory> findLastByBusinessId(String businessId){
        TypedQuery<FlowInstance> instanceQuery = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.businessId = :businessId  order by ft.lastEditedDate desc", FlowInstance.class);
        instanceQuery.setParameter("businessId",businessId);
        FlowInstance flowInstance = instanceQuery.getSingleResult();

        TypedQuery<FlowHistory> flowHistoryQuery = entityManager.createQuery("select ft.id from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.id = :instanceId  order by ft.lastEditedDate desc", FlowHistory.class);
        flowHistoryQuery.setParameter("instanceId",flowInstance.getId());
        List<FlowHistory> flowHistoryList = flowHistoryQuery.getResultList();
        initFlowTaskAppModule(flowHistoryList);
        return flowHistoryList;
    }

    public PageResult<FlowHistory> findByPageByBusinessModelId(String executorAccount, Search searchConfig){
        PageInfo pageInfo = searchConfig.getPageInfo();

        TypedQuery<Integer> queryTotal = entityManager.createQuery("select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorAccount  = :executorAccount  ", Integer.class);
        queryTotal.setParameter("executorAccount",executorAccount);
        Integer total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorAccount  = :executorAccount order by ft.lastEditedDate desc", FlowHistory.class);
        query.setParameter("executorAccount",executorAccount);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowHistory>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(result.size());
        pageResult.setTotal(total);

        return pageResult;
    }


    private List<FlowHistory> initFlowTaskAppModule(List<FlowHistory>  result ){
        if(result!=null && !result.isEmpty()){
            for(FlowHistory flowHistory:result){
                String apiBaseAddress = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                flowHistory.setApiBaseAddressAbsolute(apiBaseAddress);
                apiBaseAddress =  apiBaseAddress.substring(apiBaseAddress.lastIndexOf(":"));
                apiBaseAddress=apiBaseAddress.substring(apiBaseAddress.indexOf("/"));
                String webBaseAddress = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                flowHistory.setWebBaseAddressAbsolute(webBaseAddress);
                webBaseAddress =  webBaseAddress.substring(webBaseAddress.lastIndexOf(":"));
                webBaseAddress = webBaseAddress.substring(webBaseAddress.indexOf("/"));
                flowHistory.setApiBaseAddress(apiBaseAddress);
                flowHistory.setWebBaseAddress(webBaseAddress);
//                WorkPageUrl workPageUrl = flowHistory.getWorkPageUrl();
//                flowHistory.setCompleteTaskServiceUrl(flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getCompleteTaskServiceUrl());
//                if(workPageUrl!=null){
//                    flowHistory.setTaskFormUrl(flowHistory.getWebBaseAddressAbsolute()+workPageUrl.getUrl());
//                    flowHistory.setTaskFormUrlXiangDui(webBaseAddress+workPageUrl.getUrl());
//                    String appModuleId = workPageUrl.getAppModuleId();
//                    AppModule appModule = appModuleDao.findOne(appModuleId);
//                    if(appModule!=flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule()){
//                        webBaseAddress = appModule.getWebBaseAddress();
//                        flowHistory.setTaskFormUrl(webBaseAddress+workPageUrl.getUrl());
//                        webBaseAddress =  webBaseAddress.substring(webBaseAddress.lastIndexOf(":"));
//                        webBaseAddress = webBaseAddress.substring(webBaseAddress.indexOf("/"));
//                        flowHistory.setTaskFormUrlXiangDui(webBaseAddress+workPageUrl.getUrl());
//                    }
//                }
            }
        }
        return result;
    }

    public PageResult<FlowHistory> findByPage(String executorId, Search searchConfig){
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties= searchConfig.getQuickSearchProperties();
        String  quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId ";
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
        hqlQuery+=" order by ft.createdDate desc";
        TypedQuery<Long> queryTotal = entityManager.createQuery( hqlCount, Long.class);
        queryTotal.setParameter("executorId",executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery(hqlQuery, FlowHistory.class);
        query.setParameter("executorId",executorId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowHistory>  result = query.getResultList();
        initFlowTaskAppModule(result);
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());
        return pageResult;
    }

}
