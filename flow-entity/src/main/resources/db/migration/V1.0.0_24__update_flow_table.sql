/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/1/15 16:11:02                           */
/*==============================================================*/


alter table flow_solidify_executor
   modify column executor_ids varchar(6000) not null;

