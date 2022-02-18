-- mysql
ALTER TABLE `flow_history`   
  ADD  KEY `idx_flow_def_id` (`flow_def_id`),
  ADD  KEY `idx_flow_execute_status` (`flow_execute_status`);





-- postgresql
CREATE INDEX "idx_flow_def_id" ON "public"."flow_history" USING btree (
  "flow_def_id"
);

CREATE INDEX "idx_flow_execute_status" ON "public"."flow_history" USING btree (
  "flow_execute_status"
);





