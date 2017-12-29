/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/12/4 16:55:10                           */
/*==============================================================*/


alter table flow_task
   add trust_state int comment '委托状态';

alter table flow_task
   add trust_owner_taskId varchar(36) comment '被委托的任务id';

