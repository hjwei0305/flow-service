package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.Validation;
import com.ecmp.flow.api.*;
import com.ecmp.flow.basic.vo.Employee;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.BusinessUtil;
import com.ecmp.flow.constant.BusinessEntityAnnotaion;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.DefaultBusinessModelDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.CodeGenerator;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.SignalPoolTaskVO;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class DefaultBusinessModelService extends BaseEntityService<DefaultBusinessModel> implements IDefaultBusinessModelService {

    private final Logger logger = LoggerFactory.getLogger(DefaultBusinessModelService.class);

    @Autowired
    private DefaultBusinessModelDao defaultBusinessModelDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    protected BaseEntityDao<DefaultBusinessModel> getDao() {
        return this.defaultBusinessModelDao;
    }

    /**
     * 数据保存操作
     */
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<DefaultBusinessModel> save(DefaultBusinessModel entity) {
        Validation.notNull(entity, "持久化对象不能为空");
//        String businessCode = NumberGenerator.getNumber(DefaultBusinessModel.class);
        String businessCode = CodeGenerator.genCodes(6, 1).get(0);
        if (StringUtils.isEmpty(entity.getBusinessCode())) {
            entity.setBusinessCode(businessCode);
        }
        return super.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String changeCreateDepict(String id, String changeText) {
        Map<String, Object> variables = new HashMap<String, Object>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if (entity != null) {
//            defaultBusinessModelDao.saveAndFlush(entity);
            if (StringUtils.isNotEmpty(changeText)) {

                try {
                    JSONObject jsonObject = JSONObject.fromObject(changeText);
                    List<String> callActivtiySonPaths = null;
                    callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                    if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                        List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                        for (String callActivityPath : callActivtiySonPaths) {
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                                initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            changeText = "before";
            entity.setWorkCaption(changeText + ":" + entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
        }
        String param = JsonUtils.toJson(variables);
        return param;
    }

    /**
     * @param id         业务单据id
     * @param changeText 参数文本
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String changeCompletedDepict(String id, String changeText) {
        Map<String, Object> variables = new HashMap<String, Object>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if (entity != null) {
//            defaultBusinessModelDao.saveAndFlush(entity);
            if (StringUtils.isNotEmpty(changeText)) {

                try {
                    JSONObject jsonObject = JSONObject.fromObject(changeText);
                    List<String> callActivtiySonPaths = null;
                    callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                    if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                        List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                        for (String callActivityPath : callActivtiySonPaths) {
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                                initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
            changeText = "after";
            entity.setWorkCaption(changeText + ":" + entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
        }
        String param = JsonUtils.toJson(variables);
        return param;
    }

    /**
     * @param flowInvokeParams 流程输入参数
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Executor> getPersonToExecutorConfig(FlowInvokeParams flowInvokeParams) {
        String businessId = flowInvokeParams.getId();
        List<Executor> executors = new ArrayList<Executor>();
        if (StringUtils.isNotEmpty(businessId)) {
            DefaultBusinessModel defaultBusinessModel = defaultBusinessModelDao.findOne(businessId);
            if (defaultBusinessModel != null) {
                String orgid = defaultBusinessModel.getOrgId();
                //根据组织机构ID获取员工集合
                List<Employee> employeeList = flowCommonUtil.getEmployeesByOrgId(orgid);
                List<String> idList = new ArrayList<String>();
                for (Employee e : employeeList) {
                    idList.add(e.getId());
                }
                //获取执行人
                if (idList.isEmpty()) {
                    idList.add("00A0E06A-CAAF-11E7-AA54-0242C0A84202");
                    idList.add("8A6A1592-4A95-11E7-A011-960F8309DEA7");

                }
                //根据用户的id列表获取执行人
                executors = flowCommonUtil.getBasicUserExecutors(idList);
            }
        }
        return executors;
    }


    /**
     * @param id         业务单据id
     * @param changeText 参数文本
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean testReceiveCall(String id, String changeText) {
        boolean result = false;
        String receiveTaskActDefId = null;
        Map<String, Object> variables = new HashMap<String, Object>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if (entity != null) {
            if (StringUtils.isNotEmpty(changeText)) {
                JSONObject jsonObject = JSONObject.fromObject(changeText);
                //HashMap<String,Object> params =   JsonUtils.fromJson(changeText, new TypeReference<HashMap<String,Object>>() {});
                receiveTaskActDefId = jsonObject.get("receiveTaskActDefId") + "";
                List<String> callActivtiySonPaths = null;
                try {
                    callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                    List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                        }
                    }
                }
            }
            changeText = "ReceiveCall";
            entity.setWorkCaption(entity.getWorkCaption() + ":" + changeText);
            defaultBusinessModelDao.save(entity);
            final String fReceiveTaskActDefId = receiveTaskActDefId;
            new Thread(new Runnable() {//模拟异步
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000 * 20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
                    proxy.signalByBusinessId(id, fReceiveTaskActDefId, variables);
                }
            }).start();

            result = true;
        }
        return result;
    }

    public boolean checkStartFlow(String id) {
        return true;
    }

    public void endCall(String id) {
        System.out.println("id=" + id);
    }

    /**
     * 解析子流程绝对路径
     *
     * @param callActivityPath 路径值
     * @return 路径值为key，子流程id为value的MAP对象
     */
    protected Map<String, String> initCallActivtiy(String callActivityPath, boolean ifStart) {
        Map<String, String> resultMap = new HashMap<String, String>();
        //  String str ="/caigouTestZhu/CallActivity_3/yewushengqing2";
        String str = callActivityPath;
        String[] resultArray = str.split("/");
        if ((resultArray.length < 4) || (resultArray.length % 2 != 0)) {
            throw new RuntimeException("子流程路径解析错误");
        }
        List<String> resultList = new ArrayList<String>();
        for (int i = 1; i < resultArray.length; i++) {
            resultList.add(resultArray[i]);
        }
        int size = resultList.size();
        for (int j = 1; j < size; ) {
            String key = resultList.get(size - j);
            int endIndex = str.lastIndexOf(key) + key.length();
            String path = str.substring(0, endIndex);
            resultMap.put(path, key);
            j += 2;
            if (!ifStart) {
                break;//只生成一条测试数据
            }
        }
        return resultMap;
    }

    public FlowOperateResult newServiceCall(FlowInvokeParams flowInvokeParams) {
        FlowOperateResult result = new FlowOperateResult();
        String businessId = flowInvokeParams.getId();
        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(businessId);
        String changeText = "newServiceCall";
        entity.setWorkCaption(changeText + ":" + entity.getWorkCaption());
        defaultBusinessModelDao.save(entity);
        int shujishu = this.getShuiJiShu(0, 10);
        if (shujishu == 5) {
            throw new RuntimeException("测试随机抛出错误信息:" + new Date());
        } else if (shujishu == 4) {
            result.setSuccess(false);
            result.setMessage("测试随机业务异常信息:" + new Date());
        }
        return result;
    }

    public FlowOperateResult newServiceCallFailure(FlowInvokeParams flowInvokeParams) {
        FlowOperateResult result = new FlowOperateResult();
        String businessId = flowInvokeParams.getId();
        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(businessId);
        String changeText = "newServiceCall_failure";
        entity.setWorkCaption(changeText + ":" + entity.getWorkCaption());
        defaultBusinessModelDao.save(entity);
        result.setSuccess(true);
        result.setMessage("测试业务异常信息:" + new Date());
        return result;
    }

    /**
     * 获取指定范围的随机数
     *
     * @param max
     * @param min
     * @return
     */
    private static int getShuiJiShu(int min, int max) {
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }


    /**
     * 仅针对跨业务实体子任务的测试初始化方法
     *
     * @param defaultBusinessModelList
     * @param defaultBusinessModel2List
     * @param defaultBusinessModel3List
     * @param callActivityPathMap
     * @param variables
     * @param parentBusinessModel
     */
    protected void initCallActivityBusiness(List<DefaultBusinessModel> defaultBusinessModelList, List<DefaultBusinessModel2> defaultBusinessModel2List, List<DefaultBusinessModel3> defaultBusinessModel3List, Map<String, String> callActivityPathMap, Map<String, Object> variables, IBusinessFlowEntity parentBusinessModel) {
        IDefaultBusinessModelService defaultBusinessModelService = ApiClient.createProxy(IDefaultBusinessModelService.class);
        IDefaultBusinessModel2Service defaultBusinessModel2Service = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
        IDefaultBusinessModel3Service defaultBusinessModel3Service = ApiClient.createProxy(IDefaultBusinessModel3Service.class);

        IFlowDefinationService flowDefinationService = ApiClient.createProxy(IFlowDefinationService.class);

        for (Map.Entry<String, String> entry : callActivityPathMap.entrySet()) {
            String realDefiniationKey = entry.getValue();
            String realPathKey = entry.getKey();
            FlowDefination flowDefination = flowDefinationService.findByKey(realDefiniationKey);
            String sonBusinessModelCode = flowDefination.getFlowType().getBusinessModel().getClassName();
            if ("com.ecmp.flow.entity.DefaultBusinessModel".equals(sonBusinessModelCode)) {
                DefaultBusinessModel defaultBusinessModel = new DefaultBusinessModel();
                BeanUtils.copyProperties(parentBusinessModel, defaultBusinessModel);
                String name = "temp_测试跨业务实体子流程_默认业务实体" + System.currentTimeMillis();
                defaultBusinessModel.setName(name);
                defaultBusinessModel.setFlowStatus(FlowStatus.INPROCESS);
                defaultBusinessModel.setWorkCaption(parentBusinessModel.getWorkCaption() + "||" + name);
                defaultBusinessModel.setId(null);
                defaultBusinessModel.setBusinessCode(null);
                OperateResultWithData<DefaultBusinessModel> resultWithData = defaultBusinessModelService.save(defaultBusinessModel);
                String defaultBusinessModelId = resultWithData.getData().getId();
                variables.put(realPathKey, defaultBusinessModelId);
                defaultBusinessModel = resultWithData.getData();
                defaultBusinessModelList.add(defaultBusinessModel);
            } else if ("com.ecmp.flow.entity.DefaultBusinessModel2".equals(sonBusinessModelCode)) {
                DefaultBusinessModel2 defaultBusinessModel2Son = new DefaultBusinessModel2();
                BeanUtils.copyProperties(parentBusinessModel, defaultBusinessModel2Son);
                String name = "temp_测试跨业务实体子流程_采购实体" + System.currentTimeMillis();
                defaultBusinessModel2Son.setName(name);
                defaultBusinessModel2Son.setFlowStatus(FlowStatus.INPROCESS);
                defaultBusinessModel2Son.setWorkCaption(parentBusinessModel.getWorkCaption() + "||" + name);
                defaultBusinessModel2Son.setId(null);
                defaultBusinessModel2Son.setBusinessCode(null);
                OperateResultWithData<DefaultBusinessModel2> resultWithData = defaultBusinessModel2Service.save(defaultBusinessModel2Son);
                String defaultBusinessModelId = resultWithData.getData().getId();
                variables.put(realPathKey, defaultBusinessModelId);
                defaultBusinessModel2Son = resultWithData.getData();
                defaultBusinessModel2List.add(defaultBusinessModel2Son);
            } else if ("com.ecmp.flow.entity.DefaultBusinessModel3".equals(sonBusinessModelCode)) {
                DefaultBusinessModel3 defaultBusinessModel3Son = new DefaultBusinessModel3();
                BeanUtils.copyProperties(parentBusinessModel, defaultBusinessModel3Son);
                String name = "temp_测试跨业务实体子流程_销售实体" + System.currentTimeMillis();
                defaultBusinessModel3Son.setName(name);
                defaultBusinessModel3Son.setFlowStatus(FlowStatus.INPROCESS);
                defaultBusinessModel3Son.setWorkCaption(parentBusinessModel.getWorkCaption() + "||" + name);
                defaultBusinessModel3Son.setId(null);
                defaultBusinessModel3Son.setBusinessCode(null);
                OperateResultWithData<DefaultBusinessModel3> resultWithData = defaultBusinessModel3Service.save(defaultBusinessModel3Son);
                String defaultBusinessModelId = resultWithData.getData().getId();
                variables.put(realPathKey, defaultBusinessModelId);
                defaultBusinessModel3Son = resultWithData.getData();
                defaultBusinessModel3List.add(defaultBusinessModel3Son);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult changeCreateDepictNew(FlowInvokeParams flowInvokeParams) {
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            Thread.sleep(1000 * 20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FlowOperateResult result = new FlowOperateResult();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                try {

                    List<String> callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                    if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                        List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                        for (String callActivityPath : callActivtiySonPaths) {
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                                initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
            String changeText = "before";
            if (flowInvokeParams.getNextNodeUserInfo() != null) {
                Map<String, List<String>> map = flowInvokeParams.getNextNodeUserInfo();
                Set set = map.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapentry = (Map.Entry) iterator.next();
                    changeText += mapentry.getKey() + "/" + mapentry.getValue();
                }
//                changeText+=flowInvokeParams.getNextNodeUserInfo().get(0).get(0);
            }
            entity.setWorkCaption(changeText + ":" + entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult changeCompletedDepictNew(FlowInvokeParams flowInvokeParams) {
        Map<String, Object> variables = new HashMap<String, Object>();
        FlowOperateResult result = new FlowOperateResult();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                try {

                    List<String> callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                    if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                        List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                        for (String callActivityPath : callActivtiySonPaths) {
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                                initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
            String changeText = "after";
            if (flowInvokeParams.getNextNodeUserInfo() != null) {
                Map<String, List<String>> map = flowInvokeParams.getNextNodeUserInfo();
                Set set = map.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapentry = (Map.Entry) iterator.next();
                    changeText += mapentry.getKey() + "/" + mapentry.getValue();
                }
//                changeText+=flowInvokeParams.getNextNodeUserInfo().get(0).get(0);
            }
            entity.setWorkCaption(changeText + ":" + entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult testReceiveCallNew(FlowInvokeParams flowInvokeParams) {
        FlowOperateResult result = new FlowOperateResult();
        String receiveTaskActDefId = null;
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                receiveTaskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = null;
                callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                    List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                        }
                    }
                }
                String changeText = "ReceiveCall";
                entity.setWorkCaption(entity.getWorkCaption() + ":" + changeText);
                defaultBusinessModelDao.save(entity);
                final String fReceiveTaskActDefId = receiveTaskActDefId;
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        long time = 20; //默认20秒
                        int index = 20;//重试20次
                        while (index > 0) {
                            try {
                                Thread.sleep(1000 * time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
                                OperateResult resultTemp = proxy.signalByBusinessId(flowInvokeParams.getId(), fReceiveTaskActDefId, variables);
                                if (resultTemp.successful()) {
                                    return;
                                } else {
                                    time = time * 2; //加倍
                                    logger.error(resultTemp.getMessage());
                                }
                            } catch (Exception e) {
                                time = time * 4; //加倍
                                logger.error(e.getMessage(), e);
                            }
                            index--;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult testPoolTaskSignal(FlowInvokeParams flowInvokeParams) {
        System.out.println(flowInvokeParams.getPoolTaskCode());
        FlowOperateResult result = new FlowOperateResult();
        String taskActDefId = null;
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                taskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = null;
                callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                    List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                        }
                    }
                }
                entity.setWorkCaption("工作池任务：[ID:" + flowInvokeParams.getId() + ",actId:" + flowInvokeParams.getTaskActDefId() + "]");
                defaultBusinessModelDao.save(entity);
                final String fTaskActDefId = taskActDefId;
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        long time = 60; //默认60秒
                        int index = 2;//重试2次
                        while (index > 0) {
                            try {
                                Thread.sleep(1000 * time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
                                //工作池任务设置单个执行人
//                                OperateResult resultTemp = proxy.signalPoolTaskByBusinessId(flowInvokeParams.getId(), fTaskActDefId,"1592D012-A330-11E7-A967-02420B99179E" ,variables);
                                //工作池任务设置多个执行人
                                SignalPoolTaskVO SignalPoolTaskVO = new SignalPoolTaskVO();
                                SignalPoolTaskVO.setBusinessId(flowInvokeParams.getId());
                                SignalPoolTaskVO.setPoolTaskActDefId(fTaskActDefId);
                                List<String> userIds = new ArrayList<>();
                                userIds.add("1592D012-A330-11E7-A967-02420B99179E");
                                userIds.add("1AE28F00-2FFC-11E9-AC2E-0242C0A84417");
                                SignalPoolTaskVO.setUserIds(userIds);
                                SignalPoolTaskVO.setMap(new HashMap<>());
                                ResponseData resultTemp = proxy.signalPoolTaskByBusinessIdAndUserList(SignalPoolTaskVO);
                                if (resultTemp.successful()) {
                                    return;
                                } else {
                                    time = time * 2; //加倍
                                    logger.error(resultTemp.getMessage());
                                }
                            } catch (Exception e) {
                                time = time * 4; //加倍
                                logger.error(e.getMessage(), e);
                            }
                            index--;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.setSuccess(true);
        result.setMessage("test success!");
//        result.setUserId("1592D012-A330-11E7-A967-02420B99179E");
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult testPoolTaskCreatePool(FlowInvokeParams flowInvokeParams) {
        System.out.println(flowInvokeParams.getPoolTaskCode());
        FlowOperateResult result = new FlowOperateResult();
        String taskActDefId = null;
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                taskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = null;
                callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                    List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                        }
                    }
                }
                String changeText = "PoolCallPoolCreate";
                entity.setWorkCaption(entity.getWorkCaption() + ":" + changeText);
                defaultBusinessModelDao.save(entity);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.setSuccess(true);
        result.setMessage("test success!");
//        result.setUserId("8A6A1592-4A95-11E7-A011-960F8309DEA7");
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult testPoolTaskComplete(FlowInvokeParams flowInvokeParams) {
        FlowOperateResult result = new FlowOperateResult();
        String taskActDefId = null;
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                taskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = null;
                callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (callActivtiySonPaths != null && !callActivtiySonPaths.isEmpty()) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                    List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                        }
                    }
                }
                String changeText = "PoolCallComplete";
                entity.setWorkCaption(entity.getWorkCaption() + ":" + changeText);
                defaultBusinessModelDao.save(entity);
                final String fTaskActDefId = taskActDefId;
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        long time = 200; //默认200秒
                        int index = 2;//重试20次
                        while (index > 0) {
                            try {
                                Thread.sleep(1000 * time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
                                FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
                                OperateResultWithData<FlowStatus> resultTemp = proxy.completePoolTask(flowInvokeParams.getId(), fTaskActDefId, "8A6A1592-4A95-11E7-A011-960F8309DEA7", flowTaskCompleteVO);
                                if (resultTemp.successful()) {
                                    return;
                                } else {
                                    time = time * 2; //加倍
                                    logger.error(resultTemp.getMessage());
                                }
                            } catch (Exception e) {
                                time = time * 4; //加倍
                                logger.error(e.getMessage(), e);
                            }
                            index--;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    public Map<String, Object> businessPropertiesAndValues(String businessModelCode, String id) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        IBusinessModelService businessModelService;
        businessModelService = ApiClient.createProxy(IBusinessModelService.class);
        BusinessModel businessModel = businessModelService.findByClassName(businessModelCode);
        String daoBeanName = null;
        if (businessModel != null) {
            daoBeanName = getDaoBeanName(businessModelCode);
        }
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao) applicationContext.getBean(daoBeanName);
        DefaultBusinessModel content = (DefaultBusinessModel) appModuleDao.findOne(id);
        DefaultBusinessModel2 defaultBusinessModel2 = new DefaultBusinessModel2();
        defaultBusinessModel2.setId("testId2");
        defaultBusinessModel2.setBusinessCode("testBusinessCode2");
        defaultBusinessModel2.setWorkCaption("testWorkCaption2");
        defaultBusinessModel2.setName("testName2");
        defaultBusinessModel2.setCount(4);
        defaultBusinessModel2.setUnitPrice(3);
        defaultBusinessModel2.setSum(defaultBusinessModel2.getCount() * defaultBusinessModel2.getUnitPrice());
        content.setDefaultBusinessModel2(defaultBusinessModel2);
        return BusinessUtil.getPropertiesAndValues(content, null);
    }

    private String getDaoBeanName(String className) throws ClassNotFoundException {
        BusinessEntityAnnotaion businessEntityAnnotaion = this.getBusinessEntityAnnotaion(className);
        return businessEntityAnnotaion.daoBean();
    }

    private BusinessEntityAnnotaion getBusinessEntityAnnotaion(String className) throws ClassNotFoundException {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(className)) {
            Class sourceClass = Class.forName(className);
            BusinessEntityAnnotaion businessEntityAnnotaion = (BusinessEntityAnnotaion) sourceClass.getAnnotation(BusinessEntityAnnotaion.class);
            return businessEntityAnnotaion;
        } else {
            throw new RuntimeException("className is null!");
        }
    }

}
