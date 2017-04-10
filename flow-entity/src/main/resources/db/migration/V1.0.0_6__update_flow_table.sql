/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/5 16:53:58                            */
/*==============================================================*/


alter table flow_instance
   add suspended boolean comment '是否挂起';

alter table flow_instance
   add ended boolean comment '是否已经结束';


