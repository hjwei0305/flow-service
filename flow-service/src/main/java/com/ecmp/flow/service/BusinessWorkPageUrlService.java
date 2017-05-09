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
        businessWorkPageUrlDao.deleteBybusinessModuleId(id);
        if (StringUtils.isBlank(selectWorkPageIds)) {
            return;
        } else {
            String[] str = selectWorkPageIds.split(",");
            for (int i = 0; i < str.length; i++) {
                BusinessWorkPageUrl businessWorkPageUrl = new BusinessWorkPageUrl();
                businessWorkPageUrl.setBusinessModuleId(id);
                businessWorkPageUrl.setWorkPageUrlId(str[i]);
                businessWorkPageUrlDao.save(businessWorkPageUrl);
            }
        }
    }
}

