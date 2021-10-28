ALTER TABLE `sei_flow`.`flow_task`   
  ADD  KEY `fk_flow_task_executor_id` (`executor_id`),
  ADD  KEY `fk_flow_definition_id` (`flow_definition_id`);