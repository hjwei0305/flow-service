package com.ecmp.flow.api;


import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import com.ecmp.flow.vo.FlowSolidifyExecutorVO;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import java.util.List;

@Path("flowSolidifyExecutor")
@Api(value = "IFlowSolidifyExecutorService 固化流程执行人接口")
public interface IFlowSolidifyExecutorService  extends IBaseService<FlowSolidifyExecutor, String> {


    /**
     * 通过执行人VO集合保存固化流程的执行人信息
     * @param executorVoList 固化执行人VO集合
     * @return
     */
    @POST
    @Path("saveByExecutorVoList")
    @ApiOperation(value = "通过执行人VO集合保存固化流程的执行人信息",notes = "通过执行人VO集合保存固化流程的执行人信息")
    ResponseData saveByExecutorVoList(List<FlowSolidifyExecutorVO> executorVoList, @QueryParam("businessModelCode") String businessModelCode, @QueryParam("businessId") String businessId);


}
