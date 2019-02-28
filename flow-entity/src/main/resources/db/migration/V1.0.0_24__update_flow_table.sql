/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2019/2/26 10:54:12                           */
/*==============================================================*/


/*==============================================================*/
/* Table: flow_history                                          */
/*==============================================================*/
ALTER TABLE `flow_history`
ADD COLUMN `flow_execute_status`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '流程节点执行状态'
 AFTER `task_json_def`;

