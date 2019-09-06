package com.ecmp.flow.api;


import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import com.ecmp.flow.vo.FindSolidifyExecutorVO;
import com.ecmp.flow.vo.FlowSolidifyExecutorVO;
import com.ecmp.flow.vo.FlowTaskCompleteWebVO;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("flowSolidifyExecutor")
@Api(value = "IFlowSolidifyExecutorService 固化流程执行人接口")
public interface IFlowSolidifyExecutorService  extends IBaseService<FlowSolidifyExecutor, String> {



    /**
     * 通过VO集合保存固化流程的执行人信息
     * @return
     */
    @POST
    @Path("saveSolidifyInfoByExecutorVos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过执行人VO集合保存固化流程的执行人信息",notes = "通过执行人VO集合保存固化流程的执行人信息")
    ResponseData saveSolidifyInfoByExecutorVos(FindSolidifyExecutorVO findSolidifyExecutorVO);



    /**
     * 通过执行人VO集合保存固化流程的执行人信息
     * @param executorVoList 固化执行人VO集合
     * @return
     */
    @POST
    @Path("saveByExecutorVoList")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过执行人VO集合保存固化流程的执行人信息",notes = "通过执行人VO集合保存固化流程的执行人信息")
    ResponseData saveByExecutorVoList(List<FlowSolidifyExecutorVO> executorVoList, @QueryParam("businessModelCode") String businessModelCode, @QueryParam("businessId") String businessId);


    /**
     * 给FlowTaskCompleteWebVO设置执行人和紧急状态
     * @param list  FlowTaskCompleteWebVO集合
     * @param businessId  业务表单id
     * @return
     */
    @POST
    @Path("setInstancyAndIdsByTaskList")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "给FlowTaskCompleteWebVO设置执行人和紧急状态",notes = "给FlowTaskCompleteWebVO设置执行人和紧急状态")
    ResponseData setInstancyAndIdsByTaskList(List<FlowTaskCompleteWebVO> list, @QueryParam("businessId") String businessId);


    /**
     * 根据表单查询需要自动执行的待办
     * @param businessId
     * @return
     */
    @POST
    @Path("selfMotionExecuteTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据表单查询需要自动执行的待办",notes = "根据表单查询需要自动执行的待办")
    void  selfMotionExecuteTask(@QueryParam("businessId")String businessId);

    /**
     * 根据表单ID查询固化流程执行人配置信息
     * @param businessId  业务表单ID
     * @return
     */
    @GET
    @Path("getExecuteInfoByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据表单ID查询固化流程执行人配置信息",notes = "根据表单ID查询固化流程执行人配置信息")
    ResponseData   getExecuteInfoByBusinessId(@QueryParam("businessId")String businessId);




}
