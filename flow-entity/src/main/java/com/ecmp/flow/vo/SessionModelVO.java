package com.ecmp.flow.vo;

import java.io.Serializable;

public class SessionModelVO  implements Serializable {

    private  String randomKey;//随机值(会话ID)
    private  String appId;//应用标识
    private  String account;//登录账号
    private  String userId;//登录用户ID
    private  String userName;//登录用户名
    private  String authorityPolicy;//登录用户权限策略
    private  String userType;//登录用户类型
    private  String tenant;//登录用户租户代码
    private  String email;//登录用户email
    private  String ip;//客户端IP
    private  String sub;//登录账号
    private  String exp;//token过期时间
    private  String iat;//token生成时间


    public String getRandomKey() {
        return randomKey;
    }

    public void setRandomKey(String randomKey) {
        this.randomKey = randomKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAuthorityPolicy() {
        return authorityPolicy;
    }

    public void setAuthorityPolicy(String authorityPolicy) {
        this.authorityPolicy = authorityPolicy;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getIat() {
        return iat;
    }

    public void setIat(String iat) {
        this.iat = iat;
    }
}
