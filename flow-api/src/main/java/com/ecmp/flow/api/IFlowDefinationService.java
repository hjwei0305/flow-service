package com.ecmp.flow.api;

import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.basic.vo.Employee;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.vo.FlowStartResultVO;
import com.ecmp.flow.vo.FlowStartVO;
import com.ecmp.flow.vo.UserQueryVo;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：流程定义服务API接口定义
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p>
 * <p>
 * 版本          变更时间             变更人                     变更原因
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p>
 * <p>
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)                新建
 * </p>
 * *************************************************************************************************
 */
@Path("flowDefination")
@Api(value = "IFlowDefinationService 流程定义服务API接口")
public interface IFlowDefinationService extends IBaseService<FlowDefination, String> {

    /**
     * 保存一个实体
     *
     * @param flowDefination 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体", notes = "测试 保存实体")
    OperateResultWithData<FlowDefination> save(FlowDefination flowDefination);

    /**
     * 获取分页数据
     *
     * @return 实体清单
     */
    @POST
    @Path("findByPage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    PageResult<FlowDefination> findByPage(Search searchConfig);

    /**
     * 通过流程定义ID发布最新版本的流程
     *
     * @param id 实体
     * @return 发布id
     * @throws UnsupportedEncodingException 编码异常
     */
    @POST
    @Path("deployById")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程定义ID发布最新版本的流程", notes = "测试")
    public String deployById(String id) throws UnsupportedEncodingException;

    /**
     * 通过流程版本ID发布指定版本的流程
     *
     * @param id 实体
     * @return 发布id
     * @throws UnsupportedEncodingException 编码异常
     */
    @POST
    @Path("deployByVersionId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程版本ID发布指定版本的流程", notes = "测试")
    public String deployByVersionId(String id) throws UnsupportedEncodingException;

    /**
     * 通过ID启动流程实体
     *
     * @param id          流程id
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @POST
    @Path("startById/{id}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID启动流程实体,附加启动ID", notes = "测试")
    public FlowInstance startById(@PathParam("id") String id, @PathParam("businessKey") String businessKey, Map<String, Object> variables);

    /**
     * 通过ID启动流程实体
     *
     * @param id          流程id
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @POST
    @Path("startByIdWithStartUserId/{id}/{startUserId}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID启动流程实体", notes = "测试")
    public FlowInstance startById(@PathParam("id") String id, @PathParam("startUserId") String startUserId, @PathParam("businessKey") String businessKey, Map<String, Object> variables);

    /**
     * 通过Key启动流程实体
     *
     * @param key         定义Key
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @POST
    @Path("startByKey/{key}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Key启动流程实体", notes = "测试")
    public FlowInstance startByKey(@PathParam("key") String key, @PathParam("businessKey") String businessKey, Map<String, Object> variables);

    /**
     * 通过Key启动流程实体
     *
     * @param key         定义Key
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @POST
    @Path("startByKeyWithStartUserId/{key}/{startUserId}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Key启动流程实体,附加启动用户ID", notes = "测试")
    public FlowInstance startByKey(@PathParam("key") String key, @PathParam("startUserId") String startUserId, @PathParam("businessKey") String businessKey, Map<String, Object> variables);


    @GET
    @Path("getFlowDefVersion")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Key启动流程实体,附加启动用户ID", notes = "测试")
    public FlowDefVersion getFlowDefVersion(@QueryParam("id") String id,
                                            @QueryParam("versionCode") Integer versionCode,
                                            @QueryParam("businessModelCode") String businessModelCode,
                                            @QueryParam("businessId") String businessId);


    /**
     * 重置流程图位置
     *
     * @param id
     * @return
     */
    @GET
    @Path("resetPosition/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程定义key重置流程图位置", notes = "测试")
    ResponseData resetPosition(@PathParam("id") String id);



    /**
     * 重置流程图位置(兼容网关)
     *
     * @param id
     * @return
     */
    @GET
    @Path("resetPositionOfGateway")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程定义key重置流程图位置(兼容网关)", notes = "通过流程定义key重置流程图位置(兼容网关)")
    ResponseData resetPositionOfGateway(@QueryParam("id") String id);


//    /**
//     * 通过businessModelCode启动流程实体
//     * @param typeCode typeCode 类型代码
//     * @param startUserId 流程启动人
//     * @param businessKey 业务KEY
//     * @param variables  其他参数
//     * @return 流程实例
//     */
//    @POST
//    @Path("startByBusinessModelWithStartUserId/{typeCode}/{startUserId}/{businessKey}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "通过businessModelCode启动流程实体,附加启动用户ID",notes = "测试")
//    public FlowInstance startByTypeCode(@PathParam("typeCode") String typeCode,@PathParam("startUserId") String startUserId,@PathParam("businessKey")String businessKey, Map<String, Object> variables);


    /**
     * 通过vo对象启动流程实体
     *
     * @param flowStartVO 启动传输对象
     * @return FlowStartResultVO 启动结果
     * @throws NoSuchMethodException 方法找不到异常
     */
    @POST
    @Path("startByVO")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过vo对象启动流程实体", notes = "测试")
    public OperateResultWithData<FlowStartResultVO> startByVO(FlowStartVO flowStartVO) throws NoSuchMethodException;

    /**
     * 切换版本状态
     *
     * @param id     单据id
     * @param status 状态
     * @return 操作结果
     */
    @POST
    @Path("changeStatus/{id}/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "json流程定义保存实体", notes = "测试 json流程定义保存实体")
    public OperateResultWithData<FlowDefination> changeStatus(@PathParam("id") String id, @PathParam("status") FlowDefinationStatus status);


    /**
     * 切换版本状态（兼容网关）
     *
     * @param id     单据id
     * @param status 状态
     * @return 操作结果
     */
    @POST
    @Path("changeStatusOfGateway")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "json流程定义保存实体（兼容网关）", notes = "json流程定义保存实体（兼容网关）")
    OperateResultWithData<FlowDefination> changeStatusOfGateway(@QueryParam("id") String id, @QueryParam("status") FlowDefinationStatus status);



    /**
     * 切换版本状态
     *
     * @param flowTypeId 流程类型id
     * @param expression 表达式UEL
     * @return 操作结果
     * @throws ClassNotFoundException    类找不到异常
     * @throws IllegalAccessException    访问异常
     * @throws IllegalArgumentException  参数不匹配异常
     * @throws InvocationTargetException 目标异常
     * @throws InstantiationException    实例化异常
     * @throws NoSuchMethodException     方法找不到异常
     */
    @POST
    @Path("validateExpression")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证UEL表达式是否正常", notes = "验证UEL表达式是否正常")
    public OperateResultWithData<FlowDefination> validateExpression(@QueryParam("flowTypeId") String flowTypeId, @QueryParam("expression") String expression) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException;

    /**
     * 通过key查找流程定义
     *
     * @param key 定义key
     * @return 流程定义
     */
    @GET
    @Path("findByKey/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过key查找流程定义", notes = "通过key查找流程定义")
    public FlowDefination findByKey(@PathParam("key") String key);

    @POST
    @Path("testStart")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "test", notes = "test")
    public void testStart(@QueryParam("flowKey") String flowKey, @QueryParam("businessKey") String businessKey);

