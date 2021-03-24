package com.ecmp.flow.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.regex.Pattern;

@Component
public class SqlCommonUtil implements Serializable {


    static String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
            + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";

    static Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);


    /**
     * 参数校验
     * @param str
     * @return 是否满足sql注入(false为满足)
     */
    public static boolean isValid(String str) {
        if (StringUtils.isNotEmpty(str)) {
            if (sqlPattern.matcher(str).find()) {
                return false;
            }
        }
        return true;
    }


}
