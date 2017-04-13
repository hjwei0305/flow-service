package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowServiceUrlService;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程服务地址管理控制器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/30 16:10      陈飞(Vision.Mac)            新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/maindata/flowServiceUrl")
public class FlowServiceUrlController {

    @RequestMapping()
    public String showFlowServiceUrl(Model model) {
        return "maindata/FlowServiceUrlView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public Object find() throws JsonProcessingException {
      //  System.out.println("---------------------------------------------");
        IFlowServiceUrlService proxy = ApiClient.createProxy(IFlowServiceUrlService.class);
        List<FlowServiceUrl> flowServiceUrlList = proxy.findAll();
        for (int i=0;i<flowServiceUrlList.size();i++){
            System.out.println(flowServiceUrlList.get(i));
        }
        String flowServiceUrl = JsonUtil.serialize(flowServiceUrlList);
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
        OperateStatus operateStatus = new OperateStatus(result.successful(),result.getMessage());
        String updateSuccess = JsonUtil.serialize(operateStatus);
        return updateSuccess;
    }
}
