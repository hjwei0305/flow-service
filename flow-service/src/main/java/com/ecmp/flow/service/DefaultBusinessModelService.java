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
import com.ecmp.flow.constant.BusinessEntityAnnotaion;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.DefaultBusinessModelDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.BusinessUtil;
import com.ecmp.flow.util.CodeGenerator;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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


    @Autowired
    private DefaultBusinessModelDao defaultBusinessModelDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    protected BaseEntityDao<DefaultBusinessModel> getDao() {
        return this.defaultBusinessModelDao;
    }

    /**
     * 条件属性说明
     *
     * @param businessModelCode 业务实体代码
     * @param all               是否查询全部
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public ResponseData properties(String businessModelCode, Boolean all) throws ClassNotFoundException {
        Map<String, String> map = new HashMap<>();
        map.put("unitPrice", "单价");
        map.put("count", "数量");
        map.put("customeInt", "额外属性");
        map.put("name", "名称");
        if (all) {
            map.put("orgId", "组织机构ID");
        }
        return ResponseData.operationSuccessWithData(map);
    }


    @Override
    public ResponseData propertiesRemark(String businessModelCode) throws ClassNotFoundException {
        Map<String, String> map = new HashMap<>();
        map.put("count", "【数字】，表示订单中购买的数量");
        map.put("unitPrice", "【数字】，表示订单中单个物体的价格");
        map.put("customeInt", "【数字】，1：表示普通订单，2：表示未检测的特殊订单，3：表示检验合格的特殊订单...");
        return ResponseData.operationSuccessWithData(map);
    }


    /**
     * 条件属性初始值
     *
     * @param businessModelCode 业务实体代码
     * @return
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    @Override
    public ResponseData initPropertiesAndValues(String businessModelCode) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Map<String, Object> map = new HashMap<>();
        map.put("unitPrice", 0.0);
        map.put("count", 0);
        map.put("customeInt", 0);
        map.put("name", "中文字符串");
        return ResponseData.operationSuccessWithData(map);
    }

    /**
     * 条件属性值
     *
     * @param businessModelCode 业务实体代码
     * @param id                单据id
     * @param all
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    @Override
    public ResponseData propertiesAndValues(String businessModelCode, String id, Boolean all) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        DefaultBusinessModel bean = defaultBusinessModelDao.findOne(id);
        Map<String, Object> map = new HashMap<>();
        map.put("unitPrice", bean.getUnitPrice());
        map.put("count", bean.getCount());
        map.put("customeInt", bean.getCount());
        map.put("name", bean.getName());

        map.put("orgId", bean.getOrgId());
        map.put("orgCode", bean.getOrgCode());
        map.put("orgPath", bean.getOrgPath());
        map.put("tenantCode", bean.getTenantCode());
        map.put("workCaption", bean.getWorkCaption());
        map.put("businessCode", bean.getBusinessCode());
        map.put("id", bean.getId());
        return ResponseData.operationSuccessWithData(map);
    }


    /**
     * 重置单据状态
     *
     * @param businessModelCode 业务实体代码
     * @param id                单据id
     * @param status            状态
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    @Override
    public ResponseData resetState(String businessModelCode, String id, FlowStatus status) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        DefaultBusinessModel bean = defaultBusinessModelDao.findOne(id);
        bean.setFlowStatus(status);
        this.save(bean);
        return ResponseData.operationSuccessWithData(true);
    }

    /**
     * 数据保存操作
     */
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<DefaultBusinessModel> save(DefaultBusinessModel entity) {
        Validation.notNull(entity, "持久化对象不能为空");
        String businessCode = CodeGenerator.genCodes(6, 1).get(0);
        if (StringUtils.isEmpty(entity.getBusinessCode())) {
            entity.setBusinessCode(businessCode);
        }
        return super.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String changeCreateDepict(String id, String changeText) {
        Map<String, Object> variables = new HashMap<>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if (entity != null) {
            if (StringUtils.isNotEmpty(changeText)) {

                try {
                    JSONObject jsonObject = JSONObject.fromObject(changeText);
                    List<String> callActivtiySonPaths =  jsonObject.getJSONArray("callActivtiySonPaths");
                    if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        for (String callActivityPath : callActivtiySonPaths) {
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                                initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
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
            if (StringUtils.isNotEmpty(changeText)) {
                try {
                    JSONObject jsonObject = JSONObject.fromObject(changeText);
                    List<String> callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                    if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        for (String callActivityPath : callActivtiySonPaths) {
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                                initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
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
    public ResponseData<List<Executor>> getPersonToExecutorConfig(FlowInvokeParams flowInvokeParams) {
        String businessId = flowInvokeParams.getId();
        List<Executor> executors = new ArrayList<>();
        if (StringUtils.isNotEmpty(businessId)) {
            DefaultBusinessModel defaultBusinessModel = defaultBusinessModelDao.findOne(businessId);
            if (defaultBusinessModel != null) {
                String orgid = defaultBusinessModel.getOrgId();
                //根据组织机构ID获取员工集合
                List<Employee> employeeList = flowCommonUtil.getEmployeesByOrgId(orgid);
                List<String> idList = new ArrayList<>();
                for (Employee e : employeeList) {
                    idList.add(e.getId());
                }
                //获取执行人
                if (CollectionUtils.isEmpty(idList)) {
                    idList.add("394DE15B-F6FF-11EA-8F02-0242C0A8460D");
                    idList.add("A1206710-78AA-11EA-88E0-0242C0A84603");

                }
                //根据用户的id列表获取执行人
                executors = flowCommonUtil.getBasicUserExecutors(idList);
            }
        }
        return ResponseData.operationSuccessWithData(executors);
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

    public ResponseData newServiceCallFailure(FlowInvokeParams flowInvokeParams) {
        return ResponseData.operationFailure("测试调用模块的报错信息是否传递！");
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
     * @param callActivityPathMap
     * @param variables
     * @param parentBusinessModel
     */
    protected void initCallActivityBusiness(List<DefaultBusinessModel> defaultBusinessModelList, Map<String, String> callActivityPathMap, Map<String, Object> variables, DefaultBusinessModel parentBusinessModel) {
        IDefaultBusinessModelService defaultBusinessModelService = ApiClient.createProxy(IDefaultBusinessModelService.class);

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
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData changeCreateDepictNew(FlowInvokeParams flowInvokeParams) {
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            entity.setWorkCaption(entity.getWorkCaption() + "：后台调用的事前事件接口！");
            defaultBusinessModelDao.save(entity);
        } catch (Exception e) {
            return ResponseData.operationFailure(e.getMessage());
        }
        return ResponseData.operationSuccess();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData changeCompletedDepictNew(FlowInvokeParams flowInvokeParams) {
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            entity.setWorkCaption(entity.getWorkCaption() + "：后台调用了事后事件接口！");
            defaultBusinessModelDao.save(entity);
        } catch (Exception e) {
            return ResponseData.operationFailure(e.getMessage());
        }
        return ResponseData.operationSuccess();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData<FlowOperateResult> testReceiveCall(FlowInvokeParams flowInvokeParams) {
        Map<String, Object> variables = new HashMap<>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                String receiveTaskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
                        }
                    }
                }
                entity.setWorkCaption(entity.getWorkCaption() + ":ReceiveID:" + receiveTaskActDefId);
                defaultBusinessModelDao.save(entity);
            }
        } catch (Exception e) {
            return ResponseData.operationFailure(e.getMessage());
        }
        return ResponseData.operationSuccess();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData<FlowOperateResult> testReceiveCallNew(FlowInvokeParams flowInvokeParams) {
        String receiveTaskActDefId;
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                receiveTaskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
                        }
                    }
                }
                entity.setWorkCaption(entity.getWorkCaption() + ":ReceiveID:" + receiveTaskActDefId);
                defaultBusinessModelDao.save(entity);
                final String fReceiveTaskActDefId = receiveTaskActDefId;
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        long time = 10; //默认10秒
                        int index = 3;//重试3次
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
                                    LogUtil.error(resultTemp.getMessage());
                                }
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage(), e);
                            }
                            index--;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            return ResponseData.operationFailure(e.getMessage());
        }
        return ResponseData.operationSuccess();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData testPoolTaskSignal(FlowInvokeParams flowInvokeParams) {
        System.out.println(flowInvokeParams.getPoolTaskCode());
        String taskActDefId = null;
        Map<String, Object> variables = new HashMap<String, Object>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                taskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
                        }
                    }
                }
                entity.setWorkCaption("工作池任务：[ID:" + flowInvokeParams.getId() + ",actId:" + flowInvokeParams.getTaskActDefId() + "]");
                defaultBusinessModelDao.save(entity);
                final String fTaskActDefId = taskActDefId;


