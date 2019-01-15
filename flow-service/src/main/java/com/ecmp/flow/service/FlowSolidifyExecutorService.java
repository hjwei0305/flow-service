package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.dao.FlowSolidifyExecutorDao;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import com.ecmp.flow.vo.FlowSolidifyExecutorVO;
import com.ecmp.flow.vo.FlowTaskCompleteWebVO;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;


@Service
public class FlowSolidifyExecutorService extends BaseEntityService<FlowSolidifyExecutor> implements IFlowSolidifyExecutorService {

    @Autowired
    private FlowSolidifyExecutorDao flowSolidifyExecutorDao;

    protected BaseEntityDao<FlowSolidifyExecutor> getDao() {
        return this.flowSolidifyExecutorDao;
    }


    /**
     * 通过执行人VO集合保存固化流程的执行人信息
     *
     * @param executorVoList    固化执行人VO集合
     * @param businessModelCode
     * @param businessId
     * @return
     */
    @Transactional
    public ResponseData saveByExecutorVoList(List<FlowSolidifyExecutorVO> executorVoList, String businessModelCode, String businessId) {
        ResponseData responseData = new ResponseData();
        if (executorVoList == null || executorVoList.size() == 0 || StringUtils.isEmpty(businessModelCode) || StringUtils.isEmpty(businessId)) {
            return this.writeErrorLogAndReturnData(null, "请求参数不能为空！");
        }
        //新启动流程时，清除以前的数据
        List<FlowSolidifyExecutor> list = flowSolidifyExecutorDao.findListByProperty("businessId", businessId);
        if (list != null && list.size() > 0) {
            list.forEach(bean -> flowSolidifyExecutorDao.delete(bean));
        }

        try {
            executorVoList.forEach(executorVo -> {
                FlowSolidifyExecutor bean = new FlowSolidifyExecutor();
                bean.setBusinessCode(businessModelCode);
                bean.setBusinessId(businessId);
                bean.setActTaskDefKey(executorVo.getActTaskDefKey());
                bean.setInstancyStatus(executorVo.getInstancyStatus());
                bean.setExecutorIds(executorVo.getExecutorIds());
                flowSolidifyExecutorDao.save(bean);
            });
        } catch (Exception e) {
            return this.writeErrorLogAndReturnData(e, "节点信息、紧急状态、执行人不能为空！");
        }
        return responseData;
    }

    /**
     * 给FlowTaskCompleteWebVO设置执行人和紧急状态
     *
     * @param list       FlowTaskCompleteWebVO集合
     * @param businessId 业务表单id
     * @return
     */
    public ResponseData setInstancyAndIdsByTaskList(List<FlowTaskCompleteWebVO> list, String businessId) {
        ResponseData responseData = new ResponseData();
        if (list == null || list.size() == 0 || StringUtils.isEmpty(businessId)) {
            return this.writeErrorLogAndReturnData(null, "参数不能为空！");
        }

        for (FlowTaskCompleteWebVO webvO : list) {
            Search search = new Search();
            search.addFilter(new SearchFilter("businessId", businessId));
            search.addFilter(new SearchFilter("actTaskDefKey", webvO.getNodeId()));
            List<FlowSolidifyExecutor> solidifyExecutorlist = flowSolidifyExecutorDao.findByFilters(search);
            if (solidifyExecutorlist == null || solidifyExecutorlist.size() == 0) {
                return this.writeErrorLogAndReturnData(null, "固化执行人未设置！");
            }
            webvO.setInstancyStatus(solidifyExecutorlist.get(0).getInstancyStatus());
            webvO.setUserIds(solidifyExecutorlist.get(0).getExecutorIds());
        }
        responseData.setData(list);
        return responseData;
    }


    /**
     * 通过BusinessId删除固化流程执行人列表
     * @param businessId  业务表单id
     * @return
     */
    public ResponseData  deleteByBusinessId(String businessId){
        ResponseData responseData = new ResponseData();
        if(StringUtils.isEmpty(businessId)){
            return this.writeErrorLogAndReturnData(null, "参数不能为空！");
        }
        flowSolidifyExecutorDao.deleteByBusinessId(businessId);
        return  responseData;
    }



    public ResponseData writeErrorLogAndReturnData(Exception e, String msg) {
        if (e != null) {
            LogUtil.error(e.getMessage());
        }
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(false);
        responseData.setMessage(msg);
        return responseData;
    }


}
