package com.ecmp.flow.dao.util;

import org.apache.commons.lang3.StringUtils;

/**
 * <strong>实现功能:</strong>
 * <p>页面URL工具类</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-08-01 14:27
 */
public class PageUrlUtil {
    /**
     * 拼接完整的URL
     * @param baseAddress 基地址
     * @param pageUrl 页面地址
     * @return 完整URL
     */
    public static String buildUrl(String baseAddress, String pageUrl){
        if (StringUtils.endsWith(baseAddress, "/")){
            baseAddress = StringUtils.substringBeforeLast(baseAddress, "/");
        }
        if (StringUtils.startsWith(pageUrl, "/")){
            pageUrl = StringUtils.substringAfter(pageUrl, "/");
        }
        return baseAddress + "/" + pageUrl;
    }

    /**
     * 拼接完整的URL
     * @param baseAddress 基地址
     * @param pageUrl 页面地址
     * @param params 参数
     * @return 完整URL
     */
    public static String buildUrl(String baseAddress, String pageUrl, String params){
        String address = buildUrl(baseAddress, pageUrl);
        if (StringUtils.containsAny(address, "?")){
            return address + "&" + params;
        }
        return address + "?" + params;
    }
}
