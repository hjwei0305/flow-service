package com.ecmp.flow.controller.design;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.basic.api.IPositionCategoryService;
import com.ecmp.basic.api.IPositionService;
import com.ecmp.basic.entity.Position;
import com.ecmp.basic.entity.PositionCategory;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.*;
import com.ecmp.flow.api.common.api.IFlowCommonConditionService;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程设计器控制器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/14 11:22      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/design")
@IgnoreCheckAuth
public class FlowDesignController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
//        model.addAttribute("orgName", orgName);
        return "/design/WorkFlowView";
    }

    @RequestMapping(value = "showLook", method = RequestMethod.GET)
    public String look() {
        return "/design/LookWorkFlowView";
    }

    /**
     * 流程设计保存
     *
     * @param def json文本
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String save(String def, boolean deploy) throws JAXBException, UnsupportedEncodingException, CloneNotSupportedException {
        OperateStatus status = OperateStatus.defaultSuccess();
        JSONObject defObj = JSONObject.fromObject(def);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        String id=definition.getProcess().getId();
        String reg="^[a-zA-Z][A-Za-z0-9]{5,79}$";
        if(!id.matches(reg)){
            status=new OperateStatus(false, ContextUtil.getMessage("10001"));
            return JsonUtil.serialize(status);
        }
        definition.setDefJson(def);
        if (!deploy) {
            IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
            OperateResultWithData<FlowDefVersion> result = proxy.save(definition);
            status.setSuccess(result.successful());
            status.setMsg(result.getMessage());
            status.setData(result.getData());
        } else {
            IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
            OperateResultWithData<FlowDefVersion> result = proxy.save(definition);
            if(	result.successful()){
                IFlowDefinationService proxy2 = ApiClient.createProxy(IFlowDefinationService.class);
                String deployById = proxy2.deployById(result.getData().getFlowDefination().getId());
            }
            status.setSuccess(result.successful());
            status.setMsg(result.getMessage());
            status.setData(result);
        }
        return JsonUtil.serialize(status);
    }

    /**
     * 通过业务实体ID获取条件POJO属性说明
     *
     * @param businessModelId
     * @return
     * @throws ClassNotFoundException
     */
    @ResponseBody
    @RequestMapping(value = "getProperties", method = RequestMethod.POST)
    public String getProperties(String businessModelId) throws ClassNotFoundException {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowCommonConditionService proxy = ApiClient.createProxy(IFlowCommonConditionService.class);
        Map<String, String> result = proxy.getPropertiesForConditionPojoByBusinessModelId(businessModelId);
        status.setData(result);
        return JsonUtil.serialize(status);
    }


    /**
     * 获取工作界面
     *
     * @param businessModelId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "listAllWorkPage", method = RequestMethod.POST)
    public String listAllWorkPage(String businessModelId) {
        OperateStatus status = OperateStatus.defaultSuccess();
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        List<WorkPageUrl> result = proxy.findSelectEdByBusinessModelId(businessModelId);
        status.setData(result);
        return JsonUtil.serialize(status);
    }

    /**
     * 获取流程设计
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getEntity", method = RequestMethod.POST)
    public String getEntity(String id, int versionCode) {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        FlowDefVersion data = proxy.getFlowDefVersion(id, versionCode);
        status.setData(data);
        return JsonUtil.serialize(status);
    }

    /**
     * 通过流程版本获取流程设计内容，提供编辑
     *
     * @param flowDefVersionId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getEntityByVersionId", method = RequestMethod.POST)
    public String getEntity(String flowDefVersionId) {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        FlowDefVersion data = proxy.findOne(flowDefVersionId);
        status.setData(data);
        return JsonUtil.serialize(status);
    }

    /**
     * 获取岗位类别列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "listPosType")
    public List<PositionCategory> listPositonType(String notInIds) {
        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
        List<PositionCategory> data = proxy.findAll();
        return data;
    }

    /**
     * 获取岗位列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "listPos")
    public PageResult<Position> listPositon(ServletRequest request) {
        Search search = SearchUtil.genSearch(request);
        search.addQuickSearchProperty("code");
        search.addQuickSearchProperty("name");
        search.addQuickSearchProperty("organization.name");
        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        return proxy.findByPage(search);
    }

    /**
     * 查询流程服务地址
     *
     * @return 服务地址清单
     * @throws ParseException
     */
    @RequestMapping(value = "listAllServiceUrl")
    @ResponseBody
    public String listServiceUrl(String busModelId) throws ParseException {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        List<FlowServiceUrl> flowServiceUrlPageResult = proxy.findByBusinessModelId(busModelId);
        status.setData(flowServiceUrlPageResult);
        return JsonUtil.serialize(status);
    }

    /**
     * 根据流程实例获取当前流程所在节点
     * @param id 版本id
     * @param instanceId
     * @return
     */
    @RequestMapping(value = "getLookInfo")
    @ResponseBody
    public String getLookInfo(String id, String instanceId) {
        OperateStatus status = OperateStatus.defaultSuccess();
        FlowDefVersion def = null;
        Map<String, Object> data = new HashedMap();
        if(StringUtils.isNotEmpty(instanceId)){
            IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
            FlowInstance flowInstance = proxy.findOne(instanceId);
            if(flowInstance != null){
                def = flowInstance.getFlowDefVersion();
            }
        }
        if(def == null){
            IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
            def = proxy.findOne(id);
        }
        data.put("def", def);
        IFlowInstanceService proxy2 = ApiClient.createProxy(IFlowInstanceService.class);
        if(StringUtils.isNotEmpty(instanceId)){
            Map<String,String> nodeIds = proxy2.currentNodeIds(instanceId);
            data.put("currentNodes", nodeIds);
        }else{
            data.put("currentNodes", "[]");
        }
        status.setData(data);
        return JsonUtil.serialize(status);
    }
}
