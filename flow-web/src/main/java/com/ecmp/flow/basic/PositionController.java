package com.ecmp.flow.basic;

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

import com.ecmp.basic.api.IPositionService;
import com.ecmp.basic.entity.Position;
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


@Controller
@RequestMapping(value = "basic/position")
public class PositionController {
    @RequestMapping(value = "show",method = RequestMethod.GET)
    public String show() {
        return "basic/PositionView";
    }

    /**
     * 获取所有的岗位
     * @return 所有岗位清单
     */
    @ResponseBody
    @RequestMapping("findAll")
    public List<Position> findAll(){
        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        List<Position> positionList = proxy.findAll();
        return positionList;
    }

    /**
     * 通过组织机构Id获取岗位
     * @param orgId 组织机构Id
     * @return 岗位清单
     */
    @ResponseBody
    @RequestMapping("findByOrgId")
    public List<Position> findByOrgId(String orgId){
        System.out.println(orgId);
        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        List<Position> results = proxy.findByOrganizationId(orgId);
        return results;
    }

    /**
     * 保存一个岗位
     * @param  position 岗位
     * @return 保存后的岗位
     */
    @ResponseBody
    @RequestMapping("save")
    public String save(Position position){
        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        OperateResultWithData<Position> result = proxy.save(position);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }

    /**
     * 删除一个岗位
     * @param id 岗位的Id标识
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping("delete")
    public String delete(String id){
        IPositionService proxy = ApiClient.createProxy(IPositionService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }
}

