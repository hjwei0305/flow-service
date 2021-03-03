package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseRelationDao;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import com.ecmp.flow.vo.FlowTaskControlAndPushVo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FlowTaskControlAndPushDao extends BaseRelationDao<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush> {

    /**
     * 查询关系表数据
     *
     * @return
     */
    @Query("select  new com.ecmp.flow.vo.FlowTaskControlAndPushVo (ak.id,ak.parent.id,ak.child.id) from FlowTaskControlAndPush ak where ak.parent.id = :controlId ")
    List<FlowTaskControlAndPushVo> getRelationsByControlId(@Param("controlId") String controlId);


    /**
     * 查询关系表数据
     *
     * @return
     */
    @Query("select  new com.ecmp.flow.vo.FlowTaskControlAndPushVo (ak.id,ak.parent.id,ak.child.id) from FlowTaskControlAndPush ak where ak.child.id = :pushId ")
    List<FlowTaskControlAndPushVo> getRelationsByPushId(@Param("pushId") String pushId);

    /**
     * 删除关系表
     */
    @Transactional
    @Modifying
    @Query("delete from FlowTaskControlAndPush ak where ak.id in (:idList)  ")
    void deleteRelationsByIds(@Param("idList") List<String> idList);


}
