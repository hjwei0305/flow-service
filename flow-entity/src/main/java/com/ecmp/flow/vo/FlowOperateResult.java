package com.ecmp.flow.vo;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：事件操作结果VO
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/11/9 17:25      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowOperateResult implements Serializable{
    /**
     * 成功状态
     */
    private boolean success;
    /**
     *  返回消息
     */
    private String message;

    /**
     *  签收人，针对需要立即签收执行人的情况，如工作池任务
     */
    private String userId;

    public FlowOperateResult(){
        this.success=true;
        this.message="操作成功";
    }

    public FlowOperateResult(boolean success,String message){
        this.success=success;
        this.message=message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String toString(){
        return "成功状态："+this.isSuccess()+";返回消息="+this.getMessage();
    }
}
