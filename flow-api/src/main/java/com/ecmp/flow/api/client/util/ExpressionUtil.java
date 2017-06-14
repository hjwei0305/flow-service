package com.ecmp.flow.api.client.util;

import com.ecmp.flow.constant.ConditionAnnotaion;
import com.ecmp.flow.entity.IConditionPojo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 表达式Entity相关工具方法
 * 
 * @author tanjun
 *
 * @param <T>
 */
public class ExpressionUtil<T extends IConditionPojo> {
	/**
	 * 通过条件Entity获取属性信息
	 * 
	 * @param object
	 *            entity对象
	 * @return 结果
	 * @throws ClassNotFoundException
	 */
	public LinkedHashMap<String, String> getProperties(T object) throws ClassNotFoundException {
		LinkedHashMap<String, String> result = null;
		if (object != null) {
			Class sourceClass = object.getClass();
			String className = sourceClass.getName();
			result = getProperties(className, null);
		}
		return result;
	}

	/**
	 * 通过条件Entity获取属性信息
	 * 
	 * @param object
	 *            entity对象
	 * @param excludeProperties
	 *            排除字段
	 * @return 结果
	 * @throws ClassNotFoundException
	 */
	public LinkedHashMap<String, String> getProperties(T object, String[] excludeProperties)
			throws ClassNotFoundException {
		LinkedHashMap<String, String> result = null;
		if (object != null) {
			Class sourceClass = object.getClass();
			String className = sourceClass.getName();
			result = getProperties(className, excludeProperties);
		}
		return result;
	}

	/**
	 * 获取条件pojo属性、属性说明Map(有序)
	 * 
	 * @param className
	 *            类名称（全路径）
	 * @param excludeProperties
	 *            需要排除的字段
	 * @return pojo属性、属性说明Map
	 * @throws ClassNotFoundException
	 *             异常信息
	 */
	public static LinkedHashMap<String, String> getProperties(String className, String[] excludeProperties)
			throws ClassNotFoundException {
		LinkedHashMap<String, String> result = null;
		Map<String, Object[]> tempMap = new HashMap<String, Object[]>();
		// String[] excludeProperties = { "class", "pk", "equalFlag" };//
		// 不用包括在内的字段
		if (className != null) {
			Class sourceClass = Class.forName(className);
			Method[] sourceMethods = sourceClass.getMethods();// 得到某类的所有公共方法，包括父类
			result = new LinkedHashMap<String, String>();
			for (Method sourceMethod : sourceMethods) {
				ConditionAnnotaion conditionAnnatation = sourceMethod.getAnnotation(ConditionAnnotaion.class);
				String sourceFieldName = getFieldName(sourceMethod, excludeProperties);
				if (conditionAnnatation == null || sourceFieldName == null || "".equals(sourceFieldName) || conditionAnnatation.canSee()==false) {
					continue;
				}
				String annationName = conditionAnnatation.name();
				int rank = conditionAnnatation.rank();
				Object[] tempResult = { annationName, rank };
				tempMap.put(sourceFieldName, tempResult);
			}
			// 将Map里面的所以元素取出来先变成一个set，然后将这个set装到一个list里面
			List<Entry<String, Object[]>> list = new ArrayList<Entry<String, Object[]>>(tempMap.entrySet());
			// 定义一个comparator
			Comparator<Entry<String, Object[]>> comparator = new Comparator<Entry<String, Object[]>>() {
				@Override
				public int compare(Entry<String, Object[]> p1, Entry<String, Object[]> p2) {
					// 之所以使用减号，是想要按照数从高到低来排列
					return -((int) p1.getValue()[1] - (int) p2.getValue()[1]);
				}
			};
			Collections.sort(list, comparator);
			for (Entry<String, Object[]> entry : list) {
				result.put(entry.getKey(), (String) entry.getValue()[0]);
			}
		}
 
		return result;
	}

	public static LinkedHashMap<String, String> getProperties(String className) throws ClassNotFoundException {
		return getProperties(className, null);

	}
	
