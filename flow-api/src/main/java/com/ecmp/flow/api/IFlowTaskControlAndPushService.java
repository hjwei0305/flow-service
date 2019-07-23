package com.ecmp.flow.api;


import com.ecmp.core.api.IBaseRelationService;
import com.ecmp.flow.entity.FlowTaskControlAndPush;
import com.ecmp.flow.entity.FlowTaskPush;
import com.ecmp.flow.entity.FlowTaskPushControl;
import io.swagger.annotations.Api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("flowTaskControlAndPush")
@Api(value = "IFlowTaskControlAndPushService 推送任务关系API接口")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface IFlowTaskControlAndPushService extends IBaseRelationService<FlowTaskControlAndPush, FlowTaskPushControl, FlowTaskPush> {

}
