package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IBusinessWorkPageUrlService;
import com.ecmp.flow.dao.BusinessWorkPageUrlDao;
import com.ecmp.flow.entity.BusinessWorkPageUrl;
import com.ecmp.vo.OperateResultWithData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.awt.*;
import java.util.*;
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
public class BusinessWorkPageUrlService extends BaseService<BusinessWorkPageUrl, String> implements IBusinessWorkPageUrlService {

    @Autowired
    private BusinessWorkPageUrlDao businessWorkPageUrlDao;

    @Override
    public void saveBusinessWorkPageUrlByIds(String id, String selectWorkPageIds) {
       // List<BusinessWorkPageUrl> businessWorkPageUrls = businessWorkPageUrlDao.findByBusinessModuleId(id);
       // if(businessWorkPageUrls != null){
//            for(int i=0;i<businessWorkPageUrls.size();i++){
//                businessWorkPageUrlDao.delete(businessWorkPageUrls.get(i));
//            }
        org.springframework.orm.jpa.JpaTransactionManager  transactionManager =(org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
        TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
        try {
            //逻辑代码，可以写上你的逻辑处理代码
            businessWorkPageUrlDao.deleteBybusinessModuleId(id);
            transactionManager.commit(status);
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(status);
            throw e;
        }

            if(StringUtils.isBlank(selectWorkPageIds)){
                return;
            }else {
                String[] str = selectWorkPageIds.split(",");
                for(int i=0;i<str.length;i++){
                    BusinessWorkPageUrl businessWorkPageUrl = new BusinessWorkPageUrl();
                    businessWorkPageUrl.setBusinessModuleId(id);
                    businessWorkPageUrl.setWorkPageUrlId(str[i]);
                    businessWorkPageUrlDao.save(businessWorkPageUrl);
                }
            }
        }
    }

