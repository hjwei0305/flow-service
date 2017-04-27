package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.dao.AppModuleDao;
import com.ecmp.flow.entity.AppModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：应用模块服务实现
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class AppModuleService extends BaseService<AppModule, String> implements IAppModuleService{

    @Autowired
    private AppModuleDao appModuleDao;

//    public String hello(){
//        return "hello";
//    }
//    public String hello(String v){
//        return this.hello()+v;
//    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<AppModule> findAll() {
//        List<AppModule> list=this.appModuleDao.findAll();
//        //Hibernate.initialize(list);// 强制加载相关联pojo
//        return list;
//    }
}
