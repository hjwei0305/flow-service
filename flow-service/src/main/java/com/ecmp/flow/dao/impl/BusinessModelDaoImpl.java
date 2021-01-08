package com.ecmp.flow.dao.impl;

import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.flow.dao.CustomAppModuleDao;
import com.ecmp.flow.dao.CustomBusinessModelDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/9 20:26      马超(Vision.Mac)                新建
 * <br>
 * *************************************************************************************************
 */
public class BusinessModelDaoImpl extends BaseEntityDaoImpl<BusinessModel> implements CustomBusinessModelDao {
    public BusinessModelDaoImpl(EntityManager entityManager) {
        super(BusinessModel.class, entityManager);
    }
    public List<BusinessModel> findByAppModuleCodes(List<String> codeList){
        TypedQuery<BusinessModel> businessModelQuery = entityManager.createQuery("select ft from com.ecmp.flow.entity.BusinessModel ft where ft.appModule.code in :codeList  order by ft.lastEditedDate desc", BusinessModel.class);
        businessModelQuery.setParameter("codeList",codeList);
        List<BusinessModel> businessModelList = businessModelQuery.getResultList();
        return businessModelList;
    }
}
