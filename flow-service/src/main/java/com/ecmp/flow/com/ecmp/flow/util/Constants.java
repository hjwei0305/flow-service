package com.ecmp.flow.com.ecmp.flow.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统级静态常量. 可通过framework.properties初始化,同时保持常量static & final的特征.
 * 
 * @see ConfigurableContants
 */
public class Constants extends ConfigurableContants {

	// 静态初始化读入framework.properties中的设置
	static {
		init("framework.properties");
	}

	/**
	 * 从framework.properties中读取activtiTaskStatus.assinge的值，
	 * 如果配置文件不存在或配置文件中不存在该值时，默认取值"assinge"
	 */
	public final static String TASK_STATUS_CLAIM= getProperty(
			"activtiTaskStatus.Claim", "claim");


}
