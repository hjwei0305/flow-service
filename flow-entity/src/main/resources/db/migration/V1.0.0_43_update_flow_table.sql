-- mysql

ALTER TABLE `business_model`   
  ADD COLUMN `phone_look_url` VARCHAR(255) NULL COMMENT '移动端查看表单URL' AFTER `look_url`;

ALTER TABLE  `flow_type`
  ADD COLUMN `phone_look_url` VARCHAR(255) NULL COMMENT '移动端查看单据URL' AFTER `look_url`;



-- postgresql

ALTER TABLE "public"."business_model"   
  ADD COLUMN "phone_look_url" VARCHAR(255);
  
COMMENT ON COLUMN "public"."business_model"."phone_look_url" IS '移动端查看表单URL';  


ALTER TABLE "public"."flow_type"
  ADD COLUMN "phone_look_url" VARCHAR(255);

COMMENT ON COLUMN "public"."flow_type"."phone_look_url" IS '移动端查看表单URL';