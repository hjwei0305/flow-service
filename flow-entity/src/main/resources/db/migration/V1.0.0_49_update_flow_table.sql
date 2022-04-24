-- mysql
ALTER TABLE `flow_task_push_control`   
  ADD  KEY `idx_control_push_false` (`push_false`);




-- postgresql
CREATE INDEX "idx_control_push_false" ON "public"."flow_task_push_control" USING btree (
  "push_false"
);





