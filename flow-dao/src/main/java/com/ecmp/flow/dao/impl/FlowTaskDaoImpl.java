package com.ecmp.flow.dao.impl;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.AppModuleDao;
import com.ecmp.flow.dao.CustomFlowTaskDao;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.WorkPageUrl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    String hqlQueryOrder = "   order by ft.priority desc,ft.createdDate asc ";


    public PageResult<FlowTask> findByPageByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        if (Objects.isNull(pageInfo)) {
            pageInfo = new PageInfo();
        }
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();

        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where  (ft.trustState !=1  or ft.trustState is null )";
        String hqlQuery = "select ft          from com.ecmp.flow.entity.FlowTask ft where  (ft.trustState !=1  or ft.trustState is null )";

        if (StringUtils.isNotEmpty(flowTypeId) && !"".equals(flowTypeId)) {
            hqlCount += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.id  = '" + flowTypeId + "' ))";
            hqlQuery += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.id  = '" + flowTypeId + "' ))";
        } else if (StringUtils.isNotEmpty(businessModelId) && !"".equals(businessModelId)) {
            hqlCount += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = '" + businessModelId + "' ) )";
            hqlQuery += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = '" + businessModelId + "' ) )";
        } else if (StringUtils.isNotEmpty(appModuleId) && !"".equals(appModuleId)) {
            hqlCount += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.id='" + appModuleId + "'   )) )";
            hqlQuery += " and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.id='" + appModuleId + "'   )) )";
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
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += " order by ft.createdDate desc";
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     *
     * @param taskId 待办任务Id
     * @return 待办任务
     */
    @Override
    public FlowTask findTaskById(String taskId) {
        FlowTask flowTask = findOne(taskId);
        if (Objects.nonNull(flowTask)) {
            initFlowTask(flowTask);
        }
        return flowTask;
    }

    public PageResult<FlowTask> findByPageByBusinessModelId(String businessModelId, String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  = :executorId " +
                "and (ft.trustState !=1  or ft.trustState is null ) " +
                "and ft.flowDefinitionId " +
                "in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                "in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                "in(select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  = :executorId " +
                "and (ft.trustState !=1  or ft.trustState is null ) " +
                "and ft.flowDefinitionId " +
                "in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                "in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                "in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
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
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }



    public PageResult<FlowTask> findByPageByBusinessModelIdOfPower(String businessModelId, List<String> executorIdList, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  in (:executorIdList) " +
                "and (ft.trustState !=1  or ft.trustState is null ) " +
                "and ft.flowDefinitionId " +
                "in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                "in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                "in(select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  in (:executorIdList) " +
                "and (ft.trustState !=1  or ft.trustState is null ) " +
                "and ft.flowDefinitionId " +
                "in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                "in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                "in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
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
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorIdList", executorIdList);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorIdList", executorIdList);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    /**
     * 完成待办任务的URL设置
     *
     * @param flowTasks 待办任务清单
     * @return 待办任务
     */
    @Override
    public void initFlowTasks(List<FlowTask> flowTasks) {
        if (CollectionUtils.isEmpty(flowTasks)) {
            return;
        }
        flowTasks.forEach(this::initFlowTask);
    }

    /**
     * 完成待办任务的URL设置
     *
     * @param flowTask 待办任务
     * @return 待办任务
     */
    private void initFlowTask(FlowTask flowTask) {
        String apiBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
        String apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
        if (StringUtils.isNotEmpty(apiBaseAddress)) {
            flowTask.setApiBaseAddressAbsolute(apiBaseAddress);
            String[] tempApiBaseAddress = apiBaseAddress.split("/");
            if (tempApiBaseAddress != null && tempApiBaseAddress.length > 0) {
                apiBaseAddress = tempApiBaseAddress[tempApiBaseAddress.length - 1];
                flowTask.setApiBaseAddress("/" + apiBaseAddress + "/");
            }
        }
        String webBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
        String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);

        if (StringUtils.isNotEmpty(webBaseAddress)) {
            flowTask.setWebBaseAddressAbsolute(webBaseAddress);
            flowTask.setLookWebBaseAddressAbsolute(webBaseAddress);
            String[] tempWebBaseAddress = webBaseAddress.split("/");
            if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                flowTask.setWebBaseAddress("/" + webBaseAddress + "/");
                flowTask.setLookWebBaseAddress("/" + webBaseAddress + "/");
            }
        }
        WorkPageUrl workPageUrl = flowTask.getWorkPageUrl();
        String completeTaskServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getCompleteTaskServiceUrl();
        String businessDetailServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
        if (StringUtils.isEmpty(completeTaskServiceUrl)) {
            completeTaskServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getCompleteTaskServiceUrl();
        }
        if (StringUtils.isEmpty(businessDetailServiceUrl)) {
            businessDetailServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
        }
        flowTask.setCompleteTaskServiceUrl(completeTaskServiceUrl);
        flowTask.setBusinessDetailServiceUrl(businessDetailServiceUrl);
        if (workPageUrl != null) {
            flowTask.setTaskFormUrl(PageUrlUtil.buildUrl(Constants.getConfigValueByWeb(webBaseAddressConfig), workPageUrl.getUrl()));
            String taskFormUrlXiangDui = "/" + webBaseAddress + "/" + workPageUrl.getUrl();
            taskFormUrlXiangDui = taskFormUrlXiangDui.replaceAll("\\//", "/");
            flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
            String appModuleId = workPageUrl.getAppModuleId();
            AppModule appModule = appModuleDao.findOne(appModuleId);
            if (appModule != null && !appModule.getId().equals(flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getId())) {
                webBaseAddressConfig = appModule.getWebBaseAddress();
                webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                flowTask.setTaskFormUrl(PageUrlUtil.buildUrl(webBaseAddress, workPageUrl.getUrl()));
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    flowTask.setWebBaseAddressAbsolute(webBaseAddress);
                    String[] tempWebBaseAddress = webBaseAddress.split("/");
                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                        flowTask.setWebBaseAddress("/" + webBaseAddress + "/");
                    }
                }
                taskFormUrlXiangDui = "/" + webBaseAddress + "/" + workPageUrl.getUrl();
                taskFormUrlXiangDui = taskFormUrlXiangDui.replaceAll("\\//", "/");
                flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
            }
        }
    }

    public PageResult<FlowTask> findByPage(String executorId, String appSign, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  = :executorId " +
                "and (ft.trustState !=1  or ft.trustState is null) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  = :executorId " +
                "and (ft.trustState !=1  or ft.trustState is null) ";
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
        // 限制应用标识
        if (!StringUtils.isBlank(appSign)) {
            String appSignSql = "and ft.flowDefinitionId " +
                    "in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                    "in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                    "in(select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.code like :appSign)))";
            hqlCount += appSignSql;
            hqlQuery += appSignSql;
        }

        if (sortOrders != null && sortOrders.size() > 0) {
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    public PageResult<FlowTask> findByPageOfPower(List<String> executorIdList, String appSign, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId  in (:executorIdList) " +
                "and (ft.trustState !=1  or ft.trustState is null) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft " +
                "where ft.executorId   in (:executorIdList) " +
                "and (ft.trustState !=1  or ft.trustState is null) ";
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
        // 限制应用标识
        if (!StringUtils.isBlank(appSign)) {
            String appSignSql = "and ft.flowDefinitionId " +
                    "in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                    "in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                    "in(select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.code like :appSign)))";
            hqlCount += appSignSql;
            hqlQuery += appSignSql;
        }

        if (sortOrders != null && sortOrders.size() > 0) {
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorIdList", executorIdList);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorIdList", executorIdList);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    public PageResult<FlowTask> findByPageCanBatchApproval(String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true ";
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
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public PageResult<FlowTask> findByPageCanBatchApprovalOfPower(List<String> executorIdList, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  in (:executorIdList) and ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )  ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  in (:executorIdList) and ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )  ";
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
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorIdList", executorIdList);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorIdList", executorIdList);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public Long findCountByExecutorId(String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId  and  (ft.trustState !=1  or ft.trustState is null ) ";
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer("and (");
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
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        Long total = queryTotal.getSingleResult();
        return total;
    }

    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(String businessModelId, String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )";
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
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelIdOfPower(String businessModelId, List<String> executorIdList, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  in (:executorIdList) and ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )    and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  in (:executorIdList) and ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )  and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )";
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
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorIdList", executorIdList);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorIdList", executorIdList);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        initFlowTasks(result);
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }
}
