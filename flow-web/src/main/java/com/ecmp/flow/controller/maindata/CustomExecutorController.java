package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.flow.basic.vo.Employee;
import com.ecmp.flow.basic.vo.EmployeeQueryParam;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.api.IBusinessSelfDefEmployeeService;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.BusinessSelfDefEmployee;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.core.GenericType;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/customExecutor")
@IgnoreCheckAuth
public class CustomExecutorController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/CustomExecutorView";
    }

    /**
     * 查询所有业务实体
     * @return 操作结果
     */
    @RequestMapping(value = "listAllBusinessModel")
    @ResponseBody
    public OperateStatus listAllBusinessModel() {
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        List<BusinessModel> businessModelList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, businessModelList);
        return  operateStatus;
    }

    /**
     * 根据业务实体ID查询执行人
     * @param businessModuleId
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "listExecutor")
    @ResponseBody
    public Object listExecutor(String  businessModuleId) throws ParseException {
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        List<BusinessSelfDefEmployee> businessSelfDefEmployees = proxy.findByBusinessModelId(businessModuleId);
        List<String> selectedExecutorIds = new ArrayList<>();
        for(int i=0;i<businessSelfDefEmployees.size();i++){
            selectedExecutorIds.add(businessSelfDefEmployees.get(i).getEmployeeId());
        }
//        IEmployeeService proxy2 = ApiClient.createProxy(IEmployeeService.class);
//        List<Employee> selectedExecutor = proxy2.findByIds(selectedExecutorIds);

        Map<String,Object> params = new HashMap();
        params.put("employeeIds",selectedExecutorIds);
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_EMPLOYEE_FINDBYIDS_URL;
        List<Employee> selectedExecutor = ApiClient.getEntityViaProxy(url,new GenericType<List<Employee>>() {},params);
        if(selectedExecutor == null){
            List<String> list = new ArrayList<>();
            return list;
        }else{
            return selectedExecutor;
        }
    }

    /**
     * 查询所有的用户
     * @param businessModelId
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "listAllExecutorNotSelected")
    @ResponseBody
    public PageResult<Employee> listAllExecutorNotSelected(String businessModelId, @RequestParam(value = "page") int page) throws ParseException {
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        List<BusinessSelfDefEmployee> businessSelfDefEmployees = proxy.findByBusinessModelId(businessModelId);
        List<String> selectedExecutorIds = new ArrayList<>();
        for(int i=0;i<businessSelfDefEmployees.size();i++){
            selectedExecutorIds.add(businessSelfDefEmployees.get(i).getEmployeeId());
        }
        EmployeeQueryParam employeeQueryParam = new EmployeeQueryParam();
        employeeQueryParam.setIds(selectedExecutorIds);
        employeeQueryParam.setPage(page);
        employeeQueryParam.setRows(15);
//        IEmployeeService proxy2 = ApiClient.createProxy(IEmployeeService.class);
//        PageResult<Employee> notSelectedExecutor = proxy2.findByEmployeeParam(employeeQueryParam);
        String url = Constants.BASIC_SERVICE_URL+Constants.BASIC_EMPLOYEE_FINDBYPARAM_URL;
        PageResult<Employee> notSelectedExecutor = ApiClient.postViaProxyReturnResult(url,new GenericType<PageResult<Employee>>() {},employeeQueryParam);
        return notSelectedExecutor;
    }

    /**
     * 查询当前业务实体下已分配的执行人
     * @param businessModelId
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "listAllExecutorSelected")
    @ResponseBody
    public List<Employee> listAllExecutorSelected(String businessModelId) throws ParseException {
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        List<BusinessSelfDefEmployee> businessSelfDefEmployees = proxy.findByBusinessModelId(businessModelId);
        List<String> selectedExecutorIds = new ArrayList<>();
        for(int i=0;i<businessSelfDefEmployees.size();i++){
            selectedExecutorIds.add(businessSelfDefEmployees.get(i).getEmployeeId());
        }
//        IEmployeeService proxy2 = ApiClient.createProxy(IEmployeeService.class);
//        List<Employee> selectedExecutor = proxy2.findByIds(selectedExecutorIds);
        Map<String,Object> params = new HashMap();
        params.put("employeeIds",selectedExecutorIds);
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_EMPLOYEE_FINDBYIDS_URL;
        List<Employee> selectedExecutor = ApiClient.getEntityViaProxy(url,new GenericType<List<Employee>>() {},params);
        if(selectedExecutor == null){
            List<Employee> list = new ArrayList<>();
            return list;
        }else{
            return selectedExecutor;
        }
    }

    /**
     * 保存分配的执行人
     * @param businessModelId
     * @param
     */
    @RequestMapping(value = "saveSetCustomExecutor")
    @ResponseBody
    public OperateStatus saveSetCustomExecutor(String businessModelId,String selectedCustomExecutorIds) {
        System.out.println(businessModelId);
        System.out.println(selectedCustomExecutorIds);
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        proxy.saveCustomExecutor(businessModelId,selectedCustomExecutorIds);
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG);
        return operateStatus;
    }

    /**
     * 根据组织机构的id获取员工(不包含冻结)
     *
     * @param organizationId 组织机构的id
     * @return 员工清单
     */
    @RequestMapping(value = "listAllUser")
    @ResponseBody
    public List<Employee> listAllUser(String organizationId) {
//        IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
//        List<Employee> employees = proxy.findByOrganizationIdWithoutFrozen(organizationId);
        Map<String,Object> params = new HashMap();
        params.put("organizationId",organizationId);
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_EMPLOYEE_FINDBYORGANIZATIONID_URL;
        List<Employee> employees = ApiClient.getEntityViaProxy(url,new GenericType<List<Employee>>() {},params);
        return employees;
    }
}
