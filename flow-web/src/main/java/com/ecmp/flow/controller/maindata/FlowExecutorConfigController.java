package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowExecutorConfigService;
import com.ecmp.flow.entity.FlowExecutorConfig;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程自定义执行人配置控制层方法实现
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/7/5 9:30      陈爽(chenshuang)            新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/flowExecutorConfig")
public class FlowExecutorConfigController {

    /**
     * 查询自定义执行人配置
     * @param request
     * @return 自定义执行人配置清单
     * @throws ParseException
     */
    @RequestMapping(value = "listCombo")
    @ResponseBody
    public OperateStatus listCombo(ServletRequest request) throws ParseException {
        OperateStatus status = OperateStatus.defaultSuccess();
        Search search = SearchUtil.genSearch(request);
        IFlowExecutorConfigService proxy = ApiClient.createProxy(IFlowExecutorConfigService.class);
        status.setData(proxy.findByFilters(search));
        return status;
    }


    /**
     * 查询自定义执行人配置
     * @param request
     * @return 自定义执行人配置清单
     * @throws ParseException
     */
    @RequestMapping(value = "list")
    @ResponseBody
    public List<FlowExecutorConfig> list(ServletRequest request) throws ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowExecutorConfigService proxy = ApiClient.createProxy(IFlowExecutorConfigService.class);
        return proxy.findByFilters(search);
    }

    /**
     * 根据id删除自定义执行人配置
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IFlowExecutorConfigService proxy = ApiClient.createProxy(IFlowExecutorConfigService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 修改自定义执行人配置
     * @param flowExecutorConfig
     * @return 操作结果
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(FlowExecutorConfig flowExecutorConfig) {
        IFlowExecutorConfigService proxy = ApiClient.createProxy(IFlowExecutorConfigService.class);
        OperateResultWithData<FlowExecutorConfig> result = proxy.save(flowExecutorConfig);
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }
}
