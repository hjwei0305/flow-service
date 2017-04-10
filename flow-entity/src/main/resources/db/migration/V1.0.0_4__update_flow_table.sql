/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/3/31 16:51:12                           */
/*==============================================================*/


DROP TABLE IF EXISTS tmp_flow_defVersion;

RENAME TABLE flow_defVersion TO tmp_flow_defVersion;

/*==============================================================*/
/* Table: flow_defVersion                                       */
/*==============================================================*/
CREATE TABLE flow_defVersion
(
   id                   VARCHAR(36) NOT NULL COMMENT 'ID',
   actDefId             VARCHAR(36) NOT NULL COMMENT '定义ID',
   defKey               VARCHAR(255) NOT NULL COMMENT '定义KEY',
   NAME                 VARCHAR(80) NOT NULL COMMENT '名称',
   actDeployId          VARCHAR(36) COMMENT '部署ID',
   startUel             VARCHAR(255) COMMENT '启动条件UEL',
   versionCode          INT COMMENT '版本号',
   priority             INT COMMENT '优先级',
   defJson              TEXT COMMENT '流程JSON文本',
   defBpmn              TEXT COMMENT '流程BPMN文本',
   defXML               TEXT COMMENT '最终定义XML',
   depict               VARCHAR(255) COMMENT '描述',
   createdBy            VARCHAR(100) COMMENT '创建人',
   createdDate          TIMESTAMP COMMENT '创建时间',
   lastModifiedBy       VARCHAR(100) COMMENT '最后更新者',
   lastModifiedDate     TIMESTAMP COMMENT '最后更新时间',
   flowDefination_id    VARCHAR(36) COMMENT '关联流程类型',
   PRIMARY KEY (id)
);

ALTER TABLE flow_defVersion COMMENT '流程定义版本';

INSERT INTO flow_defVersion (id, actDefId, defKey, NAME, actDeployId, startUel, versionCode, priority, defJson, defBpmn, defXML, depict, createdBy, lastModifiedBy, flowDefination_id)
SELECT id, NULL, defKey, NAME, actDeployId, startUel, versionCode, priority, defJson, defBpmn, defXML, depict, createdBy, lastModifiedBy, flowDefination_id
FROM tmp_flow_defVersion;

/*
* 自定义drop语句, 清除外键关联
*/
ALTER TABLE tmp_flow_defVersion DROP FOREIGN KEY FK_defVersion_defination;
ALTER TABLE flow_instance  DROP FOREIGN KEY  FK_FK_instance_defVersion;

DROP TABLE IF EXISTS tmp_flow_defVersion;

/*==============================================================*/
/* Index: Index_defKey                                          */
/*==============================================================*/
CREATE UNIQUE INDEX Index_defKey ON flow_defVersion
(
   defKey
);

ALTER TABLE flow_defVersion ADD CONSTRAINT FK_defVersion_defination FOREIGN KEY (flowDefination_id)
      REFERENCES flow_defination (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE flow_instance ADD CONSTRAINT FK_FK_instance_defVersion FOREIGN KEY (flowDefVersion_id)
      REFERENCES flow_defVersion (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

