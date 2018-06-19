/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/11/14 19:23:45                          */
/*==============================================================*/


alter table flow_def_version
   add start_check_service_url varchar(2000) comment '启动时调用检查服务';

alter table flow_def_version
   add end_call_service_url varchar(2000) comment '流程结束调用服务地址';

