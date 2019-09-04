package com.ecmp.flow.api;


import com.ecmp.flow.vo.SaveEntityVo;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程设计器默认服务接口（原FlowDesignController的方法提供成接口）
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2019/9/4            何灿坤                      新建
 * <p/>
 * *************************************************************************************************
 */

@Path("design")
@Api(value = "IFlowDesignService 流程设计器默认服务接口")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface IFlowDesignService {


    /**
     * 获取流程设计
     *
     * @param id
     * @return
     */
    @POST
    @Path("getEntity")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取流程设计", notes = "获取流程设计")
    ResponseData getEntity(@QueryParam("id") String id,
                           @QueryParam("versionCode") Integer versionCode,
                           @QueryParam("businessModelCode") String businessModelCode,
                           @QueryParam("businessId") String businessId);




    /**
     * 流程设计保存(发布)
     * @return
     */
    @POST
    @Path("save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "流程设计保存(发布)", notes = "流程设计保存(发布)")
    ResponseData save(SaveEntityVo entityVo)throws JAXBException, UnsupportedEncodingException, CloneNotSupportedException;





    /**
     * 查询流程服务地址
     *
     * @return
     */
    @POST
    @Path("listAllServiceUrl")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询流程服务地址", notes = "查询流程服务地址")
    ResponseData listAllServiceUrl(@QueryParam("busModelId") String busModelId)throws ParseException;





}
