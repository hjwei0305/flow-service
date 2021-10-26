package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.service.FlowInstanceService;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.core.GenericType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：服务调用
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/27 13:06      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ServiceCallUtil {

    public static FlowOperateResult callService(String serviceUrlId, String serviceName, String flowTaskName, String businessId, String... args) {
        FlowOperateResult result = null;
        if (!StringUtils.isEmpty(serviceUrlId)) {
            ApplicationContext applicationContext = ContextUtil.getApplicationContext();
            FlowServiceUrlDao flowServiceUrlDao = (FlowServiceUrlDao) applicationContext.getBean(Constants.FLOW_SERVICE_URL_DAO);
            FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(serviceUrlId);
            if (flowServiceUrl != null) {

                Map<String, String> paramMap = new HashMap<>();
                FlowInvokeParams params = new FlowInvokeParams();
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(args[0])) {
                    try {
                        JSONObject jsonObject = JSONObject.fromObject(args[0]);
                        if (jsonObject.has(Constants.APPROVED)) {
                            String approved = jsonObject.get(Constants.APPROVED) + "";
                            if (StringUtils.isNotEmpty(approved) && !Constants.NULL_S.equalsIgnoreCase(approved)) {
                                params.setAgree(Boolean.parseBoolean(approved));
                            }
                        }
                        if (jsonObject.has(Constants.APPROVE_RESULT)) {
                            String approveResult = jsonObject.get(Constants.APPROVE_RESULT) + "";
                            if (StringUtils.isNotEmpty(approveResult) && !Constants.NULL_S.equalsIgnoreCase(approveResult)) {
                                params.setFinalAgree(Boolean.parseBoolean(approveResult));
                            }
                        }
                        if (jsonObject.has(Constants.RECEIVE_TASK_ACT_DEF_ID)) {
                            String receiveTaskActDefId = jsonObject.get(Constants.RECEIVE_TASK_ACT_DEF_ID) + "";
                            if (StringUtils.isNotEmpty(receiveTaskActDefId) && !Constants.NULL_S.equalsIgnoreCase(receiveTaskActDefId)) {
                                params.setTaskActDefId(receiveTaskActDefId);
                            }
                        }
                        if (jsonObject.has(Constants.POOL_TASK_ACT_DEF_ID)) {
                            String poolTaskActDefId = jsonObject.get(Constants.POOL_TASK_ACT_DEF_ID) + "";
                            if (StringUtils.isNotEmpty(poolTaskActDefId) && !Constants.NULL_S.equalsIgnoreCase(poolTaskActDefId)) {
                                params.setTaskActDefId(poolTaskActDefId);
                            }
                        }
                        if (jsonObject.has(Constants.POOL_TASK_CODE)) {
                            String poolTaskCode = jsonObject.get(Constants.POOL_TASK_CODE) + "";
                            if (StringUtils.isNotEmpty(poolTaskCode) && !Constants.NULL_S.equalsIgnoreCase(poolTaskCode)) {
                                params.setPoolTaskCode(poolTaskCode);
                            }
                        }
                        if (jsonObject.has(Constants.REJECT)) {
                            int reject = jsonObject.getInt(Constants.REJECT);
                            if (reject == 1) {
                                params.setReject(true);//是否被驳回
                            }
                        }
                        if (jsonObject.has(Constants.CALL_ACTIVITY_SON_PATHS)) {
                            List<String> callActivitySonPaths = jsonObject.getJSONArray(Constants.CALL_ACTIVITY_SON_PATHS);
                            params.setCallActivitySonPaths(callActivitySonPaths);
                        }
                        if (jsonObject.has(Constants.OPINION)) {
                            String opinion = jsonObject.get(Constants.OPINION) + "";
                            paramMap.put(Constants.OPINION, opinion);
                        }
                        if (jsonObject.has("selectedNodesUserMap")) {
                            JSONObject itemJSONObj = jsonObject.getJSONObject("selectedNodesUserMap");
                            Map<String, List<String>> itemMap = (Map<String, List<String>>) JSONObject.toBean(itemJSONObj, Map.class);
                            params.setNextNodeUserInfo(itemMap);
                        }
                        if (jsonObject.has("currentNodeCode")) {
                            String currentNodeCode = jsonObject.get("currentNodeCode") + "";
                            paramMap.put("nodeCode", currentNodeCode);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
                try {
                    FlowInstanceService flowInstanceService = (FlowInstanceService) applicationContext.getBean(Constants.FLOW_INSTANCE_SERVICE);
                    FlowInstance flowInstance = flowInstanceService.findLastInstanceByBusinessId(businessId);
                    if (flowInstance != null) {
                        paramMap.put("flowInstanceName", flowInstance.getFlowName());
                        paramMap.put("flowTaskName", flowTaskName);
                    }
                } catch (Exception e) {
                    LogUtil.error("获取流程名称和单据名称错误！", e);
                }

                params.setId(businessId);
                params.setParams(paramMap);
                String url;
                String clientUrl = flowServiceUrl.getUrl();
                if (PageUrlUtil.isAppModelUrl(clientUrl)) {
                    url = PageUrlUtil.buildUrl(PageUrlUtil.getBaseApiUrl(), clientUrl);
                } else {
                    AppModule appModule = flowServiceUrl.getBusinessModel().getAppModule();
                    String apiBaseAddressConfig = appModule.getApiBaseAddress();
                    String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
                    url = PageUrlUtil.buildUrl(clientApiBaseUrl, clientUrl);
                }

                Date startDate = new Date();
                SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                try {
                    ResponseData res = ApiClient.postViaProxyReturnResult(url, new GenericType<ResponseData>() {
                    }, params);
                    Date endDate = new Date();
                    if (res.successful()) {
                        LogUtil.bizLog("[{}]开始请求节点【{}】的事件【{}】,请求地址：{},请求参数：{},返回信息时间：[{}],返回信息：[{}]", sim.format(startDate), flowTaskName, flowServiceUrl.getName(), url, JsonUtils.toJson(params), sim.format(endDate), JsonUtils.toJson(res));
                        result = new FlowOperateResult(true, res.getMessage());
                        //FlowOperateResult可以直接返回执行人（工作池任务）
                        if (res.getData() != null && StringUtils.isNotEmpty(res.getData().toString())) {
                            try {
                                Map<String, Object> map = (Map<String, Object>) res.getData();
                                String userIds = map.get("userIds").toString();
                                result.setUserId(userIds);
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        LogUtil.error("[{}]开始请求节点【{}】的事件【{}】,接口返回错误信息：{},请求地址：{},请求参数：{}", sim.format(startDate), flowTaskName, flowServiceUrl.getName(), JsonUtils.toJson(res), url, JsonUtils.toJson(params));
                        result = new FlowOperateResult(false, ContextUtil.getMessage("10357", flowTaskName, flowServiceUrl.getName(), res.getMessage()));
                    }
                } catch (Exception e) {
                    LogUtil.error("[{}]开始请求节点【{}】的事件【{}】,接口调用异常：{},请求地址：{},请求参数：{}", sim.format(startDate), flowTaskName, flowServiceUrl.getName(), e.getMessage(), url, JsonUtils.toJson(params), e);
                    result = new FlowOperateResult(false, ContextUtil.getMessage("10358", flowTaskName, flowServiceUrl.getName(), e.getMessage()));

                }
            } else {
                LogUtil.error("获取节点【{}】配置的事件【{}】失败，可能已经被删除：【serviceId={}】", flowTaskName, serviceName, serviceUrlId);
                throw new FlowException(ContextUtil.getMessage("10359", flowTaskName, serviceName));
            }
        }
        return result;
    }
}
