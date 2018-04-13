package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowHistoryService;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.entity.FlowHistory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    protected BaseEntityDao<FlowHistory> getDao(){
        return this.flowHistoryDao;
    }

    @Override
    public List<FlowHistory> findByInstanceId(String instanceId) {
        return flowHistoryDao.findByInstanceId(instanceId);
    }

    @Override
    public PageResult<FlowHistory> findByPageAndUser(Search searchConfig){
        String userId = ContextUtil.getUserId();
        return flowHistoryDao.findByPageByBusinessModelId(userId,searchConfig);
    }

    @Override
   public PageResult<FlowHistory> findByPage(Search searchConfig){
        PageResult<FlowHistory> result = super.findByPage(searchConfig);
        if(result!=null){
            List<FlowHistory>  flowHistoryList = result.getRows();
            this.initUrl(flowHistoryList);
        }
        return result;
    }
    private List<FlowHistory> initUrl(List<FlowHistory>  result ){
        if(result!=null && !result.isEmpty()){
            for(FlowHistory flowHistory:result){
                String apiBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddressConfig();
                String apiBaseAddress =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                flowHistory.setApiBaseAddressAbsolute(apiBaseAddress);
                apiBaseAddress =  apiBaseAddress.substring(apiBaseAddress.lastIndexOf(":"));
                apiBaseAddress=apiBaseAddress.substring(apiBaseAddress.indexOf("/"));
                String webBaseAddressConfig = flowHistory.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddressConfig();
                String webBaseAddress =  ContextUtil.getGlobalProperty(webBaseAddressConfig);
                flowHistory.setWebBaseAddressAbsolute(webBaseAddress);
                webBaseAddress =  webBaseAddress.substring(webBaseAddress.lastIndexOf(":"));
                webBaseAddress = webBaseAddress.substring(webBaseAddress.indexOf("/"));
                flowHistory.setApiBaseAddress(apiBaseAddress);
                flowHistory.setWebBaseAddress(webBaseAddress);
            }
        }
        return result;
    }

    public PageResult<FlowHistory> findByBusinessModelId(String businessModelId, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        if(StringUtils.isNotEmpty(businessModelId)){
            return flowHistoryDao.findByPageByBusinessModelId(businessModelId, userId, searchConfig);
        }else{
            return flowHistoryDao.findByPage(userId, searchConfig);
        }
    }
}
