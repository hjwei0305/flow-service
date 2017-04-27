package com.ecmp.flow.vo.conditon;

import com.ecmp.flow.constant.ConditionAnnotaion;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.IConditionPojo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 应用模块Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */

public class AppModuleCondition extends AppModule implements IConditionPojo {

    @ConditionAnnotaion(name="名称")
    public String getName() {
        return super.getName();
    }

    @ConditionAnnotaion(name="id序号",rank = 10)
    public String getId() {
        return super.getId();
    }

    @ConditionAnnotaion(name="code",rank = -10)
    public String getCode() {
        return super.getCode();
    }

    public void init(){
          this.setName("name");
          this.setCode("code");
          this.setId("id");
    }

}
