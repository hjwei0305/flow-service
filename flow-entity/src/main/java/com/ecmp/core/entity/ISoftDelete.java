package com.ecmp.core.entity;

/**
 * 实现功能：软删除特征接口
 * 实体实现该接口平台默认将dao层的delete方法以软删除方式实现
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2019-07-18 13:23
 */
public interface ISoftDelete {

    String DELETED = "deleted";

    /**
     * 建议以当前时间戳{@link System#currentTimeMillis()}作为deleted的值
     * 以非0的值为删除做判断
     *
     * @return 等于0则可用，非0则删除
     */
    Long getDeleted();

    /**
     * 当前时间戳{@link System#currentTimeMillis()}作为deleted的值
     *
     * @param deleted 当前时间戳
     */
    void setDeleted(Long deleted);
}
