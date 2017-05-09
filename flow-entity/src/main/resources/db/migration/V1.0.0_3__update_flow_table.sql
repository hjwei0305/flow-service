/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/4 19:59:01                            */
/*==============================================================*/


drop table if exists tmp_business_model_page_url;

rename table business_model_page_url to tmp_business_model_page_url;

/*==============================================================*/
/* Table: business_model_page_url                               */
/*==============================================================*/
create table business_model_page_url
(
   id                   varchar(36) not null comment 'ID',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   work_page_url_id     varchar(36) comment '关联工作页面',
   business_model_id    varchar(36) comment '关联业务实体模型',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table business_model_page_url comment '业务实体工作界面配置';

insert into business_model_page_url (id, created_by, created_date, last_modified_by, last_modified_date, business_model_id, version)
select id, created_by, created_date, last_modified_by, last_modified_date, business_model_id, version
from tmp_business_model_page_url;

