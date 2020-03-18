package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.vo.ResponseData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class FlowHistoryServiceTest extends BaseContextTestCase {

    @Autowired
    private FlowHistoryService  service;

    @Test
    public void listFlowHistory(){
        String businessModelId="";
        Search search = new Search();
        search.setFilters(null);
        search.setPageInfo(new PageInfo());
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("flowTaskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("flowInstance.creatorName");
        search.addQuickSearchProperty("flowInstance.creatorAccount");
        search.setQuickSearchValue("");
        List<SearchOrder> sortOrders = new ArrayList<>();
        SearchOrder searchOrder = new SearchOrder("createdDate",SearchOrder.Direction.DESC);
        sortOrders.add(searchOrder);
        search.setSortOrders(sortOrders);
        ResponseData res = service.listFlowHistory(businessModelId,search);
        System.out.print(ApiJsonUtils.toJson(res));
    }

}
