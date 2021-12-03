-- mysql

ALTER TABLE `flow_task`   
  ADD COLUMN `label_reason` VARCHAR(2000) NULL COMMENT '标注原因' AFTER `jump_back_previous`;




-- postgresql

ALTER TABLE "public"."flow_task"   
  ADD COLUMN "label_reason" VARCHAR(2000);
  
COMMENT ON COLUMN "public"."flow_task"."label_reason" IS '标注原因';  

