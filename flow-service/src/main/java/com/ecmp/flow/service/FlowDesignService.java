package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.api.IFlowDesignService;
import com.ecmp.flow.basic.vo.OrganizationDimension;
import com.ecmp.flow.basic.vo.Position;
import com.ecmp.flow.basic.vo.PositionCategory;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.vo.SaveEntityVo;
import com.ecmp.flow.vo.SearchVo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Service
public class FlowDesignService implements IFlowDesignService {

    @Autowired
    private FlowDefinationService flowDefinationService;

    @Autowired
    private FlowDefVersionService flowDefVersionService;

    @Autowired
    private FlowServiceUrlService flowServiceUrlService;

    @Autowired
    private WorkPageUrlService workPageUrlService;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private FlowInstanceService flowInstanceService;

    @Autowired
    private FlowSolidifyExecutorService flowSolidifyExecutorService;


    @Override
    public ResponseData getEntity(String id, Integer versionCode, String businessModelCode, String businessId) {
        if (StringUtils.isEmpty(id)) {
            return ResponseData.operationFailure("10113", "id");
        }
        if (versionCode == null) {
            return ResponseData.operationFailure("10113", "versionCode");
        }
        try {
            FlowDefVersion data = flowDefinationService.getFlowDefVersion(id, versionCode, businessModelCode, businessId);
            return ResponseData.operationSuccessWithData(data);
        } catch (Exception e) {
            LogUtil.error("获取流程定义出错!", e);
            throw new FlowException(ContextUtil.getMessage("10114"));
        }
    }


    /**
     * 验证流程图是否合规,并且对有并行网关和包容网关的流程写标记
     *
     * @param def 流程图json字符串
     * @return
     */
    public ResponseData checkFlowJson(String def) throws Exception {
        JSONObject defObj = JSONObject.fromObject(def);
        JSONObject processObj = defObj.getJSONObject("process");
        JSONObject nodesObj = processObj.getJSONObject("nodes");
        //找到起点节点并且检查并行网关和包容网关是否成对
        ResponseData checkRes = getStartNodeKey(nodesObj);
        if (checkRes.getSuccess()) {
            String startKey = (String) checkRes.getData();




            return  ResponseData.operationSuccessWithData(defObj.toString());
        } else {
            return checkRes;
        }
    }


    /**
     * 找到起点节点并且检查并行网关和包容网关是否成对
     *
     * @param nodesObj
     * @return
     */
    public ResponseData getStartNodeKey(JSONObject nodesObj) {
        Iterator iterator = nodesObj.keys();
        String startNodeKey = "";
        int parallelGateway = 0; //并行网关个数
        int inclusiveGateway = 0;//包容网关个数
        while  (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (key.contains("StartEvent")) {
                startNodeKey = key;
            } else if (key.contains("ParallelGateway")) {
                ++parallelGateway;
            } else if (key.contains("InclusiveGateway")) {
                ++inclusiveGateway;
            }
        }
        if (parallelGateway % 2 != 0) {
            //并行网关必须成对使用，当前流程定义只有【{0}】个并行网关！
            return  ResponseData.operationFailure("10399",parallelGateway);
        }
        if(inclusiveGateway % 2 !=0){
            //包容网关必须成对使用，当前流程定义只有【{0}】个包容网关！
            return  ResponseData.operationFailure("10400",parallelGateway);
        }
        return ResponseData.operationSuccessWithData(startNodeKey);
    }


    @Override
    public ResponseData save(SaveEntityVo entityVo) throws JAXBException, UnsupportedEncodingException, CloneNotSupportedException {
        String def = entityVo.getDef();
        Boolean deploy = entityVo.getDeploy();
        if (StringUtils.isNotEmpty(def) || deploy != null) {
            JSONObject defObj = JSONObject.fromObject(def);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            if (StringUtils.isNotEmpty(entityVo.getFlowDefinationId())) {
                //仅用于数据同步后的统一发布
                definition.setId(entityVo.getFlowDefinationId());
            }
            String id = definition.getProcess().getId();
            String reg = "^[a-zA-Z][A-Za-z0-9]{5,79}$";
            if (!id.matches(reg)) {
                return ResponseData.operationFailure("10115");
            }

            //验证流程图是否合规,并且对有并行网关和包容网关的流程写标记
            try {
                ResponseData checkRes = checkFlowJson(def);
                if (checkRes.getSuccess()) {
                    def = (String) checkRes.getData();
                } else {
                    return checkRes;
                }
            } catch (Exception e) {
                LogUtil.error("验证流程图合规性报错:{}", e.getMessage(), e);
                return ResponseData.operationFailure("10398", e.getMessage());
            }

            definition.setDefJson(def);
            OperateResultWithData<FlowDefVersion> result;
            if (!deploy) {
                result = flowDefVersionService.save(definition);
                if (result.successful()) {
                    return ResponseData.operationSuccessWithData(result.getData());
                }
            } else {
                result = flowDefVersionService.save(definition);
                if (result.successful()) {
                    flowDefinationService.deployById(result.getData().getFlowDefination().getId());
                    return ResponseData.operationSuccessWithData(result.getData());
                }
            }
            return ResponseData.operationFailure(result.getMessage());
        } else {
            return ResponseData.operationFailure("10006");
        }
    }


