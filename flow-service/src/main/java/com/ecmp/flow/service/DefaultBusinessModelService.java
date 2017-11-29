package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.util.NumberGenerator;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.Validation;
import com.ecmp.flow.api.*;
import com.ecmp.flow.basic.vo.Employee;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.DefaultBusinessModelDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.GenericType;
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
public class DefaultBusinessModelService extends BaseEntityService<DefaultBusinessModel> implements IDefaultBusinessModelService{

    private final Logger logger = LoggerFactory.getLogger(DefaultBusinessModelService.class);

    @Autowired
    private DefaultBusinessModelDao defaultBusinessModelDao;

    protected BaseEntityDao<DefaultBusinessModel> getDao(){
        return this.defaultBusinessModelDao;
    }

    /**
     * 数据保存操作
     */
    @SuppressWarnings("unchecked")
    @Transactional( propagation= Propagation.REQUIRED)
    public OperateResultWithData<DefaultBusinessModel> save(DefaultBusinessModel entity) {
        Validation.notNull(entity, "持久化对象不能为空");
        String businessCode = NumberGenerator.getNumber(DefaultBusinessModel.class);
//        String businessCode = CodeGenerator.genCodes(6,1).get(0);
        if(StringUtils.isEmpty(entity.getBusinessCode())){
            entity.setBusinessCode(businessCode);
        }
       return super.save(entity);
    }

    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public String changeCreateDepict(String id,String changeText){
        Map<String,Object> variables = new HashMap<String,Object>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if(entity != null){
//            defaultBusinessModelDao.saveAndFlush(entity);
            if(StringUtils.isNotEmpty(changeText)){

                try{
                    JSONObject jsonObject = JSONObject.fromObject(changeText);
                    List<String> callActivtiySonPaths = null;
                    callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                    if(callActivtiySonPaths!=null && !callActivtiySonPaths.isEmpty()){
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                        List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                        for(String callActivityPath:callActivtiySonPaths){
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);
                                initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
            changeText="before";
            entity.setWorkCaption(changeText+":"+entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
        }
        String param = JsonUtils.toJson(variables);
        return param;
    }

    /**
     *
     * @param id  业务单据id
     * @param changeText   参数文本
     * @return
     */
    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public String changeCompletedDepict(String id,String changeText){
        Map<String,Object> variables = new HashMap<String,Object>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if(entity != null){
//            defaultBusinessModelDao.saveAndFlush(entity);
            if(StringUtils.isNotEmpty(changeText)){

                try{
                    JSONObject jsonObject = JSONObject.fromObject(changeText);
                    List<String> callActivtiySonPaths = null;
                    callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                    if(callActivtiySonPaths!=null && !callActivtiySonPaths.isEmpty()){
                        //测试跨业务实体子流程,并发多级子流程测试
                        List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                        List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                        List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                        for(String callActivityPath:callActivtiySonPaths){
                            if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                                Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);
                                initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                            }
                        }
                    }
                }catch (Exception e){
                    logger.error(e.getMessage());
                }

            }
            changeText="after";
            entity.setWorkCaption(changeText+":"+entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
        }
        String param = JsonUtils.toJson(variables);
        return param;
    }

    /**
     *
     * @param businessId  业务单据id
     * @param paramJson  参数json
     * @return
     */
    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public List<Executor> getPersonToExecutorConfig(String businessId,String paramJson){
        List<Executor> result = new ArrayList<Executor>();
        if(StringUtils.isNotEmpty(businessId)){
            DefaultBusinessModel defaultBusinessModel = defaultBusinessModelDao.findOne(businessId);
            if(defaultBusinessModel!=null){
                String orgid = defaultBusinessModel.getOrgId();
//                IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
//                //获取市场部所有人员
//                List<Employee> employeeList   = proxy.findByOrganizationId(orgid);
                Map<String,Object> params = new HashMap();
                params.put("organizationId",orgid);
                String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL;
                List<Employee> employeeList=ApiClient.getEntityViaProxy(url,new GenericType<List<Employee>>() {},params);
                List<String> idList = new ArrayList<String>();
                for(Employee e : employeeList){
                    idList.add(e.getId());
                }
                //获取执行人
//                result = proxy.getExecutorsByEmployeeIds(idList);
                Map<String,Object> paramsV2 = new HashMap();
                paramsV2.put("employeeIds",idList);
                url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
                result = ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},paramsV2);
            }
        }
        return result;
    }


    /**
     *
     * @param id  业务单据id
     * @param changeText   参数文本
     * @return
     */
    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public boolean testReceiveCall(String id,String changeText){
        boolean result = false;
      String receiveTaskActDefId = null;
      Map<String,Object> variables = new HashMap<String,Object>();

        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if(entity != null){
            if(StringUtils.isNotEmpty(changeText)){
                JSONObject jsonObject = JSONObject.fromObject(changeText);
                //HashMap<String,Object> params =   JsonUtils.fromJson(changeText, new TypeReference<HashMap<String,Object>>() {});
                receiveTaskActDefId = jsonObject.get("receiveTaskActDefId")+"";
                List<String> callActivtiySonPaths = null;
                try{
                    callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
                if(callActivtiySonPaths!=null && !callActivtiySonPaths.isEmpty()){
                    //测试跨业务实体子流程,并发多级子流程测试
                    List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
                    List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
                    List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
                    for(String callActivityPath:callActivtiySonPaths){
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, entity);
                        }
                    }
                }
            }
            changeText="ReceiveCall";
            entity.setWorkCaption(entity.getWorkCaption()+":"+changeText);
            defaultBusinessModelDao.save(entity);
            final String  fReceiveTaskActDefId = receiveTaskActDefId;
            new Thread(new Runnable() {//模拟异步
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000*20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
                    proxy.signalByBusinessId(id,fReceiveTaskActDefId,variables);
                }
            }).start();

            result = true;
        }
        return result;
    }

    public boolean checkStartFlow(String id){
        return true;
    }

    public void endCall(String id){
        System.out.println("id="+id);
    }

    /**
     * 解析子流程绝对路径
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
            if(!ifStart){
                break;//只生成一条测试数据
            }
        }
        return resultMap;
    }

    public FlowOperateResult newServiceCall(FlowInvokeParams flowInvokeParams){
        FlowOperateResult result = new FlowOperateResult();
        String businessId = flowInvokeParams.getId();
        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(businessId);
        String changeText = "newServiceCall";
        entity.setWorkCaption(changeText+":"+entity.getWorkCaption());
        defaultBusinessModelDao.save(entity);
        int shujishu = this.getShuiJiShu(0,10);
        if(shujishu==5){
            throw  new RuntimeException("测试随机抛出错误信息:"+new Date());
        }else if(shujishu ==4){
            result.setSuccess(false);
            result.setMessage("测试随机业务异常信息:"+new Date());
        }
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
                defaultBusinessModel.setWorkCaption(parentBusinessModel.getWorkCaption()+"||"+name);
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
                defaultBusinessModel2Son.setWorkCaption(parentBusinessModel.getWorkCaption()+"||"+name);
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
                defaultBusinessModel3Son.setWorkCaption(parentBusinessModel.getWorkCaption()+"||"+name);
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
}
