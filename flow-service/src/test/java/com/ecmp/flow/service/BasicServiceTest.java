package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.basic.vo.*;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.vo.UserQueryParamVo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * basic接口测试
 */
public class BasicServiceTest  extends BaseContextTestCase{

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    /**
     * 1、获取所有组织机构树（不包含冻结）
     * 2、获取所有有权限的组织机构树(未配置默认获取全部)
     */
    @Test
    public void getBasicAllOrgs() {
        //获取所有组织机构树（不包含冻结）
        List<Organization> list = flowCommonUtil.getBasicAllOrgs();
        System.out.println(ApiJsonUtils.toJson(list));
        //获取所有有权限的组织机构树(未配置默认获取全部)
        List<Organization> list2 = flowCommonUtil.getBasicAllOrgByPower();
        System.out.println(ApiJsonUtils.toJson(list2));
    }

    /**
     * 获取指定节点的父组织机构列表
     */
    @Test
    public void getParentOrganizations() {
        String nodeId="F611DEFA-46E6-11EA-911F-0242C0A84604";
        List<Organization> list = flowCommonUtil.getParentOrganizations(nodeId);
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 获取所有岗位列表
     */
    @Test
    public void getBasicPositionFindbypage() {
        Search search = new Search();
        PageResult<Position> list = flowCommonUtil.getBasicPositionFindbypage(search);
        System.out.println(ApiJsonUtils.toJson(list));
    }


    /**
     * 获取所有岗位类别列表
     */
    @Test
    public void getBasicPositioncategoryFindall() {
        List<PositionCategory> list = flowCommonUtil.getBasicPositioncategoryFindall();
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 获取所有组织维度
     */
    @Test
    public void getBasicOrgDimension() {
        List<OrganizationDimension> list = flowCommonUtil.getBasicOrgDimension();
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 根据组织机构的id获取员工(不包含冻结)
     */
    @Test
    public void getEmployeesByOrgId() {
        String orgId="877035BF-A40C-11E7-A8B9-02420B99179E";
        List<Employee> list = flowCommonUtil.getEmployeesByOrgId(orgId);
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 获取组织机构下员工（新增：是否包含子节点）
     */
    @Test
    public void getEmployeesByOrgIdAndQueryParam() {
        UserQueryParamVo vo = new UserQueryParamVo();
        vo.setOrganizationId("877035BF-A40C-11E7-A8B9-02420B99179E");
        vo.setIncludeSubNode(true);
        //快速查询
//        ArrayList<String> properties = new ArrayList();
//        properties.add("code");
//        properties.add("user.userName");
//        vo.setQuickSearchProperties(properties);
//        vo.setQuickSearchValue("张");
        //排序
        List<SearchOrder> listOrder = new ArrayList<SearchOrder>();
        listOrder.add(new SearchOrder("createdDate", SearchOrder.Direction.DESC));
        vo.setSortOrders(listOrder);
        //分页
        vo.setPageInfo(new PageInfo());
        PageResult<Employee> list = flowCommonUtil.getEmployeesByOrgIdAndQueryParam(vo);
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 根据组织机构的id获取员工(不包含冻结)
     */
    @Test
    public void getBasicUserExecutor() {
        String userId="EF2E29F8-DE1C-11E7-AD2C-0242C0A84202";
        //根据员工ID获取执行人
        Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
        System.out.println(ApiJsonUtils.toJson(executor));
        //根据员工ID列表获取执行人
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);
        userIds.add("96CE11A5-4E0D-11EA-AD80-0242C0A84607");
        List<Executor> list = flowCommonUtil.getBasicUserExecutors(userIds);
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 根据岗位的id列表获取执行人
     */
    @Test
    public void getBasicExecutorsByPositionIds() {
        List<String> positionIds = new ArrayList<>();
        positionIds.add("282648C6-4881-11EA-B817-0242C0A84603");
        List<Executor> list = flowCommonUtil.getBasicExecutorsByPositionIds(positionIds,"C34F75A2-4703-11EA-911F-0242C0A84604");
        System.out.println(ApiJsonUtils.toJson(list));
    }


    /**
     * 根据岗位类别的id列表获取执行人
     */
    @Test
    public void getBasicExecutorsByPostCatIds() {
        List<String> postCatIds = new ArrayList<>();
        postCatIds.add("0069CA45-4598-11EA-983E-0242C0A84604");
        List<Executor> list = flowCommonUtil.getBasicExecutorsByPostCatIds(postCatIds,"C34F75A2-4703-11EA-911F-0242C0A84604");
        System.out.println(ApiJsonUtils.toJson(list));
    }


    /**
     * 通过岗位ids、组织维度ids和单据所属组织机构id来获取执行人
     */
    @Test
    public void getExecutorsByPositionIdsAndorgDimIds() {
        List<String> positionIds = new ArrayList<>();
        positionIds.add("282648C6-4881-11EA-B817-0242C0A84603");
        List<String> orgDimIds = new ArrayList<>();
        orgDimIds.add("0");
        List<Executor> list = flowCommonUtil.getExecutorsByPositionIdsAndorgDimIds(positionIds,orgDimIds,"C34F75A2-4703-11EA-911F-0242C0A84604");
        System.out.println(ApiJsonUtils.toJson(list));
    }


    /**
     * 根据岗位类别的id列表和组织机构id列表获取执行人
     */
    @Test
    public void getExecutorsByPostCatIdsAndOrgs() {
        List<String> positionIds = new ArrayList<>();
        positionIds.add("282648C6-4881-11EA-B817-0242C0A84603");
        List<String> orgIds = new ArrayList<>();
        orgIds.add("C34F75A2-4703-11EA-911F-0242C0A84604");
        List<Executor> list = flowCommonUtil.getExecutorsByPostCatIdsAndOrgs(positionIds,orgIds);
        System.out.println(ApiJsonUtils.toJson(list));
    }

    /**
     * 获取当前用户有权限的应用模块
     */
    @Test
    public void getBasicTenantAppModule() {
        List<com.ecmp.flow.basic.vo.AppModule> list = flowCommonUtil.getBasicTenantAppModule();
        System.out.println(ApiJsonUtils.toJson(list));
    }













}
