/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/6/13 20:55:00                           */
/*==============================================================*/


alter table default_business_model
   add tenant_code varchar(10) comment '租户代码';

alter table flow_defination
   add basic_org_code varchar(6) comment '组织机构code';

