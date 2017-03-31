package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.entity.FlowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class FlowInstanceService extends BaseService<FlowInstance, String> implements IFlowInstanceService {

    @Autowired
    private FlowInstanceDao flowInstanceDao;
}
