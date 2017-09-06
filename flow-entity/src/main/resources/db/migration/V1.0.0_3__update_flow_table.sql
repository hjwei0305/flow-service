/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/9/6 10:08:48                            */
/*==============================================================*/


/*==============================================================*/
/* Table: app_module                                            */
/*==============================================================*/
create table app_module
(
   id                   varchar(36) not null comment 'Id',
   code                 varchar(20) not null comment '代码',
   name                 varchar(30) not null comment '名称',
   remark               varchar(255) default NULL comment '备注',
   web_base_address     varchar(255) default NULL comment 'web基地址',
   api_base_address     varchar(255) default NULL comment 'api基地址',
   rank                 int(11) not null comment '排序号',
   creator_id           varchar(36) default NULL comment '创建人Id',
   creator_account      varchar(50) default NULL comment '创建人账号',
   creator_name         varchar(50) default NULL comment '创建人姓名',
   created_date         datetime default NULL comment '创建时间',
   last_editor_id       varchar(36) default NULL comment '最后修改人Id',
   last_editor_account  varchar(50) default NULL comment '最后修改人账号',
   last_editor_name     varchar(50) default NULL comment '最后修改人姓名',
   last_edited_date     datetime default NULL comment '最后修改时间',
   primary key (id),
   unique key uk_app_module_code (code),
   key idx_app_module_rank (rank)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用模块';

alter table app_module comment '应用模块';

alter table flow_instance
   add parent_id varchar(36) comment '父流程实例';

alter table flow_instance
   modify column businessModelRemark varchar(1000);

alter table flow_instance add constraint FK_fk_flow_instance_parent_id foreign key (parent_id)
      references flow_instance (id) on delete restrict on update restrict;

