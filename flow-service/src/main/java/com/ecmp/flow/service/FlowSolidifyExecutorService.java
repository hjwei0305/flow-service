package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.dao.FlowSolidifyExecutorDao;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import com.ecmp.flow.vo.FlowSolidifyExecutorVO;
import com.ecmp.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public ResponseData saveByExecutorVoList(List<FlowSolidifyExecutorVO> executorVoList,
                             String businessModelCode,String businessId){
        ResponseData   responseData  = new ResponseData();







      return  responseData;
    }



}
