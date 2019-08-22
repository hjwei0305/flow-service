/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/8/22 16:20:11                           */
/*==============================================================*/


alter table flow_history
   add old_task_id varchar(36)  NULL DEFAULT NULL comment '关联的taskid';

