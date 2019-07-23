package com.ecmp.flow.api;


import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowTaskPush;
import io.swagger.annotations.Api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("flowTaskPush")
@Api(value = "IFlowTaskPushService 流程推送任务服务API接口")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface IFlowTaskPushService extends IBaseService<FlowTaskPush, String> {


}
