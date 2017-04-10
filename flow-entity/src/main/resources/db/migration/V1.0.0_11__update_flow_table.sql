/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/7 12:13:53                            */
/*==============================================================*/


alter table flow_history
   add actTaskKey varchar(255) comment '实际任务定义KEY';

alter table flow_task
   add actTaskKey varchar(255) comment '实际任务定义KEY';

alter table flow_task
   change column claimTime actClaimTime datetime;

