package com.ecmp.flow.common.util;

import com.ecmp.context.BaseApplicationContext;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.log.util.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;


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
@Component
public class Constants extends ConfigurableContants {

    // 静态初始化读入framework.properties中的设置
    static {
        init("framework.properties");
    }

    public static String getBaseApi(){
        return BaseApplicationContext.getProperty("BASE_API");
    }

    public static String getBaseWeb(){
        return BaseApplicationContext.getProperty("BASE_WEB");
    }

    public static String getAuthBaseApi(){return BaseApplicationContext.getProperty("SEI_AUTH_API");}


    /**
     * 从流程外部配置文件中读取参数
     * @param key
     * @return
     */
    public static String getFlowPropertiesByKey(String key){
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return   BaseApplicationContext.getProperty(key);
    }

    /**
     * 获取键值对配置参数（内部）
     *
     * @return
     */
    public static String getConfigKeyValueProperties(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return getProperty(key);
    }

    /**
     * 获取API基地址
     * @param key
     * @return
     */
    public static String getConfigValueByApi(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String value = BaseApplicationContext.getProperty(key);
        if (value == null) {
            LogUtil.error("获取API基地址参数[" + key + "]失败！");
            throw new FlowException(ContextUtil.getMessage("10370",key));
        }
        return  PageUrlUtil.buildUrl(getBaseApi(),value);
    }

    /**
     * 获取web基地址
     * @param key
     * @return
     */
    public static String getConfigValueByWeb(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String value = BaseApplicationContext.getProperty(key);
        if (value == null) {
            LogUtil.error("获取WEB基地址参数[" + key + "]失败！");
            throw new FlowException(ContextUtil.getMessage("10371",key));
        }
        return getBaseWeb() +  value;
    }


    public static String getBasicServiceUrl() {
        return getBaseApi() + BaseApplicationContext.getProperty("BASIC_API");
    }


    //推送basic任务的基地址（可以替换推送的基模块）
    public static String getPushTaskBasicServiceUrl(){
        String baseUrl =  getFlowPropertiesByKey("OVERWRITE_PUSH_BASIC_BASE_URL_TO");
        if(StringUtils.isEmpty(baseUrl)){
            baseUrl = "BASIC_API";
        }
        return getBaseApi() + BaseApplicationContext.getProperty(baseUrl);
    }

    //------------------------------------------------获取执行人--------------------------------//

    /**
     * 根据用户的id列表获取执行人
     */
    public static String getBasicUserGetExecutorsbyUseridsUrl() {
        String BASIC_USER_GETEXECUTORSBYUSERIDS_URL = getBasicServiceUrl() + getProperty(
                "basic.user.getExecutorsByUserIds", "/user/getExecutorsByUserIds");
        return BASIC_USER_GETEXECUTORSBYUSERIDS_URL;
    }

    /**
     * 通过岗位ids、组织维度ids和单据所属组织机构id来获取执行人
     *
     * @return
     */
    public static String getBasicPositionGetexecutorsbypositionidsUrl() {
        String BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL = getBasicServiceUrl() + getProperty(
                "basic.position.getExecutorsByPositionIds", "/position/getExecutorsByPositionIds");
        return BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL;
    }

    /**
     * 根据岗位类别的id列表获取执行人
     */
    public static String getBasicPositionGetexecutorsbyposcateidsUrl() {
        //根据单据的orgId查找最近的符合岗位类别的人员
        String BASIC_POSITION_GETEXECUTORSBYPOSCATANDORGROOT_URL = getBasicServiceUrl() + getProperty(
                "basic.position.getExecutorsByPostCatAndOrgToRoot", "/position/getExecutorsByPostCatAndOrgToRoot");
        return BASIC_POSITION_GETEXECUTORSBYPOSCATANDORGROOT_URL;
    }


    /**
     * 通过岗位类别ids和组织机构ids获取执行人
     *
     * @return
     */
    public static String getExecutorsByPostCatAndOrgUrl() {
        String BASIC_POSITONCATANDORG_GETEXECUTORS_URL = getBasicServiceUrl() + getProperty(
                "basic.positionCatAndOrg.getExecutors", "/employee/getExecutorsByPostCatAndOrg");
        return BASIC_POSITONCATANDORG_GETEXECUTORS_URL;
    }


