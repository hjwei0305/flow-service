package com.ecmp.flow.api.common.api;

import com.ecmp.flow.clientapi.ICommonConditionService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("condition")
@Api(value = "IFlowCommonConditionService 条件通用服务API接口")
public interface IFlowCommonConditionService extends ICommonConditionService {
}
