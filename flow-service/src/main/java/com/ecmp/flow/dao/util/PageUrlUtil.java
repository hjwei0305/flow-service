package com.ecmp.flow.dao.util;

import com.ecmp.flow.common.util.Constants;
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
     *
     * @param baseAddress 基地址
     * @param pageUrl     页面地址
     * @return 完整URL
     */
    public static String buildUrl(String baseAddress, String pageUrl) {
        if (StringUtils.endsWith(baseAddress, "/")) {
            baseAddress = StringUtils.substringBeforeLast(baseAddress, "/");
        }
        if (StringUtils.startsWith(pageUrl, "/")) {
            pageUrl = StringUtils.substringAfter(pageUrl, "/");
        }
        if (StringUtils.isBlank(baseAddress)) {
            return pageUrl;
        }
        return baseAddress + "/" + pageUrl;
    }

    /**
     * 拼接完整的URL
     *
     * @param baseAddress 基地址
     * @param pageUrl     页面地址
     * @param params      参数
     * @return 完整URL
     */
    public static String buildUrl(String baseAddress, String pageUrl, String params) {
        String address = buildUrl(baseAddress, pageUrl);
        if (StringUtils.containsAny(address, "?")) {
            return address + "&" + params;
        }
        return address + "?" + params;
    }


    /**
     * 判断地址里面是否带了应用模块相对地址
     *
     * @param pageUrl 需要判断的url
     * @return 返回是否带有应用模块相对地址的布尔值
     */
    public static boolean isAppModelUrl(String pageUrl) {
        if (StringUtils.isEmpty(pageUrl)) {
            return false;
        }
        String checkUrl = pageUrl;
        if (StringUtils.startsWith(checkUrl, "/")) {
            checkUrl = StringUtils.substringAfter(checkUrl, "/");
        }
        int count = 0;
        while (checkUrl.contains("/")) {
            checkUrl = checkUrl.substring(checkUrl.indexOf("/") + 1);
            count++;
        }
        return count > 1;
    }


    public static String getBaseApiUrl() {
        String baseApiUrl = Constants.getBaseApi();
        //K8S配置文件中base_api地址为空
        if (StringUtils.isEmpty(baseApiUrl)) {
            String baseWebUrl = Constants.getBaseWeb();
            String gatewayUrl = Constants.getFlowPropertiesByKey("GATEWAY_NAME");
            if (StringUtils.isEmpty(gatewayUrl)) {
                baseApiUrl = buildUrl(baseWebUrl, "/api-gateway");
            } else {
                baseApiUrl = buildUrl(baseWebUrl, gatewayUrl);
            }
        }
        return baseApiUrl;
    }

}
