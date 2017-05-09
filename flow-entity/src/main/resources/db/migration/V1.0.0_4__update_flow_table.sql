/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/5/4 20:50:28                            */
/*==============================================================*/


alter table business_model_page_url add constraint FK_fk_businessWorkUrl_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table business_model_page_url add constraint FK_fk_businessWorkUrl_workPageUrl_id foreign key (work_page_url_id)
      references work_page_url (id) on delete restrict on update restrict;

alter table work_page_url add constraint FK_fk_workPageUrl_appModule_id foreign key (app_module_id)
      references app_module (id) on delete restrict on update restrict;

