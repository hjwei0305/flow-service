package com.ecmp.flow.com.ecmp.flow.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * *************************************************************************************************
 * <p/>
 * 可用Properties文件配置的Contants基类.
 * 本类既保持了Contants的static和final(静态与不可修改)特性,又拥有了可用Properties文件配置的特征,
 * 主要是应用了Java语言中静态初始化代码的特性. <p/> 子类可如下编写
 *
 * <pre>
 *  public class Constants extends ConfigurableContants {
 *   static {
 *     init(&quot;framework.properties&quot;);
 *  }
 *  &lt;p/&gt;
 *  public final static String ERROR_BUNDLE_KEY = getProperty(&quot;constant.error_bundle_key&quot;, &quot;errors&quot;); }
 * </pre>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/04/06 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
public class ConfigurableContants {
	protected static final Log logger = LogFactory
			.getLog(ConfigurableContants.class);

	protected static Properties p = new Properties();

	/**
	 * 静态读入属性文件到Properties p变量中
	 */
	protected static void init(String propertyFileName) {
		InputStream in = null;
		try {
			in = ConfigurableContants.class.getClassLoader().getResourceAsStream(propertyFileName);
			if (in != null){
				logger.debug("load " + propertyFileName + " into Contants!");
				p.load(in);
			}
		} catch (IOException e) {
			logger.error("load " + propertyFileName + " into Contants error!");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("close " + propertyFileName + " error!");
				}
			}
		}
	}

	/**
	 * 封装了Properties类的getProperty函数,使p变量对子类透明.
	 *
	 * @param key
	 *            property key.
	 * @param defaultValue
	 *            当使用property key在properties中取不到值时的默认值.
	 */
	protected static String getProperty(String key, String defaultValue) {
		return p.getProperty(key, defaultValue);
	}

	/**
	 * 封装了Properties类的getProperty函数,使p变量对子类透明.
	 *
	 * @param key
	 *            property key.
	 */
	protected static String getProperty(String key) {
		return p.getProperty(key);
	}
}
