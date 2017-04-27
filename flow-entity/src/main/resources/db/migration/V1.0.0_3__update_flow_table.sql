/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/25 11:16:36                           */
/*==============================================================*/


alter table flow_hi_varinst
   drop column act_def_version_id;

alter table flow_variable
   drop column act_def_version_id;


