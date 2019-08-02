/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/7/19 15:50:17                           */
/*==============================================================*/


drop table if exists business_model_selfDefEmployee;

/*==============================================================*/
/* Table: flow_task_control_and_push                            */
/*==============================================================*/
create table flow_task_control_and_push
(
   id                   varchar(36) not null,
   control_id           varchar(36) not null,
   push_id              varchar(36) not null,
   creator_id           varchar(36),
   creator_account      varchar(50),
   creator_name         varchar(50),
   created_date         datetime,
   last_editor_id       varchar(36),
   last_editor_account  varchar(50),
   last_editor_name     varchar(50),
   last_edited_date     datetime,
   primary key (id)
);

alter table flow_task_control_and_push comment '关联父表和子表的关联关系';

/*==============================================================*/
/* Index: Index_control_and_push                                */
/*==============================================================*/
create index Index_control_and_push on flow_task_control_and_push
(
   control_id,
   push_id
);

/*==============================================================*/
/* Index: fk_task_push_id                                       */
/*==============================================================*/
create index fk_task_push_id on flow_task_control_and_push
(
   push_id
);

/*==============================================================*/
/* Table: flow_task_push                                        */
/*==============================================================*/
create table flow_task_push
(
   id                   varchar(36) not null,
   flow_task_id         varchar(36) not null,
   version              int comment '版本-乐观锁',
   flow_instance_id     varchar(36),
   flow_name            varchar(80) not null,
   task_name            varchar(80) not null,
   act_task_def_key     varchar(255) not null,
   task_form_url        text,
   task_status          varchar(80),
   proxy_status         varchar(80),
   flow_definition_id   varchar(36) not null,
   act_task_id          varchar(36) comment '引擎流程任务ID',
   executor_name        varchar(80),
   executor_id          varchar(36) comment '执行人ID',
   executor_account     varchar(36),
   candidate_id         varchar(36) comment '候选人ID',
   candidate_account    varchar(36),
   execute_date         datetime,
   depict               varchar(255),
   priority             int comment '优先级',
   owner_id             varchar(36) comment '所属人ID',
   owner_account        varchar(36) comment '所属人',
   owner_name           varchar(100) comment '所属人名称',
   act_type             varchar(60) comment '实际任务类型',
   act_claim_time       datetime comment '签收时间',
   act_due_date         datetime comment '实际触发时间',
   act_task_key         varchar(255) comment '实际任务定义KEY',
   pre_id               varchar(36),
   can_reject           bit(1) comment '能否驳回',
   can_suspension       bit(1) comment '能否中止',
   task_json_def        text comment '任务json定义 ',
   businessModel_remark varchar(255) comment '业务摘要',
   execute_time         int comment '额定工时(分钟)',
   tenant_code          varchar(10) comment '租户代码',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   can_batch_approval   boolean comment '能否批量审批',
   can_mobile           boolean comment '能否由移动端执行审批',
   work_page_url_id     varchar(36) comment '工作界面id',
   trust_state          int,
   trust_owner_task_id  varchar(36),
   allow_add_sign       bit(1) comment '允许加签',
   allow_subtract_sign  bit(1) comment '允许减签',
   primary key (id)
);

alter table flow_task_push comment '推送任务子表（任务记录表）';

/*==============================================================*/
/* Index: fk_push_task_id                                       */
/*==============================================================*/
create  index fk_push_task_id on flow_task_push
(
   flow_task_id
);

/*==============================================================*/
/* Table: flow_task_push_control                                */
/*==============================================================*/
create table flow_task_push_control
(
   id                   varchar(36) not null,
   app_module_id        varchar(36) not null,
   business_model_id    varchar(36) not null,
   flow_type_id         varchar(36) not null,
   flow_instance_id     varchar(36) not null,
   flow_instance_name   varchar(80),
   flow_act_task_def_key varchar(30) not null,
   flow_task_name       varchar(255),
   business_id          varchar(36),
   business_code        varchar(100),
   executor_name_list   varchar(2000),
   push_type            varchar(20),
   push_status          varchar(20),
   push_url             varchar(255),
   push_number          int,
   push_success         int,
   push_false           int,
   push_start_date      datetime,
   push_end_date        datetime,
   tenant_code          varchar(10),
   creator_id           varchar(36),
   creator_account      varchar(50),
   creator_name         varchar(50),
   created_date         datetime,
   last_editor_id       varchar(36),
   last_editor_account  varchar(50),
   last_editor_name     varchar(50),
   last_edited_date     datetime,
   primary key (id)
);

alter table flow_task_push_control comment '推送任务记录父表';

/*==============================================================*/
/* Index: fk_control_instance_id                                */
/*==============================================================*/
create  index fk_control_instance_id on flow_task_push_control
(
   flow_instance_id
);

/*==============================================================*/
/* Index: fk_control_task__key                                  */
/*==============================================================*/
create  index fk_control_task__key on flow_task_push_control
(
   flow_act_task_def_key
);

alter table flow_task_control_and_push add constraint FK_fk_push_control_id foreign key (control_id)
      references flow_task_push_control (id) on delete restrict on update restrict;

alter table flow_task_control_and_push add constraint FK_fk_push_task_id foreign key (push_id)
      references flow_task_push (id) on delete restrict on update restrict;

