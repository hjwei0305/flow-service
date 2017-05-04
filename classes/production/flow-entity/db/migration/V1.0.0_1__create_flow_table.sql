/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/3 9:53:19                             */
/*==============================================================*/



drop table if exists app_module;


drop table if exists business_model;


drop table if exists flow_def_version;


drop table if exists flow_defination;

drop table if exists flow_hi_varinst;

drop table if exists flow_history;

drop table if exists flow_instance;


drop table if exists flow_service_url;

drop table if exists flow_task;


drop table if exists flow_type;

drop table if exists flow_variable;

drop table if exists work_page_url;

/*==============================================================*/
/* Table: app_module                                            */
/*==============================================================*/
create table app_module
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(60) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table app_module comment '应用模块';

/*==============================================================*/
/* Index: idx_app_module_code                                   */
/*==============================================================*/
create unique index idx_app_module_code on app_module
(
   code
);

/*==============================================================*/
/* Table: business_model                                        */
/*==============================================================*/
create table business_model
(
   id                   varchar(36) not null comment 'ID',
   class_name           varchar(255) comment '类全路径',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   app_module_id        varchar(36),
   version              int comment '版本-乐观锁',
   conditon_bean        varchar(255) comment '转换对象',
   primary key (id)
);

alter table business_model comment '业务实体模型';

/*==============================================================*/
/* Index: idx_business_model_class_name                         */
/*==============================================================*/
create unique index idx_business_model_class_name on business_model
(
   class_name
);

