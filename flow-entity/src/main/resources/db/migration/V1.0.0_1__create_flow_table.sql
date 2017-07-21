/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/7/20 18:21:08                           */
/*==============================================================*/


/*==============================================================*/
/* Table: business_model                                        */
/*==============================================================*/
create table business_model
(
   id                   varchar(36) not null comment 'ID',
   version              int comment '版本-乐观锁',
   name                 varchar(80) not null comment '名称',
   class_name           varchar(255) not null comment '类全路径',
   conditon_bean        varchar(255) comment '转换对象',
   dao_bean             varchar(255) comment '数据访问对象名称',
   depict               varchar(255) comment '描述',
   app_module_id        varchar(36) comment '关联应用模块ID',
   app_module_code      varchar(20) comment '关联应用模块代码',
   app_module_name      varchar(80) comment '关联的应用模块Name',
   look_url             varchar(6000) comment '查看表单URL',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table business_model comment '业务实体模型';

/*==============================================================*/
/* Index: idx_business_model_class_name                         */
/*==============================================================*/
create unique index idx_business_model_class_name on business_model
(
   class_name
);

/*==============================================================*/
/* Table: business_model_page_url                               */
/*==============================================================*/
create table business_model_page_url
(
   id                   varchar(36) not null comment 'ID',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   work_page_url_id     varchar(36) comment '关联工作页面',
   business_model_id    varchar(36) comment '关联业务实体模型',
   version              int comment '版本-乐观锁',
   primary key (id)
);

alter table business_model_page_url comment '业务实体工作界面配置';

/*==============================================================*/
/* Table: business_model_selfDefEmployee                        */
/*==============================================================*/
create table business_model_selfDefEmployee
(
   id                   varchar(36) not null comment 'ID',
   version              int comment '版本-乐观锁',
   business_model_id    varchar(36) not null comment '关联业务实体模型',
   employee_id          varchar(36) not null comment '企业员工ID',
   employee_name        varchar(80) comment '用户名称',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table business_model_selfDefEmployee comment '业务实体自定义执行人配置';

/*==============================================================*/
/* Table: default_business_model                                */
/*==============================================================*/
create table default_business_model
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   flowStatus           varchar(10) not null comment '流程状态',
   tenant_code          varchar(10) comment '租户代码',
   orgCode              varchar(20) comment '组织机构代码',
   orgId                varchar(36) comment '组织机构Id',
   orgName              varchar(80) comment '组织机构名称',
   orgPath              varchar(500) comment '组织机构层级路径',
   priority             int comment '优先级',
   workCaption          varchar(1000) comment '工作说明',
   version              int comment '版本-乐观锁',
   business_code        varchar(20) comment '业务编号',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   applyCaption         varchar(4000) comment '申请说明',
   unitPrice            double comment '单价',
   count                int comment '数量',
   sum                  double comment '金额',
   primary key (id)
);

alter table default_business_model comment '工作流默认业务实体实现';

/*==============================================================*/
/* Table: default_business_model2                               */
/*==============================================================*/
create table default_business_model2
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   flowStatus           varchar(10) not null comment '流程状态',
   tenant_code          varchar(10) comment '租户代码',
   orgCode              varchar(20) comment '组织机构代码',
   orgId                varchar(36) comment '组织机构Id',
   orgName              varchar(80) comment '组织机构名称',
   orgPath              varchar(500) comment '组织机构层级路径',
   priority             int comment '优先级',
   workCaption          varchar(1000) comment '工作说明',
   version              int comment '版本-乐观锁',
   business_code        varchar(20) comment '业务编号',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   applyCaption         varchar(4000) comment '申请说明',
   unitPrice            double comment '单价',
   count                int comment '数量',
   sum                  double comment '金额',
   primary key (id)
);

alter table default_business_model2 comment '采购业务测试单据';

/*==============================================================*/
/* Table: default_business_model3                               */
/*==============================================================*/
create table default_business_model3
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   flowStatus           varchar(10) not null comment '流程状态',
   tenant_code          varchar(10) comment '租户代码',
   orgCode              varchar(20) comment '组织机构代码',
   orgId                varchar(36) comment '组织机构Id',
   orgName              varchar(80) comment '组织机构名称',
   orgPath              varchar(500) comment '组织机构层级路径',
   priority             int comment '优先级',
   workCaption          varchar(1000) comment '工作说明',
   version              int comment '版本-乐观锁',
   business_code        varchar(20) comment '业务编号',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   applyCaption         varchar(4000) comment '申请说明',
   unitPrice            double comment '单价',
   count                int comment '数量',
   sum                  double comment '金额',
   primary key (id)
);

