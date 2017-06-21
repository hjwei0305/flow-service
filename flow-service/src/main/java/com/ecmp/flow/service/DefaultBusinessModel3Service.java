package com.ecmp.flow.service;

import com.ecmp.config.util.NumberGenerator;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.Validation;
import com.ecmp.flow.api.IDefaultBusinessModel3Service;
import com.ecmp.flow.dao.DefaultBusinessModel3Dao;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel3;
import com.ecmp.flow.util.CodeGenerator;
import com.ecmp.vo.OperateResultWithData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class DefaultBusinessModel3Service extends BaseEntityService<DefaultBusinessModel3> implements IDefaultBusinessModel3Service {

    @Autowired
    private DefaultBusinessModel3Dao defaultBusinessModel3Dao;

    protected BaseEntityDao<DefaultBusinessModel3> getDao(){
        return this.defaultBusinessModel3Dao;
    }

    /**
     * 数据保存操作
     */
    @SuppressWarnings("unchecked")
    public OperateResultWithData<DefaultBusinessModel3> save(DefaultBusinessModel3 entity) {
        Validation.notNull(entity, "持久化对象不能为空");
//        String businessCode = NumberGenerator.GetNumber(DefaultBusinessModel3.class);
        String businessCode = CodeGenerator.genCodes(6,1).get(0);
        if(StringUtils.isEmpty(entity.getBusinessCode())){
            entity.setBusinessCode(businessCode);
        }
        return super.save(entity);
    }
}