/*==============================================================*/
/* Table: flow_def_version                                      */
/*==============================================================*/
create table flow_def_version
(
   id                   varchar(36) not null comment 'ID',
   act_def_id           varchar(255) not null comment '定义ID',
   def_key              varchar(255) not null comment '定义KEY',
   name                 varchar(80) not null comment '名称',
   act_deploy_id        varchar(36) comment '部署ID',
   start_uel            varchar(255) comment '启动条件UEL',
   version_code         int comment '版本号',
   priority             int comment '优先级',
   def_json             text comment '流程JSON文本',
   def_bpmn             text comment '流程BPMN文本',
   def_xml              text comment '最终定义XML',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   flow_defination_id   varchar(36) comment '关联流程类型',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table flow_def_version comment '流程定义版本';

/*==============================================================*/
/* Index: idx_flow_def_version_def_key                          */
/*==============================================================*/
create unique index idx_flow_def_version_def_key on flow_def_version
(
   def_key
);

/*==============================================================*/
/* Table: flow_defination                                       */
/*==============================================================*/
create table flow_defination
(
   id                   varchar(36) not null comment 'ID',
   def_key              varchar(255) not null comment '定义Key',
   name                 varchar(80) not null comment '名称',
   last_version_id      varchar(36) comment '最新版本ID',
   start_uel            varchar(255) comment '启动条件UEL',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   flow_type_id         varchar(36),
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table flow_defination comment '流程定义';

/*==============================================================*/
/* Index: idx_flow_defination_def_key                           */
/*==============================================================*/
create unique index idx_flow_defination_def_key on flow_defination
(
   def_key
);

/*==============================================================*/
/* Table: flow_hi_varinst                                       */
/*==============================================================*/
create table flow_hi_varinst
(
   id                   varchar(36) not null comment 'ID',
   type                 varchar(20) not null comment '类型',
   name                 varchar(80) not null comment '名称',
   task_history_id      varchar(36) comment '关联的历史任务ID',
   instance_id          varchar(36) comment '关联的流程实例ID',
   def_version_id       varchar(36) comment '关联的流程定义版本ID',
   defination_id        varchar(36) comment '关联的流程定义ID',
   act_task_id          varchar(36) comment '关联的流程引擎任务ID',
   act_instance_id      varchar(36) comment '关联的流程引擎流程实例ID',
   act_defination_id    varchar(36) comment '关联的流程引擎流程定义ID',
   depict               varchar(255),
   created_by           varchar(100),
   created_date         datetime,
   last_modified_by     varchar(100),
   last_modified_date   datetime,
   v_double             double comment '值-double',
   v_long               bigint comment '值-整形',
   v_text               varchar(4000) comment '值-text',
   primary key (id)
);

alter table flow_hi_varinst comment '历史参数表,记录任务执行中传递的参数，主要用于记录流程任务流转过程中传递给引擎的业务数据参数';

/*==============================================================*/
/* Table: flow_history                                          */
/*==============================================================*/
create table flow_history
(
   id                   varchar(36) not null,
   flow_name            varchar(80) not null,
   flow_task_name       varchar(80) not null,
   flow_run_id          varchar(36) not null,
   flow_def_id          varchar(36) not null,
   owner_account        varchar(100) comment '所属人',
   owner_name           varchar(100) comment '所属人名称',
   executor_name        varchar(80),
   executor_account     varchar(100),
   candidate_account    varchar(100),
   depict               varchar(255),
   created_by           varchar(100),
   created_date         datetime,
   last_modified_by     varchar(100),
   last_modified_date   datetime,
   flow_instance_id     varchar(36),
   act_history_id       varchar(36) comment '引擎流程历史ID',
   act_claim_time       datetime comment '签收时间',
   act_type             varchar(60) comment '实际任务类型',
   act_work_time_in_millis bigint comment '执行的工作时间间隔',
   act_duration_in_millis bigint comment '实际执行的时间间隔',
   act_end_time         datetime comment '结束时间',
   act_start_time       datetime comment '实际开始时间',
   act_task_def_key     varchar(255) comment '实际任务定义KEY',
   pre_id               varchar(36) comment '上一个任务ID',
   next_id              varchar(36) comment '下一个任务ID',
   task_status          varchar(80) comment '任务状态',
   primary key (id)
);

alter table flow_history comment '流程历史';

/*==============================================================*/
/* Table: flow_instance                                         */
/*==============================================================*/
create table flow_instance
(
   id                   varchar(36) not null,
   flow_name            varchar(80) not null,
   business_id          varchar(36) not null,
   start_date           timestamp,
   end_date             timestamp,
   depict               varchar(255),
   created_by           varchar(100),
   created_date         timestamp,
   last_modified_by     varchar(100),
   last_modified_date   timestamp,
   flow_def_version_id  varchar(36),
   act_instance_id      varchar(36) comment '引擎流程实例ID',
   suspended            boolean comment '是否挂起',
   ended                boolean comment '是否已经结束',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table flow_instance comment '流程实例';

/*==============================================================*/
/* Table: flow_service_url                                      */
/*==============================================================*/
create table flow_service_url
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(60) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   url                  text comment 'URL地址',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table flow_service_url comment '服务地址管理';

/*==============================================================*/
/* Index: idx_flow_service_url_code                             */
/*==============================================================*/
create unique index idx_flow_service_url_code on flow_service_url
(
   code
);

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
   executor_account     varchar(100),
   candidate_account    varchar(100),
   created_date         datetime,
   execute_date         datetime,
   depict               varchar(255),
   created_by           varchar(100),
   last_modified_by     varchar(100),
   last_modified_date   datetime,
   flow_instance_id     varchar(36),
   act_task_id          varchar(36) comment '引擎流程任务ID',
   priority             int comment '优先级',
   owner_account        varchar(100) comment '所属人',
   owner_name           varchar(100) comment '所属人名称',
   act_type             varchar(60) comment '实际任务类型',
   act_claim_time       datetime comment '签收时间',
   act_due_date         datetime comment '实际触发时间',
   act_task_key         varchar(255) comment '实际任务定义KEY',
   pre_id               varchar(36),
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table flow_task comment '流程任务';

/*==============================================================*/
/* Table: flow_type                                             */
/*==============================================================*/
create table flow_type
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(255) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   business_model_id    varchar(36) comment '关联业务实体模型',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table flow_type comment '流程类型';

/*==============================================================*/
/* Index: idx_flow_type_code                                    */
/*==============================================================*/
create unique index idx_flow_type_code on flow_type
(
   code
);

/*==============================================================*/
/* Table: flow_variable                                         */
/*==============================================================*/
create table flow_variable
(
   id                   varchar(36) not null comment 'ID',
   type                 varchar(20) not null comment '类型',
   name                 varchar(80) not null comment '名称',
   task_id              varchar(36) comment '关联的任务ID',
   instance_id          varchar(36) comment '关联的流程实例ID',
   def_version_id       varchar(36) comment '关联的流程定义版本ID',
   defination_id        varchar(36) comment '关联的流程定义ID',
   act_task_id          varchar(36) comment '关联的流程引擎任务ID',
   act_instance_id      varchar(36) comment '关联的流程引擎流程实例ID',
   act_defination_id    varchar(36) comment '关联的流程引擎流程定义ID',
   depict               varchar(255),
   created_by           varchar(100),
   created_date         datetime,
   last_modified_by     varchar(100),
   last_modified_date   datetime,
   version              int comment '版本-乐观锁',
   v_double             double comment '值-double',
   v_long               bigint comment '值-整形',
   v_text               varchar(4000) comment '值-text',
   primary key (id)
);

alter table flow_variable comment '运行参数表,记录任务执行中传递的参数，主要用于任务撤回时工作流引擎的数据还原';

/*==============================================================*/
/* Table: work_page_url                                         */
/*==============================================================*/
create table work_page_url
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   url                  text not null comment 'URL地址',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   app_module_id        varchar(36),
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table work_page_url comment '服务地址管理';

alter table business_model add constraint fk_business_app_module_id foreign key (app_module_id)
      references app_module (id) on delete restrict on update restrict;

alter table flow_def_version add constraint fk_def_version_defination_id foreign key (flow_defination_id)
      references flow_defination (id) on delete restrict on update restrict;

alter table flow_defination add constraint fk_flow_defination_type_id foreign key (flow_type_id)
      references flow_type (id) on delete restrict on update restrict;

alter table flow_hi_varinst add constraint FK_fk_flow_hi_varinst_history foreign key (task_history_id)
      references flow_history (id) on delete restrict on update restrict;

alter table flow_hi_varinst add constraint FK_fk_flow_hi_varinst_instance foreign key (instance_id)
      references flow_instance (id) on delete cascade on update restrict;

alter table flow_history add constraint fk_flow_history_instance_id foreign key (flow_instance_id)
      references flow_instance (id) on delete restrict on update restrict;

alter table flow_instance add constraint fk_instance_def_version_id foreign key (flow_def_version_id)
      references flow_def_version (id) on delete restrict on update restrict;

alter table flow_task add constraint fk_flow_task_instance_id foreign key (flow_instance_id)
      references flow_instance (id) on delete restrict on update restrict;

alter table flow_type add constraint fk_flow_type_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_variable add constraint fk_flow_variable_instance foreign key (instance_id)
      references flow_instance (id) on delete cascade on update restrict;

alter table flow_variable add constraint FK_fk_flow_variable_task foreign key (task_id)
      references flow_task (id) on delete cascade on update restrict;

