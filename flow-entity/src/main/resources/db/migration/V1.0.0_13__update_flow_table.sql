/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/7/4 16:42:29                            */
/*==============================================================*/


/*==============================================================*/
/* Table: flow_executor_config                                  */
/*==============================================================*/
create table flow_executor_config
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(60) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   url                  text not null comment 'API地址',
   param                text comment '参数',
   depict               varchar(255) comment '描述',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   version              int comment '版本-乐观锁',
   business_model_id    varchar(36) comment '关联业务实体模型',
   primary key (id)
);

alter table flow_executor_config comment '自定义执行人配置';

/*==============================================================*/
/* Index: idx_flow_service_url_code                             */
/*==============================================================*/
create unique index idx_flow_service_url_code on flow_executor_config
(
   code
);

alter table flow_executor_config add constraint FK_fk_flow_executorConfig_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

