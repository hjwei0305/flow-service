/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/11/20 16:36:11                          */
/*==============================================================*/


alter table flow_task
   add work_page_url_id varchar(36) comment '工作界面';

alter table flow_task add constraint FK_fk_flow_task_workPageUrl_id foreign key (work_page_url_id)
      references work_page_url (id) on delete restrict on update restrict;

