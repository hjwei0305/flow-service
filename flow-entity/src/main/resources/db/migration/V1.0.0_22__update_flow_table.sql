/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/1/8 9:47:32                             */
/*==============================================================*/


alter table flow_def_version
   add solidify_flow boolean comment '是否为固化流程';

alter table flow_defination
   add solidify_flow boolean comment '是否为固化流程';