    @Override
    public ResponseData listAllServiceUrl(String busModelId) throws ParseException {
        if (StringUtils.isEmpty(busModelId)) {
            return ResponseData.operationFailure("10006");
        }
        List<FlowServiceUrl> flowServiceUrlPageResult = flowServiceUrlService.findByBusinessModelId(busModelId);
        return ResponseData.operationSuccessWithData(flowServiceUrlPageResult);
    }


    @Override
    public ResponseData listAllWorkPage(String businessModelId) {
        if (StringUtils.isEmpty(businessModelId)) {
            return ResponseData.operationFailure("10006");
        }
        List<WorkPageUrl> result = workPageUrlService.findSelectEdByBusinessModelId(businessModelId);
        return ResponseData.operationSuccessWithData(result);
    }

    @Override
    public PageResult<Position> listPositon(SearchVo searchVo) {
        Search search = new Search();
        search.addQuickSearchProperty("code");
        search.addQuickSearchProperty("name");
        search.addQuickSearchProperty("organization.name");
        if (StringUtils.isNotEmpty(searchVo.getQuick_value())) {
            search.setQuickSearchValue(searchVo.getQuick_value());
        }

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(searchVo.getPage());
        pageInfo.setRows(searchVo.getRows());
        search.setPageInfo(pageInfo);

        if (StringUtils.isNotEmpty(searchVo.getSidx())) {
            SearchOrder searchOrder = new SearchOrder();
            if ("asc".equals(searchVo.getSord())) {
                search.addSortOrder(searchOrder.asc(searchVo.getSidx()));
            } else {
                search.addSortOrder(searchOrder.desc(searchVo.getSidx()));
            }
        }

        PageResult<Position> result = flowCommonUtil.getBasicPositionFindbypage(search);
        return result;
    }

    @Override
    public List<PositionCategory> listPositonType() {
        return flowCommonUtil.getBasicPositioncategoryFindall();
    }

    @Override
    public List<OrganizationDimension> listOrganizationDimension() {
        return flowCommonUtil.getBasicOrgDimension();
    }


    @Override
    public ResponseData getLookInfo(String id, String instanceId) {
        FlowDefVersion def = null;
        Map<String, Object> data = new HashedMap();
        String businessId = "";
        Boolean ended = true;
        if (StringUtils.isNotEmpty(instanceId)) {
            FlowInstance flowInstance = flowInstanceService.findOne(instanceId);
            if (flowInstance != null) {
                def = flowInstance.getFlowDefVersion();
                businessId = flowInstance.getBusinessId();
                ended = flowInstance.isEnded();
            }
        }
        if (def == null) {
            def = flowDefVersionService.findOne(id);
        }
        data.put("def", def);
        if (StringUtils.isNotEmpty(instanceId)) {
            Map<String, String> nodeIds = flowInstanceService.currentNodeIds(instanceId);
            data.put("currentNodes", nodeIds);
        } else {
            data.put("currentNodes", "[]");
        }
        //如果是固化流程，加载配合执行人信息
        if (def.getSolidifyFlow() != null && def.getSolidifyFlow() == true && ended == false) {
            if (StringUtils.isNotEmpty(businessId)) {
                try {
                    ResponseData res = flowSolidifyExecutorService.getExecuteInfoByBusinessId(businessId);
                    if (res.getSuccess()) {
                        data.put("solidifyExecutorsInfo", res.getData());
                    } else {
                        data.put("solidifyExecutorsInfo", null);
                    }
                } catch (Exception e) {
                    LogUtil.error("加载固化执行人列表失败，详情请查看日志!", e);
                    return ResponseData.operationFailure("10116");
                }
            }
        }
        return ResponseData.operationSuccessWithData(data);
    }


}
