package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowInstanceDao extends BaseEntityDao<FlowInstance> {

    public  FlowInstance findByActInstanceId(String actInstanceId);

    public  FlowInstance findByBusinessId(String businessId);

    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.businessId = :businessId  order by ft.lastEditedDate desc")
    public List<FlowInstance> findByBusinessIdOrder(@Param("businessId")String businessId);


    /**
     * 根据启动人id查询流程实例
     * @param creatorId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended ")
    public List<FlowInstance> findByCreatorId(@Param("creatorId")String creatorId,@Param("ended")Boolean ended);



}