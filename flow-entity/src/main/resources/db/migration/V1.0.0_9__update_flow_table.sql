/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/6 19:06:46                            */
/*==============================================================*/


alter table flow_appModule
   modify column createdDate datetime;

alter table flow_appModule
   modify column lastModifiedDate datetime;

alter table flow_bpmType
   modify column createdDate datetime;

alter table flow_bpmType
   modify column lastModifiedDate datetime;

alter table flow_businessModel
   modify column createdDate datetime;

alter table flow_businessModel
   modify column lastModifiedDate datetime;

alter table flow_defVersion
   modify column actDefId varchar(255) not null;

alter table flow_defVersion
   modify column createdDate datetime;

alter table flow_defVersion
   modify column lastModifiedDate datetime;

alter table flow_defination
   modify column createdDate datetime;

alter table flow_defination
   modify column lastModifiedDate datetime;

alter table flow_history
   add claimTime datetime comment '签收时间';

alter table flow_history
   add actType varchar(60) comment '实际任务类型';

alter table flow_history
   modify column createdDate datetime;

alter table flow_history
   modify column lastModifiedDate datetime;

alter table flow_serviceUrl
   modify column createdDate datetime;

alter table flow_serviceUrl
   modify column lastModifiedDate datetime;

alter table flow_task
   add actType varchar(60) comment '实际任务类型';

alter table flow_task
   add claimTime datetime comment '签收时间';

alter table flow_task
   modify column createdDate datetime;

alter table flow_task
   modify column executeDate datetime;

alter table flow_task
   modify column lastModifiedDate datetime;

