package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseRelationDao;
import com.ecmp.core.service.BaseRelationService;
import com.ecmp.flow.api.IFlowTaskControlAndPushService;
import com.ecmp.flow.dao.FlowTaskControlAndPushDao;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FlowTaskControlAndPushService extends BaseRelationService<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush>
        implements IFlowTaskControlAndPushService {

    @Autowired
    private FlowTaskControlAndPushDao dao;

    @Override
    public BaseRelationDao<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush> getDao() {
        return dao;
    }

    @Autowired
    public FlowTaskPushService flowTaskPushService;

    /**
     * 获取可以分配的子实体清单
     *
     * @return 子实体清单
     */
    @Override
    protected List<FlowTaskPush> getCanAssignedChildren(String parentId) {
        return super.getChildrenFromParentId(parentId);
    }


}
