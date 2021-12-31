package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.entity.RelationParam;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowTaskPushControlService;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowTaskPushControlDao;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import com.ecmp.flow.vo.CleaningPushHistoryVO;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ecmp.flow.common.util.Constants.*;


@Service
public class FlowTaskPushControlService extends BaseEntityService<FlowTaskPushControl> implements IFlowTaskPushControlService {

    @Autowired
    private FlowTaskPushControlDao flowTaskPushControlDao;

    @Autowired
    private FlowTaskPushService flowTaskPushService;

    @Autowired
    private FlowTaskControlAndPushService flowTaskControlAndPushService;

    @Autowired
    private FlowTaskService flowTaskService;


    protected BaseEntityDao<FlowTaskPushControl> getDao() {
        return this.flowTaskPushControlDao;
    }


    /**
     * 通过推送控制表Id重新推送当前任务
     *
     * @param pushControlId 推送控制表ID
     * @return
     */
    public ResponseData pushAgainByControlId(String pushControlId) {
        FlowTaskPushControl flowTaskPushControl = flowTaskPushControlDao.findOne(pushControlId);
        ResponseData responseData;
        if (flowTaskPushControl != null) {
            responseData = this.pushAgainByControl(flowTaskPushControl);
        } else {
            responseData = ResponseData.operationFailure("10181");
        }
        return responseData;
    }

    /**
     * 通过控制表重新推送任务
     *
     * @param flowTaskPushControl 推送控制表
     * @return
     */
    public ResponseData pushAgainByControl(FlowTaskPushControl flowTaskPushControl) {
        String pushType = flowTaskPushControl.getPushType();
        if (StringUtils.isEmpty(pushType)) {
            return ResponseData.operationFailure("10182");
        }
        String PushStatus = flowTaskPushControl.getPushStatus();
        if (StringUtils.isEmpty(PushStatus)) {
            return ResponseData.operationFailure("10183");
        }
        try {
            //得到需要推送的任务集合
            List<FlowTaskPush> pushList = flowTaskControlAndPushService.getChildrenFromParentId(flowTaskPushControl.getId());
            List<FlowTask> taskList = this.copyPushTaskToFlowTask(pushList);
            if (taskList == null || taskList.size() == 0) {
                return ResponseData.operationFailure("10184");
            }
            return this.pushAgainByTypeAndStatus(pushType, PushStatus, taskList);
        } catch (Exception e) {
            LogUtil.error("重新推送任务获取参数失败！", e);
            return ResponseData.operationFailure("10185", e.getMessage());
        }
    }


    /**
     * 通过类型、状态和流程任务，重新推送
     *
     * @param pushType     推送类型
     * @param pushStatus   推送状态
     * @param flowTaskList 流程任务集合
     * @return
     */
    public ResponseData pushAgainByTypeAndStatus(String pushType, String pushStatus, List<FlowTask> flowTaskList) {
        ResponseData responseData;
        if (TYPE_BASIC.equals(pushType)) { //推送到basic
            responseData = this.pushAgainToBasic(pushStatus, flowTaskList);
        } else {
            responseData = ResponseData.operationFailure("10186");
        }
        return responseData;
    }


    /**
     * 重新推送到basic
     *
     * @return
     */
    public ResponseData pushAgainToBasic(String pushStatus, List<FlowTask> flowTaskList) {
        Boolean boo;
        if (STATUS_BASIC_NEW.equals(pushStatus)) {//新增待办
            boo = flowTaskService.pushToBasic(flowTaskList, null, null, null);
        } else if (STATUS_BASIC_OLD.equals(pushStatus)) { //待办转已办
            boo = flowTaskService.pushToBasic(null, flowTaskList, null, null);
        } else if (STATUS_BASIC_DEL.equals(pushStatus)) { //删除待办
            boo = flowTaskService.pushToBasic(null, null, flowTaskList, null);
        } else if (STATUS_BASIC_END.equals(pushStatus)) { //归档（终止）
            boo = flowTaskService.pushToBasic(null, null, null, flowTaskList.get(0));
        } else {
            return ResponseData.operationFailure("10187");
        }

        if (boo) {
            return ResponseData.operationSuccess("10188");
        } else {
            return ResponseData.operationFailure("10189");
        }
    }


    /**
     * 将推送任务集合转换成流程任务集合
     *
     * @param pushList 推送任务集合
     * @return
     */
    public List<FlowTask> copyPushTaskToFlowTask(List<FlowTaskPush> pushList) throws Exception {
        List<FlowTask> taskList = new ArrayList<FlowTask>();
        pushList.forEach(push -> {
            FlowTask flowTask = new FlowTask();
            BeanUtils.copyProperties(push, flowTask);
            flowTask.setId(push.getFlowTaskId());
            taskList.add(flowTask);
        });
        return taskList;
    }

