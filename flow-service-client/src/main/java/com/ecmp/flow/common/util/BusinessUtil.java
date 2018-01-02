package com.ecmp.flow.common.util;

import com.ecmp.annotation.Remark;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.constant.ConditionAnnotaion;
import com.ecmp.flow.vo.BusinessFormValue;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：业务单据工具方法
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/12/27 16:35      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class BusinessUtil {

    public static Map<String, Object> getPropertiesAndValues(Object conditionPojo, String[] excludeProperties)
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,NoSuchMethodException{
        Map<String, Object> result = null;
        Map<String, List> tempMap = new HashMap<String, List>();
        // String[] excludeProperties = { "class", "pk", "equalFlag" };//
        // 不用包括在内的字段
        if (conditionPojo != null) {
            Class sourceClass = conditionPojo.getClass();
//            //额外属性值初始化
//            Method customLogicMethod = sourceClass.getMethod("customLogic");
//            customLogicMethod.invoke(conditionPojo);
            Method[] sourceMethods = sourceClass.getMethods();// 得到某类的所有公共方法，包括父类
            result = new LinkedHashMap<String, Object>();
            for (Method sourceMethod : sourceMethods) {
                Remark annotation = sourceMethod.getAnnotation(Remark.class);
//                String sourceFieldName = getFieldName(sourceMethod, excludeProperties);
                if (annotation == null) {
                    continue;
                }
                String key = annotation.value();
                int  rank = annotation.rank();
                boolean hasSon = false;
                String sourceFieldNameDes = ContextUtil.getMessage(key);
                if(StringUtils.isEmpty(sourceFieldNameDes)){
                    throw new RuntimeException("sourceFieldName's Internationalization can not find! key = "+ key);
                }
                Object v = sourceMethod.invoke(conditionPojo,  null);
                if(v!=null){
                    List tempResult = new ArrayList();
                    BusinessFormValue businessFormValue = new BusinessFormValue();
                   if(ifBaseType(v)){
                   }else if(!ifListOrMapType(v)){
                      v = getPropertiesAndValues(v,null);
                      hasSon = true;
                   }else{
                       throw new RuntimeException("v'type can not support,type = "+ v.getClass());
                   }
                   if(v!=null) {
                       businessFormValue.setValue(v);
                       businessFormValue.setHasSon(hasSon);
                       tempResult.add(businessFormValue);
                       tempResult.add(rank);
//                       tempResult.add(rank);
                       tempMap.put(sourceFieldNameDes, tempResult);
                   }
                }
            }

            // 将Map里面的所以元素取出来先变成一个set，然后将这个set装到一个list里面
            List<Map.Entry<String, List>> list = new ArrayList<Map.Entry<String, List>>(tempMap.entrySet());
            // 定义一个comparator
            Comparator<Map.Entry<String, List>> comparator = new Comparator<Map.Entry<String, List>>() {
                @Override
                public int compare(Map.Entry<String, List> p1, Map.Entry<String, List> p2) {
                    // 之所以使用减号，是想要按照数从高到低来排列
                    return -((int)p1.getValue().get(1)- (int)p2.getValue().get(1));
                }
            };
            Collections.sort(list, comparator);
            for (Map.Entry<String, List> entry : list) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return result;
    }

    public static boolean ifBaseType(Object param){
        boolean result = false;
        if (param instanceof Integer || param instanceof String
        || param instanceof Double || param instanceof Float
        || param instanceof Long || param instanceof Boolean
        || param instanceof Date) {
            result = true;
        }
        return result;
    }

    public static boolean ifListOrMapType(Object param){
        boolean result = false;
        if (param instanceof Map || param instanceof Collection
               ) {
            result = true;
        }
        return result;
    }
}
