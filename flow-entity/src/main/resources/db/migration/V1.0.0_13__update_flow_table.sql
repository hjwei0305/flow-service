/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/11/28 18:42:28                          */
/*==============================================================*/


alter table flow_def_version
   add start_after_service_id varchar(36) comment '启动完成时调用服务id';

alter table flow_def_version
   add start_after_service_name varchar(255) comment '启动完成时调用服务名称';

alter table flow_def_version
   add start_after_service_aync boolean comment '启动完成时调用服务是否异步';

