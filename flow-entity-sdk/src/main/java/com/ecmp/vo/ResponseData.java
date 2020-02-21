package com.ecmp.vo;

import com.ecmp.annotation.Remark;
//import com.ecmp.context.ContextUtil;
import com.ecmp.util.EnumUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2018/8/9 9:01
 */
public class ResponseData<T> implements Serializable {
    private static final long serialVersionUID = 652732203000606893L;
    /**
     * 操作状态
     */
    StatusEnum status;
    /**
     * 成功标志
     */
    Boolean success = Boolean.TRUE;
    /**
     * 返回数据
     */
    private Integer statusCode = 200;
    /**
     * 消息
     */
    protected String message;

    /**
     * 返回数据
     */
    protected T data;

    public ResponseData() {
    }

    /**
     * @param status  操作状态
     * @param key     多语言key
     * @param msgArgs 消息参数
     */
    ResponseData(StatusEnum status, String key, Object[] msgArgs) {
        this.success = StatusEnum.SUCCESS.equals(status);
        this.status = status;

        this.message = message(key, msgArgs);
    }

    static String message(String key, Object[] msgArgs) {
        if (Objects.nonNull(msgArgs)) {
//            return ContextUtil.getMessage(key, msgArgs, ContextUtil.getLocale());
            return "sdk去除了多语言引用";
        } else {
//            return ContextUtil.getMessage(key, ContextUtil.getLocale());
            return "sdk去除了多语言引用";
        }
    }

    public Boolean getSuccess() {
        return success;
    }

