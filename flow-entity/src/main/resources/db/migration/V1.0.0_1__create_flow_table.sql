/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/3/23 22:14:19                           */
/*==============================================================*/


drop table if exists flow_appModule;

drop table if exists flow_bpmType;

drop table if exists flow_businessModel;

/*==============================================================*/
/* Table: flow_appModule                                        */
/*==============================================================*/
create table flow_appModule
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(60) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   createdBy            varchar(100) comment '创建人',
   createdDate          timestamp comment '创建时间',
   lastModifiedBy       varchar(100) comment '最后更新者',
   lastModifiedDate     timestamp comment '最后更新时间',
   primary key (id),
   unique key AK_Key_2 (code)
);

alter table flow_appModule comment '应用模块';

/*==============================================================*/
/* Table: flow_bpmType                                          */
/*==============================================================*/
create table flow_bpmType
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(255) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   createdBy            varchar(100) comment '创建人',
   createdDate          timestamp comment '创建时间',
   lastModifiedBy       varchar(100) comment '最后更新者',
   lastModifiedDate     timestamp comment '最后更新时间',
   businessModel_id     varchar(36) comment '关联业务实体模型',
   primary key (id),
   unique key AK_Key_2 (code)
);

alter table flow_bpmType comment '流程类型';

/*==============================================================*/
/* Table: flow_businessModel                                    */
/*==============================================================*/
create table flow_businessModel
(
   id                   varchar(36) not null comment 'ID',
   className            varchar(255) comment '类全路径',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   createdBy            varchar(100) comment '创建人',
   createdDate          timestamp comment '创建时间',
   lastModifiedBy       varchar(100) comment '最后更新者',
   lastModifiedDate     timestamp comment '最后更新时间',
   appModule_id         varchar(36) comment '关联应用模块',
   primary key (id),
   unique key AK_Key_2 (className)
);

alter table flow_businessModel comment '业务实体模型';

alter table flow_bpmType add constraint FK_Reference_2 foreign key (businessModel_id)
      references flow_businessModel (id) on delete restrict on update restrict;

alter table flow_businessModel add constraint FK_Reference_1 foreign key (appModule_id)
      references flow_appModule (id) on delete restrict on update restrict;

