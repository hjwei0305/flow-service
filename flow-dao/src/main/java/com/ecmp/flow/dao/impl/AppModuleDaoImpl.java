package com.ecmp.flow.dao.impl;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.dao.CustomAppModuleDao;
import com.ecmp.flow.dao.CustomFlowHistoryDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collection;
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
public class AppModuleDaoImpl extends BaseEntityDaoImpl<AppModule> implements CustomAppModuleDao {
    public AppModuleDaoImpl(EntityManager entityManager) {
        super(AppModule.class, entityManager);
    }
    public List<AppModule> findByCodes(List<String> codeList){
        TypedQuery<AppModule> appModuleQuery = entityManager.createQuery("select ft.id from com.ecmp.flow.entity.AppModule ft where code in :codeList  order by ft.lastEditedDate desc", AppModule.class);
        appModuleQuery.setParameter("codeList",codeList);
        List<AppModule> AppModuleList = appModuleQuery.getResultList();
        return AppModuleList;
    }
}
