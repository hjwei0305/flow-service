package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.BusinessWorkPageUrl;
import com.ecmp.flow.entity.WorkPageUrl;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessWorkPageUrlDao extends BaseDao<BusinessWorkPageUrl, String> {

    public BusinessWorkPageUrl findByBusinessModuleIdAndWorkPageUrlId(String businessModuleId, String workPageUrlId);

}