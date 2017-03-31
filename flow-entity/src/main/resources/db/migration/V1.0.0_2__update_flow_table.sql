/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/3/30 18:31:20                           */
/*==============================================================*/


DROP TABLE IF EXISTS tmp_flow_defination;

RENAME TABLE flow_defination TO tmp_flow_defination;

/*==============================================================*/
/* Table: flow_defination                                       */
/*==============================================================*/
CREATE TABLE flow_defination
(
   id                   VARCHAR(36) NOT NULL COMMENT 'ID',
   defKey               VARCHAR(255) NOT NULL COMMENT '定义Key',
   NAME                 VARCHAR(80) NOT NULL COMMENT '名称',
   lastVersionId        INT COMMENT '最新版本ID',
   startUel             VARCHAR(255) COMMENT '启动条件UEL',
   depict               VARCHAR(255) COMMENT '描述',
   createdBy            VARCHAR(100) COMMENT '创建人',
   createdDate          TIMESTAMP COMMENT '创建时间',
   lastModifiedBy       VARCHAR(100) COMMENT '最后更新者',
   lastModifiedDate     TIMESTAMP COMMENT '最后更新时间',
   flowType_id          VARCHAR(36) COMMENT '关联流程类型',
   PRIMARY KEY (id)
);

ALTER TABLE flow_defination COMMENT '流程定义';

INSERT INTO flow_defination (id, defKey, NAME, lastVersionId, startUel, depict, createdBy, lastModifiedBy, flowType_id)
SELECT id, CODE, NAME, lastVersionId, startUel, depict, createdBy, lastModifiedBy, flowType_id
FROM tmp_flow_defination;

/*
* 自定义drop语句, 清除外键关联
*/
ALTER TABLE tmp_flow_defination DROP FOREIGN KEY FK_defination_bpmType;
ALTER TABLE flow_defVersion  DROP FOREIGN KEY  FK_defVersion_defination;

DROP TABLE IF EXISTS tmp_flow_defination;

/*==============================================================*/
/* Index: Index_code                                            */
/*==============================================================*/
CREATE UNIQUE INDEX Index_code ON flow_defination
(
   defKey
);

ALTER TABLE flow_defVersion ADD CONSTRAINT FK_defVersion_defination FOREIGN KEY (flowDefination_id)
      REFERENCES flow_defination (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE flow_defination ADD CONSTRAINT FK_defination_bpmType FOREIGN KEY (flowType_id)
      REFERENCES flow_bpmType (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

