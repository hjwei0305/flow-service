package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.BusinessWorkPageUrl;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface BusinessWorkPageUrlDao extends BaseEntityDao<BusinessWorkPageUrl> {

    public BusinessWorkPageUrl findByBusinessModuleIdAndWorkPageUrlId(String businessModuleId, String workPageUrlId);

    public List<BusinessWorkPageUrl> findByBusinessModuleId(String businessModuleId);

    @Transactional(Transactional.TxType.REQUIRED)
    @Modifying
    @Query("delete from com.ecmp.flow.entity.BusinessWorkPageUrl b where b.businessModuleId = :businessModuleId")
    public void deleteBybusinessModuleId(@Param("businessModuleId")String businessModuleId);
}