package com.ecmp.flow;

import org.apache.cxf.transport.servlet.CXFServlet;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：系统配置管理的Servlet容器类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-03-28 11:58      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */
@WebServlet(initParams = @WebInitParam(name = "config-location",value = "classpath:applicationContext-activiti.xml")
        , urlPatterns = "/*", loadOnStartup = 1)
public class ConfigServiceServlet extends CXFServlet {
    private static final long serialVersionUID = 1L;
}
