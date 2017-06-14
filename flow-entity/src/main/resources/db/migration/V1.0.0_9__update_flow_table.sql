/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/6/12 15:04:14                           */
/*==============================================================*/


alter table flow_history
   add canCancel bit(1) comment '能否驳回';

alter table flow_history
   add taskJsonDef varchar(1200) comment '任务json定义 ';

alter table flow_history
   add businessModelRemark varchar(255) comment '业务摘要';

alter table flow_task
   add canReject bit(1) comment '能否驳回';

alter table flow_task
   add canSuspension bit(1) comment '能否中止';

alter table flow_task
   add taskJsonDef varchar(1200) comment '任务json定义 ';

alter table flow_task
   add businessModelRemark varchar(255) comment '业务摘要';

