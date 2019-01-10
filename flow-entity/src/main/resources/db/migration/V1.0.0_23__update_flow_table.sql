/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/1/10 14:49:26                           */
/*==============================================================*/


/*==============================================================*/
/* Table: flow_solidify_executor                                */
/*==============================================================*/
create table flow_solidify_executor
(
   id                   varchar(36) not null comment '主键',
   business_code        varchar(255) not null comment '业务类全路径',
   business_id          varchar(36) not null comment '业务类主键',
   flow_instance_id     varchar(36) comment '关联流程实例',
   act_task_def_key     varchar(50) not null comment '任务定义KEY',
   instancy_status      boolean not null comment '是否紧急',
   executor_ids         varchar(1000) not null comment '执行人ids',
   before_task_def_key  varchar(50) comment '上一节点任务key',
   task_order           int comment '逻辑任务执行顺序',
   tenant_code          varchar(10) comment '租户代码',
   creator_id           varchar(36) comment '创建人id',
   creator_account      varchar(50) comment '创建人账号',
   creator_name         varchar(50) comment '创建人名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后修改人id',
   last_editor_account  varchar(50) comment '最后修改人账号',
   last_editor_name     varchar(50) comment '最后修改人名称',
   last_edited_date     datetime comment '最后修改时间',
   primary key (id)
);

alter table flow_solidify_executor comment '流程固化执行人表';

alter table flow_solidify_executor add constraint FK_fk_flow_executor_instance_id foreign key (flow_instance_id)
      references flow_instance (id) on delete restrict on update restrict;

