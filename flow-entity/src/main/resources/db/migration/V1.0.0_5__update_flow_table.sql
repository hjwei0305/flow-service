/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/8 15:09:57                            */
/*==============================================================*/


drop table if exists app_module;

drop table if exists tmp_business_model;

rename table business_model to tmp_business_model;

drop table if exists tmp_work_page_url;

rename table work_page_url to tmp_work_page_url;

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
   version              int comment '版本-乐观锁',
   conditon_bean        varchar(255) comment '转换对象',
   app_module_id        varchar(36),
   primary key (id)
);

alter table business_model comment '业务实体模型';

insert into business_model (id, class_name, name, depict, created_by, created_date, last_modified_by, last_modified_date, version, conditon_bean, app_module_id)
select id, class_name, name, depict, created_by, created_date, last_modified_by, last_modified_date, version, conditon_bean, app_module_id
from tmp_business_model;

drop table tmp_business_model;

/*==============================================================*/
/* Index: idx_business_model_class_name                         */
/*==============================================================*/
create unique index idx_business_model_class_name on business_model
(
   class_name
);

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
   version              int comment '版本-乐观锁',
   app_module_id        varchar(36),
   primary key (id)
);

alter table work_page_url comment '服务地址管理';

insert into work_page_url (id, name, url, depict, created_by, created_date, last_modified_by, last_modified_date, version, app_module_id)
select id, name, url, depict, created_by, created_date, last_modified_by, last_modified_date, version, app_module_id
from tmp_work_page_url;

drop table tmp_work_page_url;

alter table business_model_page_url add constraint FK_fk_businessWorkUrl_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table business_model_page_url add constraint FK_fk_businessWorkUrl_workPageUrl_id foreign key (work_page_url_id)
      references work_page_url (id) on delete restrict on update restrict;

alter table flow_type add constraint fk_flow_type_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

