package com.ecmp.vo;

import com.ecmp.annotation.Remark;

/**
 * <strong>实现功能：</strong>
 * <p>
 * 登录状态枚举
 * </p>
 *
 * @author <a href="mailto:chao2.ma@changhong.com">马超(Vision.Mac)</a>
 * @version 1.0.1 2017/5/23 19:11
 */
public enum LoginStatus {

    /**
     * 登录成功
     */
    @Remark(value = "登陆成功")
    success,

    /**
     * 登录失败
     * 账号密码错误或账号不存在
     */
    @Remark(value = "登陆失败", comments = "账号密码错误或账号不存在")
    failure,

    /**
     * 多租户
     * 登录时需要传入租户代码
     */
    @Remark(value = "多租户", comments = "登录时需要传入租户代码")
    multiTenant,

    /**
     * 验证码错误
     */
    @Remark(value = "验证码错误", comments = "验证码错误")
    captchaError,

    /**
     * 账号被冻结
     */
    @Remark(value = "账号被冻结", comments = "账号被冻结")
    frozen,

    /**
     * 账号被锁定
     */
    @Remark(value = "账号被锁定", comments = "账号被锁定")
    locked
}
