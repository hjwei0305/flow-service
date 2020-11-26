package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IDisagreeReasonService;
import com.ecmp.flow.dao.DisagreeReasonDao;
import com.ecmp.flow.entity.DisagreeReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DisagreeReasonService extends BaseEntityService<DisagreeReason> implements IDisagreeReasonService  {

    @Autowired
    private DisagreeReasonDao disagreeReasonDao;

    @Override
    protected BaseEntityDao<DisagreeReason> getDao() {
        return this.disagreeReasonDao;
    }


}
