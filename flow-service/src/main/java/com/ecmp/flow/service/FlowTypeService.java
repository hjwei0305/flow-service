package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowTypeService;
import com.ecmp.flow.dao.FlowTypeDao;
import com.ecmp.flow.entity.FlowType;
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
public class FlowTypeService extends BaseEntityService<FlowType> implements IFlowTypeService {

    @Autowired
    private FlowTypeDao flowTypeDao;

    protected BaseEntityDao<FlowType> getDao(){
        return this.flowTypeDao;
    }

    @Override
    public List<FlowType> findByBusinessModelId(String businessModelId) {
        return flowTypeDao.findByBusinessModelId(businessModelId);
    }
}