//                new Thread(new Runnable() {//模拟异步
//                    @Override
//                    public void run() {
//                        long time = 30; //默认60秒
//                        int index = 2;//重试2次
//                        while (index > 0) {
//                            try {
//                                Thread.sleep(1000 * time);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
//                                //工作池任务设置单个执行人
////                                OperateResult resultTemp = proxy.signalPoolTaskByBusinessId(flowInvokeParams.getId(), fTaskActDefId,"1592D012-A330-11E7-A967-02420B99179E" ,variables);
//                                //工作池任务设置多个执行人
//                                SignalPoolTaskVO SignalPoolTaskVO = new SignalPoolTaskVO();
//                                SignalPoolTaskVO.setBusinessId(flowInvokeParams.getId());
//                                SignalPoolTaskVO.setPoolTaskActDefId(fTaskActDefId);
//                                List<String> userIds = new ArrayList<>();
//                                userIds.add("1592D012-A330-11E7-A967-02420B99179E");
//                                userIds.add("1AE28F00-2FFC-11E9-AC2E-0242C0A84417");
//                                SignalPoolTaskVO.setUserIds(userIds);
//                                SignalPoolTaskVO.setMap(new HashMap<>());
//                                ResponseData resultTemp = proxy.signalPoolTaskByBusinessIdAndUserList(SignalPoolTaskVO);
//                                if (resultTemp.successful()) {
//                                    return;
//                                } else {
//                                    time = time * 2; //加倍
//                                }
//                            } catch (Exception e) {
//                                time = time * 2; //加倍
//                            }
//                            index--;
//                        }
//                    }
//                }).start();


                //直接返回用户ID也可以直接进行待办添加（工作池任务添加执行人方式2）
                //test123456/测试  20202020/test
                return ResponseData.operationSuccessWithData("394DE15B-F6FF-11EA-8F02-0242C0A8460D,A1206710-78AA-11EA-88E0-0242C0A84603");
            }
        } catch (Exception e) {
            return ResponseData.operationFailure(e.getMessage());
        }
        return ResponseData.operationSuccess();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowOperateResult testPoolTaskCreatePool(FlowInvokeParams flowInvokeParams) {
        System.out.println(flowInvokeParams.getPoolTaskCode());
        FlowOperateResult result = new FlowOperateResult();
        String taskActDefId;
        Map<String, Object> variables = new HashMap<>();
        try {
            DefaultBusinessModel entity = defaultBusinessModelDao.findOne(flowInvokeParams.getId());
            if (entity != null) {
                taskActDefId = flowInvokeParams.getTaskActDefId();
                List<String> callActivtiySonPaths = flowInvokeParams.getCallActivitySonPaths();
                if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
                        }
                    }
                }
                String changeText = "PoolCallPoolCreate";
                entity.setWorkCaption(entity.getWorkCaption() + ":" + changeText + ":" + taskActDefId);
                defaultBusinessModelDao.save(entity);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.setSuccess(true);
        result.setMessage("test success!");
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
                List<String> callActivtiySonPaths =  flowInvokeParams.getCallActivitySonPaths();
                if (!CollectionUtils.isEmpty(callActivtiySonPaths)) {
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    for (String callActivityPath : callActivtiySonPaths) {
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath, true);
                            initCallActivityBusiness(defaultBusinessModelList, callActivityPathMap, variables, entity);
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
                                }
                            } catch (Exception e) {
                                time = time * 4; //加倍
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
