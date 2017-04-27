package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.api.client.util.ExpressionUtil;
import com.ecmp.flow.entity.IConditionPojo;
import com.ecmp.flow.api.common.api.IConditionServer;
import org.apache.commons.beanutils.BeanUtils;
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
    @Override
    public Map<String, String> getPropertiesForConditionPojo(String conditonPojoClassName) throws ClassNotFoundException {
        return ExpressionUtil.getProperties(conditonPojoClassName);
    }

    @Override
    public Map<String, Object> getPropertiesAndValues(String conditonPojoClassName) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return ExpressionUtil.getPropertiesAndValues(conditonPojoClassName);
    }

    @Override
    public Map<String,Object> getConditonPojoMap(String conditonPojoClassName, String daoBeanName,String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        Class conditonPojoClass = Class.forName(conditonPojoClassName);
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao)applicationContext.getBean(daoBeanName);
        IConditionPojo conditionPojo = (IConditionPojo)conditonPojoClass.newInstance();
        IConditionPojo content = (IConditionPojo)appModuleDao.findOne(id);
        BeanUtils.copyProperties(conditionPojo,content);
        if(content!=null){
            return new ExpressionUtil<IConditionPojo>().getPropertiesAndValues(conditionPojo);
        }else {
            return  null;
        }
//        return  (IConditionPojo)method.invoke(appModuleDao,id);
    }
}
