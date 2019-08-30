/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/8/28 9:57:44                            */
/*==============================================================*/


/*==============================================================*/
/* Table: task_make_over_power                                  */
/*==============================================================*/
create table task_make_over_power
(
   id                   varchar(36) not null comment '主键',
   user_id              varchar(36) not null comment '授权人id',
   user_account         varchar(50) not null comment '授权人账户',
   user_name            varchar(50) not null comment '授权人名称',
   power_user_id        varchar(36) not null comment '被授权人id',
   power_user_account   varchar(50) not null comment '被授权人账户',
   power_user_name      varchar(50) not null comment '被授权人名称',
   power_start_date     date not null comment '授权开始日期',
   power_end_date       date not null comment '授权结束日期',
   open_status          boolean not null default 0 comment '启用状态',
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

alter table task_make_over_power comment '待办转授权记录表';

