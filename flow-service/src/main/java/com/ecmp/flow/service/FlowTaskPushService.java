package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowTaskPushService;
import com.ecmp.flow.dao.FlowTaskPushDao;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.FlowTaskPush;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class FlowTaskPushService extends BaseEntityService<FlowTaskPush> implements IFlowTaskPushService {

    @Autowired
    private FlowTaskPushDao flowTaskPushDao;

    protected BaseEntityDao<FlowTaskPush> getDao() {
        return this.flowTaskPushDao;
    }


    /**
     * 通过任务集合保存推送任务集合
     *
     * @param taskList 流程任务集合
     * @return
     */
    public List<FlowTaskPush> saveListByFlowTaskList(List<FlowTask> taskList) throws Exception {
        List<FlowTaskPush> list = new ArrayList<FlowTaskPush>();
        taskList.forEach(flowTask -> {
            //如果是basic和业务模块都推送的情况，可以会出现一个任务id两条数据的情况
            List<FlowTaskPush> flowTaskPushList = flowTaskPushDao.findListByProperty("flowTaskId", flowTask.getId());
            FlowTaskPush flowTaskPush;
            if (flowTaskPushList == null || flowTaskPushList.size() == 0) {
                flowTaskPush = new FlowTaskPush();
                BeanUtils.copyProperties(flowTask, flowTaskPush);
                flowTaskPush.setId(null);
                flowTaskPush.setFlowTaskId(flowTask.getId());
            } else {
                flowTaskPush = flowTaskPushList.get(0);
                String oldId = flowTaskPush.getId();
                Boolean taskAuto = flowTaskPush.getNewTaskAuto();
                BeanUtils.copyProperties(flowTask, flowTaskPush);
                flowTaskPush.setId(oldId);
                flowTaskPush.setNewTaskAuto(taskAuto);
                flowTaskPush.setFlowTaskId(flowTask.getId());
            }
            list.add(flowTaskPush);
        });
        flowTaskPushDao.save(list);
        return list;
    }

    /**
     * 通过任务集合更新推送任务集合
     *
     * @param taskList 流程任务集合
     * @return
     */
    public List<FlowTaskPush> updateListByFlowTaskList(List<FlowTask> taskList, List<FlowTaskPush> pushList) throws Exception {
        List<FlowTaskPush> list = new ArrayList<FlowTaskPush>();
        taskList.forEach(flowTask -> {
            pushList.forEach(push -> {
                if (flowTask.getId().equals(push.getFlowTaskId())) {
                    String oldId = push.getId();
                    Boolean newAuto = push.getNewTaskAuto();
                    BeanUtils.copyProperties(flowTask, push);
                    push.setId(oldId);
                    push.setNewTaskAuto(newAuto); //这样在重新推送待办才能保证参数的正确
                    push.setFlowTaskId(flowTask.getId());
                    list.add(push);
                }
            });
        });
        flowTaskPushDao.save(list);
        return list;
    }


}
