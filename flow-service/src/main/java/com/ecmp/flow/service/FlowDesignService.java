package com.ecmp.flow.service;

import com.ecmp.flow.api.IFlowDesignService;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.vo.SaveEntityVo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;


@Service
public class FlowDesignService implements IFlowDesignService {

    @Autowired
    private FlowDefinationService flowDefinationService;

    @Autowired
    private FlowDefVersionService flowDefVersionService;

    @Autowired
    private FlowServiceUrlService flowServiceUrlService;

    @Autowired
    private WorkPageUrlService workPageUrlService;


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



  public  ResponseData  save(SaveEntityVo entityVo) throws JAXBException, UnsupportedEncodingException, CloneNotSupportedException {
       String def  =  entityVo.getDef();
       Boolean deploy =  entityVo.getDeploy();
       ResponseData responseData = new ResponseData();
       if(StringUtils.isNotEmpty(def)||deploy!=null){
           JSONObject defObj = JSONObject.fromObject(def);
           Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
           String id=definition.getProcess().getId();
           String reg="^[a-zA-Z][A-Za-z0-9]{5,79}$";
           if(!id.matches(reg)){
               return ResponseData.operationFailure("流程代码以字母开头，允许数字或字母，且长度在6-80之间！");
           }
           definition.setDefJson(def);
           if (!deploy) {
               OperateResultWithData<FlowDefVersion> result = flowDefVersionService.save(definition);
               responseData.setSuccess(result.successful());
               responseData.setMessage(result.getMessage());
               responseData.setData(result.getData());
           } else {
               OperateResultWithData<FlowDefVersion> result = flowDefVersionService.save(definition);
               if(result.successful()){
                   flowDefinationService.deployById(result.getData().getFlowDefination().getId());
               }
               responseData.setSuccess(result.successful());
               responseData.setMessage(result.getMessage());
               responseData.setData(result.getData());
           }
           return responseData;
       }else{
           return  ResponseData.operationFailure("参数不能为空！");
       }
  }




  public ResponseData listAllServiceUrl( String busModelId)throws ParseException {
       if(StringUtils.isEmpty(busModelId)){
           return  ResponseData.operationFailure("参数不能为空！");
       }
      List<FlowServiceUrl> flowServiceUrlPageResult = flowServiceUrlService.findByBusinessModelId(busModelId);
      return ResponseData.operationSuccessWithData(flowServiceUrlPageResult);
  }



    public ResponseData listAllWorkPage( String businessModelId) {
        if(StringUtils.isEmpty(businessModelId)){
            return  ResponseData.operationFailure("参数不能为空！");
        }
        List<WorkPageUrl> result= workPageUrlService.findSelectEdByBusinessModelId(businessModelId);
        return ResponseData.operationSuccessWithData(result);
    }


}
