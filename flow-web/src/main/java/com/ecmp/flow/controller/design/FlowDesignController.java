package com.ecmp.flow.controller.design;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.*;
import com.ecmp.flow.basic.vo.Position;
import com.ecmp.flow.basic.vo.PositionCategory;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.GenericType;
import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ecmp.flow.api.client.util.ExpressionUtil.getAppModule;

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

    @RequestMapping(value = "import", method = RequestMethod.GET)
    public String importExcel() {
//        model.addAttribute("orgName", orgName);
        return "/design/ImportView";
    }

    @ResponseBody
    @RequestMapping(value = "importCols", method = RequestMethod.POST)
    public OperateStatus importCols() {
        OperateStatus status = OperateStatus.defaultSuccess();
        Col col = new Col();
        Content s1 = new Content("s1","s1",1);
        Content s2 = new Content("s2","s2",2);
        Content s3 = new Content("s3","s3",3);

        col.getSource().add(s1);
        col.getSource().add(s2);
        col.getSource().add(s3);

        Content t1 = new Content("t1","t1",1);
        Content t2 = new Content("t2","t2",2);
        Content t3 = new Content("t3","t3",3);
        col.getTarget().add(t1);
        col.getTarget().add(t2);
        col.getTarget().add(t3);

        status.setData(col);
       return status;
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
    public OperateStatus save(String def, boolean deploy) throws JAXBException, UnsupportedEncodingException, CloneNotSupportedException {
        OperateStatus status = OperateStatus.defaultSuccess();
        JSONObject defObj = JSONObject.fromObject(def);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        String id=definition.getProcess().getId();
        String reg="^[a-zA-Z][A-Za-z0-9]{5,79}$";
        if(!id.matches(reg)){
            status=new OperateStatus(false, ContextUtil.getMessage("10001"));
            return status;
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
                proxy2.deployById(result.getData().getFlowDefination().getId());
            }
            status.setSuccess(result.successful());
            status.setMsg(result.getMessage());
            status.setData(result);
        }
        return status;
    }

    /**
     * 通过业务实体ID获取条件POJO属性说明
     *
     * @param businessModelCode
     * @return
     * @throws ClassNotFoundException
     */
    @ResponseBody
    @RequestMapping(value = "getProperties", method = RequestMethod.POST)
    public OperateStatus getProperties(String businessModelCode) throws ClassNotFoundException {
        OperateStatus status = OperateStatus.defaultSuccess();
        Map<String, String> result=null;
        IBusinessModelService  businessModelService = ApiClient.createProxy(IBusinessModelService.class);
        BusinessModel businessModel = businessModelService.findByClassName(businessModelCode);
        if (businessModel != null) {
            String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddressConfig();
            String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
            String clientApiUrl = clientApiBaseUrl + businessModel.getConditonProperties();
            Map<String,Object> params = new HashMap();
            params.put("businessModelCode",businessModelCode);
            params.put("all",false);
            result = ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,String> >() {},params);
        }
        status.setData(result);
        return status;
    }


    /**
     * 获取工作界面
     *
     * @param businessModelId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "listAllWorkPage", method = RequestMethod.POST)
    public OperateStatus listAllWorkPage(String businessModelId) {
        OperateStatus status = OperateStatus.defaultSuccess();
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        List<WorkPageUrl> result = proxy.findSelectEdByBusinessModelId(businessModelId);
        status.setData(result);
        return status;
    }

    /**
     * 获取流程设计
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getEntity", method = RequestMethod.POST)
    public OperateStatus getEntity(String id, int versionCode) {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        FlowDefVersion data = proxy.getFlowDefVersion(id, versionCode);
        status.setData(data);
        return status;
    }

    /**
     * 通过流程版本获取流程设计内容，提供编辑
     *
     * @param flowDefVersionId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getEntityByVersionId", method = RequestMethod.POST)
    public OperateStatus getEntity(String flowDefVersionId) {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        FlowDefVersion data = proxy.findOne(flowDefVersionId);
        status.setData(data);
        return status;
    }

    /**
     * 获取岗位类别列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "listPosType")
    public List<PositionCategory> listPositonType(String notInIds) {
//        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
//        List<PositionCategory> data = proxy.findAll();
        String url = Constants.getBasicPositioncategoryFindallUrl();
        List<PositionCategory> positionCategoryList  = ApiClient.getEntityViaProxy(url,new GenericType<List<PositionCategory>>() {},null);
        return positionCategoryList;
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
//        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
//        return proxy.findByPage(search);
        String url = Constants.getBasicPositionFindbypageUrl();
        PageResult<Position> positionList   = ApiClient.postViaProxyReturnResult(url,new GenericType<PageResult<Position>>() {},search);
        return  positionList;
    }

    /**
     * 查询流程服务地址
     *
     * @return 服务地址清单
     * @throws ParseException
     */
    @RequestMapping(value = "listAllServiceUrl")
    @ResponseBody
    public OperateStatus listServiceUrl(String busModelId) throws ParseException {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        List<FlowServiceUrl> flowServiceUrlPageResult = proxy.findByBusinessModelId(busModelId);
        status.setData(flowServiceUrlPageResult);
        return status;
    }

    /**
     * 根据流程实例获取当前流程所在节点
     * @param id 版本id
     * @param instanceId
     * @return
     */
    @RequestMapping(value = "getLookInfo")
    @ResponseBody
    public OperateStatus getLookInfo(String id, String instanceId) {
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
        return status;
    }

    class Content {
        private String code;
        private String desc;
        private int rank;

        public Content(){

        }
        public Content(String code,String desc,int rank){
             this.code = code;
             this.desc = desc;
             this.rank = rank;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }
    class Col{
        private List<Content> target = new ArrayList<>();
        private List<Content> source = new ArrayList<>();

        public List<Content> getTarget() {
            return target;
        }

        public void setTarget(List<Content> target) {
            this.target = target;
        }

        public List<Content> getSource() {
            return source;
        }

        public void setSource(List<Content> source) {
            this.source = source;
        }
    }
}
