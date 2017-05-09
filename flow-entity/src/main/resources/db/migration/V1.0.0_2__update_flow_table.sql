/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/4 11:28:49                            */
/*==============================================================*/


/*==============================================================*/
/* Table: business_model_page_url                               */
/*==============================================================*/
create table business_model_page_url
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   url                  text not null comment 'URL地址',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   business_model_id    varchar(36) comment '关联业务实体模型',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table business_model_page_url comment '业务实体工作界面配置';

