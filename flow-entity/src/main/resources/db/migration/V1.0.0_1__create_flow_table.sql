/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/3/29 15:44:08                           */
/*==============================================================*/



drop table if exists flow_appModule;

drop table if exists flow_bpmType;

drop table if exists flow_businessModel;

drop table if exists flow_defVersion;

drop table if exists flow_defination;

drop table if exists flow_history;

drop table if exists flow_instance;


drop table if exists flow_serviceUrl;

drop table if exists flow_task;

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
   primary key (id)
);

alter table flow_appModule comment '应用模块';

/*==============================================================*/
/* Index: Index_code                                            */
/*==============================================================*/
create unique index Index_code on flow_appModule
(
   code
);

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
   primary key (id)
);

alter table flow_bpmType comment '流程类型';

/*==============================================================*/
/* Index: Index_code                                            */
/*==============================================================*/
create unique index Index_code on flow_bpmType
(
   code
);

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
   primary key (id)
);

alter table flow_businessModel comment '业务实体模型';

/*==============================================================*/
/* Index: Index_className                                       */
/*==============================================================*/
create unique index Index_className on flow_businessModel
(
   className
);

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
   primary key (id)
);

alter table flow_defVersion comment '流程定义版本';

/*==============================================================*/
/* Index: Index_defKey                                          */
/*==============================================================*/
create unique index Index_defKey on flow_defVersion
(
   defKey
);

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
   primary key (id)
);

alter table flow_defination comment '流程定义';

/*==============================================================*/
/* Index: Index_code                                            */
/*==============================================================*/
create unique index Index_code on flow_defination
(
   code
);

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
   taskOwner            varchar(100),
   taskExecutor         varchar(100),
   taskCandidate        varchar(100),
   depict               varchar(255),
   createdBy            varchar(100),
   createdDate          timestamp,
   lastModifiedBy       varchar(100),
   lastModifiedDate     timestamp,
   flowInstance_id      varchar(36),
   primary key (id)
);

alter table flow_history comment '流程历史';

/*==============================================================*/
/* Table: flow_instance                                         */
/*==============================================================*/
create table flow_instance
(
   id                   varchar(36) not null,
   flowName             varchar(80) not null,
   businessId           varchar(36) not null,
   startDate            timestamp,
   endDate              timestamp,
   depict               varchar(255),
   createdBy            varchar(100),
   createdDate          timestamp,
   lastModifiedBy       varchar(100),
   lastModifiedDate     timestamp,
   flowDefVersion_id    varchar(36),
   primary key (id)
);

alter table flow_instance comment '流程实例';

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
   primary key (id)
);

alter table flow_serviceUrl comment '服务地址管理';

/*==============================================================*/
/* Index: Index_1                                               */
/*==============================================================*/
create unique index Index_1 on flow_serviceUrl
(
   code
);

/*==============================================================*/
/* Table: flow_task                                             */
/*==============================================================*/
create table flow_task
(
   id                   varchar(36) not null,
   flowName             varchar(80) not null,
   taskName             varchar(80) not null,
   taskDefKey           varchar(255) not null,
   taskFormUrl          text,
   taskStatus           varchar(80),
   proxyStatus          varchar(80),
   flowInstanceId       varchar(36) not null,
   flowDefinitionId     varchar(36) not null,
   executorName         varchar(80),
   executorAccount      int,
   candidateAccount     int,
   createdDate          timestamp,
   executeDate          timestamp,
   depict               varchar(255),
   createdBy            varchar(100),
   lastModifiedBy       varchar(100),
   lastModifiedDate     timestamp,
   flowInstance_id      varchar(36),
   primary key (id)
);

alter table flow_task comment '流程任务';

alter table flow_bpmType add constraint FK_bpmType_businessModel foreign key (businessModel_id)
      references flow_businessModel (id) on delete restrict on update restrict;

alter table flow_businessModel add constraint FK_businessModel_appModule foreign key (appModule_id)
      references flow_appModule (id) on delete restrict on update restrict;

alter table flow_defVersion add constraint FK_defVersion_defination foreign key (flowDefination_id)
      references flow_defination (id) on delete restrict on update restrict;

alter table flow_defination add constraint FK_defination_bpmType foreign key (flowType_id)
      references flow_bpmType (id) on delete restrict on update restrict;

alter table flow_history add constraint FK_history_instance foreign key (flowInstance_id)
      references flow_instance (id) on delete restrict on update restrict;

alter table flow_instance add constraint FK_FK_instance_defVersion foreign key (flowDefVersion_id)
      references flow_defVersion (id) on delete restrict on update restrict;

alter table flow_task add constraint FK_task_instance foreign key (flowInstance_id)
      references flow_instance (id) on delete restrict on update restrict;

