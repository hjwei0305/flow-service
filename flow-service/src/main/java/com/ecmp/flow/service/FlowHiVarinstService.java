package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowHiVarinstService;
import com.ecmp.flow.dao.FlowHiVarinstDao;
import com.ecmp.flow.entity.FlowHiVarinst;
import com.ecmp.flow.entity.FlowHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：工作界面配置管理
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowHiVarinstService extends BaseEntityService<FlowHiVarinst> implements IFlowHiVarinstService {

    @Autowired
    private FlowHiVarinstDao flowHiVarinstDao;

    protected BaseEntityDao<FlowHiVarinst> getDao(){
        return this.flowHiVarinstDao;
    }
}