    /**
     * 通过流程实例ID、节点ID、推送类型、推送状态得到推送信息表集合
     *
     * @param flowInstanceId 流程实例ID
     * @param nodeId         节点ID
     * @param type           推送类型
     * @param status         推送状态
     * @return
     */
    public List<FlowTaskPushControl> getByInstanceAndNodeAndTypeAndStatus(String flowInstanceId, String nodeId, String type, String status) {
        Search search = new Search();
        search.addFilter(new SearchFilter("flowInstanceId", flowInstanceId));
        search.addFilter(new SearchFilter("flowActTaskDefKey", nodeId));
//        search.addFilter(new SearchFilter("pushType", type));
        search.addFilter(new SearchFilter("pushStatus", status));
        return flowTaskPushControlDao.findByFilters(search);
    }

    /**
     * 建立新的推送信息表和任务表（以及关联关系）
     *
     * @param type     推送类型
     * @param status   推送状态
     * @param url      推送url
     * @param success  是否推送成功
     * @param taskList 推送的任务集合
     * @return
     */
    public void saveNewControlInfo(String type, String status, String url, Boolean success, List<FlowTask> taskList) throws Exception {
        //保存推送信息父表
        FlowTaskPushControl control = this.saveBeanByFlowTask(type, status, url, success, taskList);
        //保存推送任务集合
        List<FlowTaskPush> pushTaskList = flowTaskPushService.saveListByFlowTaskList(taskList);
        //推送任务ID集合
        List<String> pushIdList = new ArrayList<>();
        pushTaskList.forEach(a -> pushIdList.add(a.getId()));
        //创建关联关系
        RelationParam relationParam = new RelationParam();
        relationParam.setParentId(control.getId());
        relationParam.setChildIds(pushIdList);
        flowTaskControlAndPushService.insertRelationsByParam(relationParam);
        //如果推送不成功，进行重新推送3次（任何一次成功就终止）：第一次间隔1分钟，第二次间隔2分钟,第三次间隔3分钟
        //也有定时任务： flowTaskPushControl/pushFailTimingTask   流程配置文件中PUSH_FAIL_TASK_TIME配置查询多少小时之内失败的任务进行推送
        if (!success) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Boolean result = false;
                    int index = 3;
                    String controlId = control.getId();
                    while (!result && index > 0) {
                        try {
                            Thread.sleep(1000 * 60 * (4 - index));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            ResponseData responseData = pushAgainByControlId(controlId);
                            result = responseData.getSuccess();
                        } catch (Exception e) {
                            LogUtil.error(e.getMessage(), e);
                        }
                        index--;
                    }
                }
            }).start();
        }
    }


    /**
     * 修改的推送信息表和任务表（以及关联关系）
     *
     * @param url                 推送url
     * @param success             是否推送成功
     * @param taskList            推送的任务集合
     * @param flowTaskPushControl 需要更新的推送父表
     * @return
     */
    public void updateControlAndPush(String url, Boolean success, List<FlowTask> taskList, FlowTaskPushControl flowTaskPushControl) throws Exception {
        //更新推送信息父表
        FlowTaskPushControl control = this.updateBeanByFlowTask(flowTaskPushControl, url, success, taskList);
        List<FlowTaskPush> pushList = flowTaskControlAndPushService.getChildrenFromParentId(control.getId());
        //更新推送任务集合
        pushList = flowTaskPushService.updateListByFlowTaskList(taskList, pushList);
        //推送任务ID集合
        List<String> pushIdList = new ArrayList<>();
        pushList.forEach(a -> pushIdList.add(a.getId()));
        //创建关联关系
        RelationParam relationParam = new RelationParam();
        relationParam.setParentId(control.getId());
        relationParam.setChildIds(pushIdList);
        flowTaskControlAndPushService.insertRelationsByParam(relationParam);
    }


    /**
     * 更新或新建推送信息表和任务表（以及关联关系）
     *
     * @param type        推送类型
     * @param status      推送状态
     * @param url         推送url
     * @param success     是否推送成功
     * @param taskList    推送的任务集合
     * @param controlList 已有的推送父表
     * @throws Exception
     */
    public void updateOldControlInfo(String type, String status, String url, Boolean success,
                                     List<FlowTask> taskList, List<FlowTaskPushControl> controlList) throws Exception {
        Boolean updateInfo = false;  //是否更新了数据
        for (int i = 0; i < controlList.size(); i++) {
            List<FlowTaskPush> pushList = flowTaskControlAndPushService.getChildrenFromParentId(controlList.get(i).getId());
            int sameNumber = 0;
            if (pushList.size() == taskList.size()) {
                for (int k = 0; k < taskList.size(); k++) {
                    Boolean boo = this.ifInclude(pushList, taskList.get(k));
                    if (!boo) {
                        break;
                    }
                    sameNumber++;
                }

                //说明已经推送过
                if (sameNumber == taskList.size()) {
                    //更新推送信息
                    this.updateControlAndPush(url, success, taskList, controlList.get(i));
                    updateInfo = true;
                    break;
                }
            } else {
                continue;
            }
        }

        //表示当前推送信息需要新建
        if (updateInfo == false) {
            //新建推送任务父表和任务列表
            this.saveNewControlInfo(type, status, url, success, taskList);
        }
    }


    /**
     * 判断推送任务列表中是否存在该任务
     *
     * @param list
     * @param flowTask
     * @return
     */
    public Boolean ifInclude(List<FlowTaskPush> list, FlowTask flowTask) {
        for (int i = 0; i < list.size(); i++) {
            //要判断执行人相同是因为（工作池任务最开始是匿名用户，指定用户后直接改的执行人）
            if (list.get(i).getFlowTaskId().equals(flowTask.getId()) && list.get(i).getExecutorId().equals(flowTask.getExecutorId())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 通过流程任务构建推送信息父表
     *
     * @param taskList 推送任务集合
     * @return
     */
    public FlowTaskPushControl saveBeanByFlowTask(String type, String status, String url, Boolean success, List<FlowTask> taskList) throws Exception {
        FlowTask flowTask = taskList.get(0); //因为任务都是同类型节点推送，所以基本信息一致，默认取第一个任务进行基本信息确认
        FlowTaskPushControl control = new FlowTaskPushControl();
        control.setAppModuleId(flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getId());
        control.setBusinessModelId(flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getId());
        control.setFlowTypeId(flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId());
        control.setFlowInstanceId(flowTask.getFlowInstance().getId());
        control.setFlowInstanceName(flowTask.getFlowInstance().getFlowName());
        control.setFlowActTaskDefKey(flowTask.getActTaskDefKey());
        control.setFlowTaskName(flowTask.getTaskName());
        control.setBusinessId(flowTask.getFlowInstance().getBusinessId());
        control.setBusinessCode(flowTask.getFlowInstance().getBusinessCode());
        //执行人名称字段
        List<String> nameList = new ArrayList<String>();
        taskList.forEach(a -> nameList.add(a.getExecutorName()));
        String nameListString = nameList.toString();
        nameListString = nameListString.substring(1, nameListString.length() - 1);
        control.setExecutorNameList(nameListString);
        control.setPushType(type);
        control.setPushStatus(status);
        control.setPushUrl(url);
        control.setPushNumber(1);
        if (success) {
            control.setPushSuccess(1);
            control.setPushFalse(0);
        } else {
            control.setPushSuccess(0);
            control.setPushFalse(1);
        }
        Date date = new Date();
        control.setPushStartDate(date);
        control.setPushEndDate(date);
        control.setTenantCode(ContextUtil.getTenantCode());
        flowTaskPushControlDao.save(control);
        new Thread(new Runnable() {
            @Override
            public void run() {
                cleaningPushByMonth(control.getFlowTypeId());
            }
        }).start();
        return control;
    }


    public FlowTaskPushControl updateBeanByFlowTask(FlowTaskPushControl control, String url, Boolean success, List<FlowTask> taskList) throws Exception {
        //执行人名称字段
        List<String> nameList = new ArrayList<>();
        taskList.forEach(a -> nameList.add(a.getExecutorName()));
        String nameListString = nameList.toString();
        nameListString = nameListString.substring(1, nameListString.length() - 1);
        control.setExecutorNameList(nameListString);
        control.setPushUrl(url);
        control.setPushNumber(control.getPushNumber() + 1);
        if (success) {
            control.setPushSuccess(control.getPushSuccess() + 1);
        } else {
            control.setPushFalse(control.getPushFalse() + 1);
        }
        Date date = new Date();
        control.setPushEndDate(date);
        control.setTenantCode(ContextUtil.getTenantCode());
        flowTaskPushControlDao.save(control);
        new Thread(new Runnable() {
            @Override
            public void run() {
                cleaningPushByMonth(control.getFlowTypeId());
            }
        }).start();
        return control;
    }


    public void cleaningPushByMonth(String flowTypeId) {
        if (StringUtils.isEmpty(flowTypeId)) {
            return;
        }
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sim.format(new Date());
        Boolean setValue = redisTemplate.opsForValue().setIfAbsent(Constants.REDIS_KEY_PREFIX + "cleaningPushByMonth:" + flowTypeId + dateString, flowTypeId + dateString);
        if (BooleanUtils.isTrue(setValue)) { //表示今天还没清理过，开始清理
            //清理日期设置24小时过期
            redisTemplate.expire(Constants.REDIS_KEY_PREFIX + "cleaningPushByMonth:" + flowTypeId + dateString, 24 * 60 * 60, TimeUnit.SECONDS);
            String keepMonth = Constants.getFlowPropertiesByKey("KEEP_PUSH_LOG_MONTH");
            if (StringUtils.isEmpty(keepMonth)) {
                keepMonth = "6";
            }
            int month = Integer.parseInt(keepMonth);
            LogUtil.bizLog("开始清理推送信息数据，清理类型ID={},保留{}个月数据！", flowTypeId, keepMonth);
            CleaningPushHistoryVO cleaningPushHistoryVO = new CleaningPushHistoryVO();
            cleaningPushHistoryVO.setFlowTypeId(flowTypeId);
            cleaningPushHistoryVO.setRecentDate(month);
            cleaningPushHistoryVO.setAsyn(false);
            cleaningPushHistoryData(cleaningPushHistoryVO);
        }
    }

    @Override
    public ResponseData cleaningPushHistoryData(CleaningPushHistoryVO cleaningPushHistoryVO) {
        if (cleaningPushHistoryVO == null) {
            return ResponseData.operationFailure("10006");
        }
        if (cleaningPushHistoryVO.getRecentDate() == null) {
            return ResponseData.operationFailure("10190");
        }
        if (StringUtils.isEmpty(cleaningPushHistoryVO.getFlowTypeId())) {
            return ResponseData.operationSuccess("10059");
        }

        Search search = new Search();
        search.addFilter(new SearchFilter("flowTypeId", cleaningPushHistoryVO.getFlowTypeId()));
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -cleaningPushHistoryVO.getRecentDate());
        search.addFilter(new SearchFilter("pushStartDate", calendar.getTime(), SearchFilter.Operator.LT));
        List<FlowTaskPushControl> list = flowTaskPushControlDao.findByFilters(search);
        if (!CollectionUtils.isEmpty(list)) {
            String redisKey = ContextUtil.getTenantCode() + cleaningPushHistoryVO.getFlowTypeId() + cleaningPushHistoryVO.getRecentDate();
            Boolean setValue = redisTemplate.opsForValue().setIfAbsent("pushCleaning_" + redisKey, redisKey);
            if (!setValue) {
                Long remainingTime = redisTemplate.getExpire("pushCleaning_" + redisKey, TimeUnit.SECONDS);
                if (remainingTime == -1) {  //说明时间未设置进去（默认清理30分钟）
                    redisTemplate.expire("pushCleaning_" + redisKey, 30 * 60, TimeUnit.SECONDS);
                    remainingTime = 1800L;
                }
                return ResponseData.operationFailure("10191", remainingTime);
            } else {
                if(BooleanUtils.isFalse(cleaningPushHistoryVO.getAsyn())){
                    flowTaskControlAndPushService.cleaningPushHistoryData(list, redisKey);
                }else{
                    //异步清理历史数据
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskControlAndPushService.cleaningPushHistoryData(list, redisKey);
                        }
                    }).start();
                }
                return ResponseData.operationSuccess("10192");
            }
        } else {
            return ResponseData.operationSuccess("10193");
        }
    }


    @Override
    public ResponseData pushFailTimingTask() {
        String pushFailTaskTime = Constants.getFlowPropertiesByKey("PUSH_FAIL_TASK_TIME");
        int time;
        if (StringUtils.isEmpty(pushFailTaskTime)) {
            time = 24;
        } else {
            try {
                time = Integer.parseInt(pushFailTaskTime);
            } catch (Exception e) {
                time = 24;
            }
        }
        Search search = new Search();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -time);
        search.addFilter(new SearchFilter("pushStartDate", calendar.getTime(), SearchFilter.Operator.GT));
        search.addFilter(new SearchFilter("pushSuccess", 0));
        List<FlowTaskPushControl> list = flowTaskPushControlDao.findByFilters(search);
        if (!CollectionUtils.isEmpty(list)) {
            for (FlowTaskPushControl bean : list) {
                this.pushAgainByControl(bean);
            }
            return ResponseData.operationSuccess("10194");
        } else {
            return ResponseData.operationSuccess("10195");
        }
    }
}
