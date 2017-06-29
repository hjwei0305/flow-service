/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/6/22 18:36:18                           */
/*==============================================================*/


drop table if exists tmp_flow_defination;

rename table flow_defination to tmp_flow_defination;

drop table if exists tmp_flow_task;

rename table flow_task to tmp_flow_task;

alter table business_model
   add look_url varchar(6000) comment '查看表单URL';

alter table default_business_model
   add business_code varchar(20) comment '业务编号';

alter table flow_def_version
   add flowDefinationStatus smallint comment '状态';

alter table flow_def_version
   modify column def_json mediumtext;

alter table flow_def_version
   modify column def_bpmn mediumtext;

alter table flow_def_version
   modify column def_xml mediumtext;

/*==============================================================*/
/* Table: flow_defination                                       */
/*==============================================================*/
create table flow_defination
(
   id                   varchar(36) not null comment 'ID',
   def_key              varchar(255) not null comment '定义Key',
   name                 varchar(80) not null comment '名称',
   last_deloy_version_id varchar(36) comment '最新已发布版本ID',
   last_version_id      varchar(36) comment '最新版本ID',
   start_uel            varchar(255) comment '启动条件UEL',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   flow_type_id         varchar(36),
   version              int comment '版本-乐观锁',
   basic_org_id         varchar(36) comment '组织机构id',
   basic_org_code       varchar(6) comment '组织机构code',
   flowDefinationStatus smallint comment '状态',
   primary key (id)
);

alter table flow_defination comment '流程定义';

insert into flow_defination (id, def_key, name, last_version_id, start_uel, depict, created_by, created_date, last_modified_by, last_modified_date, flow_type_id, version, basic_org_id, basic_org_code)
select id, def_key, name, last_version_id, start_uel, depict, created_by, created_date, last_modified_by, last_modified_date, flow_type_id, version, basic_org_id, basic_org_code
from tmp_flow_defination;

/*==============================================================*/
/* Index: idx_flow_defination_def_key                           */
/*==============================================================*/
create unique index idx_flow_defination_def_key on flow_defination
(
   def_key
);

alter table flow_instance
   add business_code varchar(20) comment '业务编号';

alter table flow_instance
   add business_name varchar(100) comment '业务单据名称';

alter table flow_instance
   add business_extra_map longblob comment '业务单据额外属性Map';

alter table flow_instance
   add manuallyEnd smallint(1) comment '是否是人工强制退出流程';

/*==============================================================*/
/* Table: flow_task                                             */
/*==============================================================*/
create table flow_task
(
   id                   varchar(36) not null,
   flow_name            varchar(80) not null,
   task_name            varchar(80) not null,
   act_task_def_key     varchar(255) not null,
   task_form_url        text,
   task_status          varchar(80),
   proxy_status         varchar(80),
   flow_definition_id   varchar(36) not null,
   executor_name        varchar(80),
   executor_id          varchar(36) comment '执行人ID',
   executor_account     varchar(36),
   candidate_id         varchar(36) comment '候选人ID',
   candidate_account    varchar(36),
   created_date         datetime,
   execute_date         datetime,
   depict               varchar(255),
   created_by           varchar(100),
   last_modified_by     varchar(100),
   last_modified_date   datetime,
   flow_instance_id     varchar(36),
   act_task_id          varchar(36) comment '引擎流程任务ID',
   priority             int comment '优先级',
   owner_id             varchar(36) comment '所属人ID',
   owner_account        varchar(36) comment '所属人',
   owner_name           varchar(100) comment '所属人名称',
   act_type             varchar(60) comment '实际任务类型',
   act_claim_time       datetime comment '签收时间',
   act_due_date         datetime comment '实际触发时间',
   act_task_key         varchar(255) comment '实际任务定义KEY',
   pre_id               varchar(36),
   version              int comment '版本-乐观锁',
   canReject            bit(1) comment '能否驳回',
   canSuspension        bit(1) comment '能否中止',
   taskJsonDef          varchar(1200) comment '任务json定义 ',
   businessModelRemark  varchar(255) comment '业务摘要',
   executeTime          int comment '额定工时(分钟)',
   primary key (id)
);

alter table flow_task comment '流程任务';

#WARNING: The following insert order will not restore columns: executor_account, candidate_account, owner_account
insert into flow_task (id, flow_name, task_name, act_task_def_key, task_form_url, task_status, proxy_status, flow_definition_id, executor_name, created_date, execute_date, depict, created_by, last_modified_by, last_modified_date, flow_instance_id, act_task_id, priority, owner_name, act_type, act_claim_time, act_due_date, act_task_key, pre_id, version, canReject, canSuspension, taskJsonDef, businessModelRemark, executeTime)
select id, flow_name, task_name, act_task_def_key, task_form_url, task_status, proxy_status, flow_definition_id, executor_name, created_date, execute_date, depict, created_by, last_modified_by, last_modified_date, flow_instance_id, act_task_id, priority, owner_name, act_type, act_claim_time, act_due_date, act_task_key, pre_id, version, canReject, canSuspension, taskJsonDef, businessModelRemark, executeTime
from tmp_flow_task;

alter table flow_def_version add constraint fk_def_version_defination_id foreign key (flow_defination_id)
      references flow_defination (id) on delete restrict on update restrict;

alter table flow_defination add constraint fk_flow_defination_type_id foreign key (flow_type_id)
      references flow_type (id) on delete restrict on update restrict;

alter table flow_task add constraint fk_flow_task_instance_id foreign key (flow_instance_id)
      references flow_instance (id) on delete restrict on update restrict;

alter table flow_variable add constraint FK_fk_flow_variable_task foreign key (task_id)
      references flow_task (id) on delete cascade on update restrict;

