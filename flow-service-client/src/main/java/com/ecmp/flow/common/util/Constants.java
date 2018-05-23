package com.ecmp.flow.common.util;

import com.ecmp.context.ContextUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Properties;

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

	// 静态初始化读入framework.properties中的设置
	static {
		init("framework.properties");
	}

	public static String getBasicServiceUrl() {
		String	BASIC_SERVICE_URL = ContextUtil.getGlobalProperty("BASIC_API");
		return BASIC_SERVICE_URL;
	}


	/**
	 * 获取所有组织维度
	 * @return
	 */
	public static String getBasicOrgDimensionUrl() {
		String BASIC_ORG_FINDORGANIZATIONDIMENSION_URL = ContextUtil.getGlobalProperty("BASIC.ORG.FINDORGANIZATIONDIMENSION");
		if(StringUtils.isEmpty(BASIC_ORG_FINDORGANIZATIONDIMENSION_URL)){
			BASIC_ORG_FINDORGANIZATIONDIMENSION_URL=getBasicServiceUrl() + getProperty(
					"basic.org.findOrganizationDimension", "/organization/findOrganizationDimension");
		}
		return BASIC_ORG_FINDORGANIZATIONDIMENSION_URL;
	}

	/**
	 * 获取所有组织机构
	 * @return
	 */
	public static String getBasicOrgListallorgsUrl() {
		String BASIC_ORG_LISTALLORGS_URL = ContextUtil.getGlobalProperty("BASIC.ORG.LISTALLORGS");
		if(StringUtils.isEmpty(BASIC_ORG_LISTALLORGS_URL)){
			BASIC_ORG_LISTALLORGS_URL=getBasicServiceUrl() + getProperty(
					"basic.org.listAllOrgs", "/organization/findOrgTreeWithoutFrozen");
		}
		return BASIC_ORG_LISTALLORGS_URL;
	}

	/**
	 * 获取指定id的组织机构
	 * @return
	 */
	public static String getBasicOrgFindoneUrl() {
		String BASIC_ORG_FINDONE_URL = ContextUtil.getGlobalProperty("BASIC.ORG.FINDONE");
		if(StringUtils.isEmpty(BASIC_ORG_FINDONE_URL)){
			BASIC_ORG_FINDONE_URL=getBasicServiceUrl() + getProperty(
					"basic.org.findOne", "/organization/findOne");
		}
		return BASIC_ORG_FINDONE_URL;
	}

	/**
	 * 获取指定id的父组织机构对象列表
	 * @return
	 */
	public static String getBasicOrgFindparentnodesUrl() {
		String BASIC_ORG_FINDPARENTNODES_URL = ContextUtil.getGlobalProperty("BASIC.ORG.FINDPARENTNODES");
		if(StringUtils.isEmpty(BASIC_ORG_FINDPARENTNODES_URL)){
			BASIC_ORG_FINDPARENTNODES_URL=getBasicServiceUrl() + getProperty(
					"basic.org.findParentNodes", "/organization/getParentNodes");
		}
		return BASIC_ORG_FINDPARENTNODES_URL;
	}

	/**
	 * 获取岗位列表
	 * @return
	 */
	public static String getBasicPositionFindbypageUrl() {
		String BASIC_POSITION_FINDBYPAGE_URL = ContextUtil.getGlobalProperty("BASIC.POSITION.FINDBYPAGE");
		if(StringUtils.isEmpty(BASIC_POSITION_FINDBYPAGE_URL)){
			BASIC_POSITION_FINDBYPAGE_URL=getBasicServiceUrl() + getProperty(
					"basic.position.findByPage", "/position/findByPage");
		}
		return BASIC_POSITION_FINDBYPAGE_URL;
	}

	/**
	 * 获取指定id岗位
	 * @return
	 */
	public static String getBasicPositionFindoneUrl() {
		String BASIC_POSITION_FINDONE_URL = ContextUtil.getGlobalProperty("BASIC.POSITION.FINDONE");
		if(StringUtils.isEmpty(BASIC_POSITION_FINDONE_URL)){
			BASIC_POSITION_FINDONE_URL=getBasicServiceUrl() + getProperty(
					"basic.position.findOne", "/position/findOne");
		}
		return BASIC_POSITION_FINDONE_URL;
	}

	/**
	 * 根据岗位id列表获取岗位
	 * @return
	 */
	public static String getBasicPositionFindbyidsUrl() {
		String BASIC_POSITION_FINDBYIDS_URL = ContextUtil.getGlobalProperty("BASIC.POSITION.FINDBYIDS");
		if(StringUtils.isEmpty(BASIC_POSITION_FINDBYIDS_URL)){
			BASIC_POSITION_FINDBYIDS_URL=getBasicServiceUrl() + getProperty(
					"basic.position.findByIds", "/position/findByIds");
		}
		return BASIC_POSITION_FINDBYIDS_URL;
	}

	/**
	 * 根据岗位的id列表获取执行人
	 * @return
	 */
	public static String getBasicPositionGetexecutorsbypositionidsUrl() {
		String BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL = ContextUtil.getGlobalProperty("BASIC.POSITION.GETEXECUTORSBYPOSITIONIDS");
		if(StringUtils.isEmpty(BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL)){
			BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL=getBasicServiceUrl() + getProperty(
					"basic.position.getExecutorsByPositionIds", "/position/getExecutorsByPositionIds");
		}
		return BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL;
	}

	/**
	 * 根据岗位类别的id列表获取执行人
	 */
	public static String getBasicPositionGetexecutorsbyposcateidsUrl() {
		String BASIC_POSITION_GETEXECUTORSBYPOSCATEIDS_URL = ContextUtil.getGlobalProperty("BASIC.POSITION.GETEXECUTORSBYPOSCATEIDS");
		if(StringUtils.isEmpty(BASIC_POSITION_GETEXECUTORSBYPOSCATEIDS_URL)){
			BASIC_POSITION_GETEXECUTORSBYPOSCATEIDS_URL=getBasicServiceUrl() + getProperty(
					"basic.position.getExecutorsByPosCateIds", "/position/getExecutorsByPosCateIds");
		}
		return BASIC_POSITION_GETEXECUTORSBYPOSCATEIDS_URL;
	}

	/**
	 * 获取所有岗位类别列表
	 */
	public static String getBasicPositioncategoryFindallUrl() {
		String BASIC_POSITIONCATEGORY_FINDALL_URL = ContextUtil.getGlobalProperty("BASIC.POSITIONCATEGORY.FINDALL");
		if(StringUtils.isEmpty(BASIC_POSITIONCATEGORY_FINDALL_URL)){
			BASIC_POSITIONCATEGORY_FINDALL_URL=getBasicServiceUrl() + getProperty(
					"basic.positionCategory.findAll", "/positionCategory/findAll");
		}
		return BASIC_POSITIONCATEGORY_FINDALL_URL;
	}

	/**
	 * 获取指定id岗位类别
	 */
	public static String getBasicPositioncategoryFindoneUrl() {
		String BASIC_POSITIONCATEGORY_FINDONE_URL = ContextUtil.getGlobalProperty("BASIC.POSITIONCATEGORY.FINDONE");
		if(StringUtils.isEmpty(BASIC_POSITIONCATEGORY_FINDONE_URL)){
			BASIC_POSITIONCATEGORY_FINDONE_URL=getBasicServiceUrl() + getProperty(
					"basic.positionCategory.findOne", "/positionCategory/findOne");
		}
		return BASIC_POSITIONCATEGORY_FINDONE_URL;
	}

	/**
	 * 根据企业员工的id列表获取执行人
	 */
	public static String getBasicEmployeeGetexecutorsbyemployeeidsUrl() {
		String BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL = ContextUtil.getGlobalProperty("BASIC.EMPLOYEE.GETEXECUTORSBYEMPLOYEEIDS");
		if(StringUtils.isEmpty(BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL)){
			BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL=getBasicServiceUrl() + getProperty(
					"basic.employee.getExecutorsByEmployeeIds", "/employee/getExecutorsByEmployeeIds");
		}
		return BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
	}

	/**
	 * 根据组织机构的id获取员工
	 */
	public static String getBasicEmployeeFindbyorganizationidUrl() {
		String BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL = ContextUtil.getGlobalProperty("BASIC.EMPLOYEE.FINDBYORGANIZATIONID");
		if(StringUtils.isEmpty(BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL)){
			BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL=getBasicServiceUrl() + getProperty(
					"basic.employee.findByOrganizationId", "/employee/findByOrganizationIdWithoutFrozen");
		}
		return BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL;
	}

	/**
	 * 通过id列表获取执行人列表
	 * @return
	 */
	public static String getBasicEmployeeFindbyidsUrl() {
		String BASIC_EMPLOYEE_FINDBYIDS_URL = ContextUtil.getGlobalProperty("BASIC.EMPLOYEE.FINDBYIDS");
		if(StringUtils.isEmpty(BASIC_EMPLOYEE_FINDBYIDS_URL)){
			BASIC_EMPLOYEE_FINDBYIDS_URL=getBasicServiceUrl() + getProperty(
					"basic.employee.findByIds", "/employee/findByIds");
		}
		return BASIC_EMPLOYEE_FINDBYIDS_URL;
	}

	/**
	 * 根据自定义的查询参数获取员工列表
	 * @return
	 */
	public static String getBasicEmployeeFindbyparamUrl() {
		String BASIC_EMPLOYEE_FINDBYPARAM_URL = ContextUtil.getGlobalProperty("BASIC.EMPLOYEE.FINDBYEMPLOYEEPARAM");
		if(StringUtils.isEmpty(BASIC_EMPLOYEE_FINDBYPARAM_URL)){
			BASIC_EMPLOYEE_FINDBYPARAM_URL=getBasicServiceUrl() + getProperty(
					"basic.employee.findByEmployeeParam", "/employee/findByEmployeeParam");
		}
		return BASIC_EMPLOYEE_FINDBYPARAM_URL;
	}


	/**
	 * 匿名用户
	 */
	public final static String ANONYMOUS="anonymous";

	/**
	 * 调用子流程时的路径值
	 */
	public final static String CALL_ACTIVITY_SON_PATHS="callActivitySonPaths";

	/**
	 * 同意-过去式
	 */
	public final static String APPROVED= "approved";

	/**
	 * 同意-现在式
	 */
	public final static String APPROVE= "approve";

	/**
	 * 同意结论
	 */
	public final static String APPROVE_RESULT= "approveResult";

	/**
	 * 驳回
	 */
	public final static String REJECT= "reject";

	/**
	 * 接收任务实际定义ID
	 */
	public final static String RECEIVE_TASK_ACT_DEF_ID= "receiveTaskActDefId";

	/**
	 * 用户池任务实际定义ID
	 */
	public final static String POOL_TASK_ACT_DEF_ID= "poolTaskActDefId";

	/**
	 * 用户池任务实际定义ID
	 */
	public final static String POOL_TASK_CALLBACK_USER_ID= "poolTaskActCallbackUserId_";


	/**
	 * 转办
	 */
	public final static String TRUST_TO_DO= "trustToDo";

	/**
	 * null对象的字符窜表示
	 */
	public final static String NULL_S = "null";

	/**
	 * 流程服务管理dao
	 */
	public final static String FLOW_SERVICE_URL_DAO = "flowServiceUrlDao";

	/**
	 * 子流程选择节点执行人参数标记
	 */
	public final static String SON_PROCESS_SELECT_NODE_USER = "_sonProcessSelectNodeUserV";

	public final static String WORK_CAPTION = "workCaption";

	public final static String BUSINESS_CODE	= "businessCode";
	/**
	 * 流程定义版本id
	 */
	public final static String FLOW_DEF_VERSION_ID = "flowDefVersionId";

	public final static String NAME	= "name";

	public final static String ID	= "id";

	public final static String ALL	= "all";

	public final static String STATUS	= "status";

	public final static String  ADMIN ="admin";

	public final static String  NODE_CONFIG ="nodeConfig";

	public final static String  NORMAL ="normal";

	public final static String  END ="end";

	public final static String  EVENT ="event";

	public final static String  EXECUTOR ="executor";

	public final static String BEFORE_EXCUTE_SERVICE_ID = "beforeExcuteServiceId";

	public final static String BEFORE_ASYNC = "beforeAsync";

	public final static String AFTER_EXCUTE_SERVICE_ID = "afterExcuteServiceId";

	public final static String  FLOW_TASK_SERVICE ="flowTaskService";

	public final static String  SUPER_EXECUTION_ID ="_superExecutionId";

	public final static String AFTER_ASYNC = "afterAsync";


	public final static String  SERVICE_TASK_ID ="serviceTaskId";

	public final static String BUSINESS_MODEL_CODE = "businessModelCode";

	public final static String CONDITION_TEXT = "conditionText";

	public final static String COUNTER_SIGN_AGREE = "counterSign_agree";

	public final static String COUNTER_SIGN_OPPOSITION = "counterSign_opposition";

	public final static String COUNTER_SIGN_WAIVER = "counterSign_waiver";

	public final static String GROOVY_UEL  = "groovyUel";

	public final static String OPINION  = "opinion";

	public final static String ORG_ID  = "orgId";
	public final static String POOL_TASK_CODE  = "poolTaskCode";



	public final static String AUTHBASURL= getProperty(
			"ecmp.auth2.url");
	public final static String AUTHCLIENTID= getProperty(
			"ecmp.auth2.client.id","normal-app");
	public final static String AUTHCLIENTPASSWORD= getProperty(
			"ecmp.auth2.client.password","");
	public final static String AUTHLOGINURL= AUTHBASURL+getProperty(
			"ecmp.auth2.login.urlpath");
	public final static String AUTHCODEURL= AUTHBASURL+getProperty(
			"ecmp.auth2.code.urlpath");
	public final static String AUTHTOKENURL= AUTHBASURL+getProperty(
			"ecmp.auth2.token.urlpath");

	public final static String AUTHDEFAULTUSERID= getProperty(
			"ecmp.auth2.user.defaultUserId");
	public final static String AUTHDEFAULTUSERPWD= getProperty(
			"ecmp.auth2.user.defaultUserPwd");

	public final static String SELFPRIVATEKEY= getProperty(
			"ecmp.self.rsa.privateKey");
	public final static String SELFPUBKEY= getProperty(
			"ecmp.self.rsa.publicKey");

}
