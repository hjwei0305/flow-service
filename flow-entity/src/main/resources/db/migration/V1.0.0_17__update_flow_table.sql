/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/12/15 12:33:38                          */
/*==============================================================*/


alter table flow_def_version
   modify column start_uel varchar(6000);

alter table flow_defination
   modify column start_uel varchar(6000);

