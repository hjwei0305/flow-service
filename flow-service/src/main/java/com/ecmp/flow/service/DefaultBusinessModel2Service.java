package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.config.util.NumberGenerator;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.Validation;
import com.ecmp.flow.api.IDefaultBusinessModel2Service;
import com.ecmp.flow.basic.vo.Employee;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Auth2ApiClient;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.DefaultBusinessModel2Dao;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.vo.OperateResultWithData;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class DefaultBusinessModel2Service extends BaseEntityService<DefaultBusinessModel2> implements IDefaultBusinessModel2Service {

    @Autowired
    private DefaultBusinessModel2Dao defaultBusinessModel2Dao;

    protected BaseEntityDao<DefaultBusinessModel2> getDao(){
        return this.defaultBusinessModel2Dao;
    }

    /**
     * 数据保存操作
     */
    @SuppressWarnings("unchecked")
    public OperateResultWithData<DefaultBusinessModel2> save(DefaultBusinessModel2 entity) {
        Validation.notNull(entity, "持久化对象不能为空");
        String businessCode = NumberGenerator.getNumber(DefaultBusinessModel2.class);
//        String businessCode = CodeGenerator.genCodes(6,1).get(0);
        if(StringUtils.isEmpty(entity.getBusinessCode())){
            entity.setBusinessCode(businessCode);
        }
        return super.save(entity);
    }

    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public List<Executor> getPersonToExecutorConfig(String businessId, String paramJson) throws Exception{
        List<Executor> result = new ArrayList<Executor>();
        if(StringUtils.isNotEmpty(businessId)){
            DefaultBusinessModel2 defaultBusinessModel = defaultBusinessModel2Dao.findOne(businessId);
            if(defaultBusinessModel!=null){
                String orgid = defaultBusinessModel.getOrgId();
//                IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
//                //获取市场部所有人员
//                List<Employee> employeeList   = proxy.findByOrganizationId(orgid);
                Map<String,Object> params = new HashedMap();
                params.put("organizationId",orgid);
//                List<Employee> employeeList  = ( List<Employee>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL, new GenericType< List<Employee>>() {
//                }, params,null);
                Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL);
                List<Employee> employeeList  = auth2ApiClient.getEntityViaProxy(new GenericType<List<Employee>>() {},params);
                List<String> idList = new ArrayList<String>();
                for(Employee e : employeeList){
                    idList.add(e.getId());
                }
                //获取执行人
//                result = proxy.getExecutorsByEmployeeIds(idList);
                Map<String,Object> paramsV2 = new HashedMap();
                paramsV2.put("employeeIds",idList);
//                result = ( List<Executor>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL, new GenericType< List<Executor>>() {
//                }, paramsV2,null);
                Auth2ApiClient auth2ApiClient2= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL);
                result  = auth2ApiClient2.getEntityViaProxy(new GenericType<List<Executor>>() {},paramsV2);
            }
        }
        return result;
    }
}
