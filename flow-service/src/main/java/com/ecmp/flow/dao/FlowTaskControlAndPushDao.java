package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseRelationDao;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowTaskControlAndPushDao extends BaseRelationDao<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush> {

}