    public ResponseData<T> setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * 获取操作状态
     *
     * @return 返回操作状态
     * @see StatusEnum
     */
    public StatusEnum getStatus() {
        return status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public ResponseData<T> setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * 获取操作消息
     *
     * @return 返回操作消息
     */
    public String getMessage() {
        return message;
    }

    public ResponseData<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 获取数据
     *
     * @return 返回数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置数据
     *
     * @param data 数据
     * @return 返回当前对象
     */
    public ResponseData<T> setData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 是否是默认的
     *
     * @return 返回false，则表示默认的，反之不是
     */
    public boolean noneDefault() {
        return !StatusEnum.DEFAULT.equals(status);
    }

    /**
     * 是否成功
     *
     * @return 返回true，则表示操作成功，反之失败
     */
    public boolean successful() {
        return success;
    }

    /**
     * 是否未成功
     *
     * @return 返回true，则表示操作失败，反之成功
     */
    public boolean notSuccessful() {
        return !successful();
    }

    /**
     * 成功
     *
     * @return 返回操作对象
     */
    public ResponseData succeed() {
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
    public ResponseData succeed(String key, Object... msgArgs) {
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
    public ResponseData fail(String key, Object... msgArgs) {
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
    public ResponseData warn(String key, Object... msgArgs) {
        this.success = Boolean.FALSE;
        this.status = StatusEnum.WARNING;
        this.message = message(key, msgArgs);
        return this;
    }

    public static <T> ResponseData<T> build() {
        return new ResponseData<>();
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "success=" + success +
                ", status=" + status +
                ", statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * @param <Data> 泛型限定
     * @return 返回一个成功的操作状态对象
     */
    public static <Data> ResponseData<Data> operationDefault() {
        return new ResponseData<>(StatusEnum.DEFAULT, "ecmp_context_00001", null);
    }
//
//    /**
//     * @param key    多语言key
//     * @param <Data> 泛型限定
//     * @return 返回一个成功的操作状态对象
//     */
//    public static <Data> ResponseData<Data> operationDefault(String key) {
//        return new ResponseData<>(StatusEnum.DEFAULT, key, null);
//    }
//
//    /**
//     * @param key     多语言key
//     * @param msgArgs 多语言填充参数
//     * @param <Data>  泛型限定
//     * @return 返回一个成功的操作状态对象
//     */
//    public static <Data> ResponseData<Data> operationDefault(String key, Object... msgArgs) {
//        return new ResponseData<>(StatusEnum.DEFAULT, key, msgArgs);
//    }
//
//    /**
//     * @param data   数据对象
//     * @param <Data> 泛型限定
//     * @return 返回一个成功的操作状态对象
//     */
//    public static <Data> ResponseData<Data> operationDefaultWithData(Data data) {
//        ResponseData<Data> response = new ResponseData<>(StatusEnum.DEFAULT, "ecmp_context_00001", null);
//        response.setData(data);
//        return response;
//    }
//
//    /**
//     * @param data    数据对象
//     * @param key     多语言key
//     * @param msgArgs 多语言填充参数
//     * @param <Data>  泛型限定
//     * @return 返回一个成功的操作状态对象
//     */
//    public static <Data> ResponseData<Data> operationDefaultWithData(Data data, String key, Object... msgArgs) {
//        ResponseData<Data> response = new ResponseData<>(StatusEnum.DEFAULT, key, msgArgs);
//        response.setData(data);
//        return response;
//    }

    /**
     * @param <Data> 泛型限定
     * @return 返回一个成功的操作状态对象
     */
    public static <Data> ResponseData<Data> operationSuccess() {
        return new ResponseData<>(StatusEnum.SUCCESS, "ecmp_context_00001", null);
    }

    /**
     * @param key    多语言key
     * @param <Data> 泛型限定
     * @return 返回一个成功的操作状态对象
     */
    public static <Data> ResponseData<Data> operationSuccess(String key) {
        return new ResponseData<>(StatusEnum.SUCCESS, key, null);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @param <Data>  泛型限定
     * @return 返回一个成功的操作状态对象
     */
    public static <Data> ResponseData<Data> operationSuccess(String key, Object... msgArgs) {
        return new ResponseData<>(StatusEnum.SUCCESS, key, msgArgs);
    }

    /**
     * @param data   数据对象
     * @param <Data> 泛型限定
     * @return 返回一个成功的操作状态对象
     */
    public static <Data> ResponseData<Data> operationSuccessWithData(Data data) {
        ResponseData<Data> response = new ResponseData<>(StatusEnum.SUCCESS, "ecmp_context_00001", null);
        response.setData(data);
        return response;
    }

    /**
     * @param data    数据对象
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @param <Data>  泛型限定
     * @return 返回一个成功的操作状态对象
     */
    public static <Data> ResponseData<Data> operationSuccessWithData(Data data, String key, Object... msgArgs) {
        ResponseData<Data> response = new ResponseData<>(StatusEnum.SUCCESS, key, msgArgs);
        response.setData(data);
        return response;
    }

    /**
     * @param key    多语言key
     * @param <Data> 泛型限定
     * @return 返回一个失败的操作状态对象
     */
    public static <Data> ResponseData<Data> operationFailure(String key) {
        return new ResponseData<>(StatusEnum.FAILURE, key, null);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @param <Data>  泛型限定
     * @return 返回一个失败的操作状态对象
     */
    public static <Data> ResponseData<Data> operationFailure(String key, Object... msgArgs) {
        return new ResponseData<>(StatusEnum.FAILURE, key, msgArgs);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @param data    数据对象
     * @param <Data>  泛型限定
     * @return 返回一个失败的操作状态对象
     */
    public static <Data> ResponseData<Data> operationFailureWithData(Data data, String key, Object... msgArgs) {
        ResponseData<Data> response = new ResponseData<>(StatusEnum.FAILURE, key, msgArgs);
        response.setData(data);
        return response;
    }

    /**
     * @param key    多语言key
     * @param <Data> 泛型限定
     * @return 返回一个警告的操作状态对象
     */
    public static <Data> ResponseData<Data> operationWarning(String key) {
        return new ResponseData<>(StatusEnum.WARNING, key, null);
    }

    /**
     * @param key     多语言key
     * @param msgArgs 多语言填充参数
     * @param <Data>  泛型限定
     * @return 返回一个警告的操作状态对象
     */
    public static <Data> ResponseData<Data> operationWarning(String key, Object... msgArgs) {
        return new ResponseData<>(StatusEnum.WARNING, key, msgArgs);
    }

    /**
     * @param key     多语言key
     * @param data    数据对象
     * @param msgArgs 多语言填充参数
     * @param <Data>  泛型限定
     * @return 返回一个警告的操作状态对象
     */
    public static <Data> ResponseData<Data> operationWarningWithData(Data data, String key, Object... msgArgs) {
        ResponseData<Data> response = new ResponseData<>(StatusEnum.WARNING, key, msgArgs);
        response.setData(data);
        return response;
    }

    /**
     * 状态枚举
     * 1-成功；0-警告；-1-失败
     */
    protected enum StatusEnum {
        /**
         * 主要用于二次开发扩展状态控制
         * 当状态为DEFAULT时,表示未被扩展
         */
        @Remark(value = "默认")
        DEFAULT,
        /**
         * 成功
         */
        @Remark(value = "成功")
        SUCCESS,
        /**
         * 失败
         */
        @Remark(value = "失败")
        FAILURE,
        /**
         * 警告
         */
        @Remark(value = "警告")
        WARNING;


        @Override
        public String toString() {
            return this.name() + "[" + this.ordinal() + "] - " + EnumUtils.getEnumItemRemark(StatusEnum.class, this);
        }
    }
}
