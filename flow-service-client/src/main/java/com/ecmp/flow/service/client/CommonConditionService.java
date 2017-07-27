package com.ecmp.flow.service.client;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.entity.BaseEntity;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.api.client.util.ExpressionUtil;
import com.ecmp.flow.clientapi.ICommonConditionService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.IBusinessFlowEntity;
import com.ecmp.flow.entity.IConditionPojo;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：通用客户端条件表达式服务
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/07/20 13:22      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class CommonConditionService implements ICommonConditionService {

    private IBusinessModelService businessModelService;
    public CommonConditionService(){
    }

    @Override
    public Map<String, String> getPropertiesForConditionPojo(String conditonPojoClassName) throws ClassNotFoundException {
        return ExpressionUtil.getProperties(conditonPojoClassName);
    }

    @Override
    public Map<String, Object> getPropertiesAndValues(String conditonPojoClassName) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException{
        return ExpressionUtil.getPropertiesAndValues(conditonPojoClassName);
    }

    @Override
    public Map<String,Object> getConditonPojoMap(String conditonPojoClassName, String daoBeanName,String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        Class conditonPojoClass = Class.forName(conditonPojoClassName);
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao)applicationContext.getBean(daoBeanName);
        IConditionPojo conditionPojo = (IConditionPojo)conditonPojoClass.newInstance();
        BaseEntity content = (BaseEntity)appModuleDao.findOne(id);
        BeanUtils.copyProperties(conditionPojo,content);
        if(conditionPojo != null){
            return new ExpressionUtil<IConditionPojo>().getPropertiesAndValues(conditionPojo);
        }else {
            return  null;
        }
    }


    @Override
    public Map<String, String> getPropertiesForConditionPojoByBusinessModelId(String businessModelId) throws ClassNotFoundException {
       String conditonPojoClassName = null;
       businessModelService = ApiClient.createProxy(IBusinessModelService.class);
       BusinessModel businessModel = businessModelService.findOne(businessModelId);
       if(businessModel != null){
           conditonPojoClassName = businessModel.getConditonBean();
       }
        return this.getPropertiesForConditionPojo(conditonPojoClassName);
    }

    @Override
    public Map<String, Object> getPropertiesAndValuesByBusinessModelId(String businessModelId) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException{
        String conditonPojoClassName = null;
        businessModelService = ApiClient.createProxy(IBusinessModelService.class);
        BusinessModel businessModel = businessModelService.findOne(businessModelId);
        if(businessModel != null){
            conditonPojoClassName = businessModel.getConditonBean();
        }
        return this.getPropertiesAndValues(conditonPojoClassName);
    }

    @Override
    public Map<String,Object> getConditonPojoMapByBusinessModelId(String businessModelId,String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        String conditonPojoClassName = null;
        String daoBeanName = null;
        businessModelService = ApiClient.createProxy(IBusinessModelService.class);
        BusinessModel businessModel = businessModelService.findOne(businessModelId);
        if(businessModel != null){
            conditonPojoClassName = businessModel.getConditonBean();
            daoBeanName = businessModel.getDaoBean();
        }
        return this.getConditonPojoMap( conditonPojoClassName,  daoBeanName, id);
    }


    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public Boolean resetState(String businessModelId,String id,FlowStatus status) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        businessModelService = ApiClient.createProxy(IBusinessModelService.class);
        BusinessModel businessModel = businessModelService.findOne(businessModelId);
        String   daoBeanName = null;
        if(businessModel != null){
          daoBeanName = businessModel.getDaoBean();
        }
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao)applicationContext.getBean(daoBeanName);
        IBusinessFlowEntity content = (IBusinessFlowEntity)appModuleDao.findOne(id);
        content.setFlowStatus(status);
        appModuleDao.save(content);
        return true;
    }
}
