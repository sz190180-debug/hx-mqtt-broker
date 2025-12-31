ALTER TABLE hx_user_task_chain_template
    ADD COLUMN group_sort_order INT NOT NULL DEFAULT 0 COMMENT '分组排序字段';