    //------------------------------------------------获取组织机构--------------------------------//

    /**
     * 获取所有组织机构树（不包含冻结）
     *
     * @return
     */
    public static String getBasicOrgListallorgsUrl() {
        String BASIC_ORG_LISTALLORGS_URL = getBasicServiceUrl() + getProperty(
                "basic.org.listAllOrgs", "/organization/findOrgTreeWithoutFrozen");
        return BASIC_ORG_LISTALLORGS_URL;
    }

    /**
     * 获取所有有权限的组织机构树
     *
     * @return
     */
    public static String getBasicOrgListByPowerUrl() {
        String BASIC_ORG_LISTALLORGS_URL;
        String POWER_Url = BaseApplicationContext.getProperty("basic.org.listallorgbypower");
        if (StringUtils.isNotEmpty(POWER_Url)) {
            BASIC_ORG_LISTALLORGS_URL = getBasicServiceUrl() + POWER_Url;
        } else {
            BASIC_ORG_LISTALLORGS_URL = getBasicServiceUrl() + getProperty(
                    "basic.org.listAllOrgs", "/organization/findOrgTreeWithoutFrozen");
        }
        return BASIC_ORG_LISTALLORGS_URL;
    }


    /**
     * 获取指定节点的父组织机构列表
     *
     * @return
     */
    public static String getBasicOrgFindparentnodesUrl() {
        String BASIC_ORG_FINDPARENTNODES_URL = getBasicServiceUrl() + getProperty(
                "basic.org.findParentNodes", "/organization/getParentNodes");
        return BASIC_ORG_FINDPARENTNODES_URL;
    }


    //------------------------------------------------获取员工--------------------------------//

    /**
     * 根据组织机构的id获取员工
     */
    public static String getBasicEmployeeFindbyorganizationidUrl() {
        String BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL = getBasicServiceUrl() + getProperty(
                "basic.employee.findByOrganizationId", "/employee/findByOrganizationIdWithoutFrozen");
        return BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL;
    }

    /**
     * 根据组织机构的id获取员工（新增：是否包含子节点）
     */
    public static String getBasicEmployeeFindByUserQueryParam() {
        String BASIC_EMPLOYEE_FINDBYUSERQUERYPARAM_URL = getBasicServiceUrl() + getProperty(
                "basic.employee.findByUserQueryParam", "/employee/findByUserQueryParam");
        return BASIC_EMPLOYEE_FINDBYUSERQUERYPARAM_URL;
    }


    //------------------------------------------------获取岗位--------------------------------//

    /**
     * 获取所有岗位列表（web端使用）
     *
     * @return
     */
    public static String getBasicPositionFindbypageUrl() {
        String BASIC_POSITION_FINDBYPAGE_URL = getBasicServiceUrl() + getProperty(
                "basic.position.findByPage", "/position/findByPage");
        return BASIC_POSITION_FINDBYPAGE_URL;
    }


    //----------------------------------------------获取岗位类别--------------------------------//

    /**
     * 获取所有岗位类别列表（web端使用）
     */
    public static String getBasicPositioncategoryFindallUrl() {
        String BASIC_POSITIONCATEGORY_FINDALL_URL = getBasicServiceUrl() + getProperty(
                "basic.positionCategory.findAll", "/positionCategory/findAll");
        return BASIC_POSITIONCATEGORY_FINDALL_URL;
    }


    //----------------------------------------------获取组织维度--------------------------------//

    /**
     * 获取所有组织维度（web端使用）
     *
     * @return
     */
    public static String getBasicOrgDimensionUrl() {
        String BASIC_ORG_FINDORGANIZATIONDIMENSION_URL = getBasicServiceUrl() + getProperty(
                "basic.org.findOrganizationDimension", "/organization/findOrganizationDimension");
        return BASIC_ORG_FINDORGANIZATIONDIMENSION_URL;
    }


    //----------------------------------------------获取应用模块--------------------------------//

    /**
     * 获取当前用户所拥有的应用模块权限url
     *
     * @return
     */
    public static String getBasicTenantAppModuleUrl() {
        String BASIC_TENANTAPPMODULE_FINDTENANTAPPMODULES_URL = getBasicServiceUrl() + getProperty(
                "basic.tenantAppModule.findTenantAppModules", "/tenantAppModule/getTenantAppModules");
        return BASIC_TENANTAPPMODULE_FINDTENANTAPPMODULES_URL;
    }


