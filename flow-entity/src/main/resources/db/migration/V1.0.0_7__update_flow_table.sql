/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2017/4/6 11:14:08                            */
/*==============================================================*/


ALTER TABLE `ecmp_flow`.`flow_task` CHANGE `executorAccount` `executorAccount` VARCHAR(100) NULL ,CHANGE `candidateAccount` `candidateAccount` VARCHAR(100) NULL ;