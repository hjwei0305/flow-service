package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowHistoryDao extends BaseEntityDao<FlowHistory>,CustomFlowHistoryDao {

    @Query("select fh from com.ecmp.flow.entity.FlowHistory fh where fh.flowInstance.id  = :instanceId order by fh.actEndTime asc")
    public List<FlowHistory> findByInstanceId(@Param("instanceId")String instanceId);

    /**
     * 根据业务实体类型id，业务单据id，获取所有业务执行的待办，
     * 包括撤销之前的历史任务以及不同流程类型、不同流程定义、不同版本的数据
//     * @param businessModelId
     * @param businessId
     * @return
     */
//    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where  ft.flowDefinitionId in(select fd.id from FlowDefination fd where fd.id in(select fType.id from FlowType fType where fType.id in( select bm.id from BusinessModel bm where bm.id = :businessModelId)) ) and ft.flowInstance.id in(select fi.id from FlowInstance fi where fi.businessId = :businessId ) order by ft.lastEditedDate desc")
    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.id in(select fi.id from FlowInstance fi where fi.businessId = :businessId ) order by ft.lastEditedDate desc")
    public List<FlowHistory> findAllByBusinessId(@Param("businessId")String businessId);


    /**
     * 根据流程实例id查询待办
     * @param actInstanceId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.actInstanceId  = :actInstanceId and ft.actTaskDefKey = :actTaskDefKey")
    public  FlowHistory findByActTaskDefKeyAndActInstanceId(@Param("actTaskDefKey")String actTaskDefKey,@Param("actInstanceId")String actInstanceId);
}