/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/11 9:59:25                            */
/*==============================================================*/


alter table flow_history
   add preId varchar(36) comment '上一个任务ID';

alter table flow_history
   add nextId varchar(36) comment '下一个任务ID';

alter table flow_history
   add taskStatus varchar(80) comment '任务状态';

alter table flow_history
   change column actTaskKey actTaskDefKey varchar(255);

alter table flow_task
   change column taskDefKey actTaskDefKey varchar(255) not null;

