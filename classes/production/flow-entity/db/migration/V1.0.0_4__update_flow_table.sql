/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/15 15:00:36                           */
/*==============================================================*/


/*==============================================================*/
/* Table: default_business_model                                */
/*==============================================================*/
create table default_business_model
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   flowStatus           varchar(10) not null comment '流程状态',
   orgCode              varchar(20) comment '组织机构代码',
   orgId                varchar(36) comment '组织机构Id',
   orgName              varchar(80) comment '组织机构名称',
   orgPath              varchar(500) comment '组织机构层级路径',
   priority             int comment '优先级',
   workCaption          varchar(1000) comment '工作说明',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table default_business_model comment '工作流默认业务实体实现';

