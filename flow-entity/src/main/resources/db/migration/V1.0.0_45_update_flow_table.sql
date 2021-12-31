-- mysql
-- 找空余时间先清除掉flow_task_push_control除主键以外的其他索引，然后再执行
ALTER TABLE `flow_task_push_control`
  ADD KEY `idx_control_page_type_query` (`flow_type_id`, `push_start_date`, `tenant_code`);

ALTER TABLE `flow_task_push_control`
  ADD KEY `idx_control_page_code_query` (`business_code`, `push_start_date`, `tenant_code`);

ALTER TABLE `flow_task_push_control`
  ADD KEY `idx_control_instance_and_key` (`flow_instance_id`, `flow_act_task_def_key`, `push_status`, `tenant_code`);

ALTER TABLE `flow_task_control_and_push`
  ADD  KEY `idx_control_id` (`control_id`);




-- postgresql
-- 找空余时间先清除掉flow_task_push_control除主键以外的其他索引，然后再执行

CREATE INDEX "idx_control_page_type_query" ON "public"."flow_task_push_control" USING btree (
  "flow_type_id","push_start_date","tenant_code"
);

CREATE INDEX "idx_control_page_code_query" ON "public"."flow_task_push_control" USING btree (
  "business_code","push_start_date","tenant_code"
);

CREATE INDEX "idx_control_instance_and_key" ON "public"."flow_task_push_control" USING btree (
  "flow_instance_id","flow_act_task_def_key","push_status","tenant_code"
);

CREATE INDEX "idx_control_id" ON "public"."flow_task_control_and_push" USING btree (
  "control_id"
);
