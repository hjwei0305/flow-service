/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/11 10:27:55                           */
/*==============================================================*/


alter table flow_history
   drop column nextId;

alter table flow_task
   add preId varchar(36) comment '上一个任务ID';

