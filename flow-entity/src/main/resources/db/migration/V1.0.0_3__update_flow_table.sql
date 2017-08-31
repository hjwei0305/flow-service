/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/8/31 13:27:55                           */
/*==============================================================*/


alter table flow_instance
   add parent_id varchar(36) comment '父流程实例';

alter table flow_instance
   add call_activity_path varchar(5000) comment '实例调用路径';

alter table flow_instance
   modify column businessModelRemark varchar(1000);

alter table flow_instance add constraint FK_fk_flow_instance_parent_id foreign key (parent_id)
      references flow_instance (id) on delete restrict on update restrict;

