package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultBusinessModelDao extends BaseEntityDao<DefaultBusinessModel> {
}