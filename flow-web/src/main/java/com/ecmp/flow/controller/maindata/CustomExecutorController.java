package com.ecmp.flow.controller.maindata;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.api.IUserService;
import com.ecmp.basic.entity.Employee;
import com.ecmp.basic.entity.User;
import com.ecmp.basic.entity.vo.EmployeeQueryParam;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IBusinessSelfDefEmployeeService;
import com.ecmp.flow.entity.BusinessSelfDefEmployee;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
public class CustomExecutorController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/CustomExecutorView";
    }

    /**
     * 根据业务实体ID查询执行人
     * @param businessModuleId
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "listExecutor")
    @ResponseBody
    public String listExecutor(String  businessModuleId) throws ParseException {
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        List<BusinessSelfDefEmployee> businessSelfDefEmployees = proxy.findByBusinessModelId(businessModuleId);
        List<String> selectedExecutorIds = new ArrayList<>();
        for(int i=0;i<businessSelfDefEmployees.size();i++){
            selectedExecutorIds.add(businessSelfDefEmployees.get(i).getEmployeeId());
        }
        IEmployeeService proxy2 = ApiClient.createProxy(IEmployeeService.class);
        List<Employee> selectedExecutor = proxy2.findByIds(selectedExecutorIds);
        if(selectedExecutor == null){
            List<String> list = new ArrayList<>();
            return JsonUtil.serialize(list);
        }else{
            return JsonUtil.serialize(selectedExecutor);
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
    public String listAllExecutorNotSelected(String businessModelId) throws ParseException {
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        List<BusinessSelfDefEmployee> businessSelfDefEmployees = proxy.findByBusinessModelId(businessModelId);
        List<String> selectedExecutorIds = new ArrayList<>();
        for(int i=0;i<businessSelfDefEmployees.size();i++){
            selectedExecutorIds.add(businessSelfDefEmployees.get(i).getEmployeeId());
        }
        EmployeeQueryParam employeeQueryParam = new EmployeeQueryParam();
        employeeQueryParam.setIds(selectedExecutorIds);
        employeeQueryParam.setPage(1);
        employeeQueryParam.setRows(15);
        IEmployeeService proxy2 = ApiClient.createProxy(IEmployeeService.class);
        PageResult<Employee> notSelectedExecutor = proxy2.findByEmployeeParam(employeeQueryParam);
        return JsonUtil.serialize(notSelectedExecutor);
    }

    /**
     * 查询当前业务实体下已分配的执行人
     * @param businessModelId
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "listAllExecutorSelected")
    @ResponseBody
    public String listAllExecutorSelected(String businessModelId) throws ParseException {
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        List<BusinessSelfDefEmployee> businessSelfDefEmployees = proxy.findByBusinessModelId(businessModelId);
        List<String> selectedExecutorIds = new ArrayList<>();
        for(int i=0;i<businessSelfDefEmployees.size();i++){
            selectedExecutorIds.add(businessSelfDefEmployees.get(i).getEmployeeId());
        }
        IEmployeeService proxy2 = ApiClient.createProxy(IEmployeeService.class);
        List<Employee> selectedExecutor = proxy2.findByIds(selectedExecutorIds);
        return JsonUtil.serialize(selectedExecutor);
    }

    /**
     * 保存分配的执行人
     * @param businessModelId
     * @param
     */
    @RequestMapping(value = "saveSetCustomExecutor")
    @ResponseBody
    public String saveSetCustomExecutor(String businessModelId,String selectedCustomExecutorIds) {
        System.out.println(businessModelId);
        System.out.println(selectedCustomExecutorIds);
        IBusinessSelfDefEmployeeService proxy = ApiClient.createProxy(IBusinessSelfDefEmployeeService.class);
        proxy.saveCustomExecutor(businessModelId,selectedCustomExecutorIds);
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG);
        return JsonUtil.serialize(operateStatus);
    }
}