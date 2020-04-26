package com.ecmp.flow.dao.impl;

import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.*;
import com.ecmp.flow.dao.CustomFlowHistoryDao;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
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

    public PageResult<FlowHistory> findByPageByBusinessModelId(String businessModelId, String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        List<SearchFilter> searchFilters = searchConfig.getFilters();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and ft.flowInstance.id in  ( select ins.id from  com.ecmp.flow.entity.FlowInstance ins where  ins.flowDefVersion.id in  (select    ve.id from com.ecmp.flow.entity.FlowDefVersion ve  where  ve.flowDefination.id in ( select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in  (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = :businessModelId  ) ) ) ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and ft.flowInstance.id in  ( select ins.id from  com.ecmp.flow.entity.FlowInstance ins where  ins.flowDefVersion.id in  (select    ve.id from com.ecmp.flow.entity.FlowDefVersion ve  where  ve.flowDefination.id in ( select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in  (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = :businessModelId  ) ) ) ) ";

        if (searchFilters != null && searchFilters.size() > 0 && searchFilters.get(0).getValue() != null) {
            SearchFilter filters = searchFilters.get(0);
            if ("valid".equals(filters.getValue())) {//有效数据
                hqlCount += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject')  or  ft.flowExecuteStatus is null  )";
                hqlQuery += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject')  or  ft.flowExecuteStatus is null  ) ";
            } else if ("record".equals(filters.getValue())) { //记录数据
                hqlCount += " and ft.flowExecuteStatus in ('end','auto') ";
                hqlQuery += " and ft.flowExecuteStatus in ('end','auto') ";
            }
        }

        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        if (sortOrders != null && sortOrders.size() > 0) {
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (i == 0) {
                    hqlQuery += "order by  ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                } else {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += " order by ft.createdDate desc";
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery(hqlQuery, FlowHistory.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);

        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowHistory> result = query.getResultList();
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    public PageResult<FlowHistory> findByPageByBusinessModelIdAndFlowStatus(String businessModelId, String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        List<SearchFilter> searchFilters = searchConfig.getFilters();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId  ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId  ";
        if (StringUtils.isNotEmpty(businessModelId)) {
            hqlCount += "  and ft.flowInstance.id in  ( select ins.id from  com.ecmp.flow.entity.FlowInstance ins where  ins.flowDefVersion.id in  (select    ve.id from com.ecmp.flow.entity.FlowDefVersion ve  where  ve.flowDefination.id in ( select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in  (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = :businessModelId  ) ) ) ) ";
            hqlQuery += "  and ft.flowInstance.id in  ( select ins.id from  com.ecmp.flow.entity.FlowInstance ins where  ins.flowDefVersion.id in  (select    ve.id from com.ecmp.flow.entity.FlowDefVersion ve  where  ve.flowDefination.id in ( select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in  (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = :businessModelId  ) ) ) ) ";
        }

        if (searchFilters != null && searchFilters.size() > 0 && searchFilters.get(0).getValue() != null) {
            for (int i = 0; i < searchFilters.size(); i++) {
                SearchFilter filters = searchFilters.get(i);
                if ("ended".equals(filters.getFieldName())) {
                    Boolean boo = (Boolean) filters.getValue();
                    hqlCount += "  and ft.flowInstance.ended = " + boo;
                    hqlQuery += "  and ft.flowInstance.ended = " + boo;
                } else {
                    Boolean boo = (Boolean) filters.getValue();
                    hqlCount += "  and ft.flowInstance.manuallyEnd = " + boo;
                    hqlQuery += "  and ft.flowInstance.manuallyEnd = " + boo;
                }
            }
        }

        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        if (sortOrders != null && sortOrders.size() > 0) {
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (i == 0) {
                    hqlQuery += " order by  ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                } else {
                    hqlQuery += " , ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += " order by ft.createdDate desc";
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        if (StringUtils.isNotEmpty(businessModelId)) {
            queryTotal.setParameter("businessModelId", businessModelId);
        }
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery(hqlQuery, FlowHistory.class);
        query.setParameter("executorId", executorId);
        if (StringUtils.isNotEmpty(businessModelId)) {
            query.setParameter("businessModelId", businessModelId);
        }

        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowHistory> result = query.getResultList();
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    public List<FlowHistory> findByAllTaskMakeOverPowerHistory() {
        TypedQuery<FlowHistory> flowHistoryQuery = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId != ft.ownerId  and  ft.executorId is not null and  ft.ownerId is not null  order by ft.lastEditedDate desc", FlowHistory.class);
        List<FlowHistory> flowHistoryList = flowHistoryQuery.getResultList();
        return flowHistoryList;
    }

    public List<FlowHistory> findLastByBusinessId(String businessId) {
        TypedQuery<FlowInstance> instanceQuery = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.businessId = :businessId  order by ft.lastEditedDate desc", FlowInstance.class);
        instanceQuery.setParameter("businessId", businessId);
        FlowInstance flowInstance = instanceQuery.getSingleResult();

        TypedQuery<FlowHistory> flowHistoryQuery = entityManager.createQuery("select ft.id from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.id = :instanceId  order by ft.lastEditedDate desc", FlowHistory.class);
        flowHistoryQuery.setParameter("instanceId", flowInstance.getId());
        List<FlowHistory> flowHistoryList = flowHistoryQuery.getResultList();
        return flowHistoryList;
    }

    public PageResult<FlowHistory> findByPageByBusinessModelId(String executorAccount, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();

        TypedQuery<Integer> queryTotal = entityManager.createQuery("select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorAccount  = :executorAccount  ", Integer.class);
        queryTotal.setParameter("executorAccount", executorAccount);
        Integer total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorAccount  = :executorAccount order by ft.lastEditedDate desc", FlowHistory.class);
        query.setParameter("executorAccount", executorAccount);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowHistory> result = query.getResultList();
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(result.size());
        pageResult.setTotal(total);

        return pageResult;
    }


//    private List<FlowHistory> initFlowTaskAppModule(List<FlowHistory>  result ){
//        if(result!=null && !result.isEmpty()){
//            for(FlowHistory flowHistory:result){
//                String apiBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
//                String apiBaseAddress =  Constants.getConfigValueByApi(apiBaseAddressConfig);
//                if(StringUtils.isNotEmpty(apiBaseAddress)){
//                    flowHistory.setApiBaseAddressAbsolute(apiBaseAddress);
//                    String[]  tempWebApiBaseAddress = apiBaseAddress.split("/");
//                    if(tempWebApiBaseAddress!=null && tempWebApiBaseAddress.length>0){
//                        apiBaseAddress = tempWebApiBaseAddress[tempWebApiBaseAddress.length-1];
//                        flowHistory.setApiBaseAddress("/"+apiBaseAddress+"/");
//                    }
//                }
//                String webBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
//                String webBaseAddress =  Constants.getConfigValueByWeb(webBaseAddressConfig);
//                if(StringUtils.isNotEmpty(webBaseAddress)){
//                    flowHistory.setWebBaseAddressAbsolute(webBaseAddress);
//                    String[]  tempWebBaseAddress = webBaseAddress.split("/");
//                    if(tempWebBaseAddress!=null && tempWebBaseAddress.length>0){
//                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length-1];
//                        flowHistory.setWebBaseAddress("/"+webBaseAddress+"/");
//                    }
//                }
//            }
//        }
//        return result;
//    }

    public PageResult<FlowHistory> findByPage(String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        List<SearchFilter> searchFilters = searchConfig.getFilters();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId ";
        if (searchFilters != null && searchFilters.size() > 0 && searchFilters.get(0).getValue() != null) {
            SearchFilter filters = searchFilters.get(0);
            if ("valid".equals(filters.getValue())) {//有效数据(以前没这个字段，都作为有效数据)
                hqlCount += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject')  or  ft.flowExecuteStatus is null  )";
                hqlQuery += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject')  or  ft.flowExecuteStatus is null  ) ";
            } else if ("record".equals(filters.getValue())) { //记录数据
                hqlCount += " and ft.flowExecuteStatus in ('end','auto') ";
                hqlQuery += " and ft.flowExecuteStatus in ('end','auto') ";
            }
        }
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append("  or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        if (sortOrders != null && sortOrders.size() > 0) {
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (i == 0) {
                    hqlQuery += " order by  ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                } else {
                    hqlQuery += " , ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += " order by ft.createdDate desc";
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowHistory> query = entityManager.createQuery(hqlQuery, FlowHistory.class);
        query.setParameter("executorId", executorId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowHistory> result = query.getResultList();
        PageResult<FlowHistory> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

}
