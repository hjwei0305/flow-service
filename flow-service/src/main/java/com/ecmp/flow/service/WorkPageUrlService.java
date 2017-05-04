package com.ecmp.flow.service;

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
public class WorkPageUrlService extends BaseService<WorkPageUrl, String> implements IWorkPageUrlService {

    @Autowired
    private WorkPageUrlDao workPageUrlDao;

    @Override
    public List<WorkPageUrl> findByAppModuleId(String appModuleId) {
        return workPageUrlDao.findByAppModuleId(appModuleId);
    }
}
