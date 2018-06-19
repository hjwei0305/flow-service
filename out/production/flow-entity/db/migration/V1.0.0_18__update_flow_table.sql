/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/12/28 10:04:34                          */
/*==============================================================*/


alter table business_model
   add business_detail_service_url varchar(255) comment '业务单据明细服务地址';

alter table flow_type
   add business_detail_service_url varchar(255) comment '业务单据明细服务地址';

