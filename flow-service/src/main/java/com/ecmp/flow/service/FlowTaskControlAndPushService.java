package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseRelationDao;
import com.ecmp.core.service.BaseRelationService;
import com.ecmp.flow.api.IFlowTaskControlAndPushService;
import com.ecmp.flow.dao.FlowTaskControlAndPushDao;
import com.ecmp.flow.dao.FlowTaskPushControlDao;
import com.ecmp.flow.dao.FlowTaskPushDao;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import com.ecmp.flow.vo.FlowTaskControlAndPushVo;
import com.ecmp.log.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class FlowTaskControlAndPushService extends BaseRelationService<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush>
        implements IFlowTaskControlAndPushService {

    @Autowired
    private FlowTaskControlAndPushDao flowTaskControlAndPushDao;

    @Override
    public BaseRelationDao<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush> getDao() {
        return flowTaskControlAndPushDao;
    }

    @Autowired
    public FlowTaskPushService flowTaskPushService;

    @Autowired
    public FlowTaskPushDao flowTaskPushDao;

    @Autowired
    public FlowTaskPushControlDao flowTaskPushControlDao;

    /**
     * 获取可以分配的子实体清单
     *
     * @return 子实体清单
     */
    @Override
    protected List<FlowTaskPush> getCanAssignedChildren(String parentId) {
        return super.getChildrenFromParentId(parentId);
    }


    public void cleaningPushHistoryData(List<FlowTaskPushControl> list, String redisKey) {
        //清理检查设置30分钟过期
        redisTemplate.expire("pushCleaning_" + redisKey, 30 * 60, TimeUnit.SECONDS);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(this::cleaningPushHistoryData);
        }
        redisTemplate.delete("pushCleaning_" + redisKey);
    }

    @Transactional
    public void cleaningPushHistoryData(FlowTaskPushControl flowTaskPushControl) {
        try {
            String controlId = flowTaskPushControl.getId();
            List<FlowTaskControlAndPushVo> relationsList = flowTaskControlAndPushDao.getRelationsByControlId(controlId);
            List<String> relationsIDList = relationsList.stream().map(FlowTaskControlAndPushVo::getId).collect(Collectors.toList());
            flowTaskControlAndPushDao.deleteRelationsByIds(relationsIDList);
            flowTaskPushControlDao.delete(controlId);
            relationsList.forEach(a -> {
                String pushId = a.getPushId();
                List<FlowTaskControlAndPushVo> andRelationsList = flowTaskControlAndPushDao.getRelationsByPushId(pushId);
                if (CollectionUtils.isEmpty(andRelationsList)) {
                    flowTaskPushDao.deletePushById(pushId);
                }
            });
        } catch (Exception e) {
            LogUtil.error("清除历史推送数据报错,[controlId = " + flowTaskPushControl.getId() + "]！", e);
        }
    }


}
