package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IDefaultBusinessModel2Service;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.dao.DefaultBusinessModel2Dao;
import com.ecmp.flow.dao.DefaultBusinessModelDao;
import com.ecmp.flow.entity.DefaultBusinessModel2;
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
public class DefaultBusinessModel2Service extends BaseEntityService<DefaultBusinessModel2> implements IDefaultBusinessModel2Service {

    @Autowired
    private DefaultBusinessModel2Dao defaultBusinessModel2Dao;

    protected BaseEntityDao<DefaultBusinessModel2> getDao(){
        return this.defaultBusinessModel2Dao;
    }


}
