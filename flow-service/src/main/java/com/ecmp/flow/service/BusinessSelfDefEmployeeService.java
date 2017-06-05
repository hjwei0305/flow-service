package com.ecmp.flow.service;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IBusinessSelfDefEmployeeService;
import com.ecmp.flow.dao.BusinessSelfDefEmployeeDao;
import com.ecmp.flow.entity.BusinessSelfDefEmployee;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：业务实体自定义执行人配置管理
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/05/25 09:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class BusinessSelfDefEmployeeService extends BaseService<BusinessSelfDefEmployee, String> implements IBusinessSelfDefEmployeeService {

    @Autowired
    private BusinessSelfDefEmployeeDao businessSelfDefEmployeeDao;

    protected BaseDao<BusinessSelfDefEmployee, String> getDao(){
        return this.businessSelfDefEmployeeDao;
    }

    public List<BusinessSelfDefEmployee> findByBusinessModelId(String businessModelId){
        return businessSelfDefEmployeeDao.findByBusinessModuleId(businessModelId);
    }

    @Override
    public void saveCustomExecutor(String businessModelId,List<BusinessSelfDefEmployee> customExecutorList) {
        businessSelfDefEmployeeDao.deleteBybusinessModuleId(businessModelId);
        if(customExecutorList.size() == 0){
            return;
        }else{
            for(int i=0;i<customExecutorList.size();i++){
                System.out.println(customExecutorList.get(i));
                BusinessSelfDefEmployee businessSelfDefEmployee = new BusinessSelfDefEmployee();
                businessSelfDefEmployee.setEmployeeId(customExecutorList.get(i).getEmployeeId());
                businessSelfDefEmployee.setEmployeeName(customExecutorList.get(i).getEmployeeName());
                businessSelfDefEmployee.setBusinessModuleId(businessModelId);
                businessSelfDefEmployeeDao.save(businessSelfDefEmployee);
            }
        }
    }
}

