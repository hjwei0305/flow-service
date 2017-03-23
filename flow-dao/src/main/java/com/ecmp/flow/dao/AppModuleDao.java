package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.AppModule;
import org.springframework.stereotype.Repository;

@Repository
public interface AppModuleDao extends BaseDao<AppModule, String> {

}