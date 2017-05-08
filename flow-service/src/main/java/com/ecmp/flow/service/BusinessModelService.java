package com.ecmp.flow.service;

import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.dao.BusinessModelDao;
import com.ecmp.flow.entity.BusinessModel;
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
public class BusinessModelService extends BaseService<BusinessModel, String> implements IBusinessModelService{

    @Autowired
    private BusinessModelDao businessModelDao;


    @Override
    public List<BusinessModel> findByAppModuleId(String appModuleId) {
        return businessModelDao.findByAppModuleId(appModuleId);
    }

}
