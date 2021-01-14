package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.api.IFlowDesignService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.basic.vo.Organization;
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
            return ResponseData.operationFailure("参数id为空");
        }
        if (versionCode == null) {
            return ResponseData.operationFailure("参数versionCode为空");
        }
        try {
            FlowDefVersion data = flowDefinationService.getFlowDefVersion(id, versionCode, businessModelCode, businessId);
            return ResponseData.operationSuccessWithData(data);
        } catch (Exception e) {
            LogUtil.error("获取流程定义出错!", e);
            throw new FlowException("获取流程定义出错,详情请查看日志！");
        }
    }


    @Override
    public ResponseData save(SaveEntityVo entityVo) throws JAXBException, UnsupportedEncodingException, CloneNotSupportedException {
        String def = entityVo.getDef();
        Boolean deploy = entityVo.getDeploy();
        if (StringUtils.isNotEmpty(def) || deploy != null) {
            JSONObject defObj = JSONObject.fromObject(def);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            String id = definition.getProcess().getId();
            String reg = "^[a-zA-Z][A-Za-z0-9]{5,79}$";
            if (!id.matches(reg)) {
                return ResponseData.operationFailure("流程代码以字母开头，允许数字或字母，且长度在6-80之间！");
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
            return ResponseData.operationFailure("参数不能为空！");
        }
    }


    @Override
    public ResponseData listAllServiceUrl(String busModelId) throws ParseException {
        if (StringUtils.isEmpty(busModelId)) {
            return ResponseData.operationFailure("参数不能为空！");
        }
        List<FlowServiceUrl> flowServiceUrlPageResult = flowServiceUrlService.findByBusinessModelId(busModelId);
        return ResponseData.operationSuccessWithData(flowServiceUrlPageResult);
    }


    @Override
    public ResponseData listAllWorkPage(String businessModelId) {
        if (StringUtils.isEmpty(businessModelId)) {
            return ResponseData.operationFailure("参数不能为空！");
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
                    return ResponseData.operationFailure("加载固化执行人列表失败，详情请查看日志!");
                }
            }
        }
        return ResponseData.operationSuccessWithData(data);
    }


}
