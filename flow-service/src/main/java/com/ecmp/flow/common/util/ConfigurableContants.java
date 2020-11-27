package com.ecmp.flow.common.util;

import com.ecmp.log.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * *************************************************************************************************
 * </p><p>
 * 可用Properties文件配置的Contants基类.
 * 本类既保持了Contants的static和final(静态与不可修改)特性,又拥有了可用Properties文件配置的特征,
 * 主要是应用了Java语言中静态初始化代码的特性. </p><p> 子类可如下编写
 *</p><p>
 * <pre>
 *  public class Constants extends ConfigurableContants {
 *   static {
 *     init("framework.properties");
 *  }
 *
 *  public final static String ERROR_BUNDLE_KEY = getProperty("constant.error_bundle_key", "errors"); }
 * </pre>
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 版本          变更时间             变更人                     变更原因
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 1.0.00      2017/04/06 22:39      谭军(tanjun)               新建
 * </p><p>
 * *************************************************************************************************
 */
public class ConfigurableContants {

	protected static Properties p = new Properties();

	/**
	 * 静态读入属性文件到Properties p变量中
	 */
	protected static void init(String propertyFileName) {
		InputStream in = null;
		try {
			in = ConfigurableContants.class.getClassLoader().getResourceAsStream(propertyFileName);
			if (in != null){
				LogUtil.debug("load " + propertyFileName + " into Contants!");
				p.load(in);
			}
		} catch (IOException e) {
			LogUtil.error("load " + propertyFileName + " into Contants error!",e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogUtil.error("close " + propertyFileName + " error!",e);
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
