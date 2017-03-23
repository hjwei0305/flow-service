package com.ecmp.flow;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：
 * Spring的支持依赖注入的JUnit4 集成测试基类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/2/8 10:20      马超(Vision)                新建
 * <p>
 * *************************************************************************************************
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/ecmp-context.xml", "classpath*:/ecmp-service.xml" })
public abstract class BasicContextTestCase extends AbstractJUnit4SpringContextTests {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

}
