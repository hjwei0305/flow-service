package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.FlowTaskPush;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface FlowTaskPushDao extends BaseEntityDao<FlowTaskPush> {

    /**
     * 删除关系表
     */
    @Transactional
    @Modifying
    @Query("delete from FlowTaskPush ak where ak.id = :id  ")
    void deletePushById(@Param("id") String id);
}
