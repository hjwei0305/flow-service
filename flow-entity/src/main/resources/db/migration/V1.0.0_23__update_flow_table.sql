/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/1/16 15:27:57                           */
/*==============================================================*/


/*==============================================================*/
/* Table: flow_solidify_executor                                */
/*==============================================================*/
create table flow_solidify_executor
(
   id                   varchar(36) not null comment '主键',
   business_code        varchar(255) not null comment '业务类全路径',
   business_id          varchar(36) not null comment '业务类主键',
   act_task_def_key     varchar(50) not null comment '任务定义KEY',
   node_type            varchar(50) not null comment '任务类型',
   instancy_status      boolean not null comment '是否紧急',
   executor_ids         varchar(6000) not null comment '执行人ids',
   true_executor_ids    varchar(36) comment '单签实际执行人ids',
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

