package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessModelDao extends BaseDao<BusinessModel, String> {

}