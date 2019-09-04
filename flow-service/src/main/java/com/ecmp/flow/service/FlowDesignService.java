package com.ecmp.flow.service;

import com.ecmp.flow.api.IFlowDesignService;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.util.FlowException;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FlowDesignService implements IFlowDesignService {

    @Autowired
   private FlowDefinationService flowDefinationService;


   public ResponseData getEntity( String id, Integer versionCode, String businessModelCode, String businessId){
       if(StringUtils.isEmpty(id)){
          return  ResponseData.operationFailure("参数id为空");
       }
       if(versionCode==null){
           return  ResponseData.operationFailure("参数versionCode为空");
       }
       try{
           FlowDefVersion  data = flowDefinationService.getFlowDefVersion(id,versionCode,businessModelCode,businessId);
           return  ResponseData.operationSuccessWithData(data);
       }catch (Exception e){
           LogUtil.error("获取流程定义出错!",e);
           throw new FlowException("获取流程定义出错,详情请查看日志！");
       }
  }

}
