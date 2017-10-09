package com.ecmp.flow.controller.basic;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.basic.vo.Position;
import com.ecmp.flow.basic.vo.PositionCategory;
import com.ecmp.flow.common.util.Auth2ApiClient;
import com.ecmp.flow.common.util.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
@RequestMapping(value = "/basic")
@IgnoreCheckAuth
public class BasicController {


    /**
     * 获取所有的组织机构
     * @return 所有组织机构树
     */
    @ResponseBody
    @RequestMapping("findAllOrgs")
    public List<Organization> findAllOrgs() throws Exception{
//        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
//        List<Organization> allOrgsList = proxy.findAllOrgs();
//        List<com.ecmp.flow.basic.vo.Organization> allOrgsList = (List<com.ecmp.flow.basic.vo.Organization>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, com.ecmp.flow.common.util.Constants.BASIC_ORG_LISTALLORGS_URL, new GenericType<List<com.ecmp.flow.basic.vo.Organization>>() {
//        }, null,null);

        Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, com.ecmp.flow.common.util.Constants.BASIC_ORG_LISTALLORGS_URL);
        List<com.ecmp.flow.basic.vo.Organization> allOrgsList = auth2ApiClient.getEntityViaProxy(new GenericType<List<com.ecmp.flow.basic.vo.Organization> >() {},null);
        return allOrgsList;
    }

    /**
     * 获取所有的岗位类别
     * @return 所有岗位类别清单
     */
    @ResponseBody
    @RequestMapping("findAllPositionCategory")
    public List<PositionCategory> findAllPositionCategory() throws Exception{
//        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
//        List<PositionCategory> positionCategoryList = proxy.findAll();
//        List<PositionCategory> positionCategoryList  = (List<PositionCategory>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, com.ecmp.flow.common.util.Constants.BASIC_POSITIONCATEGORY_FINDALL_URL, new GenericType<List<PositionCategory>>() {
//        }, null,null);
        Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, com.ecmp.flow.common.util.Constants.BASIC_POSITIONCATEGORY_FINDALL_URL);
        List<PositionCategory> positionCategoryList  = auth2ApiClient.getEntityViaProxy(new GenericType<List<PositionCategory>>() {},null);
        return positionCategoryList;
    }

    /**
     * 获取所有的岗位
     * @return 所有岗位清单
     */
    @ResponseBody
    @RequestMapping("findAllPosition")
    public PageResult<Position> findAllPosition(ServletRequest request)throws Exception{
        Search search = SearchUtil.genSearch(request);
//        Map<String,Object> params = new HashMap<String,Object>();
//        params.put("body",search);
//        PageResult<Position> positionCategoryList  = (PageResult<Position> ) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, com.ecmp.flow.common.util.Constants.BASIC_POSITION_FINDBYPAGE_URL, new GenericType< PageResult<Position>>() {
//        }, params,null);
        Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, com.ecmp.flow.common.util.Constants.BASIC_POSITION_FINDBYPAGE_URL);
        PageResult<Position> positionCategoryList   = auth2ApiClient.postViaProxyReturnResult(new GenericType<PageResult<Position>>() {},search);
//        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        return positionCategoryList;
    }
}
