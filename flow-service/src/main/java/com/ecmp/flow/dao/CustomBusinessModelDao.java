package com.ecmp.flow.dao;

import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * *************************************************************************************************
 * </p><p>
 * 实现功能：应用模块
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 版本          变更时间             变更人                     变更原因
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 1.0.00      2017/09/06 11:39      谭军(tanjun)                新建
 * </p><p>
 * *************************************************************************************************
 * </p>
 */
@Repository
public interface CustomBusinessModelDao {
    @Transactional(readOnly = true)
    public List<BusinessModel> findByAppModuleCodes(List<String> codeList);
}
