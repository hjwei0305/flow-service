package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowServiceUrlService;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowServiceUrl;
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
public class FlowServiceUrlService extends BaseEntityService<FlowServiceUrl> implements IFlowServiceUrlService {

    @Autowired
    private FlowServiceUrlDao flowServiceUrlDao;

    protected BaseEntityDao<FlowServiceUrl> getDao(){
        return this.flowServiceUrlDao;
    }


    @Override
    public List<FlowServiceUrl> findByFlowTypeId(String flowTypeId){
        return flowServiceUrlDao.findByFlowTypeId(flowTypeId);
    }
}