    /**
     * 获取所有组织机构
     *
     * @return 组织机构清单
     */
    @POST
    @Path("listAllOrgs")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取所有组织机构", notes = "获取所有组织机构")
    ResponseData listAllOrgs();


    /**
     * 获取所有有权限的组织机构
     *
     * @return 组织机构清单
     */
    @POST
    @Path("listAllOrgByPower")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取所有组织机构", notes = "获取所有组织机构")
    ResponseData listAllOrgByPower();


    /**
     * 根据组织机构的id获取员工(不包含冻结)
     *
     * @param organizationId 组织机构的id
     * @return 员工清单
     */
    @GET
    @Path("listAllUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取组织机构下员工", notes = "获取组织机构下员工")
    ResponseData listAllUser(@QueryParam("organizationId")String organizationId);


    /**
     * 获取组织机构下员工（可以包含子节点：react版本最新使用）
     * @param userQueryVo  查询对象VO
     * @return
     */
    @POST
    @Path("listUserByOrg")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取组织机构下员工（可以包含子节点）", notes = "获取组织机构下员工（可以包含子节点）")
    ResponseData listUserByOrg(UserQueryVo userQueryVo);

    @POST
    @Path("listUserByOrgByWeb")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取组织机构下员工（可以包含子节点）", notes = "获取组织机构下员工（可以包含子节点）")
    PageResult<Employee> listUserByOrgByWeb(UserQueryVo userQueryVo);
}
