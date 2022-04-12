-- mysql
ALTER TABLE `flow_history`   
  ADD  KEY `idx_flow_old_task_id` (`old_task_id`);



-- postgresql
CREATE INDEX "idx_flow_old_task_id" ON "public"."flow_history" USING btree (
  "old_task_id"
);





