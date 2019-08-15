/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/8/15 9:18:21                            */
/*==============================================================*/


alter table flow_def_version
   add start_check_service_aync boolean default 0 comment '启动时调用检查服务是否异步';

alter table flow_def_version
   add end_before_call_service_aync boolean default 0 comment '流程结束前检查服务是否异步';

alter table flow_def_version
   add end_call_service_aync boolean default 1 comment '流程结束调用服务是否异步';

