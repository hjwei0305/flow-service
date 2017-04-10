/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/7 12:31:05                            */
/*==============================================================*/


drop table if exists tmp_flow_history;

rename table flow_history to tmp_flow_history;

/*==============================================================*/
/* Table: flow_history                                          */
/*==============================================================*/
create table flow_history
(
   id                   varchar(36) not null,
   flowName             varchar(80) not null,
   flowTaskName         varchar(80) not null,
   flowRunId            varchar(36) not null,
   flowInstanceId       varchar(36) not null,
   flowDefId            varchar(36) not null,
   ownerAccount         varchar(100) comment '所属人',
   ownerName            varchar(100) comment '所属人名称',
   executorName         varchar(80),
   executorAccount      varchar(100),
   candidateAccount     varchar(100),
   depict               varchar(255),
   createdBy            varchar(100),
   createdDate          datetime,
   lastModifiedBy       varchar(100),
   lastModifiedDate     datetime,
   flowInstance_id      varchar(36),
   actHistoryId         varchar(36) comment '引擎流程历史ID',
   claimTime            datetime comment '签收时间',
   actType              varchar(60) comment '实际任务类型',
   actWorkTimeInMillis  bigint comment '执行的工作时间间隔',
   actDurationInMillis  bigint comment '实际执行的时间间隔',
   actEndTime           datetime comment '结束时间',
   actStartTime         datetime comment '实际开始时间',
   actTaskKey           varchar(255) comment '实际任务定义KEY',
   primary key (id)
);

alter table flow_history comment '流程历史';

insert into flow_history (id, flowName, flowTaskName, flowRunId, flowInstanceId, flowDefId, depict, createdBy, createdDate, lastModifiedBy, lastModifiedDate, flowInstance_id, actHistoryId, claimTime, actType, actWorkTimeInMillis, actDurationInMillis, actEndTime, actStartTime, actTaskKey)
select id, flowName, flowTaskName, flowRunId, flowInstanceId, flowDefId, depict, createdBy, createdDate, lastModifiedBy, lastModifiedDate, flowInstance_id, actHistoryId, claimTime, actType, actWorkTimeInMillis, actDurationInMillis, actEndTime, actStartTime, actTaskKey
from tmp_flow_history;

drop table tmp_flow_history;

alter table flow_history add constraint FK_history_instance foreign key (flowInstance_id)
      references flow_instance (id) on delete restrict on update restrict;

