package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IBusinessWorkPageUrlService;
import com.ecmp.flow.dao.BusinessWorkPageUrlDao;
import com.ecmp.flow.entity.BusinessWorkPageUrl;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void saveBusinessWorkPageUrlByIds(String id, String[] selectWorkPageIds) {
        for(int i=0;i<selectWorkPageIds.length;i++){
            BusinessWorkPageUrl businessWorkPageUrl = businessWorkPageUrlDao.findByBusinessModuleIdAndWorkPageUrlId(id,selectWorkPageIds[i]);
            if(businessWorkPageUrl !=null){
                businessWorkPageUrl = new BusinessWorkPageUrl();
                businessWorkPageUrl.setBusinessModuleId(id);
                businessWorkPageUrl.setWorkPageUrlId(selectWorkPageIds[i]);
                businessWorkPageUrlDao.save(businessWorkPageUrl);
            }
        }
    }
}
