package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.dao.BusinessModelDao;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

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
public class BusinessModelService extends BaseEntityService<BusinessModel> implements IBusinessModelService{

    private final Logger logger = LoggerFactory.getLogger(BusinessModel.class);

    @Autowired
    private BusinessModelDao businessModelDao;

    protected BaseEntityDao<BusinessModel> getDao(){
        return this.businessModelDao;
    }

    @Override
    public List<BusinessModel> findByAppModuleId(String appModuleId) {
        return businessModelDao.findByAppModuleId(appModuleId);
    }

    @Override
    public BusinessModel findByClassName(String className) {
        return businessModelDao.findByClassName(className);
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
            BusinessModel entity = findOne(id);
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
        return operateResult;
    }

   public OperateResultWithData<BusinessModel> save(BusinessModel businessModel){
       OperateResultWithData<BusinessModel> resultWithData = null;
       try {
           resultWithData = super.save(businessModel);
       }catch (org.springframework.dao.DataIntegrityViolationException e){
           e.printStackTrace();
           Throwable cause =  e.getCause();
           cause=  cause.getCause();
           SQLException sqlException = (SQLException)cause;
           if(sqlException!=null && sqlException.getSQLState().equals("23000")){
               resultWithData = OperateResultWithData.operationFailure("10037");//类全路径重复，请检查！
           }else{
               resultWithData = OperateResultWithData.operationFailure(e.getMessage());
           }
           logger.error(e.getMessage());
       }
       return resultWithData;
    }

}
