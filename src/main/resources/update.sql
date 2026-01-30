ALTER TABLE hx_user_task_chain_template
    ADD COLUMN group_sort_order INT NOT NULL DEFAULT 0 COMMENT '分组排序字段';

DROP TABLE IF EXISTS `amr_register_config`;
CREATE TABLE `amr_register_config` (
                                       `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
                                       `amr_id` BIGINT NOT NULL COMMENT '车辆ID',
                                       `ip` VARCHAR(50) NOT NULL COMMENT 'IP地址',
                                       `port` INT NOT NULL COMMENT '端口号',
                                       `read_address` INT NOT NULL COMMENT '读寄存器地址',
                                       `write_address` INT NOT NULL COMMENT '写寄存器地址',
                                       `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='车辆寄存器配置表';

ALTER TABLE `hx_map_vertexes`
    ADD COLUMN `weight` DOUBLE DEFAULT 0 COMMENT '货物重量';

ALTER TABLE `warehouse_column_vertexes`
    ADD COLUMN `weight` DOUBLE DEFAULT 0 COMMENT '货物重量';

-- 第一步：创建唯一索引（无COMMENT）
ALTER TABLE `amr_register_config`
    ADD UNIQUE INDEX `uk_amr_id` (`amr_id`);

-- 第二步：为已创建的索引添加注释
COMMENT ON INDEX `amr_register_config`.`uk_amr_id`
IS '车辆ID唯一索引，保证一辆车仅一条配置';