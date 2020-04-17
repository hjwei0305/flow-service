package com.ecmp.vo;

import java.io.Serializable;

/**
 * <strong>实现功能：</strong>
 * <p>
 * 定义全局操作状态对象
 * 要求所有对外服务及web传输都以该对象输出
 * </p>
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2017/3/28 16:12
 */
public final class OperateResult extends ResponseData<Object> implements Serializable {
    private static final long serialVersionUID = 652732203000606895L;

    /**
     *
     */
    private OperateResult() {
    }

//    /**
//     * @param status  操作状态
//     * @param message 消息
//     */
//    protected OperateResult(StatusEnum status, String message) {
//        super(status, message);
//    }

    /**
     * @param status  操作状态
     * @param key     多语言key
     * @param msgArgs 多语言参数
     */
    protected OperateResult(StatusEnum status, String key, Object... msgArgs) {
        super(status, key, msgArgs);
    }

    /**
     * 成功
     *
     * @return 返回操作对象
     */
    @Override
    public OperateResult succeed() {
        this.success = Boolean.TRUE;
        this.status = StatusEnum.SUCCESS;
        return this;
    }

    /**
     * 成功
     *
     * @param key     多语言key
     * @param msgArgs 多语言参数
     * @return 返回操作对象
     */
    @Override
    public OperateResult succeed(String key, Object... msgArgs) {
        this.success = Boolean.TRUE;
        this.status = StatusEnum.SUCCESS;
        this.message = message(key, msgArgs);
        return this;
    }

    /**
     * 失败
     *
     * @param key     多语言key
     * @param msgArgs 多语言参数
     * @return 返回操作对象
     */
    @Override
    public OperateResult fail(String key, Object... msgArgs) {
        this.success = Boolean.FALSE;
        this.status = StatusEnum.FAILURE;
        this.message = message(key, msgArgs);
        return this;
    }

    /**
     * 警告
     *
     * @param key     多语言key
     * @param msgArgs 多语言参数
     * @return 返回操作对象
     */
    @Override
    public OperateResult warn(String key, Object... msgArgs) {
        this.success = Boolean.FALSE;
        this.status = StatusEnum.WARNING;
        this.message = message(key, msgArgs);
        return this;
    }

    @Override
    public String toString() {
        return "OperateResult{"
                + "status=" + status
//                 +", key=" + key + ""
                + ", message=" + message
                + "}";
    }

    /**
     * @return 返回一个成功的操作状态对象
     */
    public static OperateResult operationSuccess() {
        return new OperateResult(StatusEnum.SUCCESS, "ecmp_context_00001");
    }

    /**
     * @param key 多语言key
     * @return 返回一个成功的操作状态对象
     */
    public static OperateResult operationSuccess(String key) {
        return new OperateResult(StatusEnum.SUCCESS, key);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @return 返回一个成功的操作状态对象
     */
    public static OperateResult operationSuccess(String key, Object... msgArgs) {
        return new OperateResult(StatusEnum.SUCCESS, key, msgArgs);
    }

    /**
     * @param key 多语言key
     * @return 返回一个失败的操作状态对象
     */
    public static OperateResult operationFailure(String key) {
        return new OperateResult(StatusEnum.FAILURE, key);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @return 返回一个失败的操作状态对象
     */
    public static OperateResult operationFailure(String key, Object... msgArgs) {
        return new OperateResult(StatusEnum.FAILURE, key, msgArgs);
    }

    /**
     * @param key 多语言key
     * @return 返回一个警告的操作状态对象
     */
    public static OperateResult operationWarning(String key) {
        return new OperateResult(StatusEnum.WARNING, key);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @return 返回一个警告的操作状态对象
     */
    public static OperateResult operationWarning(String key, Object... msgArgs) {
        return new OperateResult(StatusEnum.WARNING, key, msgArgs);
    }

    /**
     * @param result 操作结果
     * @param t      类型
     * @param <T>    泛型
     * @return 返回withData的结果
     */
    public static <T extends Serializable> OperateResultWithData<T> converterWithData(OperateResult result, T t) {
        return new OperateResultWithData<>(result.status, t, result.getMessage());
    }
}
