package com.ecmp.flow.service;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class FlowDefVersionServiceTest extends BaseContextTestCase {

    @Autowired
    private FlowDefVersionService service;

    @Test
    public void findByPage() {
        Search search = new Search();
        List<SearchFilter> filters = new ArrayList<>();
        SearchFilter filter = new SearchFilter();
        filter.setFieldName("flowDefination.id");
        filter.setOperator(SearchFilter.Operator.EQ);
        filter.setValue("98307F87-5150-11EA-BBE4-0242C0A84421");
        filter.setFieldType("String");
        filters.add(filter);
        search.setFilters(filters);
        PageResult<FlowDefVersion> result = service.findByPage(search);
        Assert.assertNotNull(result);
        System.out.println(JsonUtils.toJson(result));
    }
}
