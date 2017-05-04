package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.WorkPageUrl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkPageUrlDao extends BaseDao<WorkPageUrl, String> {
    /**
     * 根据应用模块的id来查询工作界面
     *
     * @param appModuleId 应用模块Id
     * @return 岗位清单
     */
    List<WorkPageUrl> findByAppModuleId(String appModuleId);
}