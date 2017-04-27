package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowVariableService;
import com.ecmp.flow.dao.FlowVariableDao;
import com.ecmp.flow.entity.FlowVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：参数管理
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/04/25 13:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowVariableService extends BaseService<FlowVariable, String> implements IFlowVariableService {

    @Autowired
    private FlowVariableDao flowVariableDao;
}
