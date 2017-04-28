package com.ecmp.flow.basic;

import com.ecmp.basic.api.IPositionCategoryService;
import com.ecmp.basic.entity.PositionCategory;
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
@RequestMapping(value = "basic/positionCategory")
public class PositionCategoryController {
    @RequestMapping(value = "show",method = RequestMethod.GET)
    public String show() {
        return "basic/PositionCategoryView";
    }

    /**
     * 获取所有的岗位类别
     * @return 所有岗位类别清单
     */
//    @ResponseBody
//    @RequestMapping("findAll")
//    public List<PositionCategory> findAll(){
//        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
//        List<PositionCategory> positionCategoryList = proxy.findAll();
//        return positionCategoryList;
//    }

    /**
     * 通过租户代码获取岗位类别
     * @param tenantCode 租户代码
     * @return 岗位类别清单
     */
    @ResponseBody
    @RequestMapping("findByTenantCode")
    public List<PositionCategory> findByTenantCode(String tenantCode){
        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
        List<PositionCategory> results = proxy.findByTenantCode(tenantCode);
        return results;
    }

    /**
     * 保存一个岗位类别
     * @param  positionCategory 岗位类别
     * @return 保存后的岗位类别
     */
    @ResponseBody
    @RequestMapping("save")
    public String save(PositionCategory positionCategory){
        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
        OperateResultWithData<PositionCategory> result = proxy.save(positionCategory);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }

    /**
     * 删除一个岗位类别
     * @param id 岗位类别的Id标识
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping("delete")
    public String delete(String id){
        IPositionCategoryService proxy = ApiClient.createProxy(IPositionCategoryService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }
}
