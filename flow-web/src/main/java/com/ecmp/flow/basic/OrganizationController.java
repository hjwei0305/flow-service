package com.ecmp.flow.basic;

import com.ecmp.basic.api.IOrganizationService;
import com.ecmp.basic.entity.Organization;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.*;
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
@RequestMapping(value = "basic/organization")
public class OrganizationController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "main/OrganizationView";
    }

    /**
     * 获取所有的组织机构
     * @return 所有组织机构树
     */
//    @ResponseBody
//    @RequestMapping("findAll")
//    public JsonTree findAll(){
//        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
//        Organization result  = proxy.findAllOrgs();
//        return new JsonTree(result, true);
//    }


    /**
     * 通过租户代码获取组织机构根节点
     *
     * @param tenantCode 租户代码
     * @return 组织机构
     */
    @ResponseBody
    @RequestMapping("findRootByTenantCode")
    public List<Organization> findRootByTenantCode(String tenantCode) {
        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
        List<Organization> result = proxy.findRootByTenantCode(tenantCode);
        return result;
    }

    /**
     * 通过租户代码获取所有组织机构
     *
     * @return 组织机构清单
     */
    @ResponseBody
    @RequestMapping("findOrgTreeByTenantCode")
    public JsonTree findOrgTreeByTenantCode(@QueryParam("tenantCode") String tenantCode) {
        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
        Organization result = proxy.findOrgTreeByTenantCode(tenantCode);
        return new JsonTree(result, true);
    }


    /**
     * 保存一个组织机构
     *
     * @param organization 组织机构
     * @return 保存后的组织机构
     */
    @ResponseBody
    @RequestMapping("save")
    public String save(Organization organization) {
        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
        OperateResultWithData<Organization> result = proxy.save(organization);
        OperateStatus status = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return JsonUtil.serialize(status);
    }

    /**
     * 删除一个组织机构
     *
     * @param id 组织机构的Id标识
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping("delete")
    public String delete(String id) {
        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus status = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(status);
    }

    /**
     * 根据组织机构的id来查询组织机构
     *
     * @param organizationId 组织机构的id
     * @return 组织机构
     */
    @ResponseBody
    @RequestMapping("findOne")
    public Organization findOne(String organizationId) {
        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
        Organization result = proxy.findOne(organizationId);
        return result;
    }

    /**
     * 移动组织机构
     *
     * @param nodeId         当前节点ID
     * @param targetParentId 目标父节点ID
     * @return 返回操作结果对象
     */
    @ResponseBody
    @RequestMapping("move")
    public String move(String nodeId, String targetParentId) {
        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
        OperateResult result = proxy.move(nodeId, targetParentId);
        OperateStatus status = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(status);
    }
}
