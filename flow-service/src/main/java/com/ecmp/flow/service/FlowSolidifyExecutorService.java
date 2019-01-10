package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.dao.FlowSolidifyExecutorDao;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import com.ecmp.flow.vo.FlowSolidifyExecutorVO;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;


@Service
public class FlowSolidifyExecutorService  extends BaseEntityService<FlowSolidifyExecutor> implements IFlowSolidifyExecutorService {

    @Autowired
    private FlowSolidifyExecutorDao flowSolidifyExecutorDao;

    protected BaseEntityDao<FlowSolidifyExecutor> getDao(){
        return this.flowSolidifyExecutorDao;
    }


    /**
     * 通过执行人VO集合保存固化流程的执行人信息
     * @param executorVoList 固化执行人VO集合
     * @param businessModelCode
     * @param businessId
     * @return
     */
    @Transactional
    public ResponseData saveByExecutorVoList(List<FlowSolidifyExecutorVO> executorVoList, String businessModelCode,String businessId){
        ResponseData   responseData  = new ResponseData();
        if(executorVoList==null||executorVoList.size()==0|| StringUtils.isEmpty(businessModelCode)||StringUtils.isEmpty(businessId)){
            return this.writeErrorLogAndReturnData(null,"请求参数不能为空！");
        }
       try{
           executorVoList.forEach(executorVo->{
               FlowSolidifyExecutor bean =new FlowSolidifyExecutor();
               bean.setBusinessCode(businessModelCode);
               bean.setBusinessId(businessId);
               bean.setActTaskDefKey(executorVo.getActTaskDefKey());
               bean.setInstancyStatus(executorVo.getInstancyStatus());
               bean.setExecutorIds(executorVo.getExecutorIds());
               flowSolidifyExecutorDao.save(bean);
           });
       }catch (Exception e){
           return this.writeErrorLogAndReturnData(e,"节点信息、紧急状态、执行人不能为空！");
       }
        return  responseData;
    }




    public ResponseData writeErrorLogAndReturnData(Exception e,String msg){
        if (e!=null) {
            LogUtil.error(e.getMessage());
        }
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(false);
        responseData.setMessage(msg);
        return  responseData;
    }



}