    //----------------------------------------------推送流程任务--------------------------------//

    /**
     * 得到推送新生产待办的接口地址
     *
     * @return
     */
    public static String getBasicPushNewTaskUrl() {
        String BASIC_PUSH_NEW_TASK_URL = getPushTaskBasicServiceUrl() + getProperty(
                "basic.push.newTask", "/task/pushNewTask");
        return BASIC_PUSH_NEW_TASK_URL;
    }

    /**
     * 得到推送新执行已办的接口地址
     *
     * @return
     */
    public static String getBasicPushOldTaskUrl() {
        String BASIC_PUSH_OLD_TASK_URL = getPushTaskBasicServiceUrl() + getProperty(
                "basic.push.oldTask", "/task/pushOldTask");
        return BASIC_PUSH_OLD_TASK_URL;
    }

    /**
     * 得到推送需要删除待办的接口地址
     *
     * @return
     */
    public static String getBasicPushDelTaskUrl() {
        String BASIC_PUSH_DEL_TASK_URL = getPushTaskBasicServiceUrl() + getProperty(
                "basic.push.delTask", "/task/pushDelTask");
        return BASIC_PUSH_DEL_TASK_URL;
    }

    /**
     * 得到推送需要归档（终止）的任务到basic模块地址
     *
     * @return
     */
    public static String getBasicPushEndTaskUrl() {
        String BASIC_PUSH_END_TASK_URL = getPushTaskBasicServiceUrl() + getProperty(
                "basic.push.endTask", "/task/pushEndTask");
        return BASIC_PUSH_END_TASK_URL;
    }


    /**
     * 查询用户个人邮件提醒信息url
     *
     * @return
     */
    public static String getUserEmailAlertFindByUserIdsUrl() {
        String BASIC_TENANTAPPMODULE_FINDTENANTAPPMODULES_URL = getBasicServiceUrl() + getProperty(
                "basic.userEmailAlert.findByUserIds", "/userEmailAlert/findByUserIds");
        return BASIC_TENANTAPPMODULE_FINDTENANTAPPMODULES_URL;
    }


    /**
     * 更新用户的最后一次提醒时间url
     *
     * @return
     */
    public static String getUsderEmailAlertUpdateLastTimesUrl() {
        String BASIC_TENANTAPPMODULE_FINDTENANTAPPMODULES_URL = getBasicServiceUrl() + getProperty(
                "basic.userEmailAlert.updateLastTimes", "/userEmailAlert/updateLastTimes");
        return BASIC_TENANTAPPMODULE_FINDTENANTAPPMODULES_URL;
    }

    /**
     * 匿名用户
     */
    public final static String ANONYMOUS = "anonymous";

    /**
     * 调用子流程时的路径值
     */
    public final static String CALL_ACTIVITY_SON_PATHS = "callActivitySonPaths";

    /**
     * 同意-过去式
     */
    public final static String APPROVED = "approved";

    /**
     * 同意-现在式
     */
    public final static String APPROVE = "approve";

    /**
     * 同意结论
     */
    public final static String APPROVE_RESULT = "approveResult";

    /**
     * 驳回
     */
    public final static String REJECT = "reject";

    /**
     * 接收任务实际定义ID
     */
    public final static String RECEIVE_TASK_ACT_DEF_ID = "receiveTaskActDefId";

    /**
     * 用户池任务实际定义ID
     */
    public final static String POOL_TASK_ACT_DEF_ID = "poolTaskActDefId";

    /**
     * 用户池任务实际定义ID
     */
    public final static String POOL_TASK_CALLBACK_USER_ID = "poolTaskActCallbackUserId_";


    /**
     * 转办
     */
    public final static String TRUST_TO_DO = "trustToDo";

    /**
     * null对象的字符窜表示
     */
    public final static String NULL_S = "null";

    /**
     * 流程服务管理dao
     */
    public final static String FLOW_SERVICE_URL_DAO = "flowServiceUrlDao";


    /**
     * 流程服务管理dao
     */
    public final static String FLOW_INSTANCE_SERVICE = "flowInstanceService";


