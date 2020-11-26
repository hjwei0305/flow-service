package com.ecmp.flow.api;


import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.DisagreeReason;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;

@Path("disagreeReason")
@Api(value = "IDisagreeReasonService 不同意原因服务API接口")
public interface IDisagreeReasonService extends IBaseService<DisagreeReason, String> {



}
