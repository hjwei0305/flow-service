package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IWorkPageUrlService;
import com.ecmp.flow.dao.WorkPageUrlDao;
import com.ecmp.flow.entity.WorkPageUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class WorkPageUrlService extends BaseEntityService<WorkPageUrl> implements IWorkPageUrlService {

    @Autowired
    private WorkPageUrlDao workPageUrlDao;

    protected BaseEntityDao<WorkPageUrl> getDao(){
        return this.workPageUrlDao;
    }


    @Override
    public List<WorkPageUrl> findByAppModuleId(String appModuleId) {
        return workPageUrlDao.findByAppModuleId(appModuleId);
    }

    @Override
    public List<WorkPageUrl> findSelectEdByAppModuleId(String appModuleId,String businessModelId) {
        return workPageUrlDao.findSelectEd(appModuleId, businessModelId);
    }

    @Override
    public List<WorkPageUrl> findNotSelectEdByAppModuleId(String appModuleId,String businessModelId) {
        return workPageUrlDao.findNotSelectEd(appModuleId, businessModelId);
    }

    @Override
    public List<WorkPageUrl> findByFlowTypeId(String flowTypeId){
        return workPageUrlDao.findByFlowTypeId(flowTypeId);
    }


    public List<WorkPageUrl> findSelectEdByBusinessModelId(String businessModelId){
     return    workPageUrlDao.findSelectEdByBusinessModelId(businessModelId);
    }
}
