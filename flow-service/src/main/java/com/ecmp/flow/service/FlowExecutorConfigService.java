package com.ecmp.flow.service;

import com.ecmp.config.util.NumberGenerator;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.Validation;
import com.ecmp.flow.api.IFlowExecutorConfigService;
import com.ecmp.flow.dao.FlowExecutorConfigDao;
import com.ecmp.flow.entity.FlowExecutorConfig;
import com.ecmp.vo.OperateResultWithData;
import org.apache.commons.lang3.StringUtils;
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
public class FlowExecutorConfigService extends BaseEntityService<FlowExecutorConfig> implements IFlowExecutorConfigService {

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    protected BaseEntityDao<FlowExecutorConfig> getDao(){
        return this.flowExecutorConfigDao;
    }
}
