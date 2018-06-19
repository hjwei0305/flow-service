 alter table flow_def_version
   add start_check_service_name varchar(255) comment '启动时调用检查服务地址名称';
   alter table flow_def_version
   add end_call_service_name varchar(255) comment '流程结束调用服务地址名称',