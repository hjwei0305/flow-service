package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowExecutorConfigService;
import com.ecmp.flow.api.IFlowServiceUrlService;
import com.ecmp.flow.dao.BusinessModelDao;
import com.ecmp.flow.dao.FlowExecutorConfigDao;
import com.ecmp.flow.entity.FlowExecutorConfig;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程自定义执行人配置API接口实现
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/7/5 9:50      陈爽(chenshuang)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowExecutorConfigService extends BaseEntityService<FlowExecutorConfig> implements IFlowExecutorConfigService {

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    @Override
    protected BaseEntityDao<FlowExecutorConfig> getDao() {
        return this.flowExecutorConfigDao;
    }


    @Override
    public OperateResultWithData<FlowExecutorConfig> saveValidateCode(FlowExecutorConfig flowExecutorConfig){
        FlowExecutorConfig  bean =  flowExecutorConfigDao.findByProperty("code",flowExecutorConfig.getCode());
        if(bean!=null&&!bean.getId().equals(flowExecutorConfig.getId())){
            return  OperateResultWithData.operationFailure("操作失败：代码已存在！");
        }
        flowExecutorConfigDao.save(flowExecutorConfig);
        return OperateResultWithData.operationSuccessWithData(flowExecutorConfig);
    }


}
