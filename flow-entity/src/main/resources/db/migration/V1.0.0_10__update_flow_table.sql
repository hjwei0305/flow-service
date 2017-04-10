/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/6 21:32:43                            */
/*==============================================================*/


alter table flow_history
   add actWorkTimeInMillis bigint comment '执行的工作时间间隔';

alter table flow_history
   add actDurationInMillis bigint comment '实际执行的时间间隔';

alter table flow_history
   add actEndTime datetime comment '结束时间';

alter table flow_history
   add actStartTime datetime comment '实际开始时间';

alter table flow_task
   add actDueDate datetime comment '实际触发时间';

