package com.ecmp.flow.dto;

import java.io.Serializable;

/**
 * 实现功能: 门户待办信息
 *
 * @author 王锦光 wangjg
 * @version 2020-06-04 17:29
 */
public class PortalFlowTask implements Serializable {
    /**
     * 待办任务Id
     */
    private String id;

    /**
     * 任务表单URL
     */
    private String taskFormUrl;
}
