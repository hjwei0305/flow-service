package com.ecmp.flow.util;

import java.util.Map;

/**
 * <p>
 * *************************************************************************************************
 * </p><p>
 * 实现功能：系统级静态常量,可通过framework.properties初始化,同时保持常量static & final的特征.
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 版本          变更时间             变更人                     变更原因
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)                新建
 * </p><p>
 * *************************************************************************************************
 * </p>
 */
public class Constants extends ConfigurableContants {
	public  static String BASICURL = null;

	// 静态初始化读入framework.properties中的设置
	static {
		init("framework.properties");
		Map<String, String> map = System.getenv();
		BASICURL = map.get("ECMP_BASIC");
	}

	/**
	 * 从framework.properties中读取basic.org.listAllOrgs的值，
	 * 如果配置文件不存在或配置文件中不存在该值时，默认取值"/organization/findOrgTreeWithoutFrozen"
	 * 获取所有组织机构列表的服务地址
	 */
	public final static String BASIC_ORG_LISTALLORGS_URL= BASICURL+getProperty(
			"basic.org.listAllOrgs", "/organization/findOrgTreeWithoutFrozen");

	/**
	 * 获取指定id的组织机构
	 */
	public final static String BASIC_ORG_FINDONE_URL= BASICURL+getProperty(
			"basic.org.findOne", "/organization/findOne");

	/**
	 * 获取岗位列表
	 */
	public final static String BASIC_POSITION_FINDBYPAGE_URL= BASICURL+getProperty(
			"basic.position.findByPage", "/position/findByPage");

	/**
	 * 获取指定id岗位
	 */
	public final static String BASIC_POSITION_FINDONE_URL= BASICURL+getProperty(
			"basic.position.findOne", "/position/findOne");

	/**
	 * 根据岗位id列表获取岗位
	 */
	public final static String BASIC_POSITION_FINDBYIDS_URL= BASICURL+getProperty(
					"basic.position.findByIds", "/position/findByIds");

	/**
	 * 根据岗位的id列表获取执行人
	 */
	public final static String BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL= BASICURL+getProperty(
			"basic.position.getExecutorsByPositionIds", "/position/getExecutorsByPositionIds");

	/**
	 * 根据岗位类别的id列表获取执行人
	 */
	public final static String BASIC_POSITION_GETEXECUTORSBYPOSCATEIDS_URL= BASICURL+getProperty(
			"basic.position.getExecutorsByPosCateIds", "/position/getExecutorsByPosCateIds");

	/**
	 * 获取岗位类别列表
	 */
	public final static String BASIC_POSITIONCATEGORY_FINDBYPAGE_URL= BASICURL+getProperty(
			"basic.positionCategory.findByPage", "/positionCategory/findByPage");

	/**
	 * 获取指定id岗位类别
	 */
	public final static String BASIC_POSITIONCATEGORY_FINDONE_URL= BASICURL+getProperty(
			"basic.positionCategory.findOne", "/positionCategory/findOne");

	/**
	 * 根据企业员工的id列表获取执行人
	 */
	public final static String BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL= BASICURL+getProperty(
			"basic.employee.getExecutorsByEmployeeIds", "/employee/getExecutorsByEmployeeIds");

	/**
	 * 根据组织机构的id获取员工
	 */
	public final static String BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL= BASICURL+getProperty(
			"basic.employee.findByOrganizationId", "/employee/findByOrganizationId");

}