	/**
	 * 获取条件pojo属性、属性说明Map(有序)
	 *
	 * @param conditionPojo
	 *            对象
	 * @return pojo属性、属性说明Map
	 * @throws ClassNotFoundException
	 *             异常信息
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public  Map<String, Object> getPropertiesAndValues(T conditionPojo) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,NoSuchMethodException{
		return this.getPropertiesAndValues(conditionPojo, null);
	}

	/**
	 * 获取条件pojo属性、属性说明Map(有序)
	 * 
	 * @param conditionPojo
	 *            对象
	 * @param excludeProperties
	 *            需要排除的字段
	 * @return pojo属性、属性说明Map
	 * @throws ClassNotFoundException
	 *             异常信息
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public  Map<String, Object> getPropertiesAndValues(T conditionPojo, String[] excludeProperties)
			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException ,NoSuchMethodException{
		Map<String, Object> result = null;
		// String[] excludeProperties = { "class", "pk", "equalFlag" };//
		// 不用包括在内的字段
		if (conditionPojo != null) {
			Class sourceClass = conditionPojo.getClass();
			//额外属性值初始化
			Method customLogicMethod = sourceClass.getMethod("customLogic");
			customLogicMethod.invoke(conditionPojo);

			Method[] sourceMethods = sourceClass.getMethods();// 得到某类的所有公共方法，包括父类
			result = new HashMap<String, Object>();
			for (Method sourceMethod : sourceMethods) {		
				
				ConditionAnnotaion conditionAnnatation = sourceMethod.getAnnotation(ConditionAnnotaion.class);
				String sourceFieldName = getFieldName(sourceMethod, excludeProperties);
				if (conditionAnnatation == null || sourceFieldName == null || "".equals(sourceFieldName)) {
					continue;
				}
				 Object v = sourceMethod.invoke(conditionPojo, null);
				 if(v!=null){
					 result.put(sourceFieldName, v);
				 }
				
			}

		} 
		return result;
	}
	
	
	/**
	 * 获取条件pojo属性、属性说明Map(有序)
	 * 
	 * @param className
	 *            类名称（全路径）
	 * @return pojo属性、属性说明Map
	 * @throws ClassNotFoundException
	 *             异常信息
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public  static Map<String, Object> getPropertiesAndValues(String className)     throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		return getPropertiesAndValues(className, null);
	}
	

	/**
	 * 获取条件pojo属性、属性说明Map(有序)
	 * 
	 * @param className
	 *            类名称（全路径）
	 * @param excludeProperties
	 *            需要排除的字段
	 * @return pojo属性、属性说明Map
	 * @throws ClassNotFoundException
	 *             异常信息
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public  static Map<String, Object> getPropertiesAndValues(String className, String[] excludeProperties)
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		Map<String, Object> result = null;
		// String[] excludeProperties = { "class", "pk", "equalFlag" };//
		// 不用包括在内的字段
		if (className != null) {
			Class sourceClass = Class.forName(className);
			Object conditionPojo = sourceClass.newInstance();
			Method initMethod = sourceClass.getMethod("init");
            initMethod.invoke(conditionPojo);
			Method[] sourceMethods = sourceClass.getMethods();// 得到某类的所有公共方法，包括父类
			result = new HashMap<String, Object>();
			for (Method sourceMethod : sourceMethods) {						
				ConditionAnnotaion conditionAnnatation = sourceMethod.getAnnotation(ConditionAnnotaion.class);
				String sourceFieldName = getFieldName(sourceMethod, excludeProperties);
				if (conditionAnnatation == null || sourceFieldName == null || "".equals(sourceFieldName)) {
					continue;
				}
				 Object v = sourceMethod.invoke(conditionPojo);
				 if(v!=null){
					 result.put(sourceFieldName, v);
				 }
			}

		} 
		return result;
	}
	
	

	// 判断拥有get方法或者is方法的字段
	private static String getGetANDIsFieldName(Method method) {
		String methodName = method.getName();
		// 取得方法的名称，判断方法名称是否以get或者is开头！
		int prefixLength;
		if (methodName.startsWith("get")) {
			prefixLength = 3;
		} else if (methodName.startsWith("is")) {
			prefixLength = 2;
		} else {
			return null;
		}
		String fieldName = methodName.substring(prefixLength);
		fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
		return fieldName;
	}

	private static String getFieldName(Method method, String[] excludeProperties) {

		String fieldName = getGetANDIsFieldName(method);
		if (fieldName == null || fieldName.equals("class"))// 去除class字段
			return null;
		if (excludeProperties != null) {
			for (String s : excludeProperties) {
				if (fieldName.equals(s))
					return null;
			}
		}

		return fieldName;
	}

//	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
////		String className = "com.ecmp.flow.common.PojoTest1";
////		LinkedHashMap<String, String> linkedHashMap = ExpressionUtil.getProperties(className);
////		for (Map.Entry<String, String> entry : linkedHashMap.entrySet()) {
////			System.out.println(entry.getKey() + ":" + entry.getValue());
////		}
//
//	    PojoTest1 p = new PojoTest1();
//	    p.setId("xcvsdfsd");
//	    p.setDate(new Date());
//
//
////		LinkedHashMap<String, Object> linkedHashMap =new ExpressionUtil<ConditionPojo>().getPropertiesAndValues(p,null);
//		LinkedHashMap<String, Object> linkedHashMap = getPropertiesAndValues(p.getClass().getName(),null);
//		for (Entry<String, Object> entry : linkedHashMap.entrySet()) {
//			System.out.println(entry.getKey() + ":" + entry.getValue());
//		}
//	}
}
