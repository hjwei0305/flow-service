package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IDisagreeReasonService;
import com.ecmp.flow.dao.DisagreeReasonDao;
import com.ecmp.flow.entity.DisagreeReason;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return ResponseData.operationSuccessWithData(list);
    }
}
