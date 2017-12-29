/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/12/7 17:13:53                           */
/*==============================================================*/


alter table flow_type
   add look_url varchar(6000) comment '查看单据的URL';

alter table flow_type
   add complete_task_service_url varchar(255) comment '完成任务时调用的web地址';

