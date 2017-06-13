package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IDefaultBusinessModel3Service;
import com.ecmp.flow.dao.DefaultBusinessModel3Dao;
import com.ecmp.flow.entity.DefaultBusinessModel3;
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
public class DefaultBusinessModel3Service extends BaseEntityService<DefaultBusinessModel3> implements IDefaultBusinessModel3Service {

    @Autowired
    private DefaultBusinessModel3Dao defaultBusinessModel3Dao;

    protected BaseEntityDao<DefaultBusinessModel3> getDao(){
        return this.defaultBusinessModel3Dao;
    }
}
