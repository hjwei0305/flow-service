/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/3/24 10:35:09                           */
/*==============================================================*/


drop table if exists flow_appModule;

drop table if exists flow_bpmType;

drop table if exists flow_businessModel;

drop table if exists flow_defVersion;

drop table if exists flow_defination;

drop table if exists flow_serviceUrl;

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

/*==============================================================*/
/* Table: flow_defVersion                                       */
/*==============================================================*/
create table flow_defVersion
(
   id                   varchar(36) not null comment 'ID',
   defKey               varchar(255) not null comment '定义KEY',
   name                 varchar(80) not null comment '名称',
   actDeployId          varchar(36) comment '部署ID',
   startUel             varchar(255) comment '启动条件UEL',
   versionCode          int comment '版本号',
   priority             int comment '优先级',
   defJson              text comment '流程JSON文本',
   defBpmn              text comment '流程BPMN文本',
   defXML               text comment '最终定义XML',
   depict               varchar(255) comment '描述',
   createdBy            varchar(100) comment '创建人',
   createdDate          timestamp comment '创建时间',
   lastModifiedBy       varchar(100) comment '最后更新者',
   lastModifiedDate     timestamp comment '最后更新时间',
   flowDefination_id    varchar(36) comment '关联流程类型',
   primary key (id),
   unique key AK_Key_2 (defKey)
);

alter table flow_defVersion comment '流程定义版本';

/*==============================================================*/
/* Table: flow_defination                                       */
/*==============================================================*/
create table flow_defination
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(255) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   lastVersionId        int comment '最新版本ID',
   startUel             varchar(255) comment '启动条件UEL',
   depict               varchar(255) comment '描述',
   createdBy            varchar(100) comment '创建人',
   createdDate          timestamp comment '创建时间',
   lastModifiedBy       varchar(100) comment '最后更新者',
   lastModifiedDate     timestamp comment '最后更新时间',
   flowType_id          varchar(36) comment '关联流程类型',
   primary key (id),
   unique key AK_Key_2 (code)
);

alter table flow_defination comment '流程定义';

/*==============================================================*/
/* Table: flow_serviceUrl                                       */
/*==============================================================*/
create table flow_serviceUrl
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(60) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   url                  text comment 'URL地址',
   depict               varchar(255) comment '描述',
   createdBy            varchar(100) comment '创建人',
   createdDate          timestamp comment '创建时间',
   lastModifiedBy       varchar(100) comment '最后更新者',
   lastModifiedDate     timestamp comment '最后更新时间',
   primary key (id),
   unique key AK_Key_2 (code)
);

alter table flow_serviceUrl comment '服务地址管理';

alter table flow_bpmType add constraint FK_Reference_2 foreign key (businessModel_id)
      references flow_businessModel (id) on delete restrict on update restrict;

alter table flow_businessModel add constraint FK_Reference_1 foreign key (appModule_id)
      references flow_appModule (id) on delete restrict on update restrict;

alter table flow_defVersion add constraint FK_Reference_4 foreign key (flowDefination_id)
      references flow_defination (id) on delete restrict on update restrict;

alter table flow_defination add constraint FK_Reference_3 foreign key (flowType_id)
      references flow_bpmType (id) on delete restrict on update restrict;