    /**
     * 子流程选择节点执行人参数标记
     */
    public final static String SON_PROCESS_SELECT_NODE_USER = "_sonProcessSelectNodeUserV";

    public final static String WORK_CAPTION = "workCaption";

    public final static String BUSINESS_CODE = "businessCode";

    public final static String BUSINESS_MONEY = "businessMoney";
    /**
     * 流程定义版本id
     */
    public final static String FLOW_DEF_VERSION_ID = "flowDefVersionId";

    public final static String NAME = "name";

    public final static String ID = "id";

    public final static String ALL = "all";

    public final static String STATUS = "status";

    public final static String ADMIN = "admin";

    public final static String NODE_CONFIG = "nodeConfig";

    public final static String NORMAL = "normal";

    public final static String END = "end";

    public final static String EVENT = "event";

    public final static String EXECUTOR = "executor";

    public final static String BEFORE_EXCUTE_SERVICE_ID = "beforeExcuteServiceId";

    public final static String BEFORE_EXCUTE_SERVICE = "beforeExcuteService";

    public final static String BEFORE_ASYNC = "beforeAsync";

    public final static String AFTER_EXCUTE_SERVICE_ID = "afterExcuteServiceId";

    public final static String AFTER_EXCUTE_SERVICE = "afterExcuteService";

    public final static String FLOW_TASK_SERVICE = "flowTaskService";

    public final static String SUPER_EXECUTION_ID = "_superExecutionId";

    public final static String AFTER_ASYNC = "afterAsync";

    public final static String SERVICE_TASK_ID = "serviceTaskId";

    public final static String SERVICE_TASK = "serviceTask";

    public final static String BUSINESS_MODEL_CODE = "businessModelCode";

    public final static String CONDITION_TEXT = "conditionText";

    public final static String COUNTER_SIGN_AGREE = "counterSign_agree";

    public final static String COUNTER_SIGN_OPPOSITION = "counterSign_opposition";

    public final static String COUNTER_SIGN_WAIVER = "counterSign_waiver";

    public final static String GROOVY_UEL = "groovyUel";

    public final static String OPINION = "opinion";

    public final static String ORG_ID = "orgId";

    public final static String POOL_TASK_CODE = "poolTaskCode";

    //推送任务类型（推送到basic）
    public final static String TYPE_BASIC = "basic";

    //推送任务类型（推送到业务模块）
    public final static String TYPE_BUSINESS = "business";

    //业务模块推送任务的状态（待办）
    public final static String STATUS_BUSINESS_INIT = "init";

    //业务模块推送任务的状态（已办）
    public final static String STATUS_BUSINESS_COMPLETED = "completed";

    //业务模块推送任务的状态（删除）
    public final static String STATUS_BUSINESS_DEDLETE = "delete";

    //baisc推送任务的状态（新增待办）
    public final static String STATUS_BASIC_NEW = "new";

    //baisc推送任务的状态（待办转已办）
    public final static String STATUS_BASIC_OLD = "old";

    //baisc推送任务的状态（删除待办）
    public final static String STATUS_BASIC_DEL = "del";

    //baisc推送任务的状态（归档（终止））
    public final static String STATUS_BASIC_END = "end";


    public final static String AUTHBASURL = getProperty(
            "ecmp.auth2.url");
    public final static String AUTHCLIENTID = getProperty(
            "ecmp.auth2.client.id", "normal-app");
    public final static String AUTHCLIENTPASSWORD = getProperty(
            "ecmp.auth2.client.password", "");
    public final static String AUTHLOGINURL = AUTHBASURL + getProperty(
            "ecmp.auth2.login.urlpath");
    public final static String AUTHCODEURL = AUTHBASURL + getProperty(
            "ecmp.auth2.code.urlpath");
    public final static String AUTHTOKENURL = AUTHBASURL + getProperty(
            "ecmp.auth2.token.urlpath");

    public final static String AUTHDEFAULTUSERID = getProperty(
            "ecmp.auth2.user.defaultUserId");
    public final static String AUTHDEFAULTUSERPWD = getProperty(
            "ecmp.auth2.user.defaultUserPwd");

    public final static String SELFPRIVATEKEY = getProperty(
            "ecmp.self.rsa.privateKey");
    public final static String SELFPUBKEY = getProperty(
            "ecmp.self.rsa.publicKey");

}
