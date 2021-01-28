package com.ecmp.flow.service;

import com.ecmp.flow.api.common.api.IFlowCommonConditionService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/7/25 17:13      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component
@Deprecated
public class FlowCommonConditionService extends CommonConditionService implements IFlowCommonConditionService {


    @Override
    public Map<String, String> propertiesRemark(String businessModelCode) throws ClassNotFoundException {
        Map<String, String> map = new HashMap<>();
        map.put("count", "【数字】，表示订单中购买的数量");
        map.put("unitPrice", "【数字】，表示订单中单个物体的价格");
        map.put("customeInt", "【数字】，1：表示普通订单，2：表示未检测的特殊订单，3：表示检验合格的特殊订单...");
        return map;
    }


}
