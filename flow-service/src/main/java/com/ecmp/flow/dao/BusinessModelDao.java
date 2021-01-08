package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.BusinessModel;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface BusinessModelDao extends BaseEntityDao<BusinessModel>,CustomBusinessModelDao {
    /**
     * 根据应用模块的id来查询业务实体
     *
     * @param appModuleId 应用模块Id,xx
     * @return 岗位清单
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<BusinessModel> findByAppModuleId(String appModuleId);

    /**
     * 根据className来查询业务实体
     *
     * @param className 业务实例代码（类全路径）
     * @return 岗位清单
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    BusinessModel findByClassName(String className);

}