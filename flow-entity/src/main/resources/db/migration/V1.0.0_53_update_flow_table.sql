-- mysql
ALTER TABLE flow_instance ADD allow_emergency TINYINT(1) DEFAULT 0 NOT NULL COMMENT '允许全流程紧急';



-- postgresql
ALTER TABLE "public"."flow_instance"
  ADD COLUMN "allow_emergency" TINYINT(1) DEFAULT 0 NOT NULL COMMENT '允许全流程紧急';
  
  
  
  