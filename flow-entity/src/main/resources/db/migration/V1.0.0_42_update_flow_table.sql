-- mysql

ALTER TABLE `work_page_url`   
  ADD COLUMN `phone_url` VARCHAR(255) NULL COMMENT '移动端地址' AFTER `url`;

ALTER TABLE `flow_instance`
  ADD COLUMN `business_money` VARCHAR(50) NULL COMMENT '业务单据展示金额字符串' AFTER `business_name`;

ALTER TABLE `flow_instance`
  DROP COLUMN `business_extra_map`;





-- postgresql

ALTER TABLE "public"."work_page_url" 
  ADD COLUMN "phone_url" varchar(255);

COMMENT ON COLUMN "public"."work_page_url"."phone_url" IS '移动端地址';


ALTER TABLE "public"."flow_instance" 
  ADD COLUMN "business_money" varchar(255);

COMMENT ON COLUMN "public"."flow_instance"."business_money" IS '业务单据展示金额字符串';


ALTER TABLE "public"."flow_instance" 
  DROP COLUMN "business_extra_map";