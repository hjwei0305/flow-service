package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.entity.FlowHistory;
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
public class FlowHistoryService extends BaseService<FlowHistory, String>{

    @Autowired
    private FlowHistoryDao flowHistoryDao;
}
