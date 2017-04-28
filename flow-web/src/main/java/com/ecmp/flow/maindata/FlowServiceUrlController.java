package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.api.IFlowServiceUrlService;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
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
@RequestMapping(value = "/maindata/flowServiceUrl")
public class FlowServiceUrlController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowServiceUrlView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException {
      //  System.out.println("---------------------------------------------");
        Search search = SearchUtil.genSearch(request);
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        PageResult<FlowServiceUrl> flowServiceUrlPageResult = proxy.findByPage(search);
        String flowServiceUrl = JsonUtil.serialize(flowServiceUrlPageResult);
        return flowServiceUrl;
    }


    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(id);
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage());
       String delSuccess = JsonUtil.serialize(operateStatus);
        return delSuccess;
    }

    @RequestMapping(value = "update")
    @ResponseBody
    public String update(FlowServiceUrl flowServiceUrl) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(flowServiceUrl);
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        OperateResultWithData<FlowServiceUrl> result = proxy.save(flowServiceUrl);
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage(),result.getData());
        String updateSuccess = JsonUtil.serialize(operateStatus);
        return updateSuccess;
    }
}
