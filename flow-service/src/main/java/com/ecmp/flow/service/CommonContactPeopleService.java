package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.ICommonContactPeopleService;
import com.ecmp.flow.dao.CommonContactPeopleDao;
import com.ecmp.flow.entity.CommonContactPeople;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：常用联系人
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/08/31          何灿坤(AK)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class CommonContactPeopleService extends BaseEntityService<CommonContactPeople> implements ICommonContactPeopleService {

    @Autowired
    private CommonContactPeopleDao commonContactPeopleDao;

    @Override
    protected BaseEntityDao<CommonContactPeople> getDao() {
        return commonContactPeopleDao;
    }


    @Override
    public List<CommonContactPeople> findCommonContactPeopleByGroupId(String commonContactGroupId) {
        if(StringUtils.isEmpty(commonContactGroupId)){
           return null;
        }
        Search search = new Search();
        search.addFilter(new SearchFilter("commonContactGroupId", commonContactGroupId, SearchFilter.Operator.EQ));
        SearchOrder searchOrder = new SearchOrder();
        search.addSortOrder(searchOrder.desc("lastEditedDate"));
        List<CommonContactPeople> list = commonContactPeopleDao.findByFilters(search);
        return list;
    }


    @Override
    public ResponseData saveList(List<CommonContactPeople> list) {
        if(CollectionUtils.isEmpty(list)){
            return  ResponseData.operationFailure("参数不能为空！");
        }
        for(CommonContactPeople bean : list){
            Search search = new Search();
            search.addFilter(new SearchFilter("commonContactGroupId", bean.getCommonContactGroupId(), SearchFilter.Operator.EQ));
            search.addFilter(new SearchFilter("userId", bean.getUserId(), SearchFilter.Operator.EQ));
            List<CommonContactPeople> peopleList =  commonContactPeopleDao.findByFilters(search);
            if(!CollectionUtils.isEmpty(peopleList)){
                bean.setId(peopleList.get(0).getId());
            }
        }
        commonContactPeopleDao.saveAll(list);
        return ResponseData.operationSuccess();
    }
}
