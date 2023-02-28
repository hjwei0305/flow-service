package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.*;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowHistoryService;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.BusinessModelDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.dto.UserFlowHistoryQueryParam;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.FlowTaskTool;
import com.ecmp.flow.vo.TodoBusinessSummaryVO;
import com.ecmp.flow.vo.phone.FlowHistoryPhoneVo;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowHistoryService extends BaseEntityService<FlowHistory> implements IFlowHistoryService {

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private FlowTaskTool flowTaskTool;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private BusinessModelDao businessModelDao;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowInstanceService flowInstanceService;


    protected BaseEntityDao<FlowHistory> getDao() {
        return this.flowHistoryDao;
    }


    /**
     * 获取流程实例历史节点集合
     * @param id
     */
    public Map<String, String> historyNodeIds(String id) {
        Map<String, String> nodeIds = new HashMap<>();
        List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(id);
        if (!CollectionUtils.isEmpty(flowHistoryList)) {
            for (FlowHistory flowHistory : flowHistoryList) {
                nodeIds.put(flowHistory.getActTaskDefKey(), "");
            }
        }
        return nodeIds;
    }


    @Override
    public List<FlowHistory> findByInstanceId(String instanceId) {
        return flowHistoryDao.findByInstanceId(instanceId);
    }

    @Override
    public PageResult<FlowHistory> findByPageAndUser(Search searchConfig) {
        String userId = ContextUtil.getUserId();
        PageResult<FlowHistory> pageResult = flowHistoryDao.findByPageByBusinessModelId(userId, searchConfig);
        List<FlowHistory> result = pageResult.getRows();
        initFlowTaskAppModule(result);
        return pageResult;
    }

    @Override
    public PageResult<FlowHistory> findByPage(Search searchConfig) {
        PageResult<FlowHistory> result = super.findByPage(searchConfig);
        if (result != null) {
            List<FlowHistory> flowHistoryList = result.getRows();
            this.initUrl(flowHistoryList);
        }
        return result;
    }

    public List<FlowHistory> findByAllTaskMakeOverPowerHistory() {
        List<FlowHistory> flowHistoryList = flowHistoryDao.findByAllTaskMakeOverPowerHistory();
        initFlowTaskAppModule(flowHistoryList);
        return flowHistoryList;
    }

    private List<FlowHistory> initUrl(List<FlowHistory> result) {
        if (!CollectionUtils.isEmpty(result)) {
            for (FlowHistory flowHistory : result) {
                String apiBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                String apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
                flowHistory.setApiBaseAddressAbsolute(apiBaseAddress);
                apiBaseAddress = apiBaseAddress.substring(apiBaseAddress.lastIndexOf(":"));
                apiBaseAddress = apiBaseAddress.substring(apiBaseAddress.indexOf("/"));
                String webBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                flowHistory.setWebBaseAddressAbsolute(webBaseAddress);
                webBaseAddress = webBaseAddress.substring(webBaseAddress.lastIndexOf(":"));
                webBaseAddress = webBaseAddress.substring(webBaseAddress.indexOf("/"));
                flowHistory.setApiBaseAddress(apiBaseAddress);
                flowHistory.setWebBaseAddress(webBaseAddress);
            }
        }
        return result;
    }


    /**
     * 查询当前用户已办单据汇总信息
     *
     * @param appSign 应用标识
     * @return 汇总信息
     */
    public List<TodoBusinessSummaryVO> findHisTorySumHeader(String appSign, String dataType) {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();
        List groupResultList;
        if (StringUtils.isNotEmpty(dataType) && !"all".equals(dataType)) {
            if ("record".equals(dataType)) {//记录数据
                groupResultList = flowHistoryDao.findHisByExecutorIdGroupRecord(userID);
            } else { //有效数据
                groupResultList = flowHistoryDao.findHisByExecutorIdGroupValid(userID);
            }
        } else {
            groupResultList = flowHistoryDao.findHisByExecutorIdGroup(userID);
        }

        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(groupResultList)) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                if (flowDefination == null) {
                    continue;
                }

                // 获取业务类型
                BusinessModel businessModel = businessModelDao.findOne(flowDefination.getFlowType().getBusinessModel().getId());
                // 限制应用标识
                boolean canAdd = true;
                if (!StringUtils.isBlank(appSign)) {
                    // 判断应用模块代码是否以应用标识开头,不是就不添加
                    if (!businessModel.getAppModule().getCode().startsWith(appSign)) {
                        canAdd = false;
                    }
                }
                if (canAdd) {
                    Integer oldCount = businessModelCountMap.get(businessModel);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    businessModelCountMap.put(businessModel, oldCount + count);
                }
            }
        }
        if (!CollectionUtils.isEmpty(businessModelCountMap)) {
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName() + "(" + map.getValue() + ")");
                voList.add(todoBusinessSummaryVO);
            }
        }
        return voList;
    }

    public List<TodoBusinessSummaryVO> findHisToryByFlowStatusSumHeader(String appSign, String orderType) {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();
        List groupResultList = null;
        if (StringUtils.isNotEmpty(orderType) && !"all".equals(orderType)) {
            if ("inFlow".equals(orderType)) {//流程中
                groupResultList = flowHistoryDao.findHisByExecutorIdAndFlowStatusGroup(userID, false, false);
            } else if ("ended".equals(orderType)) {//正常结束
                groupResultList = flowHistoryDao.findHisByExecutorIdAndFlowStatusGroup(userID, true, false);
            } else if ("abnormalEnd".equals(orderType)) {  //流程终止
                groupResultList = flowHistoryDao.findHisByExecutorIdAndFlowStatusGroup(userID, true, true);
            }
        } else {
            groupResultList = flowHistoryDao.findHisByExecutorIdGroup(userID);
        }

        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(groupResultList)) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                if (flowDefination == null) {
                    continue;
                }

                // 获取业务类型
                BusinessModel businessModel = businessModelDao.findOne(flowDefination.getFlowType().getBusinessModel().getId());
                // 限制应用标识
                boolean canAdd = true;
                if (!StringUtils.isBlank(appSign)) {
                    // 判断应用模块代码是否以应用标识开头,不是就不添加
                    if (!businessModel.getAppModule().getCode().startsWith(appSign)) {
                        canAdd = false;
                    }
                }
                if (canAdd) {
                    Integer oldCount = businessModelCountMap.get(businessModel);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    businessModelCountMap.put(businessModel, oldCount + count);
                }
            }
        }
        if (!CollectionUtils.isEmpty(businessModelCountMap)) {
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName() + "(" + map.getValue() + ")");
                voList.add(todoBusinessSummaryVO);
            }
        }
        return voList;
    }


    @Override
    public ResponseData listFlowHistoryHeader(String dataType) {
        try {
            List<TodoBusinessSummaryVO> list = this.findHisTorySumHeader("", dataType);
            return ResponseData.operationSuccessWithData(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return ResponseData.operationFailure("10118");
        }
    }

    @Override
    public ResponseData listValidFlowHistoryHeader() {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();

        List groupResultList = flowHistoryDao.findHisByExecutorIdGroupValid(userID);
        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(groupResultList)) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                if (flowDefination == null) {
                    continue;
                }
                // 获取业务类型
                BusinessModel businessModel = businessModelDao.findOne(flowDefination.getFlowType().getBusinessModel().getId());
                Integer oldCount = businessModelCountMap.get(businessModel);
                if (oldCount == null) {
                    oldCount = 0;
                }
                businessModelCountMap.put(businessModel, oldCount + count);
            }
        }
        if (!CollectionUtils.isEmpty(businessModelCountMap)) {
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName());
                voList.add(todoBusinessSummaryVO);
            }
        }
        return ResponseData.operationSuccessWithData(voList);
    }

    @Override
    public ResponseData listFlowHistoryByFlowStatusHeader(String orderType) {
        try {
            List<TodoBusinessSummaryVO> list = this.findHisToryByFlowStatusSumHeader("", orderType);
            return ResponseData.operationSuccessWithData(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return ResponseData.operationFailure("10118");
        }
    }

    @Override
    public ResponseData listFlowHistory(String businessModelId, Search searchConfig) {
        try {
            PageResult<FlowHistory> pageList = this.findByBusinessModelId(businessModelId, searchConfig);
            return ResponseData.operationSuccessWithData(pageList);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return ResponseData.operationFailure(e.getMessage());
        }
    }

    @Override
    public ResponseData listValidFlowHistory(UserFlowHistoryQueryParam queryParam) {
        //设置只查询有效数据
        List<SearchFilter> searchFilters = queryParam.getFilters();
        if (searchFilters == null) {
            searchFilters = new ArrayList<>();
        }
        SearchFilter filter = new SearchFilter("flowExecuteStatus", "valid", SearchFilter.Operator.IN);
        searchFilters.add(filter);
        queryParam.setFilters(searchFilters);
        return this.listFlowHistory(queryParam.getModelId(), queryParam);
    }

    public ResponseData listFlowHistoryByFlowStatus(String businessModelId, Search searchConfig) {
        try {
            PageResult<FlowHistory> pageList = this.findByBusinessModelIdAndFlowStatus(businessModelId, searchConfig);
            return ResponseData.operationSuccessWithData(pageList);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return ResponseData.operationFailure(e.getMessage());
        }
    }


    @Override
    public ResponseData listFlowHistoryAndExecutor(String businessModelId, Search searchConfig) {
        ResponseData responseData = this.listFlowHistory(businessModelId, searchConfig);
        if (responseData.getSuccess()) {
            PageResult<FlowHistory> pageHistory = (PageResult<FlowHistory>) responseData.getData();
            List<FlowHistory> list = pageHistory.getRows();
            list.forEach(history -> {
                if (!history.getFlowInstance().isEnded()) {   //流程未结束
                    history.setTaskExecutors(flowInstanceService.getExecutorStringByInstanceId(history.getFlowInstance().getId()));
                }
            });
        }
        return responseData;
    }

    @Override
    public ResponseData listFlowHistoryByFlowStatusAndExecutor(String businessModelId, Search searchConfig) {
        ResponseData responseData = this.listFlowHistoryByFlowStatus(businessModelId, searchConfig);
        if (responseData.getSuccess()) {
            PageResult<FlowHistory> pageHistory = (PageResult<FlowHistory>) responseData.getData();
            List<FlowHistory> list = pageHistory.getRows();
            list.forEach(history -> {
                if (!history.getFlowInstance().isEnded()) {   //流程未结束
                    history.setTaskExecutors(flowInstanceService.getExecutorStringByInstanceId(history.getFlowInstance().getId()));
                }
            });
        }
        return responseData;
    }


    /**
     * 动态设置撤回按钮是否显示（集合）
     */
    public void dynamicallySetTheRecallButton(List<FlowHistory> historyList) {
        if (CollectionUtils.isEmpty(historyList)) {
            return;
        }
        historyList.forEach(this::dynamicallySetTheRecallButtonByOne);
    }

    /**
     * 动态设置撤回按钮是否显示（单个）
     */
    public void dynamicallySetTheRecallButtonByOne(FlowHistory bean) {
        if (bean == null) {
            return;
        }
        //前台撤回按钮显示逻辑：canCancel == true && taskStatus == "COMPLETED" && flowInstance.ended == false
        if (BooleanUtils.isTrue(bean.getCanCancel()) && "COMPLETED".equalsIgnoreCase(bean.getTaskStatus()) &&
                bean.getFlowInstance() != null && BooleanUtils.isFalse(bean.getFlowInstance().isEnded())) {
            Boolean boo = flowTaskTool.checkoutTaskRollBack(bean);
            if (!boo) { //不可以显示
                bean.setCanCancel(false);
            }
        } else {
            bean.setCanCancel(false);
        }
    }

    public PageResult<FlowHistory> findByBusinessModelId(String businessModelId, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        PageResult<FlowHistory> result;
        if (StringUtils.isNotEmpty(businessModelId)) {
            result = flowHistoryDao.findByPageByBusinessModelId(businessModelId, userId, searchConfig);
        } else {
            result = flowHistoryDao.findByPage(userId, searchConfig);
        }
        //设置已办的API和WEB基地址
        initFlowTaskAppModule(result.getRows());
        //动态设置撤回按钮是否显示
        this.dynamicallySetTheRecallButton(result.getRows());
        return result;
    }

    public PageResult<FlowHistory> findByBusinessModelIdAndFlowStatus(String businessModelId, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        PageResult<FlowHistory> result = flowHistoryDao.findByPageByBusinessModelIdAndFlowStatus(businessModelId, userId, searchConfig);
        //设置已办的API和WEB基地址
        initFlowTaskAppModule(result.getRows());
        //动态设置撤回按钮是否显示
        this.dynamicallySetTheRecallButton(result.getRows());
        return result;
    }


    public PageResult<FlowHistoryPhoneVo> findByBusinessModelIdOfMobile(String businessModelId, String property, String direction,
                                                                        int page, int rows, String quickValue) {
        Search searchConfig = new Search();
        String userId = ContextUtil.getUserId();
        searchConfig.addFilter(new SearchFilter("executorId", userId, SearchFilter.Operator.EQ));
        //根据业务单据名称、业务单据号、业务工作说明快速查询
        searchConfig.addQuickSearchProperty("flowName");
        searchConfig.addQuickSearchProperty("flowTaskName");
        searchConfig.addQuickSearchProperty("flowInstance.businessCode");
        searchConfig.addQuickSearchProperty("flowInstance.businessModelRemark");
        searchConfig.addQuickSearchProperty("creatorName");
        searchConfig.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        searchConfig.setPageInfo(pageInfo);

        SearchOrder searchOrder;
        if (StringUtils.isNotEmpty(property) && StringUtils.isNotEmpty(direction)) {
            if (SearchOrder.Direction.ASC.equals(direction)) {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.ASC);
            } else {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.DESC);
            }
        } else {
            searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.ASC);
        }
        List<SearchOrder> list = new ArrayList<>();
        list.add(searchOrder);
        searchConfig.setSortOrders(list);

        PageResult<FlowHistory> historyPage = this.findByBusinessModelId(businessModelId, searchConfig);
        PageResult<FlowHistoryPhoneVo> historyVoPage = new PageResult<>();
        if (!CollectionUtils.isEmpty(historyPage.getRows())) {
            List<FlowHistory> historyList = historyPage.getRows();
            List<FlowHistoryPhoneVo> phoneVoList = new ArrayList<>();
            historyList.forEach(bean -> {
                FlowHistoryPhoneVo beanVo = new FlowHistoryPhoneVo();
                FlowInstance flowInstance = bean.getFlowInstance();
                BusinessModel businessModel = bean.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                beanVo.setId(bean.getId());
                beanVo.setFlowName(bean.getFlowName());
                beanVo.setFlowTaskName(bean.getFlowTaskName());
                beanVo.setCreatedDate(bean.getCreatedDate());
                beanVo.setTaskStatus(bean.getTaskStatus());
                beanVo.setCanCancel(bean.getCanCancel());
                beanVo.setFlowInstanceBusinessId(flowInstance.getBusinessId());
                beanVo.setFlowInstanceBusinessCode(flowInstance.getBusinessCode());
                beanVo.setFlowInstanceEnded(flowInstance.isEnded());
                beanVo.setFlowInstanceCreatorName(flowInstance.getCreatorName());
                beanVo.setBusinessModelRemark(flowInstance.getBusinessModelRemark());

                String apiBaseAddress = Constants.getConfigValueByApi(businessModel.getAppModule().getApiBaseAddress());
                beanVo.setBusinessDetailServiceUrl(PageUrlUtil.buildUrl(apiBaseAddress, businessModel.getBusinessDetailServiceUrl()));
                phoneVoList.add(beanVo);
            });
            historyVoPage.setRows(phoneVoList);
        } else {
            historyVoPage.setRows(new ArrayList<>());
        }
        historyVoPage.setPage(historyPage.getPage());
        historyVoPage.setRecords(historyPage.getRecords());
        historyVoPage.setTotal(historyPage.getTotal());
        return historyVoPage;
    }


    public PageResult<FlowHistory> findByBusinessModelIdOfPhone(String businessModelId, String property, String direction,
                                                                int page, int rows, String quickValue) {
        Search searchConfig = new Search();
        String userId = ContextUtil.getUserId();
        searchConfig.addFilter(new SearchFilter("executorId", userId, SearchFilter.Operator.EQ));
        //根据业务单据名称、业务单据号、业务工作说明快速查询
        searchConfig.addQuickSearchProperty("flowName");
        searchConfig.addQuickSearchProperty("flowTaskName");
        searchConfig.addQuickSearchProperty("flowInstance.businessCode");
        searchConfig.addQuickSearchProperty("flowInstance.businessModelRemark");
        searchConfig.addQuickSearchProperty("creatorName");
        searchConfig.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        searchConfig.setPageInfo(pageInfo);

        SearchOrder searchOrder;
        if (StringUtils.isNotEmpty(property) && StringUtils.isNotEmpty(direction)) {
            if (SearchOrder.Direction.ASC.equals(direction)) {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.ASC);
            } else {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.DESC);
            }
        } else {
            searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.ASC);
        }
        List<SearchOrder> list = new ArrayList<>();
        list.add(searchOrder);
        searchConfig.setSortOrders(list);

        return this.findByBusinessModelId(businessModelId, searchConfig);
    }


    public List<FlowHistory> initFlowTaskAppModule(List<FlowHistory> result) {
        if (!CollectionUtils.isEmpty(result)) {
            for (FlowHistory flowHistory : result) {
                String apiBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                String apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
                if (StringUtils.isNotEmpty(apiBaseAddress)) {
                    flowHistory.setApiBaseAddressAbsolute(apiBaseAddress);
                    String[] tempWebApiBaseAddress = apiBaseAddress.split("/");
                    if (tempWebApiBaseAddress != null && tempWebApiBaseAddress.length > 0) {
                        apiBaseAddress = tempWebApiBaseAddress[tempWebApiBaseAddress.length - 1];
                        flowHistory.setApiBaseAddress("/" + apiBaseAddress + "/");
                    }
                }
                String webBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    flowHistory.setWebBaseAddressAbsolute(webBaseAddress);
                    String[] tempWebBaseAddress = webBaseAddress.split("/");
                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                        flowHistory.setWebBaseAddress("/" + webBaseAddress + "/");
                    }
                }
                //已办设置移动端查看单据地址
                String phoneLookUrl = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getPhoneLookUrl();
                if (StringUtils.isEmpty(phoneLookUrl)) {
                    phoneLookUrl = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getPhoneLookUrl();
                }
                flowHistory.setPhoneUrl(StringUtils.isEmpty(phoneLookUrl) ? "NotConfig" : phoneLookUrl);
            }
        }
        return result;
    }


    /**
     * 计算流程历史是否超时（集合）
     *
     * @param list
     */
    public void setHistoryListIfTimeout(List<FlowHistory> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(this::setHistoryIfTimeout);
    }

    /**
     * 计算流程历史是否超时（单个）
     *
     * @param flowHistory
     */
    private void setHistoryIfTimeout(FlowHistory flowHistory) {
        if (flowHistory.getActStartTime() == null || flowHistory.getTiming() == null || flowHistory.getTiming() < 0.000001) {
            if (flowHistory.getTiming() == null) {
                flowHistory.setTiming(0.00);
            }
            flowHistory.setIfTimeout(false);
        } else {
            //实际用时
            Double hour = new BigDecimal((double) (flowHistory.getActEndTime().getTime() - flowHistory.getActStartTime().getTime()) / (60 * 60 * 1000)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            //额定工时
            Double timing = flowHistory.getTiming();
            if (hour > timing) {
                flowHistory.setIfTimeout(true);
            } else {
                flowHistory.setIfTimeout(false);
            }
        }
    }


    @Override
    public ResponseData getLookUrlByOldTaskId(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            return ResponseData.operationFailure("10392");
        }
        try {
            FlowTask flowTask = flowTaskDao.findOne(taskId);
            if (flowTask != null) {
                String lookUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
                String webBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                String lookUrlXiangDui = PageUrlUtil.buildUrl(webBaseAddress, lookUrl) + "?id=" + flowTask.getFlowInstance().getBusinessId();
                return ResponseData.operationSuccessWithData(lookUrlXiangDui);
            } else {
                FlowHistory flowHistory = flowHistoryDao.findByProperty("oldTaskId", taskId);
                if (flowHistory != null) {
                    String lookUrl = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
                    String webBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                    String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                    String lookUrlXiangDui = PageUrlUtil.buildUrl(webBaseAddress, lookUrl) + "?id=" + flowHistory.getFlowInstance().getBusinessId();
                    return ResponseData.operationSuccessWithData(lookUrlXiangDui);
                } else {
                    return ResponseData.operationFailure("10427");
                }
            }
        } catch (Exception e) {
            LogUtil.error("获取单据查看地址出错:{}", e.getMessage(), e);
            return ResponseData.operationFailure("10426", e.getMessage());
        }
    }
}
