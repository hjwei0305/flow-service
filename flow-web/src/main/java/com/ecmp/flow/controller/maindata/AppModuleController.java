package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * <p>
 * *************************************************************************************************
 * </p><p>
 * 实现功能：应用模块
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 版本          变更时间             变更人                     变更原因
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 1.0.00      2017/09/06 11:39      谭军(tanjun)                新建
 * </p><p>
 * *************************************************************************************************
 * </p>
 */
@Controller
@RequestMapping(value = "/appModule")
public class AppModuleController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/AppModuleView";
    }

    /**
     * 查询所有应用模块
     *
     * @return 应用模块清单
     */
    @RequestMapping("listAll")
    @ResponseBody
    public List<AppModule> listAll() {
        IAppModuleService appModuleService = ApiClient.createProxy(IAppModuleService.class);
        return appModuleService.findAll();
    }

    /**
     * 根据id删除应用模块
     *
     * @param id
     * @return 操作结果
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public OperateStatus delete(String id) throws JsonProcessingException {
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        OperateResult result = proxy.delete(id);
        boolean success = result.successful();
        String msg = result.getMessage();
        OperateStatus operateStatus = new OperateStatus(success, msg);
        return operateStatus;
    }

    /**
     * 保存应用模块
     *
     * @param appModule
     * @return 保存后的岗位
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public OperateStatus save(AppModule appModule) throws JsonProcessingException {
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        OperateResultWithData<AppModule> result = proxy.save(appModule);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return operateStatus;
    }

    /**
     * 查询所有应用模块
     *
     * @return 应用模块集合
     */
    @RequestMapping(value = "listAllForComboBox")
    @ResponseBody
    public OperateStatus listAllForComboBox() {
        IAppModuleService appModuleService = ApiClient.createProxy(IAppModuleService.class);
        List<AppModule> appModuleList = appModuleService.findAll();
        OperateStatus operateStatus = new OperateStatus(true, "操作成功", appModuleList);
        return operateStatus;

    }
}
