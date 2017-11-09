package com.ecmp.flow.vo;

import com.ecmp.core.search.PageResult;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/10/26 9:32      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowTaskPageResultVO<T extends Serializable> extends PageResult<T>{
    private Long allTotal;//所有待办的总数

    public Long getAllTotal() {
        return allTotal;
    }

    public void setAllTotal(Long allTotal) {
        this.allTotal = allTotal;
    }
}
