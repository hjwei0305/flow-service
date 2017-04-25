/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/24 17:24:46                           */
/*==============================================================*/


alter table app_module
   add version int comment '版本-乐观锁';

alter table business_model
   add version int comment '版本-乐观锁';

alter table business_model
   add conditon_bean varchar(255) comment '转换对象';

alter table flow_def_version
   add version int comment '版本-乐观锁';

alter table flow_defination
   add version int comment '版本-乐观锁';

/*==============================================================*/
/* Table: flow_hi_varinst                                       */
/*==============================================================*/
create table flow_hi_varinst
(
   id                   varchar(36) not null comment 'ID',
   type                 varchar(20) not null comment '类型',
   name                 varchar(80) not null comment '名称',
   task_id              varchar(36) comment '关联的历史任务ID',
   instance_id          varchar(36) comment '关联的流程实例ID',
   def_version_id       varchar(36) comment '关联的流程定义版本ID',
   defination_id        varchar(36) comment '关联的流程定义ID',
   act_task_id          varchar(36) comment '关联的流程引擎任务ID',
   act_instance_id      varchar(36) comment '关联的流程引擎流程实例ID',
   act_def_version_id   varchar(36) comment '关联的流程引擎流程定义版本ID',
   act_defination_id    varchar(36) comment '关联的流程引擎流程定义ID',
   depict               varchar(255),
   created_by           varchar(100),
   created_date         timestamp,
   last_modified_by     varchar(100),
   last_modified_date   timestamp,
   v_double             double comment '值-double',
   v_long               bigint comment '值-整形',
   v_text               varchar(4000) comment '值-text',
   primary key (id)
);

alter table flow_hi_varinst comment '历史参数表,记录任务执行中传递的参数，主要用于记录流程任务流转过程中传递给引擎的业务数据参数';

alter table flow_instance
   add version int comment '版本-乐观锁';

alter table flow_service_url
   add version int comment '版本-乐观锁';

alter table flow_task
   add version int comment '版本-乐观锁';

alter table flow_type
   add version int comment '版本-乐观锁';

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
   act_def_version_id   varchar(36) comment '关联的流程引擎流程定义版本ID',
   act_defination_id    varchar(36) comment '关联的流程引擎流程定义ID',
   depict               varchar(255),
   created_by           varchar(100),
   created_date         timestamp,
   last_modified_by     varchar(100),
   last_modified_date   timestamp,
   version              int comment '版本-乐观锁',
   v_double             double comment '值-double',
   v_long               bigint comment '值-整形',
   v_text               varchar(4000) comment '值-text',
   primary key (id)
);

alter table flow_variable comment '运行参数表,记录任务执行中传递的参数，主要用于任务撤回时工作流引擎的数据还原';

alter table work_page_url
   add version int comment '版本-乐观锁';

alter table flow_hi_varinst add constraint FK_fk_flow_hi_varinst_history foreign key (task_id)
      references flow_history (id) on delete restrict on update restrict;

alter table flow_hi_varinst add constraint FK_fk_flow_hi_varinst_instance foreign key (id)
      references flow_instance (id) on delete cascade on update restrict;

alter table flow_variable add constraint fk_flow_variable_instance foreign key (instance_id)
      references flow_instance (id) on delete cascade on update restrict;

alter table flow_variable add constraint FK_fk_flow_variable_task foreign key (task_id)
      references flow_task (id) on delete cascade on update restrict;

