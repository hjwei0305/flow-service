package com.ecmp.flow.vo.conditon;

import com.ecmp.flow.constant.ConditionAnnotaion;
import com.ecmp.flow.entity.IConditionPojo;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 默认业务对象Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/15 11:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */

public class DefaultBusinessModelCondition extends com.ecmp.flow.entity.DefaultBusinessModel implements IConditionPojo {

    @ConditionAnnotaion(name="名称")
    public String getName() {
        return super.getName();
    }

    @ConditionAnnotaion(name="id序号",rank = -1)
    public String getId() {
        return super.getId();
    }

    @ConditionAnnotaion(name="工作说明",rank = 1)
    public String getWorkCaption(){return super.getWorkCaption();}


    public void init(){
          this.setName("name");
          this.setId("id");
          this.setWorkCaption("work caption");
    }

}
