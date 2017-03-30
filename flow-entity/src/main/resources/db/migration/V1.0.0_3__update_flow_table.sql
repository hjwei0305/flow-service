/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/3/30 19:11:42                           */
/*==============================================================*/


drop table if exists tmp_flow_defination;

rename table flow_defination to tmp_flow_defination;

/*==============================================================*/
/* Table: flow_defination                                       */
/*==============================================================*/
create table flow_defination
(
   id                   varchar(36) not null comment 'ID',
   defKey               varchar(255) not null comment '定义Key',
   name                 varchar(80) not null comment '名称',
   lastVersionId        varchar(36) comment '最新版本ID',
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


insert into flow_defination (id, defKey, name, startUel, depict, createdBy, lastModifiedBy, flowType_id)
select id, defKey, name, startUel, depict, createdBy, lastModifiedBy, flowType_id
from tmp_flow_defination;


/*
* 自定义drop语句, 清除外键关联
*/
ALTER TABLE tmp_flow_defination DROP FOREIGN KEY FK_defination_bpmType;
ALTER TABLE flow_defVersion  DROP FOREIGN KEY  FK_defVersion_defination;

DROP TABLE IF EXISTS tmp_flow_defination;


/*==============================================================*/
/* Index: Index_code                                            */
/*==============================================================*/
create unique index Index_code on flow_defination
(
   defKey
);

alter table flow_defVersion add constraint FK_defVersion_defination foreign key (flowDefination_id)
      references flow_defination (id) on delete restrict on update restrict;

alter table flow_defination add constraint FK_defination_bpmType foreign key (flowType_id)
      references flow_bpmType (id) on delete restrict on update restrict;

