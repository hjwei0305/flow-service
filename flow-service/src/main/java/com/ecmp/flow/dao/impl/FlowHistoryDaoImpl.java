package com.ecmp.flow.dao.impl;

import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.*;
import com.ecmp.flow.dao.CustomFlowHistoryDao;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public PageResult<FlowHistory> findByPageByBusinessModelId(String businessModelId, String executorId, Search searchConfig,Boolean hideNode) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        List<SearchFilter> searchFilters = searchConfig.getFilters();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and ft.flowInstance.id in  ( select ins.id from  com.ecmp.flow.entity.FlowInstance ins where  ins.flowDefVersion.id in  (select    ve.id from com.ecmp.flow.entity.FlowDefVersion ve  where  ve.flowDefination.id in ( select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in  (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = :businessModelId  ) ) ) ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and ft.flowInstance.id in  ( select ins.id from  com.ecmp.flow.entity.FlowInstance ins where  ins.flowDefVersion.id in  (select    ve.id from com.ecmp.flow.entity.FlowDefVersion ve  where  ve.flowDefination.id in ( select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in  (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = :businessModelId  ) ) ) ) ";
        if(hideNode){
            hqlCount  += "and (ft.executorId != ft.flowInstance.creatorId or ft.depict not like '%【自动执行】%' )  ";
            hqlQuery  += "and (ft.executorId != ft.flowInstance.creatorId or ft.depict not like '%【自动执行】%' )  ";
        }

        if (!CollectionUtils.isEmpty(searchFilters)) {
            SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
            for(SearchFilter filters :  searchFilters){
                if("flowExecuteStatus".equals(filters.getFieldName())){
                    if (filters.getValue()!=null && "valid".equals(filters.getValue())) {//有效数据(以前没这个字段，都作为有效数据)
                        hqlCount += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject','haveRead')  or  ft.flowExecuteStatus is null  )";
                        hqlQuery += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject','haveRead')  or  ft.flowExecuteStatus is null  ) ";
                    } else if (filters.getValue()!=null && "record".equals(filters.getValue())) { //记录数据
                        hqlCount += " and ft.flowExecuteStatus in ('end','auto') ";
                        hqlQuery += " and ft.flowExecuteStatus in ('end','auto') ";
                    }
                }else if("startDate".equals(filters.getFieldName())){
                    if(SearchFilter.Operator.GE.equals(filters.getOperator()) && filters.getValue()!=null){ //开始日期
                        hqlCount+=" and ft.actEndTime >=  '"+sim.format(filters.getValue())+"' ";
                        hqlQuery+=" and ft.actEndTime >=  '"+sim.format(filters.getValue())+"' ";
                    }
                }else if("endDate".equals(filters.getFieldName())){
                    if(SearchFilter.Operator.LE.equals(filters.getOperator()) && filters.getValue()!=null){ //结束日期
                        //因为截取日期算的是0点，所以结束日志操作加一天
                        Date endDate = (Date)filters.getValue();
                        Calendar calendar   = new GregorianCalendar();
                        calendar.setTime(endDate);
                        calendar.add(calendar.DATE,1);
                        hqlCount+=" and ft.actEndTime <  '"+sim.format(calendar.getTime())+"' ";
                        hqlQuery+=" and ft.actEndTime <  '"+sim.format(calendar.getTime())+"' ";
                    }
                }else if("taskStatus".equals(filters.getFieldName())){
                    if (filters.getValue() != null && "VIRTUAL".equals(filters.getValue())) { //是否虚拟已办
                        hqlCount += " and ft.taskStatus =  '" + filters.getValue() + "' ";
                        hqlQuery += " and ft.taskStatus =  '" + filters.getValue() + "' ";
                    }else if(filters.getValue() != null && "NOVIRTUAL".equals(filters.getValue())){
                        hqlCount += " and ft.taskStatus !=  '" + filters.getValue() + "' ";
                        hqlQuery += " and ft.taskStatus !=  '" + filters.getValue() + "' ";
                    }
                }else{
                    if(filters.getValue()!=null){
                        hqlCount += "  and ft."+filters.getFieldName()+"  like  '%" + filters.getValue() + "%' ";
                        hqlQuery += "  and ft."+filters.getFieldName()+"  like  '%" + filters.getValue() + "%' ";
                    }
                }
            }
        }

        if (StringUtils.isNotEmpty(quickSearchValue) &&  !CollectionUtils.isEmpty(quickSearchProperties)) {
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
        if (!CollectionUtils.isEmpty(sortOrders)) {
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

        if (!CollectionUtils.isEmpty(searchFilters) && searchFilters.get(0).getValue() != null) {
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

        if (StringUtils.isNotEmpty(quickSearchValue) && !CollectionUtils.isEmpty(quickSearchProperties)) {
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
        if (!CollectionUtils.isEmpty(sortOrders)) {
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


    public PageResult<FlowHistory> findByPage(String executorId, Search searchConfig, Boolean hideNode) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        List<SearchFilter> searchFilters = searchConfig.getFilters();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId ";
        if(hideNode){
             hqlCount  += "and (ft.executorId != ft.flowInstance.creatorId or ft.depict not like '%【自动执行】%' )  ";
             hqlQuery  += "and (ft.executorId != ft.flowInstance.creatorId or ft.depict not like '%【自动执行】%' )  ";
        }


        if (!CollectionUtils.isEmpty(searchFilters)) {
            for(SearchFilter filters :  searchFilters){
                if("flowExecuteStatus".equals(filters.getFieldName())){
                    if (filters.getValue()!=null && "valid".equals(filters.getValue())) {//有效数据(以前没这个字段，都作为有效数据)
                        hqlCount += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject','haveRead')  or  ft.flowExecuteStatus is null  )";
                        hqlQuery += " and ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject','haveRead')  or  ft.flowExecuteStatus is null  ) ";
                    } else if (filters.getValue()!=null && "record".equals(filters.getValue())) { //记录数据
                        hqlCount += " and ft.flowExecuteStatus in ('end','auto') ";
                        hqlQuery += " and ft.flowExecuteStatus in ('end','auto') ";
                    }
                }else if("startDate".equals(filters.getFieldName())){
                    SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
                    if(SearchFilter.Operator.GE.equals(filters.getOperator()) && filters.getValue()!=null){ //开始日期
                        hqlCount+=" and ft.actEndTime >=  '"+sim.format(filters.getValue())+"' ";
                        hqlQuery+=" and ft.actEndTime >=  '"+sim.format(filters.getValue())+"' ";
                    }
                }else if("endDate".equals(filters.getFieldName())){
                    SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
                    if(SearchFilter.Operator.LE.equals(filters.getOperator()) && filters.getValue()!=null){ //结束日期
                        //因为截取日期算的是0点，所以结束日志操作加一天
                        Date endDate = (Date)filters.getValue();
                        Calendar calendar   = new GregorianCalendar();
                        calendar.setTime(endDate);
                        calendar.add(calendar.DATE,1);
                        hqlCount+=" and ft.actEndTime <  '"+sim.format(calendar.getTime())+"' ";
                        hqlQuery+=" and ft.actEndTime <  '"+sim.format(calendar.getTime())+"' ";
                    }
                }else if("taskStatus".equals(filters.getFieldName())){
                    if (filters.getValue() != null && "VIRTUAL".equals(filters.getValue())) { //是否虚拟已办
                        hqlCount += " and ft.taskStatus =  '" + filters.getValue() + "' ";
                        hqlQuery += " and ft.taskStatus =  '" + filters.getValue() + "' ";
                    }else if(filters.getValue() != null && "NOVIRTUAL".equals(filters.getValue())){
                        hqlCount += " and ft.taskStatus !=  '" + filters.getValue() + "' ";
                        hqlQuery += " and ft.taskStatus !=  '" + filters.getValue() + "' ";
                    }
                }else{
                    if(filters.getValue()!=null){
                        hqlCount += "  and ft."+filters.getFieldName()+"  like  '%" + filters.getValue() + "%' ";
                        hqlQuery += "  and ft."+filters.getFieldName()+"  like  '%" + filters.getValue() + "%' ";
                    }
                }
            }
        }


        if (StringUtils.isNotEmpty(quickSearchValue) && !CollectionUtils.isEmpty(quickSearchProperties)) {
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
        if (!CollectionUtils.isEmpty(sortOrders)) {
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
