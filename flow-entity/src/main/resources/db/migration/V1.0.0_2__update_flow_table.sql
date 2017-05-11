/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/11 13:17:16                           */
/*==============================================================*/


alter table flow_service_url
   add business_model_id varchar(36) comment '关联业务实体模型';

alter table flow_service_url add constraint FK_fk_flow_serviceUrl_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

