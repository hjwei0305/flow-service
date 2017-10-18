package com.ecmp.flow.controller.basic;

import com.ecmp.annotation.IgnoreCheckAuth;

import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.basic.vo.Position;
import com.ecmp.flow.basic.vo.PositionCategory;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.basic.vo.Position;
import com.ecmp.flow.basic.vo.PositionCategory;
import com.ecmp.flow.common.util.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.GenericType;
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
@RequestMapping(value = "/basic")
@IgnoreCheckAuth
public class BasicController {


    /**
     * 获取所有的组织机构
     * @return 所有组织机构树
     */
    @ResponseBody
    @RequestMapping("findAllOrgs")
    public List<Organization> findAllOrgs(){
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_ORG_LISTALLORGS_URL;
        List<com.ecmp.flow.basic.vo.Organization> allOrgsList = ApiClient.getEntityViaProxy(url,new GenericType<List<Organization> >() {},null);
        return allOrgsList;
    }

    /**
     * 获取所有的岗位类别
     * @return 所有岗位类别清单
     */
    @ResponseBody
    @RequestMapping("findAllPositionCategory")
    public List<PositionCategory> findAllPositionCategory(){
//        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
//        List<PositionCategory> positionCategoryList = proxy.findAll();
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_POSITIONCATEGORY_FINDALL_URL;
        List<PositionCategory> positionCategoryList  = ApiClient.getEntityViaProxy(url,new GenericType<List<PositionCategory>>() {},null);
        return positionCategoryList;
    }

    /**
     * 获取所有的岗位
     * @return 所有岗位清单
     */
    @ResponseBody
    @RequestMapping("findAllPosition")
    public PageResult<Position> findAllPosition(ServletRequest request){
        Search search = SearchUtil.genSearch(request);
//        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_POSITION_FINDBYPAGE_URL;
        PageResult<Position> positionCategoryList   = ApiClient.postViaProxyReturnResult(url,new GenericType<PageResult<Position>>() {},search);
        return positionCategoryList;
    }
}
