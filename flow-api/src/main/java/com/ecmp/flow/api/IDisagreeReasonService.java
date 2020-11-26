package com.ecmp.flow.api;


import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.DisagreeReason;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("disagreeReason")
@Api(value = "IDisagreeReasonService 不同意原因服务API接口")
public interface IDisagreeReasonService extends IBaseService<DisagreeReason, String> {

    /**
     * 通过流程类型ID获取不同意原因
     *
     * @param typeId
     * @return ResponseData.data=List<DisagreeReason>
     */
    @GET
    @Path("getDisagreeReasonByTypeId")
    @ApiOperation(value = "通过流程类型ID获取不同意原因", notes = "通过流程类型ID获取不同意原因")
    ResponseData getDisagreeReasonByTypeId(@QueryParam("typeId") String typeId);


}
