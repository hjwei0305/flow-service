package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.entity.BaseEntity;
import com.ecmp.flow.api.client.util.ExpressionUtil;
import com.ecmp.flow.dao.BusinessModelDao;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.IConditionPojo;
import com.ecmp.flow.api.common.api.IConditionServer;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/27 13:22      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ConditionServer   implements IConditionServer {

    @Autowired
    private BusinessModelDao businessModelDao;

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
//        return  (IConditionPojo)method.invoke(appModuleDao,id);
    }


    @Override
    public Map<String, String> getPropertiesForConditionPojoByBusinessModelId(String businessModelId) throws ClassNotFoundException {
       String conditonPojoClassName = null;
       BusinessModel businessModel = businessModelDao.findOne(businessModelId);
       if(businessModel != null){
           conditonPojoClassName = businessModel.getConditonBean();
       }
        return this.getPropertiesForConditionPojo(conditonPojoClassName);
    }

    @Override
    public Map<String, Object> getPropertiesAndValuesByBusinessModelId(String businessModelId) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException{
        String conditonPojoClassName = null;
        BusinessModel businessModel = businessModelDao.findOne(businessModelId);
        if(businessModel != null){
            conditonPojoClassName = businessModel.getConditonBean();
        }
        return this.getPropertiesAndValues(conditonPojoClassName);
    }

    @Override
    public Map<String,Object> getConditonPojoMapByBusinessModelId(String businessModelId,String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        String conditonPojoClassName = null;
        String daoBeanName = null;
        BusinessModel businessModel = businessModelDao.findOne(businessModelId);
        if(businessModel != null){
            conditonPojoClassName = businessModel.getConditonBean();
            daoBeanName = businessModel.getDaoBean();
        }
        return this.getConditonPojoMap( conditonPojoClassName,  daoBeanName, id);
    }
}
