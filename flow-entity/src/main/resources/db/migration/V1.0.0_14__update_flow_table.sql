/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/11/28 20:25:37                          */
/*==============================================================*/


alter table flow_def_version
   add end_before_call_service_url varchar(36) comment '流程结束前检查服务id';

alter table flow_def_version
   add end_before_call_service_name varchar(255) comment '流程结束前检查服务名称';

