/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/10/25 10:55:06                          */
/*==============================================================*/


alter table flow_task
   add can_batch_approval boolean comment '能否批量审批';

alter table work_page_url
   add must_commit boolean comment '是否必须提交';

