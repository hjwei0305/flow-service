package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowTypeService;
import com.ecmp.flow.basic.vo.AppModule;
import com.ecmp.flow.dao.FlowTypeDao;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.GenericType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowTypeService extends BaseEntityService<FlowType> implements IFlowTypeService {

    @Autowired
    private FlowTypeDao flowTypeDao;

    private final Logger logger = LoggerFactory.getLogger(BusinessModel.class);

    protected BaseEntityDao<FlowType> getDao(){
        return this.flowTypeDao;
    }

    @Override
    public List<FlowType> findByBusinessModelId(String businessModelId) {
        return flowTypeDao.findByBusinessModelId(businessModelId);
    }

    @Override
    public OperateResultWithData<FlowType> save(FlowType flowType){
        OperateResultWithData<FlowType> resultWithData = null;
        resultWithData = super.save(flowType);
        clearFlowDefVersion();
        return resultWithData;
    }
    private void clearFlowDefVersion(){
        String pattern = "FLowGetLastFlowDefVersion_*";
        if(redisTemplate!=null){
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys!=null&&!keys.isEmpty()){
                redisTemplate.delete(keys);
            }
        }
    }

    public PageResult<FlowType> findByPage(Search searchConfig){
        List<AppModule> appModuleList = null;
        List<String > appModuleCodeList = null;
        try {
            String url = com.ecmp.flow.common.util.Constants.getBasicTenantAppModuleUrl();
            appModuleList = ApiClient.getEntityViaProxy(url, new GenericType<List<AppModule>>() {
            }, null);
            if(appModuleList!=null && !appModuleList.isEmpty()){
                appModuleCodeList = new ArrayList<String>();
                for(AppModule appModule:appModuleList){
                    appModuleCodeList.add(appModule.getCode());
                }
            }
            if(appModuleCodeList!=null && !appModuleCodeList.isEmpty()){
                SearchFilter searchFilter =   new SearchFilter("businessModel.appModule.code", appModuleCodeList, SearchFilter.Operator.IN);
                searchConfig.addFilter(searchFilter);
            }

        }catch (Exception e){
               e.printStackTrace();
        }
        PageResult<FlowType> result = flowTypeDao.findByPage(searchConfig);
        return result;
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    public OperateResult delete(String id) {
        OperateResult operateResult = preDelete(id);
        if (Objects.isNull(operateResult) || operateResult.successful()) {
            FlowType entity = findOne(id);
            if (entity != null) {
                try {
                    getDao().delete(entity);
                }catch (org.springframework.dao.DataIntegrityViolationException e){
                    e.printStackTrace();
                    SQLException sqlException = (SQLException)e.getCause().getCause();
                    if(sqlException!=null && "23000".equals(sqlException.getSQLState())){
                        return OperateResult.operationFailure("10027");
                    }else {
                        throw  e;
                    }
                }
                return OperateResult.operationSuccess("core_00003");
            } else {
                return OperateResult.operationWarning("core_00004");
            }
        }
        clearFlowDefVersion();
        return operateResult;
    }

}
