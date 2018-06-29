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

    private Integer customeInt ;

    @ConditionAnnotaion(name="名称")
    public String getName() {
        return super.getName();
    }

//    @ConditionAnnotaion(name="id序号",rank = -1)
//    public String getId() {
//        return super.getId();
//    }

    @ConditionAnnotaion(name="单价",rank = 3)
    public double getUnitPrice() {
        return super.getUnitPrice();
    }

    @ConditionAnnotaion(name="数量",rank = 4)
    public int getCount() {
        return super.getCount();
    }

    @ConditionAnnotaion(name="额外属性",rank = 2)
    public Integer getCustomeInt() {
        return customeInt;
    }

    @Override
    @ConditionAnnotaion(name="额外属性",canSee = false)
    public int getPriority() {
        return super.getPriority();
    }

    public void setCustomeInt(Integer customeInt) {
        this.customeInt = customeInt;
    }

    public void init(){
          this.setName("name");
          this.setId("id");
          this.setWorkCaption("work caption");
          this.setCount(0);
          this.setUnitPrice(0.0);
          this.setCustomeInt(0);
    }

    public void customLogic(){
        customeInt = this.getPriority()+10;
    }

}
