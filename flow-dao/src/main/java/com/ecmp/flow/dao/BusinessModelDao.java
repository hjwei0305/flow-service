package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.BusinessModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessModelDao extends BaseEntityDao<BusinessModel> {
    /**
     * 根据应用模块的id来查询业务实体
     *
     * @param appModuleId 应用模块Id,xx
     * @return 岗位清单
     */
    List<BusinessModel> findByAppModuleId(String appModuleId);

}