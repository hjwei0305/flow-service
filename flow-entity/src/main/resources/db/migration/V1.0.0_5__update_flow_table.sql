ALTER TABLE flow_history ADD COLUMN actHistoryId  VARCHAR(36) NOT NULL COMMENT '引擎流程历史ID';
ALTER TABLE flow_instance ADD COLUMN actInstanceId        VARCHAR(36) NOT NULL COMMENT '引擎流程实例ID';
ALTER TABLE flow_task ADD COLUMN actTaskId            VARCHAR(36) NOT NULL COMMENT '引擎流程任务ID';