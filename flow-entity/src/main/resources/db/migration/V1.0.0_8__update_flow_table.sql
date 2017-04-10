/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/6 13:46:23                            */
/*==============================================================*/


alter table flow_task
   add priority int comment '优先级';

alter table flow_task
   add ownerAccount varchar(100) comment '所属人';

alter table flow_task
   add ownerName varchar(100) comment '所属人名称';

