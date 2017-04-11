package com.ecmp.flow.api;

import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程服务地址服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowTask")
@Api(value = "IFlowTaskService 流程服务地址服务API接口")
public interface IFlowTaskService extends IBaseService<FlowTask, String>{
    /**
     * 任务签收
     * @param id 任务id
     * @param userId 用户账号
     * @return 操作结果
     */
    @POST
    @Path("claim")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "签收任务",notes = "测试")
    public OperateResult claim(String id, String userId);

    /**
     * 完成任务
     * @param id 任务id
     * @param variables 参数
     * @return 操作结果
     */
    @POST
    @Path("complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "完成任务",notes = "测试")
    public OperateResult complete(String id, Map<String, Object> variables);



    /**
     * 撤回到指定任务节点
     * @param id
     * @return
     */
    @POST
    @Path("rollBackTo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "撤回任务",notes = "测试")
    public  OperateResult rollBackTo(String id);
}
