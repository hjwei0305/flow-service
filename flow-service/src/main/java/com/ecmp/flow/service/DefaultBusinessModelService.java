package com.ecmp.flow.service;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.entity.Employee;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.config.util.NumberGenerator;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.Validation;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.dao.DefaultBusinessModelDao;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.util.CodeGenerator;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.type.TypeReference;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.redisson.misc.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public OperateResultWithData<DefaultBusinessModel> save(DefaultBusinessModel entity) {
        Validation.notNull(entity, "持久化对象不能为空");
        String businessCode = NumberGenerator.GetNumber(DefaultBusinessModel.class);
//        String businessCode = CodeGenerator.genCodes(6,1).get(0);
        if(StringUtils.isEmpty(entity.getBusinessCode())){
            entity.setBusinessCode(businessCode);
        }
       return super.save(entity);
    }

    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public boolean changeCreateDepict(String id,String changeText){
        boolean result = false;
        HashMap<String,String> params=null;
//        if(StringUtils.isNotEmpty(changeText)){
//            params =   JsonUtils.fromJson(changeText, new TypeReference<HashMap<String,Object>>() {});
//        }
        changeText="before";
        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if(entity != null){
            entity.setWorkCaption(changeText+":"+entity.getWorkCaption());
            defaultBusinessModelDao.save(entity);
//            defaultBusinessModelDao.saveAndFlush(entity);
            result = true;
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
    public boolean changeCompletedDepict(String id,String changeText){
        boolean result = false;
//        if(StringUtils.isNotEmpty(changeText)){
//            HashMap<String,String> params =   JsonUtils.fromJson(changeText, new TypeReference<HashMap<String,String>>() {});
//        }
        changeText="after";
        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if(entity != null){
            entity.setWorkCaption(entity.getWorkCaption()+":"+changeText);
            defaultBusinessModelDao.save(entity);
//            defaultBusinessModelDao.saveAndFlush(entity);
            result = true;
        }
        return result;
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
                IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
                //获取市场部所有人员
                List<Employee> employeeList   = proxy.findByOrganizationId(orgid);
                List<String> idList = new ArrayList<String>();
                for(Employee e : employeeList){
                    idList.add(e.getId());
                }
                //获取执行人
                result = proxy.getExecutorsByEmployeeIds(idList);
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
        if(StringUtils.isNotEmpty(changeText)){
            JSONObject jsonObject = JSONObject.fromObject(changeText);
            //HashMap<String,Object> params =   JsonUtils.fromJson(changeText, new TypeReference<HashMap<String,Object>>() {});
            receiveTaskActDefId = jsonObject.get("receiveTaskActDefId")+"";
        }
        changeText="ReceiveCall";
        DefaultBusinessModel entity = defaultBusinessModelDao.findOne(id);
        if(entity != null){
            entity.setWorkCaption(entity.getWorkCaption()+":"+changeText);
            defaultBusinessModelDao.save(entity);
            final String  fReceiveTaskActDefId = receiveTaskActDefId;
            new Thread(new Runnable() {//模拟异步
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000*10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
                    proxy.signalByBusinessId(id,fReceiveTaskActDefId,null);
                }
            }).start();

            result = true;
        }
        return result;
    }
}
