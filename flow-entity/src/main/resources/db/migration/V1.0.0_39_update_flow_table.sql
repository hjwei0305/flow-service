ALTER TABLE `flow_task`
ADD COLUMN `jump_back_previous`  bit(1) NULL DEFAULT NULL COMMENT '跳回上一节点' AFTER `allow_subtract_sign`;