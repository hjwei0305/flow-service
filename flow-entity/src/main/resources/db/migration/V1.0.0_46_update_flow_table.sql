-- mysql
ALTER TABLE `business_model`   
  ADD COLUMN `rank` INT(11) NOT NULL COMMENT '排序号' AFTER `push_msg_url`;




-- postgresql
ALTER TABLE "public"."business_model"   
  ADD COLUMN "rank" int4  NOT NULL;
  
COMMENT ON COLUMN "public"."business_model"."rank" IS '排序号';