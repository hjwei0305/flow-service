/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/8/15 14:24:59                           */
/*==============================================================*/


alter table flow_def_version
   add sub_process boolean comment '是否可以作为子流程引用';

alter table flow_defination
   add sub_process boolean comment '是否可以作为子流程引用';

