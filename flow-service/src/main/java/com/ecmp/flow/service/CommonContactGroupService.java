package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.ICommonContactGroupService;
import com.ecmp.flow.dao.CommonContactGroupDao;
import com.ecmp.flow.entity.CommonContactGroup;
import com.ecmp.flow.entity.CommonContactPeople;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.SessionUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：常用联系组
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/08/31          何灿坤(AK)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class CommonContactGroupService extends BaseEntityService<CommonContactGroup>  implements ICommonContactGroupService {

    @Autowired
    private CommonContactGroupDao commonContactGroupDao;

    @Autowired
    private CommonContactPeopleService commonContactPeopleService;

    @Override
    protected BaseEntityDao<CommonContactGroup> getDao() {
        return commonContactGroupDao;
    }


    @Override
    public List<CommonContactGroup> getAllGroupByUser() {
        SessionUser sessionUser = ContextUtil.getSessionUser();
        Search search = new Search();
        search.addFilter(new SearchFilter("creatorId", sessionUser.getUserId(), SearchFilter.Operator.EQ));
        SearchOrder searchOrder = new SearchOrder();
        search.addSortOrder(searchOrder.desc("rank"));
        List<CommonContactGroup> list = commonContactGroupDao.findByFilters(search);
        return list;
    }


    @Override
    public OperateResult delete(String id) {
        CommonContactPeople people = commonContactPeopleService.findFirstByProperty("commonContactGroupId",id);
        if(people == null){
            commonContactGroupDao.delete(id);
            return  OperateResult.operationSuccess();
        }
        return  OperateResult.operationFailure("该联系组下还有联系人，请先删除联系人后再删除！");
    }



}
