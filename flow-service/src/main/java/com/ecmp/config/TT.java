package com.ecmp.config;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/4/19 13:42      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class TT {
    public static void main(String[] args){
        String webBaseAddress = "http://decmp.changhong.com/flow-web/";
        webBaseAddress =  webBaseAddress.substring(webBaseAddress.indexOf("://")+3);
        webBaseAddress = webBaseAddress.substring(webBaseAddress.indexOf("/"));
        System.out.println(webBaseAddress);
    }

}