alter table default_business_model3 comment '销售业务测试单据';

/*==============================================================*/
/* Table: flow_def_version                                      */
/*==============================================================*/
create table flow_def_version
(
   id                   varchar(36) not null comment 'ID',
   act_def_id           varchar(255) not null comment '定义ID',
   def_key              varchar(255) not null comment '定义KEY',
   name                 varchar(80) not null comment '名称',
   act_deploy_id        varchar(36) comment '部署ID',
   start_uel            varchar(255) comment '启动条件UEL',
   version_code         int comment '版本号',
   priority             int comment '优先级',
   def_json             mediumtext comment '流程JSON文本',
   def_xml              mediumtext comment '最终定义XML',
   depict               varchar(255) comment '描述',
   flow_defination_id   varchar(36) comment '关联流程类型',
   version              int comment '版本-乐观锁',
   flowDefinationStatus smallint comment '状态',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_def_version comment '流程定义版本';

/*==============================================================*/
/* Table: flow_defination                                       */
/*==============================================================*/
create table flow_defination
(
   id                   varchar(36) not null comment 'ID',
   flow_type_id         varchar(36),
   def_key              varchar(255) not null comment '定义Key',
   name                 varchar(80) not null comment '名称',
   last_version_id      varchar(36) comment '最新版本ID',
   last_deloy_version_id varchar(36) comment '最新已发布版本ID',
   start_uel            varchar(255) comment '启动条件UEL',
   depict               varchar(255) comment '描述',
   version              int comment '版本-乐观锁',
   flowDefinationStatus smallint comment '状态',
   priority             int comment '优先级',
   basic_org_id         varchar(36) comment '组织机构id',
   basic_org_code       varchar(6) comment '组织机构code',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_defination comment '流程定义';

/*==============================================================*/
/* Index: idx_flow_defination_def_key                           */
/*==============================================================*/
create unique index idx_flow_defination_def_key on flow_defination
(
   def_key
);

/*==============================================================*/
/* Table: flow_executor_config                                  */
/*==============================================================*/
create table flow_executor_config
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(60) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   url                  text not null comment 'API地址',
   param                text comment '参数',
   depict               varchar(255) comment '描述',
   creator_id           varchar(36) comment '创建者ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建人',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者ID',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者',
   last_edited_date     datetime comment '最后更新时间',
   version              int comment '版本-乐观锁',
   business_model_id    varchar(36) comment '关联业务实体模型',
   primary key (id)
);

alter table flow_executor_config comment '自定义执行人配置';

/*==============================================================*/
/* Index: idx_flow_executor_config_code                         */
/*==============================================================*/
create unique index idx_flow_executor_config_code on flow_executor_config
(
   code
);

/*==============================================================*/
/* Table: flow_hi_varinst                                       */
/*==============================================================*/
create table flow_hi_varinst
(
   id                   varchar(36) not null comment 'ID',
   instance_id          varchar(36) comment '关联的流程实例ID',
   task_history_id      varchar(36) comment '关联的历史任务ID',
   type                 varchar(20) not null comment '类型',
   name                 varchar(80) not null comment '名称',
   def_version_id       varchar(36) comment '关联的流程定义版本ID',
   defination_id        varchar(36) comment '关联的流程定义ID',
   act_task_id          varchar(36) comment '关联的流程引擎任务ID',
   act_instance_id      varchar(36) comment '关联的流程引擎流程实例ID',
   act_defination_id    varchar(36) comment '关联的流程引擎流程定义ID',
   depict               varchar(255),
   v_double             double comment '值-double',
   v_long               bigint comment '值-整形',
   v_text               varchar(4000) comment '值-text',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_hi_varinst comment '历史参数表,记录任务执行中传递的参数，主要用于记录流程任务流转过程中传递给引擎的业务数据参数';

/*==============================================================*/
/* Table: flow_history                                          */
/*==============================================================*/
create table flow_history
(
   id                   varchar(36) not null,
   flow_instance_id     varchar(36),
   flow_name            varchar(80) not null,
   flow_task_name       varchar(80) not null,
   flow_run_id          varchar(36),
   flow_def_id          varchar(36) not null,
   act_history_id       varchar(36) comment '引擎流程历史ID',
   depict               varchar(255),
   act_start_time       datetime comment '实际开始时间',
   act_end_time         datetime comment '结束时间',
   act_duration_in_millis bigint comment '实际执行的时间间隔',
   act_work_time_in_millis bigint comment '执行的工作时间间隔',
   act_claim_time       datetime comment '签收时间',
   act_type             varchar(60) comment '实际任务类型',
   act_task_def_key     varchar(255) comment '实际任务定义KEY',
   owner_id             varchar(36) comment '拥有者ID',
   owner_name           varchar(100) comment '所属人名称',
   owner_account        varchar(100) comment '所属人',
   executor_id          varchar(36) comment '执行人ID',
   executor_name        varchar(80) comment '执行人名称',
   executor_account     varchar(100) comment '执行人账号',
   candidate_id         varchar(36) comment '候选人ID',
   candidate_account    varchar(100) comment '候选人账号',
   pre_id               varchar(36) comment '上一个任务ID',
   next_id              varchar(36) comment '下一个任务ID',
   task_status          varchar(80) comment '任务状态',
   canCancel            bit(1) comment '能否驳回',
   taskJsonDef          text comment '任务json定义 ',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_history comment '流程历史';

/*==============================================================*/
/* Table: flow_instance                                         */
/*==============================================================*/
create table flow_instance
(
   id                   varchar(36) not null,
   flow_def_version_id  varchar(36),
   flow_name            varchar(80) not null,
   business_id          varchar(36) not null,
   business_code        varchar(20) comment '业务编号',
   business_name        varchar(100) comment '业务单据名称',
   businessModelRemark  varchar(255) comment '业务摘要',
   business_extra_map   longblob comment '业务单据额外属性Map',
   start_date           timestamp,
   end_date             timestamp,
   act_instance_id      varchar(36) comment '引擎流程实例ID',
   suspended            boolean comment '是否挂起',
   ended                boolean comment '是否已经结束',
   version              int comment '版本-乐观锁',
   manuallyEnd          smallint(1) comment '是否是人工强制退出流程',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_instance comment '流程实例';

/*==============================================================*/
/* Table: flow_service_url                                      */
/*==============================================================*/
create table flow_service_url
(
   id                   varchar(36) not null comment 'ID',
   version              int comment '版本-乐观锁',
   name                 varchar(80) not null comment '名称',
   code                 varchar(60) not null comment '代码',
   url                  text comment 'URL地址',
   depict               varchar(255) comment '描述',
   business_model_id    varchar(36) comment '关联业务实体模型',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_service_url comment '服务地址管理';

/*==============================================================*/
/* Index: idx_flow_service_url_code                             */
/*==============================================================*/
create unique index idx_flow_service_url_code on flow_service_url
(
   code
);

/*==============================================================*/
/* Table: flow_task                                             */
/*==============================================================*/
create table flow_task
(
   id                   varchar(36) not null,
   version              int comment '版本-乐观锁',
   flow_instance_id     varchar(36),
   flow_name            varchar(80) not null,
   task_name            varchar(80) not null,
   act_task_def_key     varchar(255) not null,
   task_form_url        text,
   task_status          varchar(80),
   proxy_status         varchar(80),
   flow_definition_id   varchar(36) not null,
   act_task_id          varchar(36) comment '引擎流程任务ID',
   executor_name        varchar(80),
   executor_id          varchar(36) comment '执行人ID',
   executor_account     varchar(36),
   candidate_id         varchar(36) comment '候选人ID',
   candidate_account    varchar(36),
   execute_date         datetime,
   depict               varchar(255),
   priority             int comment '优先级',
   owner_id             varchar(36) comment '所属人ID',
   owner_account        varchar(36) comment '所属人',
   owner_name           varchar(100) comment '所属人名称',
   act_type             varchar(60) comment '实际任务类型',
   act_claim_time       datetime comment '签收时间',
   act_due_date         datetime comment '实际触发时间',
   act_task_key         varchar(255) comment '实际任务定义KEY',
   pre_id               varchar(36),
   canReject            bit(1) comment '能否驳回',
   canSuspension        bit(1) comment '能否中止',
   taskJsonDef          text comment '任务json定义 ',
   businessModelRemark  varchar(255) comment '业务摘要',
   executeTime          int comment '额定工时(分钟)',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_task comment '流程任务';

/*==============================================================*/
/* Table: flow_type                                             */
/*==============================================================*/
create table flow_type
(
   id                   varchar(36) not null comment 'ID',
   code                 varchar(255) not null comment '代码',
   name                 varchar(80) not null comment '名称',
   depict               varchar(255) comment '描述',
   business_model_id    varchar(36) comment '关联业务实体模型',
   version              int comment '版本-乐观锁',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_type comment '流程类型';

/*==============================================================*/
/* Index: idx_flow_type_code                                    */
/*==============================================================*/
create unique index idx_flow_type_code on flow_type
(
   code
);

/*==============================================================*/
/* Table: flow_variable                                         */
/*==============================================================*/
create table flow_variable
(
   id                   varchar(36) not null comment 'ID',
   type                 varchar(20) not null comment '类型',
   name                 varchar(80) not null comment '名称',
   task_id              varchar(36) comment '关联的任务ID',
   instance_id          varchar(36) comment '关联的流程实例ID',
   def_version_id       varchar(36) comment '关联的流程定义版本ID',
   defination_id        varchar(36) comment '关联的流程定义ID',
   act_task_id          varchar(36) comment '关联的流程引擎任务ID',
   act_instance_id      varchar(36) comment '关联的流程引擎流程实例ID',
   act_defination_id    varchar(36) comment '关联的流程引擎流程定义ID',
   depict               varchar(255),
   version              int comment '版本-乐观锁',
   v_double             double comment '值-double',
   v_long               bigint comment '值-整形',
   v_text               varchar(4000) comment '值-text',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table flow_variable comment '运行参数表,记录任务执行中传递的参数，主要用于任务撤回时工作流引擎的数据还原';

/*==============================================================*/
/* Table: work_page_url                                         */
/*==============================================================*/
create table work_page_url
(
   id                   varchar(36) not null comment 'ID',
   name                 varchar(80) not null comment '名称',
   url                  text not null comment 'URL地址',
   depict               varchar(255) comment '描述',
   version              int comment '版本-乐观锁',
   app_module_id        varchar(36) comment '关联应用模块ID',
   creator_id           varchar(36) comment '创建人ID',
   creator_account      varchar(50) comment '创建者账号',
   creator_name         varchar(50) comment '创建者名称',
   created_date         datetime comment '创建时间',
   last_editor_id       varchar(36) comment '最后更新者',
   last_editor_account  varchar(50) comment '最后更新者账号',
   last_editor_name     varchar(50) comment '最后更新者名称',
   last_edited_date     datetime comment '最后更新时间',
   primary key (id)
);

alter table work_page_url comment '服务地址管理';

alter table business_model_page_url add constraint FK_fk_businessWorkUrl_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table business_model_page_url add constraint FK_fk_businessWorkUrl_workPageUrl_id foreign key (work_page_url_id)
      references work_page_url (id) on delete restrict on update restrict;

alter table business_model_selfDefEmployee add constraint FK_fk_selfDefEmployee_businessModule_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_def_version add constraint fk_def_version_defination_id foreign key (flow_defination_id)
      references flow_defination (id) on delete restrict on update restrict;

alter table flow_defination add constraint fk_flow_defination_type_id foreign key (flow_type_id)
      references flow_type (id) on delete restrict on update restrict;

alter table flow_executor_config add constraint FK_fk_flow_executorConfig_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_hi_varinst add constraint FK_fk_flow_hi_varinst_history foreign key (task_history_id)
      references flow_history (id) on delete restrict on update restrict;

alter table flow_hi_varinst add constraint FK_fk_flow_hi_varinst_instance foreign key (instance_id)
      references flow_instance (id) on delete cascade on update restrict;

alter table flow_history add constraint fk_flow_history_instance_id foreign key (flow_instance_id)
      references flow_instance (id) on delete restrict on update restrict;

alter table flow_instance add constraint fk_instance_def_version_id foreign key (flow_def_version_id)
      references flow_def_version (id) on delete restrict on update restrict;

alter table flow_service_url add constraint FK_fk_flow_serviceUrl_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_task add constraint fk_flow_task_instance_id foreign key (flow_instance_id)
      references flow_instance (id) on delete restrict on update restrict;

alter table flow_type add constraint fk_flow_type_business_model_id foreign key (business_model_id)
      references business_model (id) on delete restrict on update restrict;

alter table flow_variable add constraint fk_flow_variable_instance foreign key (instance_id)
      references flow_instance (id) on delete cascade on update restrict;

alter table flow_variable add constraint FK_fk_flow_variable_task foreign key (task_id)
      references flow_task (id) on delete cascade on update restrict;

