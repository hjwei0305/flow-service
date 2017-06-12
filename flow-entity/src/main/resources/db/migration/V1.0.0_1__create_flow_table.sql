--
-- Table structure for table `business_model`
--
CREATE TABLE `business_model` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `class_name` varchar(255) DEFAULT NULL COMMENT '类全路径',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `depict` varchar(255) DEFAULT NULL COMMENT '描述',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `conditon_bean` varchar(255) DEFAULT NULL COMMENT '转换对象',
  `app_module_id` varchar(36) DEFAULT NULL COMMENT '关联应用模块ID',
  `dao_bean` varchar(255) DEFAULT NULL COMMENT '数据访问对象名称',
  `app_module_code` varchar(20) DEFAULT NULL COMMENT '关联应用模块代码',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_business_model_class_name` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务实体模型';

--
-- Table structure for table `business_model_page_url`
--
CREATE TABLE `business_model_page_url` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `work_page_url_id` varchar(36) DEFAULT NULL COMMENT '关联工作页面',
  `business_model_id` varchar(36) DEFAULT NULL COMMENT '关联业务实体模型',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_b_m_p_u_business_module_id` (`business_model_id`),
  KEY `fk_b_m_p_u_work_page_url_id` (`work_page_url_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务实体工作界面配置';


--
-- Table structure for table `business_model_selfDefEmployee`
--
CREATE TABLE `business_model_selfDefEmployee` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `employee_id` varchar(36) NOT NULL COMMENT '企业员工ID',
  `employee_name` varchar(80) DEFAULT NULL COMMENT '用户名称',
  `business_model_id` varchar(36) NOT NULL COMMENT '关联业务实体模型',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_selfDefEmployee_b_module_id` (`business_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务实体自定义执行人配置';

--
-- Table structure for table `default_business_model`
--
CREATE TABLE `default_business_model` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `flowStatus` varchar(10) NOT NULL COMMENT '流程状态',
  `orgCode` varchar(20) DEFAULT NULL COMMENT '组织机构代码',
  `orgId` varchar(36) DEFAULT NULL COMMENT '组织机构Id',
  `orgName` varchar(80) DEFAULT NULL COMMENT '组织机构名称',
  `orgPath` varchar(500) DEFAULT NULL COMMENT '组织机构层级路径',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `workCaption` varchar(1000) DEFAULT NULL COMMENT '工作说明',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `unitPrice` double DEFAULT NULL COMMENT '单价',
  `count` int(11) DEFAULT NULL COMMENT '数量',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='工作流默认业务实体实现';

--
-- Table structure for table `flow_def_version`
--
CREATE TABLE `flow_def_version` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `act_def_id` varchar(255) NOT NULL COMMENT '定义ID',
  `def_key` varchar(255) NOT NULL COMMENT '定义KEY',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `act_deploy_id` varchar(36) DEFAULT NULL COMMENT '部署ID',
  `start_uel` varchar(255) DEFAULT NULL COMMENT '启动条件UEL',
  `version_code` int(11) DEFAULT NULL COMMENT '版本号',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `def_json` text COMMENT '流程JSON文本',
  `def_bpmn` text COMMENT '流程BPMN文本',
  `def_xml` text COMMENT '最终定义XML',
  `depict` varchar(255) DEFAULT NULL COMMENT '描述',
  `flow_defination_id` varchar(36) DEFAULT NULL COMMENT '关联流程类型',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_def_version_defination_id` (`flow_defination_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程定义版本';


--
-- Table structure for table `flow_defination`
--
CREATE TABLE `flow_defination` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `def_key` varchar(255) NOT NULL COMMENT '定义Key',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `last_version_id` varchar(36) DEFAULT NULL COMMENT '最新版本ID',
  `start_uel` varchar(255) DEFAULT NULL COMMENT '启动条件UEL',
  `depict` varchar(255) DEFAULT NULL COMMENT '描述',
  `flow_type_id` varchar(36) DEFAULT NULL,
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `basic_org_id` varchar(36) DEFAULT NULL COMMENT '组织机构id',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  `flowDefinationStatus` varchar(10) NOT NULL COMMENT '流程定义状态',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_flow_defination_def_key` (`def_key`),
  KEY `fk_flow_defination_type_id` (`flow_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程定义';

--
-- Table structure for table `flow_hi_varinst`
--
CREATE TABLE `flow_hi_varinst` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `type` varchar(20) NOT NULL COMMENT '类型',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `task_history_id` varchar(36) DEFAULT NULL COMMENT '关联的历史任务ID',
  `instance_id` varchar(36) DEFAULT NULL COMMENT '关联的流程实例ID',
  `def_version_id` varchar(36) DEFAULT NULL COMMENT '关联的流程定义版本ID',
  `defination_id` varchar(36) DEFAULT NULL COMMENT '关联的流程定义ID',
  `act_task_id` varchar(36) DEFAULT NULL COMMENT '关联的流程引擎任务ID',
  `act_instance_id` varchar(36) DEFAULT NULL COMMENT '关联的流程引擎流程实例ID',
  `act_defination_id` varchar(36) DEFAULT NULL COMMENT '关联的流程引擎流程定义ID',
  `depict` varchar(255) DEFAULT NULL,
  `v_double` double DEFAULT NULL COMMENT '值-double',
  `v_long` bigint(20) DEFAULT NULL COMMENT '值-整形',
  `v_text` varchar(4000) DEFAULT NULL COMMENT '值-text',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_flow_hi_varinst_history` (`task_history_id`),
  KEY `fk_flow_hi_varinst_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='历史参数表,记录任务执行中传递的参数，主要用于记录流程任务流转过程中传递给引擎的业务数据参数';


--
-- Table structure for table `flow_history`
--
CREATE TABLE `flow_history` (
  `id` varchar(36) NOT NULL,
  `flow_name` varchar(80) NOT NULL,
  `flow_task_name` varchar(80) NOT NULL,
  `flow_run_id` varchar(36) DEFAULT NULL,
  `flow_def_id` varchar(36) NOT NULL,
  `owner_account` varchar(100) DEFAULT NULL COMMENT '所属人',
  `owner_name` varchar(100) DEFAULT NULL COMMENT '所属人名称',
  `executor_name` varchar(80) DEFAULT NULL,
  `executor_account` varchar(100) DEFAULT NULL,
  `candidate_account` varchar(100) DEFAULT NULL,
  `depict` varchar(255) DEFAULT NULL,
  `flow_instance_id` varchar(36) DEFAULT NULL,
  `act_history_id` varchar(36) DEFAULT NULL COMMENT '引擎流程历史ID',
  `act_claim_time` datetime DEFAULT NULL COMMENT '签收时间',
  `act_type` varchar(60) DEFAULT NULL COMMENT '实际任务类型',
  `act_work_time_in_millis` bigint(20) DEFAULT NULL COMMENT '执行的工作时间间隔',
  `act_duration_in_millis` bigint(20) DEFAULT NULL COMMENT '实际执行的时间间隔',
  `act_end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `act_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `act_task_def_key` varchar(255) DEFAULT NULL COMMENT '实际任务定义KEY',
  `pre_id` varchar(36) DEFAULT NULL COMMENT '上一个任务ID',
  `next_id` varchar(36) DEFAULT NULL COMMENT '下一个任务ID',
  `task_status` varchar(80) DEFAULT NULL COMMENT '任务状态',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  `canCancel` bit(1) DEFAULT NULL COMMENT '能否驳回',
  `taskJsonDef` varchar(1200) DEFAULT NULL COMMENT '任务json定义 ',
  `businessModelRemark` varchar(255) DEFAULT NULL COMMENT '业务摘要',
  PRIMARY KEY (`id`),
  KEY `fk_flow_history_instance_id` (`flow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程历史';

--
-- Table structure for table `flow_instance`
--
CREATE TABLE `flow_instance` (
  `id` varchar(36) NOT NULL,
  `flow_name` varchar(80) NOT NULL,
  `business_id` varchar(36) NOT NULL,
  `start_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `depict` varchar(255) DEFAULT NULL,
  `flow_def_version_id` varchar(36) DEFAULT NULL,
  `act_instance_id` varchar(36) DEFAULT NULL COMMENT '引擎流程实例ID',
  `suspended` tinyint(1) DEFAULT NULL COMMENT '是否挂起',
  `ended` tinyint(1) DEFAULT NULL COMMENT '是否已经结束',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_instance_def_version_id` (`flow_def_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程实例';

--
-- Table structure for table `flow_service_url`
--
CREATE TABLE `flow_service_url` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `code` varchar(60) NOT NULL COMMENT '代码',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `url` text COMMENT 'URL地址',
  `depict` varchar(255) DEFAULT NULL COMMENT '描述',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `business_model_id` varchar(36) DEFAULT NULL COMMENT '关联业务实体模型',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_flow_service_url_code` (`code`),
  KEY `fk_serUrl_business_model_id` (`business_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务地址管理';

--
-- Table structure for table `flow_task`
--
CREATE TABLE `flow_task` (
  `id` varchar(36) NOT NULL,
  `flow_name` varchar(80) NOT NULL,
  `task_name` varchar(80) NOT NULL,
  `act_task_def_key` varchar(255) NOT NULL,
  `task_form_url` text,
  `task_status` varchar(80) DEFAULT NULL,
  `proxy_status` varchar(80) DEFAULT NULL,
  `flow_definition_id` varchar(36) NOT NULL,
  `executor_name` varchar(80) DEFAULT NULL,
  `executor_account` varchar(100) DEFAULT NULL,
  `candidate_account` varchar(100) DEFAULT NULL,
  `execute_date` datetime DEFAULT NULL,
  `depict` varchar(255) DEFAULT NULL,
  `flow_instance_id` varchar(36) DEFAULT NULL,
  `act_task_id` varchar(36) DEFAULT NULL COMMENT '引擎流程任务ID',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `owner_account` varchar(100) DEFAULT NULL COMMENT '所属人',
  `owner_name` varchar(100) DEFAULT NULL COMMENT '所属人名称',
  `act_type` varchar(60) DEFAULT NULL COMMENT '实际任务类型',
  `act_claim_time` datetime DEFAULT NULL COMMENT '签收时间',
  `act_due_date` datetime DEFAULT NULL COMMENT '实际触发时间',
  `act_task_key` varchar(255) DEFAULT NULL COMMENT '实际任务定义KEY',
  `pre_id` varchar(36) DEFAULT NULL,
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  `canReject` bit(1) DEFAULT NULL COMMENT '能否驳回',
  `canSuspension` bit(1) DEFAULT NULL COMMENT '能否中止',
  `taskJsonDef` varchar(1200) DEFAULT NULL COMMENT '任务json定义 ',
  `businessModelRemark` varchar(255) DEFAULT NULL COMMENT '业务摘要',
  `executeTime` int(11) DEFAULT NULL COMMENT '额定工时(分钟)',
  PRIMARY KEY (`id`),
  KEY `fk_flow_task_instance_id` (`flow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程任务';


--
-- Table structure for table `flow_type`
--
CREATE TABLE `flow_type` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `code` varchar(255) NOT NULL COMMENT '代码',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `depict` varchar(255) DEFAULT NULL COMMENT '描述',
  `business_model_id` varchar(36) DEFAULT NULL COMMENT '关联业务实体模型',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_flow_type_code` (`code`),
  KEY `fk_flow_type_business_model_id` (`business_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程类型';

--
-- Table structure for table `flow_variable`
--
CREATE TABLE `flow_variable` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `type` varchar(20) NOT NULL COMMENT '类型',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `task_id` varchar(36) DEFAULT NULL COMMENT '关联的任务ID',
  `instance_id` varchar(36) DEFAULT NULL COMMENT '关联的流程实例ID',
  `def_version_id` varchar(36) DEFAULT NULL COMMENT '关联的流程定义版本ID',
  `defination_id` varchar(36) DEFAULT NULL COMMENT '关联的流程定义ID',
  `act_task_id` varchar(36) DEFAULT NULL COMMENT '关联的流程引擎任务ID',
  `act_instance_id` varchar(36) DEFAULT NULL COMMENT '关联的流程引擎流程实例ID',
  `act_defination_id` varchar(36) DEFAULT NULL COMMENT '关联的流程引擎流程定义ID',
  `depict` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `v_double` double DEFAULT NULL COMMENT '值-double',
  `v_long` bigint(20) DEFAULT NULL COMMENT '值-整形',
  `v_text` varchar(4000) DEFAULT NULL COMMENT '值-text',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `fk_flow_variable_instance` (`instance_id`),
  KEY `fk_flow_variable_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='运行参数表,记录任务执行中传递的参数，主要用于任务撤回时工作流引擎的数据还原';

--
-- Table structure for table `flyway_version`
--
CREATE TABLE `flyway_version` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_version_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `work_page_url`
--
CREATE TABLE `work_page_url` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `name` varchar(80) NOT NULL COMMENT '名称',
  `url` text NOT NULL COMMENT 'URL地址',
  `depict` varchar(255) DEFAULT NULL COMMENT '描述',
  `version` int(11) DEFAULT NULL COMMENT '版本-乐观锁',
  `app_module_id` varchar(36) DEFAULT NULL COMMENT '关联应用模块ID',
  `creator_id` varchar(36) DEFAULT NULL COMMENT '创建人Id',
  `creator_account` varchar(50) DEFAULT NULL COMMENT '创建人账号',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_date` datetime DEFAULT NULL COMMENT '创建时间',
  `last_editor_id` varchar(36) DEFAULT NULL COMMENT '最后修改人Id',
  `last_editor_account` varchar(50) DEFAULT NULL COMMENT '最后修改人账号',
  `last_editor_name` varchar(50) DEFAULT NULL COMMENT '最后修改人姓名',
  `last_edited_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务地址管理';

