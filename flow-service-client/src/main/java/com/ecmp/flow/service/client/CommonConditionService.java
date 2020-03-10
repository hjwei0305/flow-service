package com.ecmp.flow.service.client;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.entity.BaseEntity;
import com.ecmp.flow.api.client.util.ExpressionUtil;
import com.ecmp.flow.clientapi.ICommonConditionService;
import com.ecmp.flow.common.util.BusinessUtil;
import com.ecmp.flow.constant.BusinessEntityAnnotaion;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.IBusinessFlowEntity;
import com.ecmp.flow.entity.IConditionPojo;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
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


    public CommonConditionService() {
    }

    private Map<String, String> getPropertiesForConditionPojo(String conditonPojoClassName,Boolean all) throws ClassNotFoundException {
        return ExpressionUtil.getProperties(conditonPojoClassName,all);
    }


    private Map<String, Object> getPropertiesAndValues(String conditonPojoClassName,Boolean all) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return ExpressionUtil.getPropertiesAndValues(conditonPojoClassName,all);
    }


    private Map<String, Object> getConditonPojoMap(String conditonPojoClassName, String daoBeanName, String id,Boolean all) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        Class conditonPojoClass = Class.forName(conditonPojoClassName);
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao) applicationContext.getBean(daoBeanName);
        IConditionPojo conditionPojo = (IConditionPojo) conditonPojoClass.newInstance();
        BaseEntity content = (BaseEntity) appModuleDao.findOne(id);
        BeanUtils.copyProperties(conditionPojo, content);
        if (conditionPojo != null) {
            return new ExpressionUtil<IConditionPojo>().getPropertiesAndValues(conditionPojo,all);
        } else {
            return null;
        }
    }


    @Override
    public ResponseData<Map<String, String>> properties(String businessModelCode,Boolean all) throws ClassNotFoundException {
        String conditonPojoClassName = null;
        conditonPojoClassName = getConditionBeanName(businessModelCode);
        Map<String, String> map = this.getPropertiesForConditionPojo(conditonPojoClassName,all);
        return    ResponseData.operationSuccessWithData(map);
    }
    public Map<String, String> propertiesAll(String businessModelCode) throws ClassNotFoundException {
        String conditonPojoClassName = null;
        conditonPojoClassName = getConditionBeanName(businessModelCode);
        return this.getPropertiesForConditionPojo(conditonPojoClassName,true);
    }

    @Override
    public ResponseData<Map<String, Object>> initPropertiesAndValues(String businessModelCode) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String conditonPojoClassName = null;
        conditonPojoClassName = getConditionBeanName(businessModelCode);
        Map<String, Object> map = this.getPropertiesAndValues(conditonPojoClassName,true);
        return ResponseData.operationSuccessWithData(map);
    }

    @Override
    public ResponseData<Map<String, Object>> propertiesAndValues(String businessModelCode, String id,Boolean all) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        String conditonPojoClassName = null;
        String daoBeanName = null;
        conditonPojoClassName = getConditionBeanName(businessModelCode);
        daoBeanName = getDaoBeanName(businessModelCode);
        Map<String, Object> map = this.getConditonPojoMap(conditonPojoClassName, daoBeanName, id,all);
        return ResponseData.operationSuccessWithData(map);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData<Boolean> resetState(String businessModelCode, String id, FlowStatus status) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        String daoBeanName = null;
        daoBeanName = getDaoBeanName(businessModelCode);
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao) applicationContext.getBean(daoBeanName);
        IBusinessFlowEntity content = (IBusinessFlowEntity) appModuleDao.findOne(id);
        if(status==FlowStatus.INIT){//针对流程强制终止时，表单已经被删除的情况
            if(content!=null){
                content.setFlowStatus(status);
                appModuleDao.save(content);
            }
        }else{
          if(content==null){
             throw new RuntimeException("business.id do not exist, can not start or complete the process!");
           }
            content.setFlowStatus(status);
            appModuleDao.save(content);
        }
        return  ResponseData.operationSuccessWithData(true);
    }

    public ResponseData<Map<String,Object>> businessPropertiesAndValues(String businessModelCode,String id) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,NoSuchMethodException{
        String daoBeanName = null;
        daoBeanName = getDaoBeanName(businessModelCode);
        ApplicationContext applicationContext = ContextUtil.getApplicationContext();
        BaseDao appModuleDao = (BaseDao) applicationContext.getBean(daoBeanName);
        IBusinessFlowEntity content = (IBusinessFlowEntity) appModuleDao.findOne(id);
        Map<String,Object> map = BusinessUtil.getPropertiesAndValues(content,null);
        return   ResponseData.operationSuccessWithData(map);
    }
    private String getDaoBeanName(String className)throws ClassNotFoundException {
        BusinessEntityAnnotaion businessEntityAnnotaion = this.getBusinessEntityAnnotaion(className);
         return   businessEntityAnnotaion.daoBean();
    }
    private String getConditionBeanName(String className)throws ClassNotFoundException {
        BusinessEntityAnnotaion businessEntityAnnotaion = this.getBusinessEntityAnnotaion(className);
        return   businessEntityAnnotaion.conditionBean();
    }
    private BusinessEntityAnnotaion getBusinessEntityAnnotaion(String className)throws ClassNotFoundException {
        if (StringUtils.isNotEmpty(className)) {
            Class sourceClass = Class.forName(className);
            BusinessEntityAnnotaion businessEntityAnnotaion = (BusinessEntityAnnotaion) sourceClass.getAnnotation(BusinessEntityAnnotaion.class);
            return  businessEntityAnnotaion;
        }else {
            throw new RuntimeException("className is null!");
        }
    }

    public ResponseData<String> pushTasksToDo(List<FlowTask> list){
        List<String> megList = new ArrayList<String>();
        if(list!=null&&list.size()>0){
            list.forEach(a->megList.add("【是否已处理："+a.getTaskStatus()+"-id="+a.getId()+"】"));
        }
        LogUtil.bizLog("推动待办成功到达："+ JsonUtils.toJson(megList));
        return ResponseData.operationSuccessWithData("推送成功！");
    }


}
