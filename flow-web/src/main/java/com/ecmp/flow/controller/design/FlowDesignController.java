package com.ecmp.flow.controller.design;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IWorkPageUrlService;
import com.ecmp.flow.api.common.api.IConditionServer;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

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
public class FlowDesignController {

    @RequestMapping(method = RequestMethod.GET)
    public String show() {
        return "/design/WorkFlowView";
    }

    /**
     * 流程设计保存
     *
     * @param def json文本
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public String save(String def, boolean deploy) throws JAXBException, UnsupportedEncodingException {
        OperateStatus status = OperateStatus.defaultSuccess();
        JSONObject defObj = JSONObject.fromObject(def);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        definition.setDefJson(def);
        if (deploy == false) {
            IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
            OperateResultWithData<FlowDefVersion> result = proxy.save(definition);
            status.setSuccess(result.successful());
            status.setMsg(result.getMessage());
            status.setData(result.getData());
        } else {
            IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
            OperateResultWithData<FlowDefVersion> result = proxy.save(definition);
            IFlowDefinationService proxy2 = ApiClient.createProxy(IFlowDefinationService.class);
            String deployById = proxy2.deployById(result.getData().getFlowDefination().getId());
            FlowInstance flowInstance = proxy2.startById(result.getData().getFlowDefination().getId(),"admin","businesskeyId",null);
            status.setSuccess(result.successful());
            status.setMsg(result.getMessage());
            status.setData(flowInstance.getId());
        }
        return JsonUtil.serialize(status);
    }

    /**
     * 通过业务实体ID获取条件POJO属性说明
     * @param businessModelId
     * @return
     * @throws ClassNotFoundException
     */
    @ResponseBody
    @RequestMapping(value = "getPropertiesForConditionPojoByBusinessModelId", method = RequestMethod.POST)
    public String getPropertiesForConditionPojoByBusinessModelId(String businessModelId) throws  ClassNotFoundException{
        IConditionServer proxy = ApiClient.createProxy(IConditionServer.class);
        Map<String, String> result = proxy.getPropertiesForConditionPojoByBusinessModelId(businessModelId);
        return JsonUtil.serialize(result);
    }


    @ResponseBody
    @RequestMapping(value = "listAllWorkPage", method = RequestMethod.POST)
    public String listAllWorkPage(String businessModelId){
    IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        List<WorkPageUrl> result = proxy.findSelectEdByBusinessModelId(businessModelId);
        return JsonUtil.serialize(result);
    }

}
