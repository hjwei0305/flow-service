package com.ecmp.flow.util;

import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.service.BaseContextTestCase;
import com.ecmp.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-05-15 11:23
 */
public class FlowCommonUtilTest extends BaseContextTestCase {
    @Autowired
    private FlowCommonUtil commonUtil;

    @Test
    public void getBasicUserExecutor() {
        String userIds = "[751444F9-BC78-11E8-8A20-0242C0A8440D,12356677]";
        userIds = XmlUtil.trimFirstAndLastChar(userIds, '[');
        userIds = XmlUtil.trimFirstAndLastChar(userIds, ']');
        List<String> ids = Arrays.asList(StringUtils.split(userIds, ','));
        List<Executor> executor = commonUtil.getBasicUserExecutors(ids);
        Assert.assertNotNull(executor);
        System.out.println(JsonUtils.toJson(executor));
    }
}