-- mysql
ALTER TABLE flow_task_push MODIFY COLUMN depict varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL;




-- postgresql
ALTER TABLE "public"."flow_task_push"   
  MODIFY COLUMN "depict" varchar(2000)  NULL;








