package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowServiceUrlService;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.List;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/flowServiceUrl")
public class FlowServiceUrlController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowServiceUrlView";
    }

    /**
     * 查询流程服务地址
     * @param request
     * @return 服务地址清单
     * @throws ParseException
     */
    @RequestMapping(value = "listServiceUrl")
    @ResponseBody
    public String listServiceUrl(String busModelId) throws ParseException {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        List<FlowServiceUrl> flowServiceUrlPageResult = proxy.findByBusinessModelId(busModelId);
        status.setData(flowServiceUrlPageResult);
        return JsonUtil.serialize(status);
    }

    /**
     * 根据id删除服务地址
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 修改服务地址
     * @param flowServiceUrl
     * @return 操作结果
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(FlowServiceUrl flowServiceUrl) {
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        OperateResultWithData<FlowServiceUrl> result = proxy.save(flowServiceUrl);
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }
}
