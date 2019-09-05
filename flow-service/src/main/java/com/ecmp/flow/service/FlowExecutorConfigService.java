package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowExecutorConfigService;
import com.ecmp.flow.dao.FlowExecutorConfigDao;
import com.ecmp.flow.entity.FlowExecutorConfig;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
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

    @Override
    public ResponseData listCombo(String businessModelId) {
        if(StringUtils.isEmpty(businessModelId)){
            return  ResponseData.operationFailure("参数不能为空!");
        }
        List<FlowExecutorConfig>  list = flowExecutorConfigDao.findListByProperty("businessModel.id",businessModelId);
        return ResponseData.operationSuccessWithData(list);
    }
}
