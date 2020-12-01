package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IDisagreeReasonService;
import com.ecmp.flow.dao.DisagreeReasonDao;
import com.ecmp.flow.entity.DisagreeReason;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;


@Service
public class DisagreeReasonService extends BaseEntityService<DisagreeReason> implements IDisagreeReasonService {

    @Autowired
    private DisagreeReasonDao disagreeReasonDao;

    @Override
    protected BaseEntityDao<DisagreeReason> getDao() {
        return this.disagreeReasonDao;
    }


    @Override
    public ResponseData getDisagreeReasonByTypeId(String typeId) {
        if (StringUtils.isEmpty(typeId)) {
            return ResponseData.operationFailure("参数流程类型ID不能为空！");
        }
        List<DisagreeReason> list = disagreeReasonDao.findByFlowTypeIdAndTenantCode(typeId, ContextUtil.getTenantCode());
        if ("commonReason".equals(typeId)) { //全局通用意见中默认添加其他选项
            DisagreeReason bean = list.stream().filter(a -> "common_else".equals(a.getCode())).findFirst().orElse(null);
            if (bean == null) {
                bean = new DisagreeReason();
                bean.setFlowTypeId("commonReason");
                bean.setFlowTypeName("全局通用原因");
                bean.setCode("common_else");
                bean.setName("其它");
                bean.setRank(99);
                bean.setStatus(true);
                bean.setDepict("如果选择其他，请注明详细原因");
                try {
                    bean = disagreeReasonDao.save(bean);
                    list.add(bean);
                } catch (Exception e) {
                }
            }
        }
        return ResponseData.operationSuccessWithData(list);
    }


    public OperateResultWithData<DisagreeReason> save(DisagreeReason bean) {
        OperateResultWithData<DisagreeReason> resultWithData;
        try {
            if("common_else".equals(bean.getCode()) && !"其他".equals(bean.getName())){
                return OperateResultWithData.operationFailure("原因其他为系统默认创建，不能更改！");
            }
            resultWithData = super.save(bean);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            Throwable cause = e.getCause();
            cause = cause.getCause();
            SQLException sqlException = (SQLException) cause;
            if (sqlException != null && sqlException.getSQLState().equals("23000")) {
                resultWithData = OperateResultWithData.operationFailure("原因代码重复，请检查！");
            } else {
                resultWithData = OperateResultWithData.operationFailure(e.getMessage());
            }
            LogUtil.error(e.getMessage(), e);
        }
        return resultWithData;
    }


    @Override
    public ResponseData updateStatusById(String id) {
        DisagreeReason bean = disagreeReasonDao.findOne(id);
        if (bean.getStatus() == true) {
            bean.setStatus(false);
        } else {
            bean.setStatus(true);
        }
        disagreeReasonDao.save(bean);
        return ResponseData.operationSuccessWithData(bean);
    }


    public List<DisagreeReason> getDisReasonListByTypeId(String typeId) {
        if (StringUtils.isEmpty(typeId)) {
            return null;
        }
        return disagreeReasonDao.findByFlowTypeIdAndCommon(typeId, ContextUtil.getTenantCode());
    }


}
