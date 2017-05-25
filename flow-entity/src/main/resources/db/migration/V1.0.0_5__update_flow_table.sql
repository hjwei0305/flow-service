/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/24 17:37:17                           */
/*==============================================================*/


/*==============================================================*/
/* Table: business_model_selfDefEmployee                        */
/*==============================================================*/
create table business_model_selfDefEmployee
(
   id                   varchar(36) not null comment 'ID',
   created_by           varchar(100) comment '创建人',
   created_date         datetime comment '创建时间',
   last_modified_by     varchar(100) comment '最后更新者',
   last_modified_date   datetime comment '最后更新时间',
   employee_id          varchar(36) not null comment '企业员工ID',
   employee_name        varchar(80) comment '用户名称',
   business_model_id    varchar(36) not null comment '关联业务实体模型',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table business_model_selfDefEmployee comment '业务实体自定义执行人配置';

/*==============================================================*/
/* Index: idx_selfDefEmployee_employeeId                        */
/*==============================================================*/
create unique index idx_selfDefEmployee_employeeId on business_model_selfDefEmployee
(
   employee_id
);

alter table business_model_selfDefEmployee add constraint FK_fk_selfDefEmployee_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

