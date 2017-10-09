/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/9/27 16:22:42                           */
/*==============================================================*/


drop table if exists tmp_business_model;

rename table business_model to tmp_business_model;

/*==============================================================*/
/* Table: business_model                                        */
/*==============================================================*/
create table business_model
(
   id                   varchar(36) not null comment 'ID',
   version              int comment '版本-乐观锁',
   name                 varchar(80) not null comment '名称',
   class_name           varchar(255) not null comment '类全路径',
   conditon_properties  varchar(255) comment '条件属性说明服务地址',
   conditon_p_value     varchar(255) comment '条件属性值服务地址',
   conditon_status_rest varchar(255) comment '流程状态重置服务地址',
   conditon_p_s_value   varchar(255) comment '条件属性初始值服务地址',
   depict               varchar(255) comment '描述',
   app_module_id        varchar(36) comment '关联应用模块ID',
   app_module_code      varchar(20) comment '关联应用模块代码',
   app_module_name      varchar(80) comment '关联的应用模块Name',
   look_url             varchar(6000) comment '查看表单URL',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table business_model comment '业务实体模型';

insert into business_model (id, version, name, class_name, depict, app_module_id, app_module_code, app_module_name, look_url, creator_id, creator_account, creator_name, created_date, last_editor_id, last_editor_account, last_editor_name, last_edited_date)
select id, version, name, class_name, depict, app_module_id, app_module_code, app_module_name, look_url, creator_id, creator_account, creator_name, created_date, last_editor_id, last_editor_account, last_editor_name, last_edited_date
from tmp_business_model;

/*==============================================================*/
/* Index: idx_business_model_class_name                         */
/*==============================================================*/
create unique index idx_business_model_class_name on business_model
(
   class_name
);

alter table business_model_page_url add constraint FK_fk_businessWorkUrl_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table business_model_selfDefEmployee add constraint FK_fk_selfDefEmployee_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_executor_config add constraint FK_fk_flow_executorConfig_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_service_url add constraint FK_fk_flow_serviceUrl_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_type add constraint fk_flow_type_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

